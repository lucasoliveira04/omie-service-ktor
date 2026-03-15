package com.omie.services.omie

import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.mapper.http.OmieRequestMapper
import com.omie.services.IdempotencyService
import kotlinx.coroutines.sync.Semaphore
import org.slf4j.LoggerFactory

class ProcessLoteService(
    private val omieGateway: OmieGateway,
    private val idempotencyService: IdempotencyService,
    private val key: String,
    private val secret: String,
    private val publishService: FaturaPublishService
) {
    private val log = LoggerFactory.getLogger(ProcessLoteService::class.java)
    private val semaphore = Semaphore(5)
    suspend fun processLote(lote: LoteDto) {
        val faturasPendentes = lote.faturas
            .filter { fatura ->
                val jaProcessada = idempotencyService.exists(fatura.keyFatura)
                if (jaProcessada) {
                    log.info(
                        "Fatura ${fatura.id} ignorada no lote ${lote.loteId} " +
                                "— já foi processada com sucesso anteriormente."
                    )
                }
                !jaProcessada
            }
            .filter { fatura ->
                val lockAdquirido = idempotencyService.acquireLock(fatura.keyFatura)
                if (!lockAdquirido) {
                    log.info(
                        "Fatura ${fatura.id} ignorada no lote ${lote.loteId} " +
                                "— está sendo processada por outro lote agora."
                    )
                }
                lockAdquirido
            }

        if (faturasPendentes.isEmpty()) {
            log.info("Nenhuma fatura pendente no lote ${lote.loteId}. Ignorando.")
            return
        }

        try {
            val request = OmieRequestMapper(key, secret).mapFaturasToRequest(faturasPendentes, lote.numeroLote)
            val response = omieGateway.sendFatura(request)

            if (response.error != null) {
                log.error(
                    "Erro retornado pelo OMIE no lote ${lote.loteId}: " +
                            "faultcode=${response.error.faultcode} | " +
                            "faultstring=${response.error.faultstring}"
                )
                faturasPendentes.forEach { fatura ->
                    publishService.publicarErroOmie(fatura, response, lote.loteId)
                }
                return
            }

            response.raw_body?.firstOrNull()?.status_lote?.forEach { status ->
                val fatura = faturasPendentes.find {
                    it.codigoLancamentoIntegracao == status.codigo_lancamento_integracao
                } ?: return@forEach

                if (status.codigo_status == "0") {
                    idempotencyService.save(fatura.keyFatura)
                    publishService.publicarSucesso(fatura, response, lote.loteId)
                    log.info("Fatura ${fatura.id} processada com sucesso - Lote dala ${lote.loteId} - Publicada na fila [bowe-dev-gerar-fatura]")
                } else {
                    publishService.publicarErroOmie(fatura, response, lote.loteId)
                    log.warn("Fatura ${fatura.id} rejeitada pelo OMIE: ${status.descricao_status} - Publicada na fila [bowe-dev-error-fatura]")
                }
            }

        } catch (ex: Exception) {
            log.error("Erro ao processar lote ${lote.loteId}: ${ex.message}")
            faturasPendentes.forEach { fatura ->
                publishService.publicarErroException(fatura, ex, lote.loteId)
            }
        }

    }
}