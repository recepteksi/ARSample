package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.model.CapturedVideo
import com.trendhive.arsample.domain.repository.MediaRepository

/**
 * Use case for retrieving all captured videos.
 */
class GetVideosUseCase(
    private val mediaRepository: MediaRepository
) {
    /**
     * Get all captured videos sorted by timestamp (newest first).
     * @return Result containing the list of videos on success
     */
    suspend operator fun invoke(): Result<List<CapturedVideo>> {
        return mediaRepository.getVideos().map { videos ->
            videos.sortedByDescending { it.timestamp }
        }
    }
}
