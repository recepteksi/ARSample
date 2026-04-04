package com.trendhive.arsample.core.base

/**
 * Base class for DDD Value Objects.
 * Value Objects are immutable and defined by their attributes.
 * 
 * Following DDD principles:
 * - Immutability: Value Objects cannot be modified after creation
 * - Equality by value: Two Value Objects are equal if their values are equal
 * - Self-validation: Validation happens at creation time via factory methods
 * 
 * @param T The type of the underlying value
 * @property value The immutable value wrapped by this Value Object
 */
abstract class BaseValueObject<T> protected constructor(val value: T) {
    
    /**
     * Value Objects are equal if their values are equal.
     * Uses class type check to ensure type safety.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as BaseValueObject<*>
        return value == other.value
    }
    
    /**
     * Hash code based on the underlying value.
     */
    override fun hashCode(): Int = value?.hashCode() ?: 0
    
    /**
     * String representation showing the class name and value.
     */
    override fun toString(): String = "${this::class.simpleName}(value=$value)"
}
