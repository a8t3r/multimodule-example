package io.eordie.multimodule.contracts.basic.filters

interface LiteralFilter<T> {
    val eq: T?
    val ne: T?
    val of: List<T>?
    val nof: List<T>?
    val nil: Boolean?
}
