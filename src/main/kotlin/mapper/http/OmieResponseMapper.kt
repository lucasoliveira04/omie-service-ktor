package com.omie.mapper.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.omie.dto.omieApi.OmieErrorDto
import com.omie.dto.omieApi.OmieResponseBody

object OmieResponseMapper {
    private val objectMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun parseBody(raw: String): OmieResponseBody? = try {
        val node = objectMapper.readTree(raw)
        if (node.has("lote")) {
            objectMapper.treeToValue(node, OmieResponseBody::class.java)
        } else null
    } catch (ex: Exception) { null }

    fun parseError(raw: String): OmieErrorDto? = try {
        val node = objectMapper.readTree(raw)
        if (node.has("faultstring") || node.has("message") || node.has("faultcode")) {
            objectMapper.treeToValue(node, OmieErrorDto::class.java)
        } else null
    } catch (ex: Exception) { null }
}