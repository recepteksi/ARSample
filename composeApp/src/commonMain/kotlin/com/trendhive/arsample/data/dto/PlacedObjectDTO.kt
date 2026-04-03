package com.trendhive.arsample.data.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for PlacedObject.
 * Used for serialization/deserialization in the data layer.
 */
@Serializable
data class PlacedObjectDTO(
    val objectId: String,
    val arObjectId: String,
    val posX: Float,
    val posY: Float,
    val posZ: Float,
    val rotX: Float,
    val rotY: Float,
    val rotZ: Float,
    val rotW: Float,
    val scale: Float
)
