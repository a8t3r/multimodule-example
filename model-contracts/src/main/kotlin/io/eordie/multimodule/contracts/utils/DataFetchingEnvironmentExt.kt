package io.eordie.multimodule.contracts.utils

import graphql.execution.ExecutionId
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import java.util.concurrent.CompletableFuture
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KSuspendFunction2
import kotlin.reflect.KSuspendFunction3

// hallmark of manual invocation from test environment
private val syntheticExecutionId = ExecutionId.from("synthetic")

private fun <V> findLoader(env: DataFetchingEnvironment, function: KCallable<*>): (Pair<DataLoader<Any, V>, Boolean>) {
    return if (function !is CallableReference) error("should be callable reference") else {
        val serviceName = (function.owner as KClass<*>).javaObjectType.simpleName
        val methodName = function.name

        val dataLoaderName = "$serviceName:$methodName"
        val dataLoader = env.getDataLoader<Any, V>(dataLoaderName)
        requireNotNull(dataLoader) to (env.executionId == syntheticExecutionId)
    }
}

fun <A : Any, V> DataFetchingEnvironment.getValuesBy(
    function: KSuspendFunction2<*, List<A>, Map<A, V>>,
    id: List<A>
): CompletableFuture<List<V>> = this.getValuesBy(function as KCallable<*>, id)

fun <A : Any, B : Any?, V> DataFetchingEnvironment.getValuesBy(
    function: KSuspendFunction3<*, List<A>, B, Map<A, V>>,
    id: List<A>,
    arg1: B
): CompletableFuture<List<V>> = this.getValuesBy(function as KCallable<*>, id, arg1)

private fun <V> DataFetchingEnvironment.getValuesBy(
    function: KCallable<*>,
    ids: List<Any>,
    vararg args: Any?
): CompletableFuture<List<V>> {
    val (loader, dispatch) = findLoader<V>(this, function)
    return loader
        .loadMany(ids.map { arrayOf(it, *args) })
        .apply { if (dispatch) loader.dispatch() }
        .thenApply { it.filterNotNull() }
}

fun <A : Any, V> DataFetchingEnvironment.getValueBy(
    function: KSuspendFunction2<*, List<A>, Map<A, V>>,
    id: A
): CompletableFuture<V> = this.getSingleValueBy(function as KCallable<*>, id)

fun <A : Any, B : Any?, V> DataFetchingEnvironment.getValueBy(
    function: KSuspendFunction3<*, List<A>, B, Map<A, V>>,
    id: A,
    arg1: B
): CompletableFuture<V> = this.getSingleValueBy(function as KCallable<*>, id, arg1)

inline fun <reified V : Any?> DataFetchingEnvironment.byId(id: Any?): CompletableFuture<V> {
    return if (id == null) CompletableFuture.completedFuture(null) else {
        val (loader, dispatch) = entityDataLoader<V>()
        val key = arrayOf(id, V::class.qualifiedName)

        loader.load(key, this.graphQlContext)
            .apply { if (dispatch) loader.dispatch() }
    }
}

inline fun <reified V : Any?> DataFetchingEnvironment.byIds(ids: List<Any>): CompletableFuture<List<V>> {
    val (loader, dispatch) = entityDataLoader<V>()

    return loader.loadMany(ids.map { arrayOf(it, V::class.qualifiedName) })
        .apply { if (dispatch) loader.dispatch() }
}

fun <V> DataFetchingEnvironment.entityDataLoader(): Pair<DataLoader<Any, V>, Boolean> {
    val loader = this.getDataLoader<Any, V>("EntityDataLoader")
    val dispatch = executionId.toString() == "synthetic"
    return requireNotNull(loader) to dispatch
}

private fun <V> DataFetchingEnvironment.getSingleValueBy(function: KCallable<*>, id: Any, vararg args: Any?): CompletableFuture<V> {
    val key = arrayOf(id, *args)
    val (loader, dispatch) = findLoader<V>(this, function)
    return loader.load(key, this.graphQlContext)
        .apply { if (dispatch) loader.dispatch() }
}
