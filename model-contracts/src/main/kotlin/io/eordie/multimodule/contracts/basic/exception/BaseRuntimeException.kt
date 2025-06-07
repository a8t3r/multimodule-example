package io.eordie.multimodule.contracts.basic.exception

import kotlinx.serialization.Serializable
import java.lang.StackTraceElement as NativeStackTraceElement

@Serializable
data class StackTraceElement(
    val className: String,
    val methodName: String,
    val fileName: String?,
    val lineNumber: Int
)

@Serializable
sealed class BaseRuntimeException() : RuntimeException() {
    open fun extensions(): Map<String, Any> = emptyMap()

    var originStackTrace: List<StackTraceElement> = stackTrace.transform()

    private fun Array<NativeStackTraceElement>.transform() = this.map {
        StackTraceElement(
            it.className, it.methodName, it.fileName, it.lineNumber
        )
    }

    fun wrapStackTrace(cause: Throwable): BaseRuntimeException = this.apply {
        this.originStackTrace = cause.stackTrace.transform()
    }

    fun unwrapStackTrace(): BaseRuntimeException = this.apply {
        this.stackTrace = originStackTrace.map {
            NativeStackTraceElement(
                it.className, it.methodName, it.fileName, it.lineNumber
            )
        }.toTypedArray()
    }
}
