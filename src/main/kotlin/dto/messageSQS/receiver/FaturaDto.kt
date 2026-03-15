package com.omie.dto.messageSQS.receiver

data class FaturaDto(
    val id: String,
    val codigoLancamentoIntegracao: String,
    val codigoCliente: Long,
    val dataVencimento: String,
    val valor: Double,
    val codigoCategoria: String,
    val dataPrevisao: String,
    val idContaCorrente: Long,
    val keyFatura: String,
    val tipoCliente: String = "default"
)
