package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.common.repository.entity.DeletedIF
import io.eordie.multimodule.common.repository.entity.DeletedIFDraft
import io.eordie.multimodule.common.repository.entity.DeletedIFProps
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.DraftInterceptor

class DeletedEntityDraftInterceptor : DraftInterceptor<DeletedIF, DeletedIFDraft> {

    override fun dependencies(): Collection<TypedProp<DeletedIF, *>> = listOf(DeletedIFProps.DELETED)

    override fun beforeSave(draft: DeletedIFDraft, original: DeletedIF?) {
        if (original === null || !isLoaded(draft, DeletedIF::deleted)) {
            draft.deleted = false
        }
    }
}
