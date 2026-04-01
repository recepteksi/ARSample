package com.trendhive.arsample.domain.model.valueobjects

import com.trendhive.arsample.domain.exception.ValidationException

sealed class ModelUri private constructor(val value: String) {
    
    companion object {
        private val VALID_EXTENSIONS = setOf("glb", "usdz", "fbx", "obj")
        
        fun create(uri: String): Result<ModelUri> {
            return when {
                uri.isBlank() -> {
                    Result.failure(ValidationException("Model URI cannot be blank"))
                }
                !hasValidExtension(uri) -> {
                    Result.failure(ValidationException("Invalid model format. Supported: .glb, .usdz, .fbx, .obj"))
                }
                else -> {
                    Result.success(ValidModelUri(uri))
                }
            }
        }
        
        private fun hasValidExtension(uri: String): Boolean {
            val extension = uri.substringAfterLast('.', "").lowercase()
            return extension in VALID_EXTENSIONS
        }
    }
    
    private class ValidModelUri(value: String) : ModelUri(value)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelUri) return false
        return value == other.value
    }
    
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "ModelUri(value='$value')"
}
