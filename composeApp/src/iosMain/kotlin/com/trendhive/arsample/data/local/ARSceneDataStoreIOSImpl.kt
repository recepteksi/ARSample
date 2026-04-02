package com.trendhive.arsample.data.local

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class ARSceneDataStoreIOSImpl : ARSceneDataStore {

    private val fileManager = NSFileManager.defaultManager

    private val documentsDir: String
        get() {
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            )
            return paths.firstOrNull() as? String ?: throw Exception("Documents directory not found")
        }

    private val scenesFile: String
        get() = "$documentsDir/ar_scenes.json"

    private var cachedScenes: MutableList<ARSceneCacheEntry>? = null

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Serializable
    private data class ARSceneCacheEntry(
        val id: String,
        val name: String,
        val objects: List<PlacedObjectCache>,
        val createdAt: Long
    )

    @Serializable
    private data class PlacedObjectCache(
        val objectId: String,
        val arObjectId: String,
        val posX: Float,
        val posY: Float,
        val posZ: Float,
        val rotX: Float,
        val rotY: Float,
        val rotZ: Float,
        val rotW: Float,
        val scale: Float
    )

    private suspend fun loadScenes(): MutableList<ARSceneCacheEntry> = withContext(Dispatchers.Default) {
        cachedScenes?.let { return@withContext it }

        // Check if file exists
        if (!fileManager.fileExistsAtPath(scenesFile)) {
            val empty = mutableListOf<ARSceneCacheEntry>()
            cachedScenes = empty
            return@withContext empty
        }

        try {
            // Read file using NSString
            val nsString = NSString.stringWithContentsOfFile(
                path = scenesFile,
                encoding = NSUTF8StringEncoding,
                error = null
            ) ?: run {
                cachedScenes = mutableListOf()
                return@withContext mutableListOf()
            }

            val jsonText = nsString as String
            val list = json.decodeFromString<List<ARSceneCacheEntry>>(jsonText)
            cachedScenes = list.toMutableList()
        } catch (e: Exception) {
            println("ARSceneDataStoreIOSImpl: Error loading scenes: ${e.message}")
            cachedScenes = mutableListOf()
        }

        cachedScenes!!
    }

    private suspend fun saveScenes(scenes: List<ARSceneCacheEntry>) = withContext(Dispatchers.Default) {
        try {
            // Serialize to JSON
            val jsonText = json.encodeToString(scenes)

            // Convert to NSString and write to file
            val nsString = jsonText as NSString
            val success = nsString.writeToFile(
                path = scenesFile,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )

            if (!success) {
                throw Exception("Failed to write scenes file")
            }

            cachedScenes = scenes.toMutableList()
        } catch (e: Exception) {
            println("ARSceneDataStoreIOSImpl: Error saving scenes: ${e.message}")
            throw e
        }
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
                    posX = obj.position.x,
                    posY = obj.position.y,
                    posZ = obj.position.z,
                    rotX = obj.rotation.x,
                    rotY = obj.rotation.y,
                    rotZ = obj.rotation.z,
                    rotW = obj.rotation.w,
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
        return loadScenes().find { it.id == sceneId }?.toARScene()
    }

    override suspend fun getAllScenes(): List<ARScene> {
        return loadScenes().map { it.toARScene() }
    }

    override suspend fun deleteScene(sceneId: String) {
        val scenes = loadScenes()
        scenes.removeAll { it.id == sceneId }
        saveScenes(scenes)
    }

    override suspend fun clearAllScenes() {
        saveScenes(emptyList())
    }

    private fun ARSceneCacheEntry.toARScene(): ARScene {
        return ARScene(
            id = id,
            name = name,
            objects = objects.map { obj ->
                PlacedObject(
                    objectId = obj.objectId,
                    arObjectId = obj.arObjectId,
                    position = Vector3(obj.posX, obj.posY, obj.posZ),
                    rotation = Quaternion(obj.rotX, obj.rotY, obj.rotZ, obj.rotW),
                    scale = obj.scale
                )
            },
            createdAt = createdAt
        )
    }
}