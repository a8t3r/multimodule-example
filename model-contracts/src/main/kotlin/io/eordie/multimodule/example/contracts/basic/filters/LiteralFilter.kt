package io.eordie.multimodule.example.contracts.basic.filters

interface LiteralFilter<T> {
    var eq: T?
    var ne: T?
    var of: List<T>?
    var nof: List<T>?
    var exists: Boolean?
}
