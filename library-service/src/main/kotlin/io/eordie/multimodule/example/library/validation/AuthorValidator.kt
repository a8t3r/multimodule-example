package io.eordie.multimodule.example.library.validation

import io.eordie.multimodule.example.library.models.AuthorModel
import io.eordie.multimodule.example.validation.EntityValidator
import io.eordie.multimodule.example.validation.isSimpleString
import io.konform.validation.Validation
import jakarta.inject.Singleton

@Singleton
class AuthorValidator : EntityValidator<AuthorModel> {

    override suspend fun onCreate(): Validation<AuthorModel> = Validation {}
    override suspend fun onUpdate(): Validation<AuthorModel> = Validation {
        AuthorModel::firstName {
            isSimpleString()
        }
        AuthorModel::lastName ifPresent {
            isSimpleString()
        }
    }
}
