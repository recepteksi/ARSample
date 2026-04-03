package com.trendhive.arsample.data.mapper

import com.trendhive.arsample.data.dto.ARObjectDTO
import com.trendhive.arsample.data.dto.PlacedObjectDTO
import com.trendhive.arsample.domain.base.BaseMapper
import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3

/**
 * Mapper for ARObject ↔ ARObjectDTO transformations.
 */
class ARObjectMapper : BaseMapper<ARObjectDTO, ARObject>() {
    
    override fun toDTO(model: ARObject): ARObjectDTO {
        return ARObjectDTO(
            id = model.id,
            name = model.name,
            modelUri = model.modelUri,
            modelType = model.modelType.name,
            thumbnailUri = model.thumbnailUri,
            createdAt = model.createdAt,
            lastPlacedAt = model.lastPlacedAt
        )
    }

    override fun toModel(dto: ARObjectDTO): ARObject {
        return ARObject(
            id = dto.id,
            name = dto.name,
            modelUri = dto.modelUri,
            modelType = ModelType.valueOf(dto.modelType),
            thumbnailUri = dto.thumbnailUri,
            createdAt = dto.createdAt,
            lastPlacedAt = dto.lastPlacedAt
        )
    }
    
    companion object {
        private val instance = ARObjectMapper()
        
        /**
         * Legacy static methods for backward compatibility.
         * Prefer using instance methods with dependency injection.
         */
        fun toDomain(dto: ARObjectDTO): ARObject = instance.toModel(dto)
        fun toEntity(model: ARObject): ARObjectDTO = instance.toDTO(model)
    }
}

/**
 * Mapper for PlacedObject ↔ PlacedObjectDTO transformations.
 */
class PlacedObjectMapper : BaseMapper<PlacedObjectDTO, PlacedObject>() {
    
    override fun toDTO(model: PlacedObject): PlacedObjectDTO {
        return PlacedObjectDTO(
            objectId = model.objectId,
            arObjectId = model.arObjectId,
            posX = model.position.x,
            posY = model.position.y,
            posZ = model.position.z,
            rotX = model.rotation.x,
            rotY = model.rotation.y,
            rotZ = model.rotation.z,
            rotW = model.rotation.w,
            scale = model.scale
        )
    }

    override fun toModel(dto: PlacedObjectDTO): PlacedObject {
        return PlacedObject(
            objectId = dto.objectId,
            arObjectId = dto.arObjectId,
            position = Vector3(dto.posX, dto.posY, dto.posZ),
            rotation = Quaternion(dto.rotX, dto.rotY, dto.rotZ, dto.rotW),
            scale = dto.scale
        )
    }
    
    companion object {
        private val instance = PlacedObjectMapper()
        
        /**
         * Legacy static methods for backward compatibility.
         * Prefer using instance methods with dependency injection.
         */
        fun toDomain(dto: PlacedObjectDTO): PlacedObject = instance.toModel(dto)
        fun toEntity(model: PlacedObject): PlacedObjectDTO = instance.toDTO(model)
    }
}