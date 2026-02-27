package com.omie.services

interface MessageConsumer<T> {
    fun listen(queue)
}