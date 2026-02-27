package com.omie.mapper.queue

import com.omie.dto.messageSQS.FaturaDto
import com.omie.dto.messageSQS.gerarFatura.GerarFaturaDto
import com.omie.dto.omieApi.OmieResponse

object GerarFaturaPublishMapper {

    fun map(
        fatura: FaturaDto,
        omieResponse: OmieResponse
    ): GerarFaturaDto {

        return GerarFaturaDto(
            fatura = fatura,
            response_omie = omieResponse
        )
    }
}