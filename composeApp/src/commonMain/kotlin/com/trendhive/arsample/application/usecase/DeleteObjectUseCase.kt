package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.application.dto.UnitResult
import com.trendhive.arsample.domain.repository.ARObjectRepository

/**
 * Input model for deleting an AR object.
 */
data class DeleteObjectInput(
    val id: String
) : BaseModel

/**
 * Use case for deleting an AR object from the library.
 */
class DeleteObjectUseCase(
    private val repository: ARObjectRepository
) : BaseUseCase<DeleteObjectInput, UnitResult> {
    
    override suspend operator fun invoke(input: DeleteObjectInput): Result<UnitResult> {
        if (input.id.isBlank()) {
            return Result.failure(ValidationException("ID cannot be blank"))
        }
        
        return try {
            repository.deleteObject(input.id)
            Result.success(UnitResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        val result = invoke(DeleteObjectInput(id))
        return result.map { }
    }
}
