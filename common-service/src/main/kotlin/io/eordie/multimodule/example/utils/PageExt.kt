package io.eordie.multimodule.example.utils

import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.repository.Convertable
import kotlin.reflect.KProperty1

fun <T : Convertable<O>, O : Any, P> Page<T>.associateBy(idMapper: (T) -> P): Map<P, O> {
    return this.data.associateBy({ idMapper.invoke(it) }, { it.convert() })
}

fun <T : Convertable<O>, O : Any, P> Page<T>.associateBy(property: KProperty1<T, P>): Map<P, O> {
    return this.data.associateBy({ property.invoke(it) }, { it.convert() })
}

fun <T : Convertable<O>, O : Any, P> Page<T>.associateByList(ids: List<P>, property: KProperty1<T, List<P>>): Map<P, List<O>> {
    return this.associateByList(ids) { property.invoke(it) }
}

fun <T : Convertable<O>, O : Any, P> Page<T>.associateByList(ids: List<P>, idMapper: (T) -> List<P>): Map<P, List<O>> {
    val index = data
        .flatMap { element -> idMapper.invoke(element).map { it to element } }
        .groupBy({ it.first }, { it.second.convert() })

    return ids.fold(mutableMapOf()) { acc, id ->
        acc[id] = index[id].orEmpty()
        acc
    }
}

fun <T : Convertable<O>, O : Any> Page<T>.convert(): Page<O> = Page(
    this.data.map { it.convert() },
    this.pageable
)
