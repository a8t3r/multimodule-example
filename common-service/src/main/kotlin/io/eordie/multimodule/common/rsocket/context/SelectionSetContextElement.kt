package io.eordie.multimodule.common.rsocket.context

import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import kotlin.coroutines.CoroutineContext

class SelectionSetContextElement(
    val selectionSet: SelectionSet
) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<SelectionSetContextElement>

    override val key: CoroutineContext.Key<*> = Key
}

fun CoroutineContext.getSelectionSet(): SelectionSet? {
    return get(SelectionSetContextElement.Key)?.selectionSet
}
