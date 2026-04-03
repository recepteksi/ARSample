package com.trendhive.arsample.data.mapper

import com.trendhive.arsample.data.dto.ARSceneDTO
import com.trendhive.arsample.data.dto.PlacedObjectDTO
import com.trendhive.arsample.domain.base.BaseMapper
import com.trendhive.arsample.domain.model.ARScene

/**
 * Mapper for ARScene ↔ ARSceneDTO transformations.
 */
class ARSceneMapper(
    private val placedObjectMapper: PlacedObjectMapper = PlacedObjectMapper()
) : BaseMapper<ARSceneDTO, ARScene>() {
    
    override fun toDTO(model: ARScene): ARSceneDTO {
        return ARSceneDTO(
            id = model.id,
            name = model.name,
            objects = model.objects.map { placedObjectMapper.toDTO(it) },
            createdAt = model.createdAt
        )
    }
    
    override fun toModel(dto: ARSceneDTO): ARScene {
        return ARScene(
            id = dto.id,
            name = dto.name,
            objects = dto.objects.map { placedObjectMapper.toModel(it) },
            createdAt = dto.createdAt
        )
    }
    
    companion object {
        private val instance = ARSceneMapper()
        
        /**
         * Legacy static methods for backward compatibility.
         * Prefer using instance methods with dependency injection.
         */
        fun toDomain(dto: ARSceneDTO): ARScene = instance.toModel(dto)
        fun toEntity(model: ARScene): ARSceneDTO = instance.toDTO(model)
    }
}
