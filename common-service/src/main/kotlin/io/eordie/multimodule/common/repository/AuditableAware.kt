package io.eordie.multimodule.common.repository

import io.eordie.multimodule.common.repository.entity.CreatedAtIF
import io.eordie.multimodule.common.repository.entity.DeletedIF
import io.eordie.multimodule.common.repository.entity.UpdatedAtIF
import io.eordie.multimodule.common.repository.entity.VersionedEntityIF
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface AuditableAware : CreatedAtIF, UpdatedAtIF, VersionedEntityIF, DeletedIF
