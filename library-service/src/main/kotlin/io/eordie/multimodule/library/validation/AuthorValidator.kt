package io.eordie.multimodule.library.validation

import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.isSimpleString
import io.eordie.multimodule.library.models.AuthorModel
import jakarta.inject.Singleton
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

@Singleton
class AuthorValidator : EntityValidator<AuthorModel> {
    override suspend fun onUpdate(value: AuthorModel) {
        validate(value) {
            validate(AuthorModel::firstName).isSimpleString()
            validate(AuthorModel::lastName).isNotEmpty().hasSize(min = 3)
        }
    }
}
