package com.omie.services.omie

import com.omie.dto.omieApi.OmieResponse
import com.omie.helpers.HttpResponseHelper
import com.omie.services.ClientHttp
import com.typesafe.config.ConfigFactory

class OmieService
    (private val clientHttp: ClientHttp) {
    private val config = ConfigFactory.load()
    private val base_url = config.getConfig("omie.api-sandbox").getString("base-url")
    private val uri = config.getConfig("omie.api-sandbox").getString("uri")
    private val responseHelper = HttpResponseHelper()
    suspend fun sendFatura(body: Any): OmieResponse {
        val url = "$base_url$uri"

        val rawResponse = clientHttp.post(
            url = url,
            body = body,
            headers = mapOf("Content-Type" to "application/json")
        )

        return responseHelper.parse(
            status = rawResponse.status,
            headers = rawResponse.headers,
            body = rawResponse.body
        )
    }
}