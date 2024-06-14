package io.eordie.multimodule.common.utils

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.contracts.basic.paging.Page

fun <T : Convertable<O>, O : Any> Page<T>.convert(): Page<O> = Page(
    this.data.map { it.convert() },
    this.pageable
)
