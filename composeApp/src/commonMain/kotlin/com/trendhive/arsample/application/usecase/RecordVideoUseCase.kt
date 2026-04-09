package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.CapturedVideo
import com.trendhive.arsample.domain.repository.MediaRepository

/**
 * Use case for recording AR scene videos.
 * Manages the start/stop recording lifecycle and validates recording state.
 */
class RecordVideoUseCase(
    private val mediaRepository: MediaRepository
) {
    /**
     * Start video recording.
     * @return Result indicating success or failure
     * @throws ValidationException if already recording
     */
    suspend fun startRecording(): Result<Unit> {
        // Check if already recording
        if (mediaRepository.isRecording()) {
            return Result.failure(ValidationException("Video recording is already in progress"))
        }
        
        return mediaRepository.startVideoRecording()
    }
    
    /**
     * Stop video recording and save the video.
     * @return Result containing the saved CapturedVideo on success
     * @throws ValidationException if not currently recording
     */
    suspend fun stopRecording(): Result<CapturedVideo> {
        // Check if not recording
        if (!mediaRepository.isRecording()) {
            return Result.failure(ValidationException("No video recording in progress"))
        }
        
        return mediaRepository.stopVideoRecording()
    }
    
    /**
     * Check if video recording is currently in progress.
     * @return true if recording, false otherwise
     */
    fun isRecording(): Boolean {
        return mediaRepository.isRecording()
    }
}
