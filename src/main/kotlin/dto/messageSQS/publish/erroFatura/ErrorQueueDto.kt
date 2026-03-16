package com.omie.dto.messageSQS.erroFatura

import com.omie.enums.TipoErro

data class ErrorQueueDto(
    val faturaId: String,
    val codigoLancamentoIntegracao: String,
    val loteId: String,
    val tipoErro: TipoErro,
    val descricaoErro: String,
    val codigoErroOmie: String,
    val timestamp: String,
    val payload: String
)