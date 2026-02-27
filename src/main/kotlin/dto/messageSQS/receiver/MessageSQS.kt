package com.omie.dto.messageSQS.receiver

data class MessageSQS<T>(

    val payload: T,

    val metadata: Map<String, String>
)