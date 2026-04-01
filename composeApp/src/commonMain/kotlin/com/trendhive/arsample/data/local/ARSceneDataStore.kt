package com.trendhive.arsample.data.local

import com.trendhive.arsample.domain.model.ARScene

interface ARSceneDataStore {
    suspend fun saveScene(scene: ARScene)
    suspend fun getScene(sceneId: String): ARScene?
    suspend fun getAllScenes(): List<ARScene>
    suspend fun deleteScene(sceneId: String)
    suspend fun clearAllScenes()
}