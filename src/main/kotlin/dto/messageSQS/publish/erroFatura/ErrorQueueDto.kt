package com.omie.dto.messageSQS.erroFatura

data class ErrorQueueDto(

    val faturaId: String,

    val descricao_erro: String,
    val payload : String,
    val codigo_erro_omie: String

)