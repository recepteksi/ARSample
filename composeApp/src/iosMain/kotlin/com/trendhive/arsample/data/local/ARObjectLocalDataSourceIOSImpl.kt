package com.trendhive.arsample.data.local

import com.trendhive.arsample.data.mapper.ARObjectMapper
import com.trendhive.arsample.data.mapper.ARObjectEntity
import com.trendhive.arsample.domain.model.ARObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
class ARObjectLocalDataSourceIOSImpl : ARObjectLocalDataSource {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
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

    private val objectsFile: String
        get() = "$documentsDir/ar_objects.json"

    private var cachedObjects: MutableList<ARObjectEntity>? = null

    private suspend fun loadObjects(): MutableList<ARObjectEntity> = withContext(Dispatchers.Default) {
        cachedObjects?.let { return@withContext it }

        // Check if file exists
        if (!fileManager.fileExistsAtPath(objectsFile)) {
            val empty = mutableListOf<ARObjectEntity>()
            cachedObjects = empty
            return@withContext empty
        }

        try {
            // Read file content as NSString
            val jsonString = NSString.stringWithContentsOfFile(
                objectsFile,
                encoding = NSUTF8StringEncoding,
                error = null
            ) as? String
            
            if (jsonString == null) {
                cachedObjects = mutableListOf()
                return@withContext mutableListOf<ARObjectEntity>()
            }
            
            // Parse JSON
            val list: List<ARObjectEntity> = json.decodeFromString(jsonString)
            cachedObjects = list.toMutableList()
        } catch (e: Exception) {
            println("Error loading objects: ${e.message}")
            cachedObjects = mutableListOf()
        }
        
        cachedObjects!!
    }

    private suspend fun saveObjects(objects: List<ARObjectEntity>) = withContext(Dispatchers.Default) {
        try {
            // Encode to JSON
            val jsonString = json.encodeToString(objects)
            
            // Write to file
            val nsString = jsonString as NSString
            val success = nsString.writeToFile(
                objectsFile,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            
            if (!success) {
                throw Exception("Failed to write file: $objectsFile")
            }
            
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