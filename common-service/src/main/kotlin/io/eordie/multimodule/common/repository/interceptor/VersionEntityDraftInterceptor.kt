package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.common.repository.entity.VersionedEntityIF
import io.eordie.multimodule.common.repository.entity.VersionedEntityIFDraft
import io.eordie.multimodule.common.repository.entity.VersionedEntityIFProps
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.DraftInterceptor

class VersionEntityDraftInterceptor : DraftInterceptor<VersionedEntityIF, VersionedEntityIFDraft> {

    override fun dependencies(): Collection<TypedProp<VersionedEntityIF, *>> = listOf(VersionedEntityIFProps.VERSION)

    override fun beforeSave(draft: VersionedEntityIFDraft, original: VersionedEntityIF?) {
        if (!isLoaded(draft, VersionedEntityIF::version)) {
            if (original != null && isLoaded(original, VersionedEntityIF::version)) {
                draft.version = original.version
            }
        }
    }
}
