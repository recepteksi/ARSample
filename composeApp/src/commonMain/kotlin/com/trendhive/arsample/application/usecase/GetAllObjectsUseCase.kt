package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.application.dto.ListResult
import com.trendhive.arsample.application.dto.NoInput
import com.trendhive.arsample.domain.repository.ARObjectRepository

/**
 * Use case for retrieving all AR objects from the library.
 */
class GetAllObjectsUseCase(
    private val repository: ARObjectRepository
) : BaseUseCase<NoInput, ListResult<ARObject>> {
    
    override suspend operator fun invoke(input: NoInput): Result<ListResult<ARObject>> {
        return try {
            val objects = repository.getAllObjects()
            Result.success(ListResult(objects))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(): List<ARObject> {
        return repository.getAllObjects()
    }
}
