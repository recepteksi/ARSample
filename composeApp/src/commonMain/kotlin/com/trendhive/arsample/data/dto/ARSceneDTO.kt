package com.trendhive.arsample.data.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for ARScene.
 * Used for serialization/deserialization in the data layer.
 */
@Serializable
data class ARSceneDTO(
    val id: String,
    val name: String,
    val objects: List<PlacedObjectDTO> = emptyList(),
    val createdAt: Long
)
