package io.eordie.multimodule.example.repository

interface Convertable<T : Any> {
    fun convert(): T
}
