package com.trendhive.arsample.infrastructure.persistence.repository

import com.trendhive.arsample.infrastructure.persistence.local.ARSceneDataStore
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.repository.ARSceneRepository

class ARSceneRepositoryImpl(
    private val sceneDataStore: ARSceneDataStore
) : ARSceneRepository {

    private var defaultSceneId: String? = null

    override suspend fun getAllScenes(): List<ARScene> {
        return sceneDataStore.getAllScenes()
    }

    override suspend fun getSceneById(id: String): ARScene? {
        return sceneDataStore.getScene(id)
    }

    override suspend fun saveScene(scene: ARScene) {
        sceneDataStore.saveScene(scene)
    }

    override suspend fun deleteScene(id: String) {
        sceneDataStore.deleteScene(id)
        if (defaultSceneId == id) {
            defaultSceneId = null
        }
    }

    override suspend fun getOrCreateDefaultScene(): ARScene {
        defaultSceneId?.let { id ->
            sceneDataStore.getScene(id)?.let { return it }
        }

        val newScene = ARScene.createDefault()
        sceneDataStore.saveScene(newScene)
        defaultSceneId = newScene.id
        return newScene
    }

    override suspend fun addObjectToScene(sceneId: String, placedObject: PlacedObject): Result<ARScene> {
        return try {
            val scene = sceneDataStore.getScene(sceneId)
                ?: return Result.failure(IllegalArgumentException("Scene not found"))

            val updatedScene = scene.copy(objects = scene.objects + placedObject)
            sceneDataStore.saveScene(updatedScene)
            Result.success(updatedScene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeObjectFromScene(sceneId: String, objectId: String): Result<ARScene> {
        return try {
            val scene = sceneDataStore.getScene(sceneId)
                ?: return Result.failure(IllegalArgumentException("Scene not found"))

            val updatedScene = scene.copy(objects = scene.objects.filter { it.objectId != objectId })
            sceneDataStore.saveScene(updatedScene)
            Result.success(updatedScene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateObjectInScene(sceneId: String, placedObject: PlacedObject): Result<ARScene> {
        return try {
            val scene = sceneDataStore.getScene(sceneId)
                ?: return Result.failure(IllegalArgumentException("Scene not found"))

            val updatedScene = scene.copy(objects = scene.objects.map { if (it.objectId == placedObject.objectId) placedObject else it })
            sceneDataStore.saveScene(updatedScene)
            Result.success(updatedScene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}