package com.omie.dto.messageSQS.gerarFatura

import com.omie.dto.messageSQS.FaturaDto
import com.omie.dto.omieApi.OmieResponse
data class GerarFaturaDto(

    val fatura: FaturaDto,
    val response_omie: OmieResponse
)