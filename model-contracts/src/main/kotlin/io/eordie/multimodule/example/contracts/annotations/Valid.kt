package io.eordie.multimodule.example.contracts.annotations

import io.eordie.multimodule.example.contracts.annotations.Group.CREATE
import io.eordie.multimodule.example.contracts.annotations.Group.UPDATE
import java.lang.annotation.Inherited

enum class Group {
    CREATE, UPDATE
}

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Valid(
    val value: Array<Group> = [CREATE, UPDATE]
)
