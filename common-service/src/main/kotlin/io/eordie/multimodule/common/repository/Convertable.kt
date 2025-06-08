package io.eordie.multimodule.common.repository

interface Convertable<T : Any> {
    fun convert(): T = EntityConverter.convert(this)
    fun convert(block: (T) -> Unit) = convert().apply(block)
}
