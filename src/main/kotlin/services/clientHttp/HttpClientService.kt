package com.omie.services.clientHttp

import com.omie.services.ClientHttp
import io.ktor.client.engine.cio.*
import sun.net.www.http.HttpClient

class HttpClientService : ClientHttp {
    private val client = HttpClient(CIO) {

    }
}