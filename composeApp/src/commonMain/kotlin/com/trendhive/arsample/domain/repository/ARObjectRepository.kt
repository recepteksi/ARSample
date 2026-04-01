package com.trendhive.arsample.domain.repository

import com.trendhive.arsample.domain.model.ARObject

interface ARObjectRepository {
    suspend fun getAllObjects(): List<ARObject>
    suspend fun getObjectById(id: String): ARObject?
    suspend fun saveObject(obj: ARObject)
    suspend fun deleteObject(id: String)
    suspend fun importObject(uri: String, name: String, modelType: com.trendhive.arsample.domain.model.ModelType): Result<ARObject>
}
