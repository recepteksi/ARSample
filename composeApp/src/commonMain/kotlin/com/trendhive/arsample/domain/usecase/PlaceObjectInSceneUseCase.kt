package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.repository.ARSceneRepository
import com.trendhive.arsample.domain.repository.ARObjectRepository

class PlaceObjectInSceneUseCase(
    private val sceneRepository: ARSceneRepository,
    private val objectRepository: ARObjectRepository
) {
    suspend operator fun invoke(
        sceneId: String,
        objectId: String,
        position: Vector3,
        rotation: Quaternion = Quaternion.IDENTITY,
        scale: Float = 1f
    ): Result<ARScene> {
        if (scale <= 0f) {
            return Result.failure(IllegalArgumentException("Scale must be positive"))
        }

        val scene = sceneRepository.getSceneById(sceneId)
            ?: return Result.failure(IllegalArgumentException("Scene not found"))
            
        val arObject = objectRepository.getObjectById(objectId)
            ?: return Result.failure(IllegalArgumentException("Object not found"))

        // objectId: placed obj unique id (for scene)
        // arObjectId: 3D model path (ARObject.modelUri) used by renderer
        val placedObject = PlacedObject(
            objectId = com.trendhive.arsample.domain.model.currentTimeMillis().toString(),
            arObjectId = arObject.modelUri,
            position = position,
            rotation = rotation,
            scale = scale
        )

        return sceneRepository.addObjectToScene(sceneId, placedObject)
    }
}
