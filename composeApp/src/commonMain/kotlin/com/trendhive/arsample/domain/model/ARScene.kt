package com.trendhive.arsample.domain.model

data class ARScene(
    val id: String,
    val name: String,
    val objects: List<PlacedObject> = emptyList(),
    val createdAt: Long = currentTimeMillis()
) {
    companion object {
        fun createDefault(): ARScene = ARScene(
            id = "scene_${currentTimeMillis()}",
            name = "Default Scene"
        )
    }
}
