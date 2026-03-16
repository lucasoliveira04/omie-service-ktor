package com.omie.services.omie

import com.omie.dto.omieApi.OmieResponse
import com.omie.helpers.HttpResponseHelper
import com.omie.services.ClientHttp
import com.typesafe.config.ConfigFactory
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.IOException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeoutException

class OmieGateway(
    private val clientHttp: ClientHttp,
    private val omieRetryConfig: OmieRetryConfig,
    private val baseUrl: String,
    private val uri: String
) { private val responseHelper = HttpResponseHelper()
    private val rateLimite = Semaphore(3)

    suspend fun sendFatura(body: Any): OmieResponse {
        rateLimite.withPermit {
            val url = "$baseUrl$uri"

            return omieRetryConfig.retry.executeSuspendFunction {
                val rawResponse = clientHttp.post(
                    url = url,
                    body = body,
                    headers = mapOf("Content-Type" to "application/json")
                )

                responseHelper.parse(
                    status = rawResponse.status,
                    headers = rawResponse.headers,
                    body = rawResponse.body
                )
            }
        }
    }
}