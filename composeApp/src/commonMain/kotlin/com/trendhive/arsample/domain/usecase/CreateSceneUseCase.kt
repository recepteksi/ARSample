package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.domain.base.BaseUseCase
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.repository.ARSceneRepository

/**
 * Input model for creating a new scene.
 */
data class CreateSceneInput(
    val name: String
) : BaseModel

/**
 * Use case for creating a new AR scene.
 */
class CreateSceneUseCase(
    private val repository: ARSceneRepository
) : BaseUseCase<CreateSceneInput, ARScene> {
    
    override suspend operator fun invoke(input: CreateSceneInput): Result<ARScene> {
        if (input.name.isBlank()) {
            return Result.failure(ValidationException("Scene name cannot be blank"))
        }

        val scene = ARScene(
            id = "scene_${currentTimeMillis()}",
            name = input.name,
            objects = emptyList(),
            createdAt = currentTimeMillis()
        )

        return try {
            repository.saveScene(scene)
            Result.success(scene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(name: String): Result<ARScene> {
        return invoke(CreateSceneInput(name))
    }
}
