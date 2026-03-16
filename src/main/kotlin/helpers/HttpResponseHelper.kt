package com.omie.helpers

import com.omie.dto.omieApi.OmieResponse
import com.omie.mapper.http.OmieResponseMapper

class HttpResponseHelper {

    fun parse(
        status: Int,
        headers: Map<String, String>,
        body: String
    ): OmieResponse {

        val errorDto = OmieResponseMapper.parseError(body)

        // Trata erro se qualquer campo de erro estiver preenchido.
        // O Omie pode retornar faultstring/faultcode sem preencher message.
        if (errorDto != null && (errorDto.message != null || errorDto.faultstring != null || errorDto.faultcode != null)) {
            return OmieResponse(
                http_status = status,
                headers = mapOf("Content-Type" to "application/json"),
                raw_body = emptyList(),
                error = errorDto
            )
        }

        val successDto = OmieResponseMapper.parseBody(body)

        return OmieResponse(
            http_status = status,
            headers = mapOf("Content-Type" to "application/json"),
            raw_body = successDto?.let { listOf(it) } ?: emptyList(),
            error = null
        )
    }
}