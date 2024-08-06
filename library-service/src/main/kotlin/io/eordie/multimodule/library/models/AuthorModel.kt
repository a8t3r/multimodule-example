package io.eordie.multimodule.library.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.CreatedAtIF
import io.eordie.multimodule.common.repository.entity.DeletedIF
import io.eordie.multimodule.common.repository.entity.UpdatedAtIF
import io.eordie.multimodule.common.repository.entity.VersionedEntityIF
import io.eordie.multimodule.contracts.library.models.Author
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.Table
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
@Table(name = "library_authors")
interface AuthorModel : CreatedAtIF, UpdatedAtIF, VersionedEntityIF, DeletedIF, Convertable<Author> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generatorType = UUIDIdGenerator::class)
    val id: UUID

    val firstName: String

    val lastName: String?

    @ManyToMany(mappedBy = "authors")
    val books: List<BookModel>
}
