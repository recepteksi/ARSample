package com.trendhive.arsample.domain.model

enum class ModelType {
    GLB,
    GLTF,
    OBJ,
    USDZ;

    companion object {
        fun fromExtension(extension: String): ModelType? {
            return when (extension.lowercase()) {
                "glb" -> GLB
                "gltf" -> GLTF
                "obj" -> OBJ
                "usdz" -> USDZ
                else -> null
            }
        }
    }
}
