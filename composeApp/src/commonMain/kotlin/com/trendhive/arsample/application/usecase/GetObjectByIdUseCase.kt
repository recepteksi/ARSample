package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.application.dto.OptionalResult
import com.trendhive.arsample.domain.repository.ARObjectRepository

/**
 * Input model for getting an AR object by ID.
 */
data class GetObjectByIdInput(
    val id: String
) : BaseModel

/**
 * Use case for retrieving a single AR object by its ID.
 */
class GetObjectByIdUseCase(
    private val repository: ARObjectRepository
) : BaseUseCase<GetObjectByIdInput, OptionalResult<ARObject>> {
    
    override suspend operator fun invoke(input: GetObjectByIdInput): Result<OptionalResult<ARObject>> {
        if (input.id.isBlank()) {
            return Result.failure(ValidationException("ID cannot be blank"))
        }
        
        return try {
            val obj = repository.getObjectById(input.id)
            Result.success(OptionalResult(obj))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(id: String): ARObject? {
        return repository.getObjectById(id)
    }
}
