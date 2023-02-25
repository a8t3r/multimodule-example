package io.eordie.multimodule.example.library.models

import io.eordie.multimodule.example.contracts.library.models.Author
import io.eordie.multimodule.example.repository.Convertable
import io.eordie.multimodule.example.repository.entity.UUIDIdentityIF
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "library_authors")
interface AuthorModel : UUIDIdentityIF, Convertable<Author> {

    val firstName: String

    val lastName: String?

    @ManyToMany(mappedBy = "authors")
    val books: List<BookModel>

    override fun convert(): Author {
        return Author(
            id,
            firstName,
            lastName,
            deleted,
            createdAt,
            updatedAt
        )
    }
}
