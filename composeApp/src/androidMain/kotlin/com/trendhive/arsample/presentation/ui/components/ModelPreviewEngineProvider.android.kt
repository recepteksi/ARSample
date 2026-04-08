package com.trendhive.arsample.presentation.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import com.google.android.filament.Engine
import io.github.sceneview.loaders.ModelLoader

private const val TAG = "ModelPreviewEngineProvider"

/**
 * Holds the shared Filament [Engine] and [ModelLoader] for all thumbnail composables
 * in a single gallery session.
 *
 * Only valid inside a [ModelPreviewEngineProvider] composable scope.
 */
data class PreviewEngineHolder(
    val engine: Engine,
    val modelLoader: ModelLoader
)

/**
 * CompositionLocal that provides a shared [PreviewEngineHolder] to all
 * [ModelPreviewThumbnail] composables nested under [ModelPreviewEngineProvider].
 *
 * Accessing this outside a provider scope returns null, which causes the thumbnail
 * to fall back to a placeholder icon — this is a safe degradation.
 */
val LocalPreviewEngine = compositionLocalOf<PreviewEngineHolder?> { null }

/**
 * Provides a single shared Filament [Engine] and [ModelLoader] for all
 * [ModelPreviewThumbnail] composables in [content].
 *
 * Root cause of the previous crash:
 *   Each `ModelPreviewScene` composable created its own [Engine] via `rememberEngine()`.
 *   In a LazyVerticalGrid with multiple visible cards the concurrent EGL surface count
 *   exceeded the Android system limit (~16), crashing the process.
 *
 * Fix:
 *   One [Engine] is created here and shared with every thumbnail cell via
 *   [LocalPreviewEngine]. The engine is destroyed when this composable leaves
 *   the composition (i.e., when the user navigates away from the gallery screen).
 */
@Composable
actual fun ModelPreviewEngineProvider(content: @Composable () -> Unit) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val holder = remember(engine, modelLoader) {
        PreviewEngineHolder(engine = engine, modelLoader = modelLoader)
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing shared preview engine")
            // Explicitly destroy Filament resources to prevent GL/EGL memory leaks
            // on repeated gallery open/close cycles.
            modelLoader.destroy()
            engine.destroy()
        }
    }

    CompositionLocalProvider(
        LocalPreviewEngine provides holder,
        content = content
    )
}
