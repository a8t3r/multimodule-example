package io.eordie.multimodule.common.repository

interface Convertable<T : Any> {
    fun convert(): T = EntityConverter.convert(this)
}
