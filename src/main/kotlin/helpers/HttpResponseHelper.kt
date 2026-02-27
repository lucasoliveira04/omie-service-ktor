package com.omie.helpers

import com.omie.dto.omieApi.OmieResponse
import com.omie.mapper.http.OmieResponseMapper
import kotlin.collections.emptyList

class HttpResponseHelper {

    fun parse(
        status: Int,
        headers: Map<String, String>,
        body: String
    ): OmieResponse {

        val errorDto = OmieResponseMapper.parseError(body)

        if (errorDto != null && errorDto.message != null) {
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