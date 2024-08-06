package io.eordie.multimodule.common.repository

enum class EntityState {
    NEW,
    EXISTING,
    PREFETCH
    ;

    fun isNotExists(): Boolean = this != EXISTING

    fun ifNotExist(block: () -> Unit) {
        if (this != EXISTING) {
            block.invoke()
        }
    }
}
