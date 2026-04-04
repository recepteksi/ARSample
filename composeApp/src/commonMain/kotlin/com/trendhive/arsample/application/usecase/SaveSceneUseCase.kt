package com.trendhive.arsample.application.usecase

import com.trendhive.arsample.domain.base.BaseModel
import com.trendhive.arsample.application.base.BaseUseCase
import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.repository.ARSceneRepository

/**
 * Input wrapper for saving a scene.
 * The scene itself is the input model.
 */
data class SaveSceneInput(
    val scene: ARScene
) : BaseModel

/**
 * Use case for saving an AR scene.
 */
class SaveSceneUseCase(
    private val repository: ARSceneRepository
) : BaseUseCase<SaveSceneInput, ARScene> {
    
    override suspend operator fun invoke(input: SaveSceneInput): Result<ARScene> {
        return try {
            repository.saveScene(input.scene)
            Result.success(input.scene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convenience method for backward compatibility.
     */
    suspend operator fun invoke(scene: ARScene): Result<ARScene> {
        return invoke(SaveSceneInput(scene))
    }
}
