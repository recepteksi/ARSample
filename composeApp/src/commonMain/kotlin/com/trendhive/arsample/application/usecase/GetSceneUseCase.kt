package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.domain.exception.ValidationException
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.application.dto.OptionalResult
import com.trendhive.arsample.domain.repository.ARSceneRepository

/**
 * Input model for getting a scene by ID.
 */
data class GetSceneInput(
    val sceneId: String
) : BaseModel

/**
 * Use case for retrieving an AR scene.
 */
class GetSceneUseCase(
    private val repository: ARSceneRepository
) : BaseUseCase<GetSceneInput, OptionalResult<ARScene>> {
    
    override suspend operator fun invoke(input: GetSceneInput): Result<OptionalResult<ARScene>> {
        if (input.sceneId.isBlank()) {
            return Result.failure(ValidationException("Scene ID cannot be blank"))
        }
        
        return try {
            val scene = repository.getSceneById(input.sceneId)
            Result.success(OptionalResult(scene))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(sceneId: String): ARScene? {
        return repository.getSceneById(sceneId)
    }

    /**
     * Gets or creates the default scene.
     */
    suspend fun getDefaultScene(): ARScene {
        return repository.getOrCreateDefaultScene()
    }
}
