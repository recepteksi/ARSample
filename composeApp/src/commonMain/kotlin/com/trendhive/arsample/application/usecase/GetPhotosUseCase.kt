package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.repository.MediaRepository

/**
 * Use case for retrieving all captured photos.
 */
class GetPhotosUseCase(
    private val mediaRepository: MediaRepository
) {
    /**
     * Get all captured photos sorted by timestamp (newest first).
     * @return Result containing the list of photos on success
     */
    suspend operator fun invoke(): Result<List<CapturedPhoto>> {
        return mediaRepository.getPhotos().map { photos ->
            photos.sortedByDescending { it.timestamp }
        }
    }
}
