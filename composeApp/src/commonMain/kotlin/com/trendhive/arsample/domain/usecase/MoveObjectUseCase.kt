package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.domain.base.BaseUseCase
import com.trendhive.arsample.domain.exception.EntityNotFoundException
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.repository.ARSceneRepository

/**
 * Input model for moving an object in a scene.
 */
data class MoveObjectInput(
    val sceneId: String,
    val objectId: String,
    val newPosition: Vector3
) : BaseModel

/**
 * Use case for moving a placed object to a new position in an AR scene.
 * Used when user drags an object to reposition it.
 */
class MoveObjectUseCase(
    private val sceneRepository: ARSceneRepository
) : BaseUseCase<MoveObjectInput, ARScene> {
    
    override suspend operator fun invoke(input: MoveObjectInput): Result<ARScene> {
        // Validate sceneId
        if (input.sceneId.isBlank()) {
            return Result.failure(ValidationException("Scene ID cannot be blank"))
        }

        // Validate objectId
        if (input.objectId.isBlank()) {
            return Result.failure(ValidationException("Object ID cannot be blank"))
        }

        // Validate position (check for NaN values)
        if (input.newPosition.x.isNaN() || input.newPosition.y.isNaN() || input.newPosition.z.isNaN()) {
            return Result.failure(ValidationException("Position contains invalid NaN values"))
        }

        // Get the scene
        val scene = sceneRepository.getSceneById(input.sceneId)
            ?: return Result.failure(EntityNotFoundException("Scene not found"))

        // Find the object in the scene
        val existingObject = scene.objects.find { it.objectId == input.objectId }
            ?: return Result.failure(EntityNotFoundException("Object not found in scene"))

        // Create updated object with new position
        val updatedObject = existingObject.copy(position = input.newPosition)

        // Update the object in the scene
        return sceneRepository.updateObjectInScene(input.sceneId, updatedObject)
    }
    
    /**
     * Convenience method for backward compatibility.
     * Moves an object to a new position in the scene.
     *
     * @param sceneId The ID of the scene containing the object
     * @param objectId The ID of the placed object to move
     * @param newPosition The new position for the object
     * @return Result containing the updated ARScene or an error
     */
    suspend operator fun invoke(
        sceneId: String,
        objectId: String,
        newPosition: Vector3
    ): Result<ARScene> {
        return invoke(MoveObjectInput(sceneId, objectId, newPosition))
    }
}
