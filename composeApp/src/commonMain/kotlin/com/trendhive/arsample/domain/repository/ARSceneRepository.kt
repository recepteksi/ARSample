package com.trendhive.arsample.domain.repository

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject

interface ARSceneRepository {
    suspend fun getAllScenes(): List<ARScene>
    suspend fun getSceneById(id: String): ARScene?
    suspend fun saveScene(scene: ARScene)
    suspend fun deleteScene(id: String)
    suspend fun getOrCreateDefaultScene(): ARScene
    suspend fun addObjectToScene(sceneId: String, placedObject: PlacedObject): Result<ARScene>
    suspend fun removeObjectFromScene(sceneId: String, objectId: String): Result<ARScene>
    suspend fun updateObjectInScene(sceneId: String, placedObject: PlacedObject): Result<ARScene>
}
