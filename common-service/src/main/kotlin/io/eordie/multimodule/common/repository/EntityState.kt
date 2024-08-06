package io.eordie.multimodule.common.repository

enum class EntityState {
    NEW,
    EXISTING,
    PREFETCH
    ;

    fun isNotExists(): Boolean = this != EXISTING

    fun ifExists(block: () -> Unit) {
        if (this == EXISTING) {
            block.invoke()
        }
    }

    fun ifNotExists(block: () -> Unit) {
        if (this != EXISTING) {
            block.invoke()
        }
    }
}
