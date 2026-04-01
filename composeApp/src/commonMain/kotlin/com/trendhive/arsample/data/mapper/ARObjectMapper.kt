package com.trendhive.arsample.data.mapper

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
import kotlinx.serialization.Serializable

@Serializable
data class ARObjectEntity(
    val id: String,
    val name: String,
    val modelUri: String,
    val modelType: String,
    val thumbnailUri: String?,
    val createdAt: Long,
    val lastPlacedAt: Long?
)

@Serializable
data class PlacedObjectEntity(
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

object ARObjectMapper {
    fun toDomain(entity: ARObjectEntity): ARObject {
        return ARObject(
            id = entity.id,
            name = entity.name,
            modelUri = entity.modelUri,
            modelType = ModelType.valueOf(entity.modelType),
            thumbnailUri = entity.thumbnailUri,
            createdAt = entity.createdAt,
            lastPlacedAt = entity.lastPlacedAt
        )
    }

    fun toEntity(domain: ARObject): ARObjectEntity {
        return ARObjectEntity(
            id = domain.id,
            name = domain.name,
            modelUri = domain.modelUri,
            modelType = domain.modelType.name,
            thumbnailUri = domain.thumbnailUri,
            createdAt = domain.createdAt,
            lastPlacedAt = domain.lastPlacedAt
        )
    }
}

object PlacedObjectMapper {
    fun toDomain(entity: PlacedObjectEntity): PlacedObject {
        return PlacedObject(
            objectId = entity.objectId,
            arObjectId = entity.arObjectId,
            position = Vector3(entity.posX, entity.posY, entity.posZ),
            rotation = Quaternion(entity.rotX, entity.rotY, entity.rotZ, entity.rotW),
            scale = entity.scale
        )
    }

    fun toEntity(domain: PlacedObject): PlacedObjectEntity {
        return PlacedObjectEntity(
            objectId = domain.objectId,
            arObjectId = domain.arObjectId,
            posX = domain.position.x,
            posY = domain.position.y,
            posZ = domain.position.z,
            rotX = domain.rotation.x,
            rotY = domain.rotation.y,
            rotZ = domain.rotation.z,
            rotW = domain.rotation.w,
            scale = domain.scale
        )
    }
}