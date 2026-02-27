package com.omie.dto.http

data class HttpResponse(
    val status: Int,
    val headers: Map<String, String>,
    val body: String
)