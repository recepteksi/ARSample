package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.repository.ARSceneRepository

class GetSceneUseCase(
    private val repository: ARSceneRepository
) {
    suspend operator fun invoke(sceneId: String): ARScene? {
        return repository.getSceneById(sceneId)
    }

    suspend fun getDefaultScene(): ARScene {
        return repository.getOrCreateDefaultScene()
    }
}
