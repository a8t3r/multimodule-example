package io.eordie.multimodule.example.repository

import io.eordie.multimodule.example.repository.entity.CreatedAtIF
import io.eordie.multimodule.example.repository.entity.DeletedIF
import io.eordie.multimodule.example.repository.entity.UpdatedAtIF
import io.eordie.multimodule.example.repository.entity.VersionedEntityIF
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface AuditableAware : CreatedAtIF, UpdatedAtIF, VersionedEntityIF, DeletedIF
