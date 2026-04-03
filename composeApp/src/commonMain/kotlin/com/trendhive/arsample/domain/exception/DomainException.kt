package com.trendhive.arsample.domain.exception

/**
 * Base sealed class for all domain exceptions.
 * Provides type-safe error handling in the domain layer.
 */
sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when domain validation fails.
 * Used by Value Objects to indicate invalid input.
 */
class ValidationException(message: String) : DomainException(message)

/**
 * Thrown when a requested entity is not found.
 */
class EntityNotFoundException : DomainException {
    constructor(message: String) : super(message)
    constructor(entityType: String, id: String) : super("$entityType with id '$id' not found")
}

/**
 * Thrown when storage operations fail.
 */
class StorageException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)
