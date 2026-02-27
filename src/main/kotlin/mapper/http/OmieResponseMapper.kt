package com.omie.mapper.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.omie.dto.omieApi.OmieErrorDto
import com.omie.dto.omieApi.OmieResponseBody

object OmieResponseMapper {
    private val objectMapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    fun parseBody(raw: String): OmieResponseBody? = try {
        objectMapper.readValue(raw)
    } catch (ex: Exception) {
        throw ex
    }
    fun parseError(raw: String): OmieErrorDto? = try {
        objectMapper.readValue(raw)
    } catch (ex: Exception) {
        throw ex
    }
}