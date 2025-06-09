package io.eordie.multimodule.contracts.utils

import kotlinx.serialization.Contextual
import java.time.OffsetDateTime
import java.util.*

typealias UuidStr = @Contextual UUID
typealias LocaleStr = @Contextual Locale
typealias OffsetDateTimeStr = @Contextual OffsetDateTime
typealias RoleSet = @Contextual EnumSet<Roles>

fun List<Roles>.asRoleSet(): RoleSet {
    val roles = this
    return if (roles.isEmpty()) EnumSet.noneOf(Roles::class.java) else {
        EnumSet.of(roles.first(), *roles.drop(1).toTypedArray())
    }
}

fun RoleSet.asList(): List<Roles> = iterator().asSequence().toList()

