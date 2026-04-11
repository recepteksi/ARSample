package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.application.usecase.DeletePhotoUseCase
import com.trendhive.arsample.application.usecase.DeleteVideoUseCase
import com.trendhive.arsample.application.usecase.GetPhotosUseCase
import com.trendhive.arsample.application.usecase.GetVideosUseCase
import com.trendhive.arsample.domain.model.CapturedPhoto
import com.trendhive.arsample.domain.model.CapturedVideo
import com.trendhive.arsample.domain.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Filter options for gallery media display.
 */
enum class GalleryFilter {
    ALL,
    PHOTOS,
    VIDEOS
}

/**
 * UI State for the Gallery screen.
 */
data class GalleryUiState(
    val photos: List<CapturedPhoto> = emptyList(),
    val videos: List<CapturedVideo> = emptyList(),
    val filter: GalleryFilter = GalleryFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMedia: MediaItem? = null,
    val isPreviewVisible: Boolean = false
) {
    /**
     * Get filtered media items based on current filter.
     * Returns a combined list sorted by timestamp (newest first).
     */
    val filteredMedia: List<MediaItem>
        get() {
            val photoItems = photos.map { MediaItem.Photo(it) }
            val videoItems = videos.map { MediaItem.Video(it) }
            
            return when (filter) {
                GalleryFilter.ALL -> (photoItems + videoItems).sortedByDescending { it.timestamp }
                GalleryFilter.PHOTOS -> photoItems.sortedByDescending { it.timestamp }
                GalleryFilter.VIDEOS -> videoItems.sortedByDescending { it.timestamp }
            }
        }
    
    /**
     * Check if gallery is empty for the current filter.
     */
    val isEmpty: Boolean
        get() = filteredMedia.isEmpty()
    
    /**
     * Get counts for display.
     */
    val photoCount: Int get() = photos.size
    val videoCount: Int get() = videos.size
    val totalCount: Int get() = photoCount + videoCount
}

/**
 * ViewModel for the Gallery screen.
 * Manages media (photos and videos) display and operations.
 */
class GalleryViewModel(
    private val getPhotosUseCase: GetPhotosUseCase,
    private val getVideosUseCase: GetVideosUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) {
    
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()
    
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        loadMedia()
    }
    
    /**
     * Load all media (photos and videos).
     */
    fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load photos and videos in parallel using coroutineScope
                coroutineScope {
                    val photosDeferred = async { getPhotosUseCase() }
                    val videosDeferred = async { getVideosUseCase() }
                    
                    val photosResult = photosDeferred.await()
                    val videosResult = videosDeferred.await()
                    
                    val photos = photosResult.getOrElse { emptyList() }
                    val videos = videosResult.getOrElse { emptyList() }
                    
                    // Check for partial failures
                    val errors = mutableListOf<String>()
                    if (photosResult.isFailure) {
                        errors.add("Photos: ${photosResult.exceptionOrNull()?.message}")
                    }
                    if (videosResult.isFailure) {
                        errors.add("Videos: ${videosResult.exceptionOrNull()?.message}")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        photos = photos,
                        videos = videos,
                        isLoading = false,
                        error = if (errors.isNotEmpty()) errors.joinToString("; ") else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load media"
                )
            }
        }
    }
    
    /**
     * Set the filter for media display.
     */
    fun setFilter(filter: GalleryFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }
    
    /**
     * Delete a photo by ID.
     */
    fun deletePhoto(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            deletePhotoUseCase(id).fold(
                onSuccess = {
                    // Remove from local state and reload
                    _uiState.value = _uiState.value.copy(
                        photos = _uiState.value.photos.filter { it.id != id },
                        isLoading = false,
                        selectedMedia = null,
                        isPreviewVisible = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete photo"
                    )
                }
            )
        }
    }
    
    /**
     * Delete a video by ID.
     */
    fun deleteVideo(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            deleteVideoUseCase(id).fold(
                onSuccess = {
                    // Remove from local state
                    _uiState.value = _uiState.value.copy(
                        videos = _uiState.value.videos.filter { it.id != id },
                        isLoading = false,
                        selectedMedia = null,
                        isPreviewVisible = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete video"
                    )
                }
            )
        }
    }
    
    /**
     * Select a media item for preview.
     */
    fun selectMedia(item: MediaItem) {
        _uiState.value = _uiState.value.copy(
            selectedMedia = item,
            isPreviewVisible = true
        )
    }
    
    /**
     * Close the preview.
     */
    fun closePreview() {
        _uiState.value = _uiState.value.copy(
            selectedMedia = null,
            isPreviewVisible = false
        )
    }
    
    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
