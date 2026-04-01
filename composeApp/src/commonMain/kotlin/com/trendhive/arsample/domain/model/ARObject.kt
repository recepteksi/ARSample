package com.trendhive.arsample.domain.model

data class ARObject(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: ModelType,
    val thumbnailUri: String? = null,
    val createdAt: Long = currentTimeMillis(),
    val lastPlacedAt: Long? = null
)
