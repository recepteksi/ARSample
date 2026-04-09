package com.trendhive.arsample.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific 3D model preview thumbnail component.
 * 
 * Displays a small interactive 3D preview of a GLB/GLTF model.
 * - Android: Uses SceneView for hardware-accelerated 3D rendering with auto-rotation
 * - iOS: Shows a placeholder icon (SceneView not available on iOS)
 * 
 * @param modelPath Path to the 3D model file (supports GLB/GLTF formats)
 * @param modifier Modifier for sizing and layout (recommended: ~80x80dp)
 * @param autoRotate Whether to slowly rotate the model for visual appeal
 */
@Composable
expect fun ModelPreviewThumbnail(
    modelPath: String,
    modifier: Modifier = Modifier,
    autoRotate: Boolean = true
)
