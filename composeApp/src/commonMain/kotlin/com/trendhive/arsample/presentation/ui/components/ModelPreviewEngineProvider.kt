package com.trendhive.arsample.presentation.ui.components

import androidx.compose.runtime.Composable

/**
 * Provides the platform-specific 3D rendering engine for [ModelPreviewThumbnail] composables.
 *
 * Wrap any composable subtree that contains [ModelPreviewThumbnail] calls with this
 * provider so that all thumbnails share a single engine instance instead of each
 * creating their own.
 *
 * Platform behaviour:
 *  - Android: Provides a shared Filament [Engine] + [ModelLoader] via [LocalPreviewEngine].
 *    Without this wrapper every thumbnail creates its own Engine, which exhausts the
 *    Android EGL surface limit and crashes the process.
 *  - iOS: No-op (iOS thumbnails do not use a shared rendering engine).
 */
@Composable
expect fun ModelPreviewEngineProvider(content: @Composable () -> Unit)
