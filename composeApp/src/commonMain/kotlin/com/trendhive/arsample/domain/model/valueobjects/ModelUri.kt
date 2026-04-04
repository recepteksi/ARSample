package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.core.base.BaseValueObject
import com.trendhive.arsample.domain.exception.ValidationException

/**
 * Value Object representing a valid 3D model file URI.
 * Supports GLB, USDZ, FBX, OBJ formats.
 * 
 * Following DDD Value Object pattern:
 * - Validation via factory method (create)
 * - Immutability guaranteed by sealed class
 * - Type safety through Result<T> return
 */
sealed class ModelUri private constructor(value: String) : BaseValueObject<String>(value) {
    
    companion object {
        private val VALID_EXTENSIONS = setOf("glb", "usdz", "fbx", "obj")
        
        /**
         * Factory method to create a ModelUri with validation.
         * 
         * @param uri The file URI to validate
         * @return Result.success with ValidModelUri if valid, Result.failure with ValidationException otherwise
         */
        fun create(uri: String): Result<ModelUri> {
            return when {
                uri.isBlank() -> 
                    Result.failure(ValidationException("Model URI cannot be blank"))
                !hasValidExtension(uri) -> 
                    Result.failure(ValidationException(
                        "Invalid model format. Supported: ${VALID_EXTENSIONS.joinToString { ".$it" }}"
                    ))
                else -> 
                    Result.success(ValidModelUri(uri))
            }
        }
        
        private fun hasValidExtension(uri: String): Boolean {
            val extension = uri.substringAfterLast('.', "").lowercase()
            return extension in VALID_EXTENSIONS
        }
    }
    
    private class ValidModelUri(value: String) : ModelUri(value)
}
