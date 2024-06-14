package io.eordie.multimodule.common.validation

interface EntityValidator<T> {
    suspend fun onCreate(value: T) = Unit
    suspend fun onUpdate(value: T)
}
