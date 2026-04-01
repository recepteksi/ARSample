package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.repository.ARSceneRepository

class RemoveObjectFromSceneUseCase(
    private val repository: ARSceneRepository
) {
    suspend operator fun invoke(sceneId: String, objectId: String): Result<ARScene> {
        if (sceneId.isBlank()) {
            return Result.failure(IllegalArgumentException("Scene ID cannot be blank"))
        }
        if (objectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Object ID cannot be blank"))
        }

        val scene = repository.getSceneById(sceneId)
            ?: return Result.failure(IllegalArgumentException("Scene not found"))

        val objectExists = scene.objects.any { it.objectId == objectId }
        if (!objectExists) {
            return Result.failure(IllegalArgumentException("Object not found in scene"))
        }

        return repository.removeObjectFromScene(sceneId, objectId)
    }
}
