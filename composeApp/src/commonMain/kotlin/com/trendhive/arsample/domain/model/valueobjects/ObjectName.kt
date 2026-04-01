package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.domain.exception.ValidationException

sealed class ObjectName private constructor(val value: String) {
    
    companion object {
        private const val MAX_LENGTH = 50
        private const val MIN_LENGTH = 1
        
        fun create(name: String): Result<ObjectName> {
            return when {
                name.isBlank() -> {
                    Result.failure(ValidationException("Object name cannot be blank"))
                }
                name.length < MIN_LENGTH -> {
                    Result.failure(ValidationException("Object name too short (min $MIN_LENGTH character)"))
                }
                name.length > MAX_LENGTH -> {
                    Result.failure(ValidationException("Object name too long (max $MAX_LENGTH characters)"))
                }
                else -> {
                    Result.success(ValidObjectName(name.trim()))
                }
            }
        }
    }
    
    private class ValidObjectName(value: String) : ObjectName(value)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjectName) return false
        return value == other.value
    }
    
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "ObjectName(value='$value')"
}
