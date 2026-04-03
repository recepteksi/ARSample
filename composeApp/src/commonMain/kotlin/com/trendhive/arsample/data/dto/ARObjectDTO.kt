package com.trendhive.arsample.data.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for ARObject.
 * Used for serialization/deserialization in the data layer.
 */
@Serializable
data class ARObjectDTO(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: String,
    val thumbnailUri: String?,
    val createdAt: Long,
    val lastPlacedAt: Long?
)
