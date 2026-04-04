package com.trendhive.arsample.application.base

import com.trendhive.arsample.domain.base.BaseModel

/**
 * Base interface for all use cases.
 */
interface BaseUseCase<in Input : BaseModel, out Output : BaseModel> {
    suspend operator fun invoke(input: Input): Result<Output>
}
