package io.eordie.multimodule.common.repository.loader

import io.eordie.multimodule.common.rsocket.client.route.RemoteRoute
import io.eordie.multimodule.common.rsocket.client.rsocket.RSocketLocalFactory
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.micronaut.context.BeanLocator
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaMethod

class EntityLoaderRoute(
    keyType: KClass<*>,
    private val valueType: KClass<*>,
    beanLocator: BeanLocator,
    rsocketFactory: RSocketLocalFactory
) : RemoteRoute(EntityLoader::class, beanLocator, rsocketFactory) {

    private val suspendedLoad = EntityLoader<Any, Any>::load
    private val suspendedLoadPermissions = EntityLoader<Any, Any>::loadPermissions

    private val idType = keyType.createType()

    private val idListType = List::class.createType(
        listOf(KTypeProjection(KVariance.INVARIANT, idType))
    )

    private val returnType = Map::class.createType(
        listOf(
            KTypeProjection(KVariance.INVARIANT, idType),
            KTypeProjection(KVariance.INVARIANT, valueType.createType())
        )
    )

    suspend fun load(ids: List<Any>, context: CoroutineContext): Map<Any, Any> {
        val method = requireNotNull(suspendedLoad.javaMethod)
        return invoke(method, context, arrayOf(ids)) as Map<Any, Any>
    }

    suspend fun loadPermissions(ids: List<Any>, context: CoroutineContext): Map<Any, List<Permission>> {
        val method = requireNotNull(suspendedLoadPermissions.javaMethod)
        return invoke(method, context, arrayOf(ids)) as Map<Any, List<Permission>>
    }

    override fun getReturnType(method: Method): KType = returnType

    override fun getGenericParameterTypes(function: KFunction<*>): List<KType> {
        return when (function) {
            suspendedLoad -> listOf(idListType)
            suspendedLoadPermissions -> listOf(idListType)
            else -> error("unknown method $function")
        }
    }

    override fun getServiceDescriptor(): Package = valueType.java.`package`
    override fun buildServiceName(): String = "${valueType.simpleName}${kotlinIFace.simpleName}"
}
