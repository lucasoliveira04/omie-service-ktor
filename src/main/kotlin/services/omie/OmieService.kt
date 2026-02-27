package com.omie.services.omie

import com.omie.dto.omieApi.OmieResponse
import com.omie.mapper.OmieResponseMapper
import com.omie.services.ClientHttp
import com.typesafe.config.ConfigFactory

class OmieService(private val clientHttp: ClientHttp) {

    val config = ConfigFactory.load()
    val key = config.getConfig("omie.api-sandbox").getString("key")
    val secret = config.getConfig("omie.api-sandbox").getString("secret")
    val base_url = config.getConfig("omie.api-sandbox").getString("base-url")
    val uri = config.getConfig("omie.api-sandbox").getString("uri")

    suspend fun sendFatura(body: Any, appKey: String, appSecret: String): OmieResponse {
        val url = "$base_url$uri"
        val rawResponse = clientHttp.post(url, body)

        val bodyDto = OmieResponseMapper.parseBody(rawResponse.body)
        val errorDto = if (bodyDto == null) OmieResponseMapper.parseError(rawResponse.body) else null

        val rawBodyList = bodyDto?.let { listOf(it) }

        return OmieResponse(
            http_status = rawResponse.status,
            headers = rawResponse.headers,
            raw_body = rawBodyList,
            error = errorDto
        )
    }
}