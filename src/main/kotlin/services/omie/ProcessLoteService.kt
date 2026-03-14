package com.omie.services.omie

import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.mapper.http.OmieRequestMapper
import com.omie.services.IdempotencyService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
        coroutineScope {
            lote.faturas
                .map { fatura ->
                    async {
                        val idempotencyKey = fatura.keyFatura

                        if (idempotencyService.exists(idempotencyKey)) {
                            return@async
                        }

                        try {
                            semaphore.withPermit {
                                val request = OmieRequestMapper(key, secret)
                                    .mapFaturasToRequest(listOf(fatura), 1)

                                val response = omieGateway.sendFatura(request)

                                log.info(
                                    "Resposta recebida para fatura ${fatura.id} " +
                                            "no lote ${lote.loteId}: ${response.http_status} - ${response.error}"
                                )

                                if (response.http_status == 200) {
                                    idempotencyService.save(idempotencyKey)
                                    publishService.publicarSucesso(fatura, response, lote.loteId)
                                    log.info("Fatura ${fatura.id} processada com sucesso no lote ${lote.loteId}")
                                } else {
                                    publishService.publicarErroOmie(fatura, response, lote.loteId)
                                }
                            }
                        } catch (ex: Exception) {
                            publishService.publicarErroException(fatura, ex, lote.loteId)
                        }
                    }
                }
                .awaitAll()
        }
    }
}