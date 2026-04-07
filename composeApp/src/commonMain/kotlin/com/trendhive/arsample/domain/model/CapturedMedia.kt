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
