package com.trendhive.arsample.domain.model

import com.trendhive.arsample.domain.base.BaseModel

data class ARObject(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: ModelType,
    val thumbnailUri: String? = null,
    val createdAt: Long = currentTimeMillis(),
    val lastPlacedAt: Long? = null
) : BaseModel
