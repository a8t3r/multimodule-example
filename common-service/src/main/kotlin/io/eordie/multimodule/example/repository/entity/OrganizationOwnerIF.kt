package io.eordie.multimodule.example.repository.entity

import org.babyfish.jimmer.sql.MappedSuperclass
import java.util.*

@MappedSuperclass
interface OrganizationOwnerIF {
    val organizationId: UUID
}
