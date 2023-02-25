package io.eordie.multimodule.example.config

import io.eordie.multimodule.example.repository.interceptor.CreatedEntityDraftInterceptor
import io.eordie.multimodule.example.repository.interceptor.DeletedEntityDraftInterceptor
import io.eordie.multimodule.example.repository.interceptor.UpdatedEntityDraftInterceptor
import io.eordie.multimodule.example.utils.OpenTelemetryExecutorLog
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.opentelemetry.api.OpenTelemetry
import jakarta.inject.Singleton
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.sql.JSqlClient
import org.babyfish.jimmer.sql.cache.Cache
import org.babyfish.jimmer.sql.cache.chain.ChainCacheBuilder
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.event.TriggerType
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.cache.KCacheFactory
import org.babyfish.jimmer.sql.kt.toKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager
import org.babyfish.jimmer.sql.runtime.Executor
import org.babyfish.jimmer.sql.runtime.JdbcTypes
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import java.sql.Connection
import java.sql.Types
import java.util.*
import java.util.function.Function
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

@Factory
class DatasourceClientConfig {

    @Singleton
    fun createSqlLogger(env: Environment, openTelemetry: OpenTelemetry): Executor {
        return if (env.activeNames.contains("test")) {
            Executor.log()
        } else {
            OpenTelemetryExecutorLog(openTelemetry.tracerBuilder("sql").build())
        }
    }

    private fun hack() {
        val internalTypes = JdbcTypes::class.declaredMembers
            .filter { it.name == "SQL_TYPE_MAP" }
            .map {
                it.isAccessible = true
                it.call() as MutableMap<Class<*>, Int>
            }
            .first()
        internalTypes[UUID::class.java] = Types.OTHER
    }

    @EachBean(JdbcOperations::class)
    @Singleton
    fun createBuilder(
        executor: Executor,
        operations: JdbcOperations,
        scalarProviders: List<ScalarProvider<*, *>>
    ): KSqlClient {
        hack()

        return JSqlClient.newBuilder()
            .setDialect(PostgresDialect())
            .apply {
                scalarProviders.forEach {
                    addScalarProvider(it)
                }
            }
            .addDraftInterceptors(
                CreatedEntityDraftInterceptor(),
                UpdatedEntityDraftInterceptor(),
                DeletedEntityDraftInterceptor()
            )
            .setExecutor(executor)
            .setCacheFactory(object : KCacheFactory {
                override fun createObjectCache(type: ImmutableType): Cache<*, *> {
                    return ChainCacheBuilder<Any, Any>()
                        .add(GuavaCacheBinder())
                        .build()
                }
            })
            .setConnectionManager(object : ConnectionManager {
                override fun <R : Any> execute(block: Function<Connection, R>): R? {
                    return operations.execute {
                        block.apply(it)
                    }
                }
            })
            .setTriggerType(TriggerType.TRANSACTION_ONLY)
            .build()
            .toKSqlClient()
    }
}
