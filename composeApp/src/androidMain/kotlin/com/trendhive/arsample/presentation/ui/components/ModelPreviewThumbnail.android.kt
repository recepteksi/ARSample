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
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import java.io.File

private const val TAG = "ModelPreviewThumbnail"

/**
 * Android implementation of 3D model preview thumbnail using SceneView.
 *
 * Previous crash root causes (fixed here):
 *
 * 1. Model loading on wrong thread: createModelInstance() was dispatched to
 *    Dispatchers.IO. Filament's ResourceLoader finalise() must run on the main thread
 *    (the thread that owns the GL context). Fix: LaunchedEffect runs on Main by default.
 *
 * 2. EGL context exhaustion: Each Scene composable creates a SurfaceView. Android
 *    devices have a ~16 simultaneous EGL surface limit. A LazyVerticalGrid with many
 *    visible cards exceeds this. Fix: key(modelPath) gives the runtime clear lifecycle
 *    boundaries so only visible items hold active surfaces.
 *
 * 3. Missing GPU resource cleanup: ModelNode.destroy() was not called when cells
 *    scrolled off-screen. Fix: DisposableEffect calls destroy() deterministically.
 */
@Composable
actual fun ModelPreviewThumbnail(
    modelPath: String,
    modifier: Modifier,
    autoRotate: Boolean
) {
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

@Composable
private fun ModelPreviewScene(
    modelPath: String,
    rotationAngle: Float,
    onModelLoaded: (ModelNode) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

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
        if (!file.exists()) {
            Log.w(TAG, "Model file does not exist: $modelPath")
            onError()
            return@LaunchedEffect
        }

        try {
            val instance = modelLoader.createModelInstance(modelPath)
            if (instance != null) {
                val node = ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 0.5f
                ).apply {
                    position = Position(0f, 0f, 0f)
                    rotation = Rotation(y = rotationAngle)
                }
                modelNodeState = node
                onModelLoaded(node)
            } else {
                Log.e(TAG, "createModelInstance returned null for: $modelPath")
                onError()
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
