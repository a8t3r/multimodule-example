package io.eordie.multimodule.contracts.utils

import kotlinx.serialization.Serializable

@Serializable
@Suppress("MagicNumber")
enum class Roles(val index: Int) {
    VIEW_ORGANIZATION(1),
    MANAGE_ORGANIZATION(2),
    VIEW_MEMBERS(3),
    MANAGE_MEMBERS(4),
    VIEW_ROLES(5),
    MANAGE_ROLES(6),
    VIEW_INVITATIONS(7),
    MANAGE_INVITATIONS(8),
    VIEW_IDENTITY_PROVIDERS(9),
    MANAGE_IDENTITY_PROVIDERS(10),

    MANAGE_ORGANIZATIONS(50),
    VIEW_ORGANIZATIONS(51),
    MANAGE_USERS(52),
    VIEW_USERS(53),
    CREATE_ORGANIZATION(54),
    ;

    fun internalName(): String = name.lowercase().replace('_', '-')

    fun isSystemRole() = index >= 50

    fun isOrganizationRole() = index <= 10

    companion object {
        private val index = entries.associateBy { it.index }

        fun toIds(values: List<Roles>) = values.map { it.index }
        fun fromIds(ids: List<Int>?) = ids.orEmpty().mapNotNull { index[it] }
    }
}
