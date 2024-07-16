package io.eordie.multimodule.library.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.library.models.Book
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
}
