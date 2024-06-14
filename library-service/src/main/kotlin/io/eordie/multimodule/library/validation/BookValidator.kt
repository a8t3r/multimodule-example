package io.eordie.multimodule.library.validation

import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.ensureAllAccessible
import io.eordie.multimodule.common.validation.isSimpleString
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.library.models.BookModel
import jakarta.inject.Singleton
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.util.*

@Singleton
class BookValidator(
    private val authors: EntityLoader<Author, UUID>
) : EntityValidator<BookModel> {

    override suspend fun onUpdate(value: BookModel) {
        validate(value) {
            validate(BookModel::name).isSimpleString()
            validate(BookModel::authorIds).isNotNull().hasSize(min = 1).ensureAllAccessible(authors)
        }
    }
}
