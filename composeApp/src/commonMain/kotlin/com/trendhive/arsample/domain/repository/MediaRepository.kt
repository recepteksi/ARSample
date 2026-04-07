package com.trendhive.arsample.domain.repository

import com.trendhive.arsample.domain.base.BaseRepository
import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.model.CapturedVideo

/**
 * Repository interface for media operations (photos and videos captured from AR scenes).
 */
interface MediaRepository : BaseRepository {
    // ==================== Photo Operations ====================
    
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
    
    // ==================== Video Operations ====================
    
    /**
     * Start video recording from the AR scene.
     * @return Result indicating success or failure
     */
    suspend fun startVideoRecording(): Result<Unit>
    
    /**
     * Stop video recording and save the video.
     * @return Result containing the CapturedVideo on success or an exception on failure
     */
    suspend fun stopVideoRecording(): Result<CapturedVideo>
    
    /**
     * Get all captured videos.
     * @return Result containing a list of CapturedVideo on success
     */
    suspend fun getVideos(): Result<List<CapturedVideo>>
    
    /**
     * Delete a captured video by ID.
     * @param id The video's unique identifier
     * @return Result indicating success or failure
     */
    suspend fun deleteVideo(id: String): Result<Unit>
    
    /**
     * Check if video recording is currently in progress.
     * @return true if recording, false otherwise
     */
    fun isRecording(): Boolean
}
