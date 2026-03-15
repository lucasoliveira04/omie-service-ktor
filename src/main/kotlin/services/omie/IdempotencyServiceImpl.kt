package com.omie.services.omie

import com.omie.services.IdempotencyService
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

class IdempotencyServiceImpl(
    private val redis: RedisCoroutinesCommands<String, String>,
    private val ttl: Long
) : IdempotencyService {

    override suspend fun save(key: String) {

      redis.set(key,
          "processed",
          SetArgs.Builder
              .nx()
              .ex(ttl))

    }

    override suspend fun exists(key: String): Boolean {
        return redis.get(key) != null
    }

    override suspend fun acquireLock(key: String): Boolean {
        val lockKey = "lock:fatura:$key"
        val result = redis.set(lockKey, "locked", SetArgs.Builder.nx().ex(30))
        return result != null
    }
}