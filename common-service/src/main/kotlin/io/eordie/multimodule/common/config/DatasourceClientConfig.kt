package io.eordie.multimodule.common.config

import io.eordie.multimodule.common.repository.interceptor.CreatedEntityDraftInterceptor
import io.eordie.multimodule.common.repository.interceptor.DeletedEntityDraftInterceptor
import io.eordie.multimodule.common.repository.interceptor.UpdatedEntityDraftInterceptor
import io.eordie.multimodule.common.repository.interceptor.VersionEntityDraftInterceptor
import io.eordie.multimodule.common.utils.OpenTelemetryExecutorLog
import io.micronaut.configuration.lettuce.cache.RedisCache
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.opentelemetry.api.OpenTelemetry
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.sql.EnumType
import org.babyfish.jimmer.sql.JSqlClient
import org.babyfish.jimmer.sql.cache.Cache
import org.babyfish.jimmer.sql.cache.chain.ChainCacheBuilder
import org.babyfish.jimmer.sql.dialect.Dialect
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.event.TriggerType
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.cache.KCacheFactory
import org.babyfish.jimmer.sql.kt.toKSqlClient
import org.babyfish.jimmer.sql.runtime.ConnectionManager
import org.babyfish.jimmer.sql.runtime.Executor
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import java.sql.Connection
import java.sql.Types
import java.util.*
import java.util.function.Function

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

    @Singleton
    fun dialect(): Dialect {
        return object : PostgresDialect() {
            override fun resolveJdbcType(sqlType: Class<*>): Int {
                return when (sqlType) {
                    java.lang.Boolean::class.java -> Types.BOOLEAN
                    java.util.List::class.java -> Types.ARRAY
                    else -> super.resolveJdbcType(sqlType)
                }
            }
        }
    }

    @EachBean(JdbcOperations::class)
    @Singleton
    fun createClientByLock(
        executor: Executor,
        dialect: Dialect,
        operations: JdbcOperations,
        scalarProviders: List<ScalarProvider<*, *>>,
        @Named("jimmer") cache: Optional<RedisCache>
    ): KSqlClient = JSqlClient.newBuilder()
        .setDialect(dialect)
        .apply {
            addScalarProvider(TPointProvider())
            addScalarProvider(TLineProvider())
            addScalarProvider(TPolygonProvider())
            addScalarProvider(TMultiPolygonProvider())
            scalarProviders.forEach {
                addScalarProvider(it)
            }
        }
        .addDraftInterceptors(
            CreatedEntityDraftInterceptor(),
            UpdatedEntityDraftInterceptor(),
            DeletedEntityDraftInterceptor(),
            VersionEntityDraftInterceptor()
        )
        .setExecutor(executor)
        .setDefaultEnumStrategy(EnumType.Strategy.ORDINAL)
        .apply {
            cache.ifPresent {
                setCacheFactory(object : KCacheFactory {
                    override fun createObjectCache(type: ImmutableType): Cache<*, *> {
                        return ChainCacheBuilder<Any, Any>()
                            .add(RedisValueBinder<Any, Any>(type, cache.get()))
                            .build()
                    }
                })
            }
        }
        .setConnectionManager(object : ConnectionManager {
            override fun <R : Any> execute(con: Connection?, block: Function<Connection, R>): R? {
                return if (con != null) {
                    block.apply(con)
                } else {
                    operations.execute {
                        block.apply(it)
                    }
                }
            }
        })
        .setTriggerType(TriggerType.TRANSACTION_ONLY)
        .build()
        .toKSqlClient()
}
