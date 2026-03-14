package com.omie

import com.omie.config.amqp.AmqpConfig
import com.omie.config.redis.RedisConfig
import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.services.ClientHttp
import com.omie.services.IdempotencyService
import com.omie.services.MessageConsumer
import com.omie.services.MessagePublish
import com.omie.services.clientHttp.HttpClientService
import com.omie.services.omie.FaturaPublishService
import com.omie.services.omie.IdempotencyServiceImpl
import com.omie.services.omie.OmieGateway
import com.omie.services.omie.ProcessLoteService
import com.omie.services.rabbitMqConsumer.AmqpConsumer
import com.omie.services.rabbitMqConsumer.AmqpPublish
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    // Config
    single(qualifier = named("rootConfig")) { ConfigFactory.load() as Config }
    single(qualifier = named("omieConfig")) {
        get<Config>(named("rootConfig")).getConfig("omie.api-sandbox")
    }

    // Infra
    single { AmqpConfig().connect() }
    single { RedisConfig(get<Config>(named("rootConfig")).getString("redis.url")).commands }

    // Services
    single<ClientHttp> { HttpClientService() }
    single { OmieGateway(get()) }
    single<IdempotencyService> { IdempotencyServiceImpl(get()) }
    single<MessagePublish> { AmqpPublish(get()) }
    single { FaturaPublishService(get()) }

    single {
        val cfg = get<Config>(named("omieConfig"))
        ProcessLoteService(
            omieGateway = get(),
            idempotencyService = get(),
            key = cfg.getString("key"),
            secret = cfg.getString("secret"),
            publishService = get()
        )
    }

    single<MessageConsumer<LoteDto>> {
        AmqpConsumer(get(), LoteDto::class.java)
    }
}