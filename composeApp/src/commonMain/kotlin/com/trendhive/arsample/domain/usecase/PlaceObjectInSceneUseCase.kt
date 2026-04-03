package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.domain.base.BaseUseCase
import com.trendhive.arsample.domain.exception.EntityNotFoundException
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.ARSceneRepository
import com.trendhive.arsample.domain.repository.ARObjectRepository

/**
 * Input model for placing an object in a scene.
 */
data class PlaceObjectInput(
    val sceneId: String,
    val objectId: String,
    val position: Vector3,
    val rotation: Quaternion = Quaternion.IDENTITY,
    val scale: Float = 1f
) : BaseModel

/**
 * Use case for placing a 3D object in an AR scene.
 */
class PlaceObjectInSceneUseCase(
    private val sceneRepository: ARSceneRepository,
    private val objectRepository: ARObjectRepository
) : BaseUseCase<PlaceObjectInput, ARScene> {
    
    override suspend operator fun invoke(input: PlaceObjectInput): Result<ARScene> {
        // Validate input
        if (input.sceneId.isBlank()) {
            return Result.failure(ValidationException("Scene ID cannot be blank"))
        }
        if (input.objectId.isBlank()) {
            return Result.failure(ValidationException("Object ID cannot be blank"))
        }
        if (input.scale <= 0f) {
            return Result.failure(ValidationException("Scale must be positive"))
        }

        val scene = sceneRepository.getSceneById(input.sceneId)
            ?: return Result.failure(EntityNotFoundException("Scene not found"))
            
        val arObject = objectRepository.getObjectById(input.objectId)
            ?: return Result.failure(EntityNotFoundException("Object not found"))

        // objectId: placed obj unique id (for scene)
        // arObjectId: 3D model path (ARObject.modelUri) used by renderer
        val placedObject = PlacedObject(
            objectId = currentTimeMillis().toString(),
            arObjectId = arObject.modelUri,
            position = input.position,
            rotation = input.rotation,
            scale = input.scale
        )

        return sceneRepository.addObjectToScene(input.sceneId, placedObject)
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(
        sceneId: String,
        objectId: String,
        position: Vector3,
        rotation: Quaternion = Quaternion.IDENTITY,
        scale: Float = 1f
    ): Result<ARScene> {
        return invoke(PlaceObjectInput(sceneId, objectId, position, rotation, scale))
    }
}
