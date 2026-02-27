package com.omie.services.rabbitMqConsumer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.omie.dto.messageSQS.receiver.MessageSQS
import com.omie.mapper.queue.MessageReceiverMapper
import com.omie.services.MessageConsumer
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class AmqpConsumer<T>(
    private val channel: Channel,
    private val clazz: Class<T>
) : MessageConsumer<T> {
    private val log =
        LoggerFactory.getLogger(AmqpConsumer::class.java)
    private val objectMapper = jacksonObjectMapper()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun listen(queueName: String, handler: suspend (MessageSQS<T>) -> Unit) {
        val deliverCallback = DeliverCallback { _, delivery ->
            val rawMessage = String(delivery.body, Charsets.UTF_8)

            scope.launch {

                try {
                    val payload: T =
                        objectMapper.readValue(rawMessage, clazz)

                    val message =
                        MessageReceiverMapper.map(
                            payload,
                            delivery.properties.headers
                        )

                    handler(message)

                    channel.basicAck(delivery.envelope.deliveryTag, false)

                } catch (e: Exception) {
                    log.info("Erro ao processar mensagem da fila {}: {}", queueName, e.message)
                    e.printStackTrace()
                }
            }
        }

        channel.basicConsume(queueName, false, deliverCallback) {}
    }
}