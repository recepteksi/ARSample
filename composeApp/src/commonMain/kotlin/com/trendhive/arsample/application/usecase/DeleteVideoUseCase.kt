package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.repository.MediaRepository

/**
 * Use case for deleting a captured video.
 */
class DeleteVideoUseCase(
    private val mediaRepository: MediaRepository
) {
    /**
     * Delete a video by its ID.
     * @param id The video's unique identifier
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(id: String): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(ValidationException("Video ID cannot be blank"))
        }
        return mediaRepository.deleteVideo(id)
    }
}
