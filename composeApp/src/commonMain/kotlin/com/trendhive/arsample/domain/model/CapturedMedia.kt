package com.trendhive.arsample.domain.model

import com.trendhive.arsample.domain.base.BaseModel

/**
 * Represents a captured photo from an AR scene.
 * Stored in the device's pictures directory.
 */
data class CapturedPhoto(
    val id: String,
    val filePath: String,
    val timestamp: Long,
    val width: Int,
    val height: Int
) : BaseModel

/**
 * Represents a captured video from an AR scene.
 * Stored in the device's movies directory.
 */
data class CapturedVideo(
    val id: String,
    val filePath: String,
    val timestamp: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int
) : BaseModel

/**
 * Represents a media item that can be either a photo or video.
 * Used for displaying mixed media in gallery views.
 */
sealed class MediaItem : BaseModel {
    abstract val id: String
    abstract val filePath: String
    abstract val timestamp: Long
    
    data class Photo(val photo: CapturedPhoto) : MediaItem() {
        override val id: String get() = photo.id
        override val filePath: String get() = photo.filePath
        override val timestamp: Long get() = photo.timestamp
    }
    
    data class Video(val video: CapturedVideo) : MediaItem() {
        override val id: String get() = video.id
        override val filePath: String get() = video.filePath
        override val timestamp: Long get() = video.timestamp
    }
}
