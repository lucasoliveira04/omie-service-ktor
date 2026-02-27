package com.omie.services.clientHttp

import com.omie.dto.http.HttpResponse
import com.omie.services.ClientHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.util.*

class HttpClientService : ClientHttp {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
            }
        }
    }

    override suspend fun post(url: String, body: Any, headers: Map<String, String>): HttpResponse {
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        return HttpResponse(
            status = response.status.value,
            headers = response.headers.toMap().mapValues { it.value.joinToString(",") },
            body = response.bodyAsText()
        )
    }
}