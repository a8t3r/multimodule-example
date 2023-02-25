package io.eordie.multimodule.example.library.validation

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.contracts.library.models.Author
import io.eordie.multimodule.example.library.models.BookModel
import io.eordie.multimodule.example.validation.EntityValidator
import io.eordie.multimodule.example.validation.ensureAllPresent
import io.eordie.multimodule.example.validation.isSimpleString
import io.konform.validation.Validation
import io.konform.validation.jsonschema.minItems
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.coroutineContext

@Singleton
class BookValidator(
    private val authors: EntityLoader<Author, UUID>
) : EntityValidator<BookModel> {
    override suspend fun onCreate(): Validation<BookModel> = Validation {}
    override suspend fun onUpdate(): Validation<BookModel> {
        val context = coroutineContext
        return Validation {
            BookModel::name {
                isSimpleString()
            }

            BookModel::authorIds {
                minItems(1)
                ensureAllPresent(context, authors)
            }
        }
    }
}
