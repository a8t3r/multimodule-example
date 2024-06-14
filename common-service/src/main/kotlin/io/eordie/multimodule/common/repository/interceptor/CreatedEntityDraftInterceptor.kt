package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.common.repository.entity.CreatedAtIF
import io.eordie.multimodule.common.repository.entity.CreatedAtIFDraft
import io.eordie.multimodule.common.repository.entity.CreatedAtIFProps
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.DraftInterceptor
import java.time.OffsetDateTime

class CreatedEntityDraftInterceptor : DraftInterceptor<CreatedAtIF, CreatedAtIFDraft> {

    override fun dependencies(): Collection<TypedProp<CreatedAtIF, *>> = listOf(CreatedAtIFProps.CREATED_AT)

    override fun beforeSave(draft: CreatedAtIFDraft, original: CreatedAtIF?) {
        if (original === null || !isLoaded(draft, CreatedAtIF::createdAt)) {
            draft.createdAt = OffsetDateTime.now()
        }
    }
}
