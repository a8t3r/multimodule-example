package io.eordie.multimodule.library.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.ext.negateUnless
import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.library.models.AuthorModel
import io.eordie.multimodule.library.models.AuthorModelDraft
import io.eordie.multimodule.library.models.books
import io.eordie.multimodule.library.models.firstName
import io.eordie.multimodule.library.models.id
import io.eordie.multimodule.library.models.lastName
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class AuthorsFactory :
    KBaseFactory<AuthorModel, AuthorModelDraft, Author, UUID, AuthorsFilter>(AuthorModel::class) {

    override fun ResourceAcl.toPredicates(
        filter: AuthorsFilter,
        table: KNonNullTable<AuthorModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.firstName.accept(filter.firstName),
            table.lastName.accept(filter.lastName),
            table.books { accept(filter.books) }.negateUnless(filter.hasBooks)
        )
    }
}
