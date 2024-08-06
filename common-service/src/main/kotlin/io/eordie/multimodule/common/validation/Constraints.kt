package io.eordie.multimodule.common.validation

import io.eordie.multimodule.contracts.basic.Permission
import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.DefaultConstraintViolation

interface CommonConstraint : Constraint {
    override val messageBundle: String get() = "common/messages"
}

object Cycle : CommonConstraint

object IsPresent : CommonConstraint

data class MissingPermission(val permission: Permission) : CommonConstraint

fun Constraint.error(dataPath: String? = null): Nothing {
    val violation = DefaultConstraintViolation(dataPath.orEmpty(), constraint = this)
    throw ConstraintViolationException(setOf(violation))
}
