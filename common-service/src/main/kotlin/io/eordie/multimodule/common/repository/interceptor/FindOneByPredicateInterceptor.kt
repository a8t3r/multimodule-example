package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.common.filter.FiltersRegistry
import io.eordie.multimodule.common.security.context.Microservices
import io.eordie.multimodule.common.utils.OpenTelemetryExecutorLog
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.ApplicationContext
import io.micronaut.core.annotation.Internal
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.reflect.ReflectionUtils
import io.micronaut.data.annotation.Query
import io.micronaut.kotlin.context.getBean
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.ast.impl.AstContext
import org.babyfish.jimmer.sql.ast.impl.query.ConfigurableRootQueryImpl
import org.babyfish.jimmer.sql.ast.impl.query.UseTableVisitor
import org.babyfish.jimmer.sql.ast.table.Table
import org.babyfish.jimmer.sql.ast.tuple.Tuple3
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.value
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.runtime.ExecutionPurpose
import org.babyfish.jimmer.sql.runtime.Executor
import org.babyfish.jimmer.sql.runtime.SqlBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass

@Internal
@Singleton
class FindOneByPredicateInterceptor(
    context: ApplicationContext
) {

    private val registry: FiltersRegistry = context.getBean<FiltersRegistry>()
    private val microservices: Microservices = context.getBean<Microservices>()
    private val conversionService: ConversionService = context.getBean<ConversionService>()
    private val executorLog: OpenTelemetryExecutorLog? = context.getBean<Executor>()
        .takeIf { it is OpenTelemetryExecutorLog } as OpenTelemetryExecutorLog?

    private fun ConfigurableRootQueryImpl<Table<Any>, *>.prepare(
        nativeSql: String
    ): Tuple3<String, MutableList<Any>, MutableList<Int>> {
        val sqlBuilder = SqlBuilder(AstContext(baseQuery.sqlClient))

        baseQuery.applyVirtualPredicates(sqlBuilder.astContext)
        accept(UseTableVisitor(sqlBuilder.astContext))

        renderTo(sqlBuilder)
        val sqlResult = sqlBuilder.build()
        val finalQuery = nativeSql + " where " + sqlResult._1.substringAfter("where", "true")

        return sqlResult.copy(_1 = finalQuery)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <X : Any, V> KConfigurableRootQuery<X, V>.asNative(): ConfigurableRootQueryImpl<Table<X>, V> {
        val query = this
        val javaQuery = ReflectionUtils.getFieldValue(query::class.java, "_javaQuery", query).get()
        return javaQuery as ConfigurableRootQueryImpl<Table<X>, V>
    }

    fun intercept(sql: KSqlClient, rootEntity: KClass<Any>, context: MethodInvocationContext<Any, Any>): Any? {
        val query = context.stringValue(Query::class.java)
            .orElseThrow { IllegalStateException("No query present in method") }

        val coroutineContext = (context.parameterValues.last() as Continuation<*>).context
        val acl = microservices.buildAcl(coroutineContext)

        val baseQuery = sql.createQuery(rootEntity) {
            val filter = context.parameterValues[0] as Any
            where(registry.toPredicates(acl, filter, table) ?: value(true))
            select(value(1))
        }

        val binding = baseQuery.asNative().prepare(query)
        val (finalQuery, parameters, positions: List<Int>?) = binding.copy(_2 = binding._2.drop(1).toMutableList())
        val returnType = context.executableMethod.returnType.wrapperType

        val block: () -> Any? = {
            sql.javaClient.connectionManager.execute { connection ->
                val jdbcTemplate = JdbcTemplate(SingleConnectionDataSource(connection, false))
                jdbcTemplate.queryForObject(
                    finalQuery,
                    IntrospectedRowMapper(returnType, conversionService),
                    *parameters.toTypedArray()
                )
            }
        }

        val response = executorLog
            ?.makeSpan(finalQuery, parameters, positions, ExecutionPurpose.QUERY) { block.invoke() }
            ?: block.invoke()

        return response
    }
}
