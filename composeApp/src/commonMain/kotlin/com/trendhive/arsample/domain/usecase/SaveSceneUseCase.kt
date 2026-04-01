package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.repository.ARSceneRepository

class SaveSceneUseCase(
    private val repository: ARSceneRepository
) {
    suspend operator fun invoke(scene: ARScene): Result<ARScene> {
        return try {
            repository.saveScene(scene)
            Result.success(scene)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
