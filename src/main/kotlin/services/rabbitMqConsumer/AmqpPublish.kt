package com.omie.services.rabbitMqConsumer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.omie.services.MessagePublish
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AmqpPublish(
    private val channel: Channel
) : MessagePublish {

    private val objectMapper = jacksonObjectMapper()

    override suspend fun publish(
        queueName: String,
        message: Any,
        metadata: Map<String, String>
    ) = withContext(Dispatchers.IO) {

        val body = objectMapper.writeValueAsBytes(message)

        val properties = AMQP.BasicProperties.Builder()
            .contentType("application/json")
            .deliveryMode(2)
            .headers(metadata)
            .build()

        channel.basicPublish(
            "",
            queueName,
            properties,
            body
        )
    }
}