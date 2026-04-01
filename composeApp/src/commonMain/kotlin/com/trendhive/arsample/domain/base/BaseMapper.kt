package com.trendhive.arsample.domain.base

/**
 * Base abstract class for mappers (DTO ↔ Model).
 */
abstract class BaseMapper<DTO, Model : BaseModel> {
    abstract fun toDTO(model: Model): DTO
    abstract fun toModel(dto: DTO): Model
    
    open fun updateModel(existingModel: Model, dto: DTO): Model {
        return toModel(dto)
    }
}
