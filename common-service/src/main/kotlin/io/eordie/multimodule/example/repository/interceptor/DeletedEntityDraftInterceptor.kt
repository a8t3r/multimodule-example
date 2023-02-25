package io.eordie.multimodule.example.repository.interceptor

import io.eordie.multimodule.example.repository.entity.DeletedIF
import io.eordie.multimodule.example.repository.entity.DeletedIFDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor

class DeletedEntityDraftInterceptor : DraftInterceptor<DeletedIF, DeletedIFDraft> {

    override fun beforeSave(draft: DeletedIFDraft, original: DeletedIF?) {
        if (original == null && !isLoaded(draft, DeletedIF::deleted)) {
            draft.deleted = false
        }
    }
}
