package com.omie.services

import com.omie.dto.http.HttpResponse

interface ClientHttp {
    suspend fun post(url: String, body: Any, headers: Map<String, String> = emptyMap()): HttpResponse
}