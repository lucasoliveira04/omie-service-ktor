package com.omie.services.omie
import com.omie.dto.omieApi.OmieResponse
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.event.RetryOnErrorEvent
import io.github.resilience4j.retry.event.RetryOnRetryEvent
import io.github.resilience4j.retry.event.RetryOnSuccessEvent
import kotlinx.io.IOException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeoutException

class OmieRetryConfig {
    private val log = LoggerFactory.getLogger(OmieRetryConfig::class.java)

    val retry: Retry

    init {
        retry = Retry.of("omie-gateway", buildRetryConfig())

        retry.eventPublisher
            .onRetry { event: RetryOnRetryEvent ->
                log.warn(
                    "Retry ${event.numberOfRetryAttempts} para OMIE — " +
                            "aguardando ${event.waitInterval.seconds}s"
                )
            }
            .onError { event: RetryOnErrorEvent ->
                log.error("OMIE esgotou ${event.numberOfRetryAttempts} tentativas")
            }
            .onSuccess { event: RetryOnSuccessEvent ->
                if (event.numberOfRetryAttempts > 0) {
                    log.info("OMIE respondeu com sucesso após ${event.numberOfRetryAttempts} retries")
                }
            }
    }

    private fun buildRetryConfig(): RetryConfig {
        return RetryConfig.custom<OmieResponse>()
            .maxAttempts(3)
            .intervalFunction(
                IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(10), 2.0)
            )
            .retryOnResult { response: OmieResponse ->
                response.error?.faultcode == "SOAP-ENV:Client-8020"
            }
            .retryOnException { ex: Throwable ->
                ex is IOException || ex is TimeoutException
            }
            .build()
    }
}