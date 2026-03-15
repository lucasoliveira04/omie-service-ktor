package com.omie.config.amqp

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class AmqpConfig(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val virtualHost: String = "/"
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
                this.virtualHost = virtualHost
            }
            connection = factory.newConnection()
            channel = connection.createChannel()
        }
        return channel
    }
}