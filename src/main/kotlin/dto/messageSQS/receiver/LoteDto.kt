package com.omie.dto.messageSQS.receiver

data class LoteDto(
    val numeroLote: Int,
    val loteId: String,
    val correlationId: String,
    val dataCriacao: String,
    val origem: String,
    val faturas: List<FaturaDto>
)
