package io.eordie.multimodule.example.library.models

import io.eordie.multimodule.example.contracts.library.models.Book
import io.eordie.multimodule.example.repository.Convertable
import io.eordie.multimodule.example.repository.entity.UUIDIdentityIF
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinSql
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "library_books")
interface BookModel : UUIDIdentityIF, Convertable<Book> {

    val name: String

    val authorIds: List<UUID>

    @ManyToMany
    @JoinSql("(%target_alias.ID)=ANY(%alias.AUTHOR_IDS)")
    val authors: List<AuthorModel>

    override fun convert(): Book {
        return Book(
            id,
            name,
            authorIds,
            deleted,
            createdAt,
            updatedAt
        )
    }
}
