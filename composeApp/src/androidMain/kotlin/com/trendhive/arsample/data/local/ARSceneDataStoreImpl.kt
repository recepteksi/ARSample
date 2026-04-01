package com.trendhive.arsample.data.local

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ARSceneDataStoreImpl(
    private val cacheDir: File
) : ARSceneDataStore {

    private val scenesFile: File
        get() = File(cacheDir, "ar_scenes.json")

    private var cachedScenes: MutableList<ARSceneCacheEntry>? = null

    private data class ARSceneCacheEntry(
        val id: String,
        val name: String,
        val objects: List<PlacedObjectCache>,
        val createdAt: Long
    )

    private data class PlacedObjectCache(
        val objectId: String,
        val arObjectId: String,
        val posX: Float, val posY: Float, val posZ: Float,
        val rotX: Float, val rotY: Float, val rotZ: Float, val rotW: Float,
        val scale: Float
    )

    private suspend fun loadScenes(): MutableList<ARSceneCacheEntry> = withContext(Dispatchers.IO) {
        cachedScenes?.let { return@withContext it }

        if (!scenesFile.exists()) {
            val empty = mutableListOf<ARSceneCacheEntry>()
            cachedScenes = empty
            return@withContext empty
        }

        try {
            val json = scenesFile.readText()
            val type = object : com.google.gson.reflect.TypeToken<List<ARSceneCacheEntry>>() {}.type
            val list: List<ARSceneCacheEntry> = com.google.gson.Gson().fromJson(json, type)
            cachedScenes = list.toMutableList()
        } catch (e: Exception) {
            cachedScenes = mutableListOf()
        }
        cachedScenes!!
    }

    private suspend fun saveScenes(scenes: List<ARSceneCacheEntry>) = withContext(Dispatchers.IO) {
        val json = com.google.gson.Gson().toJson(scenes)
        scenesFile.writeText(json)
        cachedScenes = scenes.toMutableList()
    }

    override suspend fun saveScene(scene: ARScene) {
        val scenes = loadScenes()
        val entry = ARSceneCacheEntry(
            id = scene.id,
            name = scene.name,
            objects = scene.objects.map { obj ->
                PlacedObjectCache(
                    objectId = obj.objectId,
                    arObjectId = obj.arObjectId,
                    posX = obj.position.x, posY = obj.position.y, posZ = obj.position.z,
                    rotX = obj.rotation.x, rotY = obj.rotation.y, rotZ = obj.rotation.z, rotW = obj.rotation.w,
                    scale = obj.scale
                )
            },
            createdAt = scene.createdAt
        )

        val existingIndex = scenes.indexOfFirst { it.id == scene.id }
        if (existingIndex >= 0) {
            scenes[existingIndex] = entry
        } else {
            scenes.add(entry)
        }
        saveScenes(scenes)
    }

    override suspend fun getScene(sceneId: String): ARScene? {
        return loadScenes().find { it.id == sceneId }?.let { entry ->
            ARScene(
                id = entry.id,
                name = entry.name,
                objects = entry.objects.map { obj ->
                    PlacedObject(
                        objectId = obj.objectId,
                        arObjectId = obj.arObjectId,
                        position = com.trendhive.arsample.domain.model.Vector3(obj.posX, obj.posY, obj.posZ),
                        rotation = com.trendhive.arsample.domain.model.Quaternion(obj.rotX, obj.rotY, obj.rotZ, obj.rotW),
                        scale = obj.scale
                    )
                },
                createdAt = entry.createdAt
            )
        }
    }

    override suspend fun getAllScenes(): List<ARScene> {
        return loadScenes().map { entry ->
            ARScene(
                id = entry.id,
                name = entry.name,
                objects = entry.objects.map { obj ->
                    PlacedObject(
                        objectId = obj.objectId,
                        arObjectId = obj.arObjectId,
                        position = com.trendhive.arsample.domain.model.Vector3(obj.posX, obj.posY, obj.posZ),
                        rotation = com.trendhive.arsample.domain.model.Quaternion(obj.rotX, obj.rotY, obj.rotZ, obj.rotW),
                        scale = obj.scale
                    )
                },
                createdAt = entry.createdAt
            )
        }
    }

    override suspend fun deleteScene(sceneId: String) {
        val scenes = loadScenes()
        scenes.removeAll { it.id == sceneId }
        saveScenes(scenes)
    }

    override suspend fun clearAllScenes() {
        saveScenes(emptyList())
    }
}
