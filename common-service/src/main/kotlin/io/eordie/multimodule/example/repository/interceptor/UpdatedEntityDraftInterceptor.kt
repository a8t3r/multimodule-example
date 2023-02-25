package io.eordie.multimodule.example.repository.interceptor

import io.eordie.multimodule.example.repository.entity.UpdatedAtIF
import io.eordie.multimodule.example.repository.entity.UpdatedAtIFDraft
import org.babyfish.jimmer.sql.DraftInterceptor
import java.time.OffsetDateTime

class UpdatedEntityDraftInterceptor : DraftInterceptor<UpdatedAtIF, UpdatedAtIFDraft> {

    override fun beforeSave(draft: UpdatedAtIFDraft, original: UpdatedAtIF?) {
        draft.updatedAt = OffsetDateTime.now()
    }
}
