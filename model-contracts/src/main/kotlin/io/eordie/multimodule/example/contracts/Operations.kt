package io.eordie.multimodule.example.contracts

import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

interface Query
interface Mutation

fun <V> DataFetchingEnvironment.getValueBy(function: KCallable<*>, id: Any, vararg args: Any?): CompletableFuture<V> {
    return if (function !is CallableReference) error("should be callable reference") else {
        val serviceName = (function.owner as KClass<*>).javaObjectType.simpleName
        val methodName = function.name

        val dataLoaderName = "$serviceName:$methodName"
        val loader = this.getDataLoader<Any, V>(dataLoaderName)

        val key = arrayOf(id, *args)
        loader.load(key, this.graphQlContext)
    }
}
