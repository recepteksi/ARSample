package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.ARSceneRepository

class CreateSceneUseCase(
    private val repository: ARSceneRepository
) {
    suspend operator fun invoke(name: String): Result<ARScene> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Scene name cannot be blank"))
        }

        val scene = ARScene(
            id = "scene_${currentTimeMillis()}",
            name = name,
            objects = emptyList(),
            createdAt = currentTimeMillis()
        )

        return try {
            repository.saveScene(scene)
            Result.success(scene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
