package com.trendhive.arsample.domain.base

/**
 * Base interface for all use cases.
 */
interface BaseUseCase<in Input : BaseModel, out Output : BaseModel> {
    suspend operator fun invoke(input: Input): Result<Output>
}
