package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.repository.ARObjectRepository

class DeleteObjectUseCase(
    private val repository: ARObjectRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("ID cannot be blank"))
        }

        return try {
            repository.deleteObject(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
