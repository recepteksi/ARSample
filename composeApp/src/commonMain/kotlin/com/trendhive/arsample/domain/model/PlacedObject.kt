package com.trendhive.arsample.domain.model

data class PlacedObject(
    val objectId: String,
    val arObjectId: String,
    val position: Vector3,
    val rotation: Quaternion = Quaternion.IDENTITY,
    val scale: Float = 1f
)
