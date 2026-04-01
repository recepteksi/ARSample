package com.trendhive.arsample.domain.exception

/**
 * Exception thrown when domain validation fails.
 * Used by Value Objects to indicate invalid input.
 */
class ValidationException(message: String) : Exception(message)
