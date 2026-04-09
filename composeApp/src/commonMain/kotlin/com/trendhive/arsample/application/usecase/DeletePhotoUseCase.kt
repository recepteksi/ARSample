package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.repository.MediaRepository

/**
 * Use case for deleting a captured photo.
 */
class DeletePhotoUseCase(
    private val mediaRepository: MediaRepository
) {
    /**
     * Delete a photo by its ID.
     * @param id The photo's unique identifier
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(ValidationException("Photo ID cannot be blank"))
        }
        return mediaRepository.deletePhoto(id)
    }
}
