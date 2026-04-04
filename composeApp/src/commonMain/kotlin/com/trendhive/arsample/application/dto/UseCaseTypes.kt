package com.trendhive.arsample.application.dto

import com.trendhive.arsample.domain.base.BaseModel

/**
 * Represents no input for use cases that don't need parameters.
 */
data object NoInput : BaseModel

/**
 * Wrapper for list results from use cases.
 */
data class ListResult<T>(val items: List<T>) : BaseModel

/**
 * Wrapper for optional results from use cases.
 */
data class OptionalResult<T>(val value: T?) : BaseModel

/**
 * Wrapper for Unit results from use cases.
 */
data object UnitResult : BaseModel
