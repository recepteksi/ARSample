package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.MediaRepository

/**
 * Use case for capturing and saving AR scene photos.
 * Validates image data before delegating to the repository.
 */
class CapturePhotoUseCase(
    private val mediaRepository: MediaRepository
) {
    /**
     * Capture and save a photo from the AR scene.
     * @param imageData The raw JPEG image data
     * @return Result containing the saved CapturedPhoto on success
     */
    suspend operator fun invoke(imageData: ByteArray): Result<CapturedPhoto> {
        // Validate image data
        if (imageData.isEmpty()) {
            return Result.failure(ValidationException("Image data cannot be empty"))
        }
        
        // Minimum size check (a valid JPEG should be at least a few KB)
        if (imageData.size < 100) {
            return Result.failure(ValidationException("Image data appears to be invalid (too small)"))
        }
        
        // Generate a unique filename with timestamp
        val timestamp = currentTimeMillis()
        val filename = "AR_Capture_$timestamp.jpg"
        
        return mediaRepository.savePhoto(imageData, filename)
    }
}
