package io.eordie.multimodule.common.utils

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.sql.runtime.DefaultExecutor
import org.babyfish.jimmer.sql.runtime.ExecutionPurpose
import org.babyfish.jimmer.sql.runtime.Executor
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor
import org.babyfish.jimmer.sql.runtime.SqlFormatter
import java.sql.Connection
import kotlin.time.measureTimedValue

class OpenTelemetryExecutorLog(
    private val tracer: Tracer
) : Executor {

    private val delegate = DefaultExecutor.INSTANCE
    private val formatter = SqlFormatter.INLINE_PRETTY

    fun <R> makeSpan(sql: String, variables: List<Any>, positions: List<Int>?, purpose: ExecutionPurpose, block: () -> R): R {
        val variablePositions = positions ?: run {
            sql.mapIndexedNotNull { index, c -> index.takeIf { c == '?' } }.map { it.inc() }
        }

        val tableName = sql.substringAfter(" from ", "").substringBefore(' ')
        val query = StringBuilder().apply { formatter.append(this, sql, variables, variablePositions) }

        val span = tracer.spanBuilder("$purpose $tableName")
            .setParent(Context.current())
            .setAttribute(purpose.toString(), query.toString())
            .startSpan()

        try {
            val (result, duration) = measureTimedValue {
                block.invoke()
            }
            val executionTime = Attributes.of(AttributeKey.longKey("execution_time"), duration.inWholeMilliseconds)
            span.addEvent("completed", executionTime)
            return result
        } finally {
            span.end()
        }
    }

    override fun <R : Any?> execute(args: Executor.Args<R>): R {
        return makeSpan(args.sql, args.variables, args.variablePositions, args.purpose) {
            delegate.execute(args)
        }
    }

    override fun executeBatch(
        con: Connection,
        sql: String,
        generatedIdProp: ImmutableProp?,
        purpose: ExecutionPurpose,
        sqlClient: JSqlClientImplementor
    ): Executor.BatchContext {
        return makeSpan(sql, emptyList(), emptyList(), ExecutionPurpose.UPDATE) {
            delegate.executeBatch(con, sql, generatedIdProp, purpose, sqlClient)
        }
    }
}
