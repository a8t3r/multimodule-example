package io.eordie.multimodule.library.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.library.models.Author
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
}
