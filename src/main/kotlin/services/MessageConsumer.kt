package com.omie.services

import com.omie.dto.messageSQS.receiver.MessageSQS

interface MessageConsumer<T> {

    fun listen(
        queueName: String,
        handler: suspend (MessageSQS<T>) -> Unit
    )
}