package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.core.base.BaseValueObject
import com.trendhive.arsample.domain.exception.ValidationException

/**
 * Value Object representing a valid AR object name.
 * Enforces length and character constraints.
 * 
 * Following DDD Value Object pattern:
 * - Validation via factory method (create)
 * - Immutability guaranteed by sealed class
 * - Type safety through Result<T> return
 */
sealed class ObjectName private constructor(value: String) : BaseValueObject<String>(value) {
    
    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 50
        
        /**
         * Factory method to create an ObjectName with validation.
         * Automatically trims whitespace from input.
         * 
         * @param name The object name to validate
         * @return Result.success with ValidObjectName if valid, Result.failure with ValidationException otherwise
         */
        fun create(name: String): Result<ObjectName> {
            val trimmedName = name.trim()
            return when {
                trimmedName.length < MIN_LENGTH -> 
                    Result.failure(ValidationException("Object name cannot be empty"))
                trimmedName.length > MAX_LENGTH -> 
                    Result.failure(ValidationException("Object name must be at most $MAX_LENGTH characters"))
                else -> 
                    Result.success(ValidObjectName(trimmedName))
            }
        }
    }
    
    private class ValidObjectName(value: String) : ObjectName(value)
}
