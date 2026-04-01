package com.trendhive.arsample.domain.usecase

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.repository.ARObjectRepository

class GetObjectByIdUseCase(
    private val repository: ARObjectRepository
) {
    suspend operator fun invoke(id: String): ARObject? {
        return repository.getObjectById(id)
    }
}
