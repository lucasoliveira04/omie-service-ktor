package com.omie.services
interface IdempotencyService {
    suspend fun exists(key: String): Boolean
    suspend fun save(key: String)
    suspend fun acquireLock(key: String): Boolean
}