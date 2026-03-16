package com.omie.mapper.queue

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.omie.dto.messageSQS.receiver.FaturaDto
import com.omie.dto.messageSQS.erroFatura.ErrorQueueDto
import com.omie.dto.omieApi.OmieResponse
import com.omie.enums.TipoErro
import java.time.LocalDateTime

object ErrorPublishQueueMapper {

    private val objectMapper = jacksonObjectMapper()

    fun mapFromOmieRejeicao(
        fatura: FaturaDto,
        omieResponse: OmieResponse,
        loteId: String
    ): ErrorQueueDto {
        return ErrorQueueDto(
            faturaId = fatura.id,
            codigoLancamentoIntegracao = fatura.codigoLancamentoIntegracao,
            loteId = loteId,
            tipoErro = TipoErro.OMIE_REJEICAO,
            descricaoErro = omieResponse.error?.message ?: "Fatura rejeitada sem descrição",
            codigoErroOmie = omieResponse.error?.faultcode ?: "UNKNOWN",
            timestamp = LocalDateTime.now().toString(),
            payload = serializePayload(fatura)
        )
    }

    fun mapFromOmieErroLote(
        fatura: FaturaDto,
        omieResponse: OmieResponse,
        loteId: String
    ): ErrorQueueDto {
        return ErrorQueueDto(
            faturaId = fatura.id,
            codigoLancamentoIntegracao = fatura.codigoLancamentoIntegracao,
            loteId = loteId,
            tipoErro = TipoErro.OMIE_ERRO_LOTE,
            descricaoErro = omieResponse.error?.faultstring ?: "Erro no lote sem descrição",
            codigoErroOmie = omieResponse.error?.faultcode ?: "UNKNOWN",
            timestamp = LocalDateTime.now().toString(),
            payload = serializePayload(fatura)
        )
    }

    fun mapFromException(
        fatura: FaturaDto,
        exception: Exception,
        loteId: String
    ): ErrorQueueDto {
        return ErrorQueueDto(
            faturaId = fatura.id,
            codigoLancamentoIntegracao = fatura.codigoLancamentoIntegracao,
            loteId = loteId,
            tipoErro = TipoErro.ERRO_INTERNO,
            descricaoErro = exception.message ?: "Erro interno sem descrição",
            codigoErroOmie = "INTERNAL_ERROR",
            timestamp = LocalDateTime.now().toString(),
            payload = serializePayload(fatura)
        )
    }

    private fun serializePayload(fatura: FaturaDto): String {
        return objectMapper.writeValueAsString(fatura)
    }
}