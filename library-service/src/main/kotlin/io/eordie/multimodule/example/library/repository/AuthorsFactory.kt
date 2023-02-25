package io.eordie.multimodule.example.library.repository

import io.eordie.multimodule.example.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.example.filter.accept
import io.eordie.multimodule.example.library.models.AuthorModel
import io.eordie.multimodule.example.library.models.AuthorModelDraft
import io.eordie.multimodule.example.library.models.books
import io.eordie.multimodule.example.library.models.firstName
import io.eordie.multimodule.example.library.models.id
import io.eordie.multimodule.example.library.models.lastName
import io.eordie.multimodule.example.library.models.updatedAt
import io.eordie.multimodule.example.repository.KBaseFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*
import kotlin.coroutines.CoroutineContext

@Singleton
class AuthorsFactory :
    KBaseFactory<AuthorModel, UUID, AuthorsFilter>(AuthorModel::class, AuthorModelDraft.`$`.type) {

    override fun sortingExpressions(table: KNonNullTable<AuthorModel>): List<KPropExpression<out Comparable<*>>> =
        listOf(table.firstName, table.lastName, table.updatedAt, table.id)

    override fun toPredicates(
        context: CoroutineContext,
        filter: AuthorsFilter,
        table: KNonNullTable<AuthorModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.firstName.accept(filter.firstName),
            table.lastName.accept(filter.lastName),
            filter.books?.let {
                table.books {
                    registry.toPredicates(context, it, asTableEx())
                }
            }
        )
    }
}
