package com.trendhive.arsample.data.repository

import com.trendhive.arsample.data.local.ARObjectLocalDataSource
import com.trendhive.arsample.data.local.ModelFileStorage
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.ARObjectRepository
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

class ARObjectRepositoryImpl(
    private val localDataSource: ARObjectLocalDataSource,
    private val fileStorage: ModelFileStorage
) : ARObjectRepository {

    override suspend fun getAllObjects(): List<ARObject> {
        return localDataSource.getAllObjects()
    }

    override suspend fun getObjectById(id: String): ARObject? {
        return localDataSource.getObjectById(id)
    }

    override suspend fun saveObject(obj: ARObject) {
        localDataSource.saveObject(obj)
    }

    override suspend fun deleteObject(id: String) {
        val obj = localDataSource.getObjectById(id)
        obj?.let {
            fileStorage.deleteModel(it.modelUri)
            localDataSource.deleteObject(id)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun importObject(
        uri: String,
        name: String,
        modelType: ModelType
    ): Result<ARObject> {
        return try {
            val modelData = readModelData(uri)
            val fileName = "${Uuid.random()}.${modelType.name.lowercase()}"
            val savedPath = fileStorage.saveModel(modelData, fileName)

            val newObject = ARObject(
                id = Uuid.random().toString(),
                name = name,
                modelUri = savedPath,
                modelType = modelType,
                createdAt = currentTimeMillis()
            )

            localDataSource.saveObject(newObject)
            Result.success(newObject)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun readModelData(uri: String): ByteArray {
        return fileStorage.readFromUri(uri)
    }
}