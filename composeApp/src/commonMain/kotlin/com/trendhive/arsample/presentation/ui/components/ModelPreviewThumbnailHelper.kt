package com.trendhive.arsample.presentation.ui.components

/**
 * Pure helper functions for ModelPreviewThumbnail logic.
 *
 * Extracted from platform-specific Composable implementations to enable
 * unit testing of routing and state decisions without Compose or platform SDKs.
 */
object ModelPreviewThumbnailHelper {

    /**
     * Resolves the preview strategy for a given model file path based on its extension.
     *
     * Mirrors the iOS `ModelPreviewThumbnail` routing logic:
     * - "usdz" -> USDZ (SceneKit / SCNView)
     * - "glb", "gltf" -> GLB_THUMBNAIL (placeholder icon; QLThumbnailGenerator not available in Kotlin/Native cinterop)
     * - anything else -> PLACEHOLDER
     *
     * @param modelPath Full path or URI of the 3D model file.
     * @return [PreviewStrategy] enum indicating which renderer should be used.
     */
    fun resolvePreviewStrategy(modelPath: String): PreviewStrategy {
        val extension = modelPath.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "usdz" -> PreviewStrategy.USDZ
            "glb", "gltf" -> PreviewStrategy.GLB_THUMBNAIL
            else -> PreviewStrategy.PLACEHOLDER
        }
    }

    /**
     * Determines the initial Android thumbnail state based on whether the model file exists.
     *
     * Mirrors the Android `ModelPreviewScene` LaunchedEffect file-existence guard:
     * - File missing -> [ThumbnailState.Error]
     * - File present -> [ThumbnailState.Loading] (actual load happens asynchronously)
     *
     * @param fileExists Whether the model file is present on disk.
     * @return The initial [ThumbnailState] for the thumbnail composable.
     */
    fun resolveInitialAndroidState(fileExists: Boolean): ThumbnailState {
        return if (fileExists) ThumbnailState.Loading else ThumbnailState.Error
    }

    /**
     * Determines the Android thumbnail state after a model load attempt.
     *
     * @param instanceLoaded True when `modelLoader.createModelInstance()` returned non-null.
     * @return [ThumbnailState.Loaded] on success, [ThumbnailState.Error] on failure.
     */
    fun resolveAndroidStateAfterLoad(instanceLoaded: Boolean): ThumbnailState {
        return if (instanceLoaded) ThumbnailState.Loaded else ThumbnailState.Error
    }

    /**
     * Extracts the lowercase file extension from a model path.
     *
     * @param modelPath Full path or URI of the 3D model file.
     * @return The lowercase extension string, or an empty string if absent.
     */
    fun extractExtension(modelPath: String): String {
        return modelPath.substringAfterLast('.', "").lowercase()
    }
}

/**
 * Preview strategy variants for the iOS ModelPreviewThumbnail routing.
 */
enum class PreviewStrategy {
    /** USDZ format: rendered via SceneKit SCNView */
    USDZ,

    /** GLB / GLTF format: shows placeholder icon (QLThumbnailGenerator not available in Kotlin/Native cinterop) */
    GLB_THUMBNAIL,

    /** Unknown or unsupported format: shows placeholder icon */
    PLACEHOLDER
}

/**
 * Android thumbnail loading state machine.
 */
enum class ThumbnailState {
    /** Model file exists; async load has started */
    Loading,

    /** Model loaded successfully */
    Loaded,

    /** File missing or load failed */
    Error
}
