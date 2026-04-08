package com.trendhive.arsample.presentation.ui.components

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberMainLightNode
import java.io.File

private const val TAG = "ModelPreviewThumbnail"

/**
 * Android implementation of 3D model preview thumbnail using SceneView.
 *
 * Crash root causes and fixes:
 *
 * 1. Model loading on wrong thread: createModelInstance() must run on the main thread
 *    (the GL context owner). Fix: LaunchedEffect dispatches on Main by default.
 *
 * 2. EGL context exhaustion (PRIMARY CRASH CAUSE for LazyVerticalGrid):
 *    Each Scene composable owns a SurfaceView with its own EGL surface. Android
 *    enforces a system-wide limit (~16 simultaneous EGL surfaces). A LazyVerticalGrid
 *    with multiple visible cards previously called rememberEngine() per cell, creating
 *    one Filament Engine (and SurfaceView) per thumbnail simultaneously, exceeding
 *    the limit and crashing the process.
 *
 *    Fix: The Filament Engine and ModelLoader are no longer created per-cell.
 *    Instead, they are provided by [ModelPreviewEngineProvider] (a single shared
 *    instance at the gallery screen level via [LocalPreviewEngine]).
 *    If no provider is found in the composition tree the thumbnail falls back to a
 *    static placeholder icon — a safe degradation that never crashes.
 *
 * 3. Missing GPU resource cleanup: ModelNode.destroy() is called deterministically
 *    in DisposableEffect when a cell scrolls off screen.
 */
@Composable
actual fun ModelPreviewThumbnail(
    modelPath: String,
    modifier: Modifier,
    autoRotate: Boolean
) {
    // Obtain the shared engine from the nearest ModelPreviewEngineProvider ancestor.
    // If no provider is present in the tree, engineHolder is null and we show the
    // placeholder icon instead of attempting to create a per-cell Engine (which crashed).
    val engineHolder = LocalPreviewEngine.current

    if (engineHolder == null) {
        // Safe fallback: no engine provider wrapping this composable.
        // Render a static icon instead of crashing.
        Log.w(TAG, "No ModelPreviewEngineProvider found — showing placeholder for $modelPath")
        ThumbnailPlaceholder(modifier = modifier)
        return
    }

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "modelRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(rotationAngle, autoRotate, modelNode) {
        if (autoRotate) {
            modelNode?.rotation = Rotation(y = rotationAngle)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            hasError -> {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        if (!hasError) {
            key(modelPath) {
                ModelPreviewScene(
                    modelPath = modelPath,
                    engineHolder = engineHolder,
                    rotationAngle = if (autoRotate) rotationAngle else 0f,
                    onModelLoaded = { node ->
                        modelNode = node
                        isLoading = false
                    },
                    onError = {
                        hasError = true
                        isLoading = false
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Static icon placeholder shown when no [ModelPreviewEngineProvider] is found
 * in the composition tree, or when the model fails to load.
 */
@Composable
private fun ThumbnailPlaceholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Internal composable that renders a single 3D model inside a [Scene].
 *
 * Consumes the shared [PreviewEngineHolder] provided by [ModelPreviewEngineProvider]
 * instead of creating its own [Engine]. This is the critical change that prevents
 * EGL context exhaustion in a LazyVerticalGrid.
 */
@Composable
private fun ModelPreviewScene(
    modelPath: String,
    engineHolder: PreviewEngineHolder,
    rotationAngle: Float,
    onModelLoaded: (ModelNode) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use the SHARED engine and modelLoader — not per-cell instances.
    val engine = engineHolder.engine
    val modelLoader = engineHolder.modelLoader

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0.4f, z = 2.0f)
        lookAt(Position(0f, 0f, 0f))
    }

    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 80_000f
    }

    var modelNodeState by remember { mutableStateOf<ModelNode?>(null) }

    LaunchedEffect(modelPath) {
        val file = File(modelPath)
        val initialState = ModelPreviewThumbnailHelper.resolveInitialAndroidState(file.exists())
        if (initialState == ThumbnailState.Error) {
            Log.w(TAG, "Model file does not exist: $modelPath")
            onError()
            return@LaunchedEffect
        }

        try {
            val instance = modelLoader.createModelInstance(modelPath)
            val finalState = ModelPreviewThumbnailHelper.resolveAndroidStateAfterLoad(instance != null)
            if (finalState == ThumbnailState.Error || instance == null) {
                Log.e(TAG, "createModelInstance returned null for: $modelPath")
                onError()
            } else {
                val node = ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 0.5f
                ).apply {
                    position = Position(0f, 0f, 0f)
                    rotation = Rotation(y = rotationAngle)
                }
                modelNodeState = node
                onModelLoaded(node)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model: $modelPath", e)
            onError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            modelNodeState?.destroy()
            modelNodeState = null
        }
    }

    val childNodes = remember(modelNodeState, mainLightNode) {
        listOfNotNull(modelNodeState, mainLightNode)
    }

    Scene(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        childNodes = childNodes
    )
}
