package com.omie.services.omie

import com.omie.dto.messageSQS.receiver.FaturaDto
import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.dto.omieApi.OmieResponse
import com.omie.dto.omieApi.OmieStatusDto
import com.omie.mapper.http.OmieRequestMapper
import com.omie.services.IdempotencyService
import org.slf4j.LoggerFactory

class ProcessLoteService(
    private val omieGateway: OmieGateway,
    private val idempotencyService: IdempotencyService,
    private val key: String,
    private val secret: String,
    private val publishService: FaturaPublishService
) {
    private val log = LoggerFactory.getLogger(ProcessLoteService::class.java)
    private val omieRequestMapper = OmieRequestMapper(key, secret)

    suspend fun processLote(lote: LoteDto) {
        val inicio = System.currentTimeMillis()
        log.info("Iniciando lote ${lote.loteId} | origem=${lote.origem} | totalFaturas=${lote.faturas.size}")

        val faturasPendentes = filtrarFaturasPendentes(lote)

        if (faturasPendentes.isEmpty()) {
            log.info("Lote ${lote.loteId} encerrado — nenhuma fatura pendente.")
            return
        }

        try {
            val request = omieRequestMapper.mapFaturasToRequest(faturasPendentes, lote.numeroLote)
            val response = omieGateway.sendFatura(request)

            if (response.error != null) {
                log.error(
                    "Erro retornado pelo OMIE | lote=${lote.loteId} " +
                            "| faultcode=${response.error.faultcode} " +
                            "| faultstring=${response.error.faultstring}"
                )
                faturasPendentes.forEach { fatura ->
                    publishService.publicarErroLoteOmie(fatura, response, lote.loteId)
                    log.warn("Fatura ${fatura.id} publicada em [bowe-dev-error-fatura] | motivo=ERRO_LOTE_OMIE | lote=${lote.loteId}")
                }
                return
            }

            response.raw_body?.firstOrNull()?.status_lote?.forEach { status ->
                val fatura = faturasPendentes.find {
                    it.codigoLancamentoIntegracao == status.codigo_lancamento_integracao
                } ?: run {
                    log.warn("Status recebido para codigoLancamento=${status.codigo_lancamento_integracao} sem fatura correspondente no lote ${lote.loteId}")
                    return@forEach
                }

                if (status.codigo_status == "0") {
                    idempotencyService.save(fatura.keyFatura)
                    publishService.publicarSucesso(fatura, response, lote.loteId)
                    log.info("Fatura ${fatura.id} processada com sucesso | lote=${lote.loteId} | codigoOmie=${status.codigo_lancamento_omie} | fila=[bowe-dev-gerar-fatura]")
                } else {
                    publishService.publicarErroRejeicaoOmie(fatura, response, lote.loteId)
                    log.warn("Fatura ${fatura.id} rejeitada pelo OMIE | lote=${lote.loteId} | codigo=${status.codigo_status} | descricao=${status.descricao_status} | fila=[bowe-dev-error-fatura]")
                }
            }

        } catch (ex: Exception) {
            log.error("Erro inesperado no lote ${lote.loteId} | tipo=${ex::class.simpleName} | mensagem=${ex.message}")
            faturasPendentes.forEach { fatura ->
                publishService.publicarErroException(fatura, ex, lote.loteId)
                log.warn("Fatura ${fatura.id} publicada em [bowe-dev-error-fatura] | motivo=EXCEPTION | lote=${lote.loteId}")
            }
        } finally {
            liberarLocks(faturasPendentes)
            val tempo = System.currentTimeMillis() - inicio
            log.info("Lote ${lote.loteId} finalizado | tempo=${tempo}ms | pendentes=${faturasPendentes.size}")
        }
    }

    private suspend fun filtrarFaturasPendentes(lote: LoteDto): List<FaturaDto> {
        return lote.faturas
            .filter { fatura ->
                val jaProcessada = idempotencyService.exists(fatura.keyFatura)
                if (jaProcessada) {
                    log.info(
                        "Fatura ${fatura.id} ignorada " +
                                "| motivo=JA_PROCESSADA " +
                                "| keyFatura=${fatura.keyFatura} " +
                                "| lote=${lote.loteId}"
                    )
                }
                !jaProcessada
            }
            .filter { fatura ->
                val lockAdquirido = idempotencyService.acquireLock(fatura.keyFatura)
                if (!lockAdquirido) {
                    log.info(
                        "Fatura ${fatura.id} ignorada " +
                                "| motivo=EM_PROCESSAMENTO " +
                                "| keyFatura=${fatura.keyFatura} " +
                                "| lote=${lote.loteId}"
                    )
                }
                lockAdquirido
            }
            .also { pendentes ->
                val ignoradas = lote.faturas.size - pendentes.size
                log.info("Lote ${lote.loteId} filtrado | pendentes=${pendentes.size} | ignoradas=$ignoradas")
            }
    }

    private suspend fun liberarLocks(faturas: List<FaturaDto>) {
        faturas.forEach { fatura ->
            idempotencyService.releaseLock(fatura.keyFatura)
        }
    }
}