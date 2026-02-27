package com.omie.services.omie

import com.omie.dto.messageSQS.receiver.FaturaDto
import com.omie.dto.omieApi.OmieResponse
import com.omie.enums.QueueEnum
import com.omie.mapper.queue.ErrorPublishQueueMapper
import com.omie.mapper.queue.GerarFaturaPublishMapper
import com.omie.services.MessagePublish
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class FaturaPublishService(
    private val publishService: MessagePublish
) {
    private val log = LoggerFactory.getLogger(FaturaPublishService::class.java)

    private fun buildMetadata(
        fatura: FaturaDto,
        loteId: String,
        status: String,
        eventType: String,
    ): Map<String, String> {

        return mapOf(
            "correlationId" to loteId,
            "messageId" to fatura.id,
            "loteId" to loteId,
            "codigoLancamentoIntegracao" to fatura.codigoLancamentoIntegracao,
            "timestamp" to LocalDateTime.now().toString(),
            "origem" to "omie-service",
            "status" to status,
            "eventType" to eventType
        )
    }
    suspend fun publicarSucesso(
        fatura: FaturaDto,
        response: OmieResponse,
        loteId: String
    ) {

        val dto = GerarFaturaPublishMapper.map(fatura, response)

        publishService.publish(
            queueName = QueueEnum.BOWE_DEV_GERAR_FATURA.queue,
            message = dto,
            metadata = buildMetadata(
                fatura = fatura,
                loteId = loteId,
                status = "success",
                eventType = "gerar_fatura"
            )
        )
    }

    suspend fun publicarErroOmie(
        fatura: FaturaDto,
        response: OmieResponse,
        loteId: String
    ) {

        val dto = ErrorPublishQueueMapper.mapFromOmieError(fatura, response)

        publishService.publish(
            queueName = QueueEnum.BOWE_DEV_ERROR_FATURA.queue,
            message = dto,
            metadata = buildMetadata(
                fatura = fatura,
                loteId = loteId,
                status = "error",
                eventType = "gerar_fatura_omie_error"
            )
        )
    }

    suspend fun publicarErroException(
        fatura: FaturaDto,
        exception: Exception,
        loteId: String
    ) {

        val dto = ErrorPublishQueueMapper.mapFromException(fatura, exception)

        publishService.publish(
            queueName = QueueEnum.BOWE_DEV_ERROR_FATURA.queue,
            message = dto,
            metadata = buildMetadata(
                fatura = fatura,
                loteId = loteId,
                status = "error",
                eventType = "gerar_fatura_exception"
            )
        )
    }

}