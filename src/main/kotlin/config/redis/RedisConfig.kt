package com.omie.config.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.codec.StringCodec

class RedisConfig(url: String) {
    private val client = RedisClient.create(url)
    private val connection: StatefulRedisConnection<String, String> =
        client.connect(StringCodec.UTF8)

    val commands: RedisCoroutinesCommands<String, String> =
        connection.coroutines()
}