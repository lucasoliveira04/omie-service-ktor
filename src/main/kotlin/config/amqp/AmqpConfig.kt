package com.omie.config.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class AmqpConfig(
    private val host: String = "localhost",
    private val port: Int = 5672,
    private val username: String = "guest",
    private val password: String = "guest"
) {
    private lateinit var connection: Connection
    private lateinit var channel: Channel

    fun connect(): Channel {
        if (!::channel.isInitialized) {
            val factory = ConnectionFactory().apply {
                this.host = host
                this.port = port
                this.username = username
                this.password = password
            }

            connection = factory.newConnection()
            channel = connection.createChannel()
        }
        return channel
    }
}