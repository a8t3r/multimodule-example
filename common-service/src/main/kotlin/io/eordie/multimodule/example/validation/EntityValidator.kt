package io.eordie.multimodule.example.validation

import io.konform.validation.Validation

interface EntityValidator<T> {
    suspend fun onCreate(): Validation<T>
    suspend fun onUpdate(): Validation<T>
}
