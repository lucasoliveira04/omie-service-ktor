package com.omie.services

interface MessagePublish {
    suspend fun publish(
        queueName: String,
        message: Any,
        metadata: Map<String, String> = emptyMap()
    )
}