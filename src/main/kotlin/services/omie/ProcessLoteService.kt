package com.omie.services.omie

import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.mapper.http.OmieRequestMapper
import com.omie.services.IdempotencyService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

class ProcessLoteService(
    private val OmieService: OmieService,
    private val idempotencyService: IdempotencyService,
    private val key: String,
    private val secret: String,
    private val publishService: FaturaPublishService
) {
    private val log =
        LoggerFactory.getLogger(ProcessLoteService::class.java)
    suspend fun processLote(lote: LoteDto) {

        coroutineScope {
            lote.faturas.map { fatura ->
                async {
                    val idempotencyKey = fatura.keyFatura

                    if (idempotencyService.exists(idempotencyKey)) {
                        return@async
                    }

                    try {
                        val request = OmieRequestMapper(key, secret).mapFaturasToRequest(listOf(fatura), 1)

                        val response = OmieService.sendFatura(request)

                        if (response.http_status == 200) {
                            idempotencyService.save(idempotencyKey)

                            publishService.publicarSucesso(
                                fatura,
                                response,
                                lote.loteId
                            )

                        } else {
                            publishService.publicarErroOmie(
                                fatura,
                                response,
                                lote.loteId
                            )
                        }
                    } catch (ex: Exception) {
                        publishService.publicarErroException(
                            fatura,
                            ex,
                            lote.loteId
                        )
                    }
                }.await()
            }
        }
    }
}