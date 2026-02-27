package com.omie.mapper.queue

import com.omie.dto.messageSQS.receiver.MessageSQS

object MessageReceiverMapper {

    fun <T> map(
        payload: T,
        metadata: Map<String, Any>?
    ): MessageSQS<T> {

        val headers =
            metadata.orEmpty()
                .mapValues {
                    it.value.toString()
                }

        return MessageSQS(
            payload = payload,
            metadata = headers
        )
    }
}