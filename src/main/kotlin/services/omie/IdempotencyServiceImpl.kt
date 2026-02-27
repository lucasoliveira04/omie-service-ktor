package com.omie.services.omie

import com.omie.services.IdempotencyService
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

class IdempotencyServiceImpl(
    private val redis: RedisCoroutinesCommands<String, String>
) : IdempotencyService {

    override suspend fun save(key: String) {

      redis.set(key,
          "processed",
          SetArgs.Builder
              .nx()
              .ex(60 * 60 * 24))

    }

    override suspend fun exists(key: String): Boolean {
        return redis.get(key) != null
    }
}