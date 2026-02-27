package com.omie

import com.omie.config.amqp.AmqpConfig
import com.omie.config.redis.RedisConfig
import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.enums.QueueEnum
import com.omie.services.MessageConsumer
import com.omie.services.omie.IdempotencyServiceImpl
import com.omie.services.clientHttp.HttpClientService
import com.omie.services.omie.FaturaPublishService
import com.omie.services.omie.OmieService
import com.omie.services.omie.ProcessLoteService
import com.omie.services.rabbitMqConsumer.AmqpConsumer
import com.omie.services.rabbitMqConsumer.AmqpPublish
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.Application
import org.slf4j.LoggerFactory

private val log =
    LoggerFactory.getLogger(Application::class.java)

fun main() {

    val amqpConfig = AmqpConfig()
    val amqpChannel = amqpConfig.connect()

    val config = ConfigFactory.load()

    val omieConfig = config.getConfig("omie.api-sandbox")
    val omieKey = omieConfig.getString("key")
    val omieSecret = omieConfig.getString("secret")

    val redisUrl = config.getString("redis.url")
    val messageConsumer: MessageConsumer<LoteDto> = AmqpConsumer(amqpChannel, LoteDto::class.java)
    val httpClientService = HttpClientService()
    val omieService = OmieService(httpClientService)
    val redisCommands = RedisConfig(redisUrl).commands
    val idempotencyService = IdempotencyServiceImpl(redisCommands)
    val publishQueue = AmqpPublish(amqpChannel)
    val publishFaturaQueue = FaturaPublishService(publishQueue)
    val processLoteService = ProcessLoteService(omieService, idempotencyService, omieKey, omieSecret, publishFaturaQueue)

    messageConsumer.listen(QueueEnum.BOWE_DEV_LOTE_CONTA_RECEBER_OMIE.queue) { message ->
        val loteDto = message.payload
        processLoteService.processLote(loteDto)
    }
}