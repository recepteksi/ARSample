package com.trendhive.arsample

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
import java.util.UUID

/**
 * Test data builders for creating common test objects
 */
object TestDataBuilders {

    fun createTestARScene(
        id: String = "scene_${System.currentTimeMillis()}",
        name: String = "Test Scene",
        objects: List<PlacedObject> = emptyList(),
        createdAt: Long = System.currentTimeMillis()
    ): ARScene = ARScene(
        id = id,
        name = name,
        objects = objects,
        createdAt = createdAt
    )

    fun createTestARObject(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Model",
        modelUri: String = "/path/to/model.glb",
        modelType: ModelType = ModelType.GLB,
        thumbnailUri: String? = null,
        createdAt: Long = System.currentTimeMillis(),
        lastPlacedAt: Long? = null
    ): ARObject = ARObject(
        id = id,
        name = name,
        modelUri = modelUri,
        modelType = modelType,
        thumbnailUri = thumbnailUri,
        createdAt = createdAt,
        lastPlacedAt = lastPlacedAt
    )

    fun createTestPlacedObject(
        objectId: String = UUID.randomUUID().toString(),
        arObjectId: String = "/path/to/model.glb",
        position: Vector3 = Vector3(0f, 0f, 0f),
        rotation: Quaternion = Quaternion.IDENTITY,
        scale: Float = 1f,
        createdAt: Long = System.currentTimeMillis()
    ): PlacedObject = PlacedObject(
        objectId = objectId,
        arObjectId = arObjectId,
        position = position,
        rotation = rotation,
        scale = scale,
        createdAt = createdAt
    )

    fun createTestVector3(
        x: Float = 0f,
        y: Float = 0f,
        z: Float = 0f
    ): Vector3 = Vector3(x, y, z)

    fun createTestQuaternion(
        x: Float = 0f,
        y: Float = 0f,
        z: Float = 0f,
        w: Float = 1f
    ): Quaternion = Quaternion(x, y, z, w)
}
