package com.omie.dto.omieApi

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OmieResponse(
    val http_status: Int,
    val headers: Map<String, String>,
    val raw_body: List<OmieResponseBody> ? = emptyList(),
    val error: OmieErrorDto? = null
)

data class OmieResponseBody(
    val lote: Int,
    val codigo_status: String,
    val descricao_status: String,
    val status_lote: List<OmieStatusDto>
)

data class OmieStatusDto(
    val codigo_lancamento_integracao: String,
    val codigo_lancamento_omie: Long,
    val codigo_status: String,
    val descricao_status: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OmieErrorDto(
    val faultstring: String?,
    val faultcode: String?,
    val status: String?,
    val message: String?,
)