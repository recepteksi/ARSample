package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.repository.ARObjectRepository

class ImportObjectUseCase(
    private val repository: ARObjectRepository
) {
    suspend operator fun invoke(
        uri: String,
        name: String,
        modelType: ModelType
    ): Result<ARObject> {
        if (uri.isBlank()) {
            return Result.failure(IllegalArgumentException("URI cannot be blank"))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be blank"))
        }
        if (modelType !in ModelType.entries) {
            return Result.failure(IllegalArgumentException("Invalid model type"))
        }

        return repository.importObject(uri, name, modelType)
    }
}
