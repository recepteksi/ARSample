package com.trendhive.arsample.presentation.ui.components

import androidx.compose.runtime.Composable

/**
 * iOS actual implementation of [ModelPreviewEngineProvider].
 *
 * iOS thumbnails use SceneKit SCNView or QLThumbnailGenerator — neither requires a
 * shared Filament Engine. This is a no-op pass-through.
 */
@Composable
actual fun ModelPreviewEngineProvider(content: @Composable () -> Unit) {
    content()
}
