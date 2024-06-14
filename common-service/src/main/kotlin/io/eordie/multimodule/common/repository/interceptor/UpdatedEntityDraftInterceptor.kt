package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.common.repository.entity.UpdatedAtIF
import io.eordie.multimodule.common.repository.entity.UpdatedAtIFDraft
import org.babyfish.jimmer.sql.DraftInterceptor
import java.time.OffsetDateTime

class UpdatedEntityDraftInterceptor : DraftInterceptor<UpdatedAtIF, UpdatedAtIFDraft> {

    override fun beforeSave(draft: UpdatedAtIFDraft, original: UpdatedAtIF?) {
        draft.updatedAt = OffsetDateTime.now()
    }
}
