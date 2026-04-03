package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.domain.base.BaseUseCase
import com.trendhive.arsample.domain.exception.EntityNotFoundException
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.repository.ARSceneRepository

/**
 * Input model for removing an object from a scene.
 */
data class RemoveObjectInput(
    val sceneId: String,
    val objectId: String
) : BaseModel

/**
 * Use case for removing a placed object from an AR scene.
 */
class RemoveObjectFromSceneUseCase(
    private val repository: ARSceneRepository
) : BaseUseCase<RemoveObjectInput, ARScene> {
    
    override suspend operator fun invoke(input: RemoveObjectInput): Result<ARScene> {
        if (input.sceneId.isBlank()) {
            return Result.failure(ValidationException("Scene ID cannot be blank"))
        }
        if (input.objectId.isBlank()) {
            return Result.failure(ValidationException("Object ID cannot be blank"))
        }

        val scene = repository.getSceneById(input.sceneId)
            ?: return Result.failure(EntityNotFoundException("Scene not found"))

        val objectExists = scene.objects.any { it.objectId == input.objectId }
        if (!objectExists) {
            return Result.failure(EntityNotFoundException("Object not found in scene"))
        }

        return repository.removeObjectFromScene(input.sceneId, input.objectId)
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(sceneId: String, objectId: String): Result<ARScene> {
        return invoke(RemoveObjectInput(sceneId, objectId))
    }
}
