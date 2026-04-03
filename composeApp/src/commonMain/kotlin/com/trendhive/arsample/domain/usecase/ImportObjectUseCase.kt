package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.domain.base.BaseUseCase
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.model.valueobjects.ModelUri
import com.trendhive.arsample.domain.model.valueobjects.ObjectName
import com.trendhive.arsample.domain.repository.ARObjectRepository

/**
 * Input model for importing a 3D object.
 */
data class ImportObjectInput(
    val uri: String,
    val name: String,
    val modelType: ModelType
) : BaseModel

/**
 * Use case for importing 3D objects into the library.
 * Validates input using Value Objects before delegating to repository.
 */
class ImportObjectUseCase(
    private val repository: ARObjectRepository
) : BaseUseCase<ImportObjectInput, ARObject> {
    
    override suspend operator fun invoke(input: ImportObjectInput): Result<ARObject> {
        // Validate using Value Objects
        val nameResult = ObjectName.create(input.name)
        if (nameResult.isFailure) {
            return Result.failure(nameResult.exceptionOrNull()!!)
        }
        
        val uriResult = ModelUri.create(input.uri)
        if (uriResult.isFailure) {
            return Result.failure(uriResult.exceptionOrNull()!!)
        }
        
        return repository.importObject(input.uri, input.name, input.modelType)
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(
        uri: String,
        name: String,
        modelType: ModelType
    ): Result<ARObject> {
        return invoke(ImportObjectInput(uri, name, modelType))
    }
}
