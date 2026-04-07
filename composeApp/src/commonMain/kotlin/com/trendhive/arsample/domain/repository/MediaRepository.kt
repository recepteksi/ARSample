package com.trendhive.arsample.domain.repository

import com.trendhive.arsample.domain.base.BaseRepository
import com.trendhive.arsample.domain.model.CapturedPhoto

/**
 * Repository interface for media operations (photos captured from AR scenes).
 */
interface MediaRepository : BaseRepository {
    /**
     * Save a photo to the device's pictures directory.
     * @param imageData The raw image data as bytes (JPEG format expected)
     * @param filename The desired filename for the photo
     * @return Result containing the CapturedPhoto on success or an exception on failure
     */
    suspend fun savePhoto(imageData: ByteArray, filename: String): Result<CapturedPhoto>
    
    /**
     * Get all captured photos.
     * @return Result containing a list of CapturedPhoto on success
     */
    suspend fun getPhotos(): Result<List<CapturedPhoto>>
    
    /**
     * Delete a captured photo by ID.
     * @param id The photo's unique identifier
     * @return Result indicating success or failure
     */
    suspend fun deletePhoto(id: String): Result<Unit>
}
