package com.omie.mapper.queue

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.omie.dto.messageSQS.receiver.FaturaDto
import com.omie.dto.messageSQS.erroFatura.ErrorQueueDto
import com.omie.dto.omieApi.OmieResponse

object ErrorPublishQueueMapper {

    private val objectMapper = jacksonObjectMapper()

    fun mapFromOmieError(
        fatura: FaturaDto,
        omieResponse: OmieResponse
    ): ErrorQueueDto {

        return ErrorQueueDto(
            faturaId = fatura.id,
            descricao_erro = omieResponse.error?.message ?: "Erro desconhecido",
            payload = serializePayload(fatura),
            codigo_erro_omie = omieResponse.error?.faultcode ?: "UNKNOWN"
        )
    }

    fun mapFromException(
        fatura: FaturaDto,
        exception: Exception
    ): ErrorQueueDto {

        return ErrorQueueDto(
            faturaId = fatura.id,
            descricao_erro = exception.message ?: "Erro desconhecido",
            payload = serializePayload(fatura),
            codigo_erro_omie = "INTERNAL_ERROR"
        )
    }

    private fun serializePayload(fatura: FaturaDto): String {
        return objectMapper.writeValueAsString(fatura)
    }
}