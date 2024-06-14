package io.eordie.multimodule.common.validation

import io.eordie.multimodule.common.rsocket.context.systemContext
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.basic.loader.loadOne
import org.valiktor.Validator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotNull
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun <E> Validator<E>.Property<String?>.isSimpleString() =
    this.isNotNull().isNotBlank().hasSize(min = 3)

suspend fun <T, R : Any, E> Validator<E>.Property<Iterable<T>?>.ensureAllAccessible(loader: EntityLoader<R, T>) =
    this.coValidate(IsPresent) { it != null && loader.load(it.toList()).size == it.toList().size }

suspend fun <T, R : Any, E> Validator<E>.Property<T?>.ensureIsAccessible(
    loader: EntityLoader<R, T>,
    optional: Boolean = false
) = ensureIsPresent(coroutineContext, loader, optional)

suspend fun <T, R : Any, E> Validator<E>.Property<T?>.ensureIsPresent(
    loader: EntityLoader<R, T>,
    optional: Boolean = false
) = ensureIsPresent(systemContext, loader, optional)

private suspend fun <T, R : Any, E> Validator<E>.Property<T?>.ensureIsPresent(
    context: CoroutineContext,
    loader: EntityLoader<R, T>,
    optional: Boolean = false
) =
    this.coValidate(IsPresent) {
        (optional && it == null) || (it != null && loader.loadOne(context, it) != null)
    }
