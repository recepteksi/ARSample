package com.trendhive.arsample.data.local

import com.trendhive.arsample.data.mapper.ARObjectMapper
import com.trendhive.arsample.data.mapper.ARObjectEntity
import com.trendhive.arsample.domain.model.ARObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ARObjectLocalDataSourceImpl(
    private val cacheDir: File
) : ARObjectLocalDataSource {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val objectsFile: File
        get() = File(cacheDir, "ar_objects.json")

    private var cachedObjects: MutableList<ARObjectEntity>? = null

    private suspend fun loadObjects(): MutableList<ARObjectEntity> = withContext(Dispatchers.IO) {
        cachedObjects?.let { return@withContext it }

        if (!objectsFile.exists()) {
            val empty = mutableListOf<ARObjectEntity>()
            cachedObjects = empty
            return@withContext empty
        }

        try {
            val jsonString = objectsFile.readText()
            val list: List<ARObjectEntity> = json.decodeFromString(jsonString)
            cachedObjects = list.toMutableList()
        } catch (e: Exception) {
            println("Error loading objects: ${e.message}")
            cachedObjects = mutableListOf()
        }
        cachedObjects!!
    }

    private suspend fun saveObjects(objects: List<ARObjectEntity>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(objects)
            objectsFile.writeText(jsonString)
            cachedObjects = objects.toMutableList()
        } catch (e: Exception) {
            println("Error saving objects: ${e.message}")
            throw e
        }
    }

    override suspend fun getAllObjects(): List<ARObject> {
        return loadObjects().map { ARObjectMapper.toDomain(it) }
    }

    override suspend fun getObjectById(id: String): ARObject? {
        return loadObjects().find { it.id == id }?.let { ARObjectMapper.toDomain(it) }
    }

    override suspend fun saveObject(obj: ARObject) {
        val objects = loadObjects()
        val existingIndex = objects.indexOfFirst { it.id == obj.id }
        val entity = ARObjectMapper.toEntity(obj)

        if (existingIndex >= 0) {
            objects[existingIndex] = entity
        } else {
            objects.add(entity)
        }
        saveObjects(objects)
    }

    override suspend fun deleteObject(id: String) {
        val objects = loadObjects()
        objects.removeAll { it.id == id }
        saveObjects(objects)
    }
}