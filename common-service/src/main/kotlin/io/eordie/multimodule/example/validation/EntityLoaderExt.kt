package io.eordie.multimodule.example.validation

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.konform.validation.ValidationBuilder
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import kotlin.coroutines.CoroutineContext

fun ValidationBuilder<String>.isSimpleString() {
    minLength(2)
    maxLength(32)
}

inline fun <reified T : Any, R> ValidationBuilder<R>.ensureIsPresent(
    context: CoroutineContext,
    loader: EntityLoader<T, R>
) {
    addConstraint("item should be present") {
        loader.load(context, listOf(it)).size == 1
    }
}

inline fun <reified T : Any, R> ValidationBuilder<List<R>>.ensureAllPresent(
    context: CoroutineContext,
    loader: EntityLoader<T, R>
) {
    addConstraint("all items should be present") {
        val values = loader.load(context, it)
        val missing = it.toSet() - values.keys
        missing.isEmpty()
    }
}
