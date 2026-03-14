package com.omie

import com.omie.dto.messageSQS.receiver.LoteDto
import com.omie.enums.QueueEnum
import com.omie.services.MessageConsumer
import com.omie.services.omie.ProcessLoteService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

object AppDependencies : KoinComponent {
    val consumer: MessageConsumer<LoteDto> by inject()
    val processLoteService: ProcessLoteService by inject()
}

fun main() {
    startKoin {
        modules(appModule)
    }

    val consumer = AppDependencies.consumer
    val processLoteService = AppDependencies.processLoteService

    consumer.listen(QueueEnum.BOWE_DEV_LOTE_CONTA_RECEBER_OMIE.queue) { message ->
        processLoteService.processLote(message.payload)
    }
}