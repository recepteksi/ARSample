package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.repository.ARObjectRepository

class GetAllObjectsUseCase(
    private val repository: ARObjectRepository
) {
    suspend operator fun invoke(): List<ARObject> {
        return repository.getAllObjects()
    }
}
