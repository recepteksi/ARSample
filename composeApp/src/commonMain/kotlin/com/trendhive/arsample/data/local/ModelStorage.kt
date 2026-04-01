package com.trendhive.arsample.data.local

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType

interface ARObjectLocalDataSource {
    suspend fun getAllObjects(): List<ARObject>
    suspend fun getObjectById(id: String): ARObject?
    suspend fun saveObject(obj: ARObject)
    suspend fun deleteObject(id: String)
}

interface ModelFileStorage {
    suspend fun saveModel(data: ByteArray, fileName: String): String
    suspend fun deleteModel(filePath: String): Boolean
    suspend fun getModel(filePath: String): ByteArray?
    fun listModels(): List<String>
    fun getModelsDirectory(): String
    suspend fun readFromUri(uri: String): ByteArray
}