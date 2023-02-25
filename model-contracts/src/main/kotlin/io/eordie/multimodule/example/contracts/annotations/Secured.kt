package io.eordie.multimodule.example.contracts.annotations

import io.eordie.multimodule.example.contracts.utils.Roles
import java.lang.annotation.Inherited

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Secured(
    val allowAnonymous: Boolean = false,
    val denyAll: Boolean = false,

    /**
     * synonym to allOf
     */
    vararg val value: Roles,

    val allOf: Array<Roles> = [],
    val oneOf: Array<Roles> = []
)
