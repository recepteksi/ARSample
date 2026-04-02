package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.repository.ARSceneRepository

/**
 * Use case for moving a placed object to a new position in an AR scene.
 * Used when user drags an object to reposition it.
 */
class MoveObjectUseCase(
    private val sceneRepository: ARSceneRepository
) {
    /**
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
        // Validate sceneId
        if (sceneId.isBlank()) {
            return Result.failure(IllegalArgumentException("Scene ID cannot be blank"))
        }

        // Validate objectId
        if (objectId.isBlank()) {
            return Result.failure(IllegalArgumentException("Object ID cannot be blank"))
        }

        // Validate position (check for NaN values)
        if (newPosition.x.isNaN() || newPosition.y.isNaN() || newPosition.z.isNaN()) {
            return Result.failure(IllegalArgumentException("Position contains invalid NaN values"))
        }

        // Get the scene
        val scene = sceneRepository.getSceneById(sceneId)
            ?: return Result.failure(IllegalArgumentException("Scene not found"))

        // Find the object in the scene
        val existingObject = scene.objects.find { it.objectId == objectId }
            ?: return Result.failure(IllegalArgumentException("Object not found in scene"))

        // Create updated object with new position
        val updatedObject = existingObject.copy(position = newPosition)

        // Update the object in the scene
        return sceneRepository.updateObjectInScene(sceneId, updatedObject)
    }
}
