package io.eordie.multimodule.example.repository.interceptor

import io.eordie.multimodule.example.repository.entity.CreatedAtIF
import io.eordie.multimodule.example.repository.entity.CreatedAtIFDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import java.time.OffsetDateTime

class CreatedEntityDraftInterceptor : DraftInterceptor<CreatedAtIF, CreatedAtIFDraft> {

    override fun beforeSave(draft: CreatedAtIFDraft, original: CreatedAtIF?) {
        if (original == null && !isLoaded(draft, CreatedAtIF::createdAt)) {
            draft.createdAt = OffsetDateTime.now()
        }
    }
}
