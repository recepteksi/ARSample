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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ModelPreviewThumbnail"

/**
 * Android implementation of 3D model preview thumbnail using SceneView.
 * 
 * Renders a small 3D view of the model with:
 * - Auto-rotation for visual appeal
 * - Proper lighting setup
 * - Loading state handling
 * - Error fallback to icon
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
    
    // Animation for rotation
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
    
    // Apply rotation to model
    LaunchedEffect(rotationAngle, autoRotate, modelNode) {
        if (autoRotate && modelNode != null) {
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
                // Fallback icon on error
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            isLoading -> {
                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        
        // SceneView for 3D rendering
        if (!hasError) {
            ModelPreviewSceneView(
                modelPath = modelPath,
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

/**
 * Internal SceneView component for rendering the 3D model.
 * Uses a lightweight non-AR setup optimized for thumbnails.
 */
@Composable
private fun ModelPreviewSceneView(
    modelPath: String,
    onModelLoaded: (ModelNode) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    
    // Camera positioned to view the model
    val cameraNode = rememberCameraNode(engine) {
        position = Position(z = 2.0f, y = 0.5f)
        lookAt(Position(0f, 0f, 0f))
    }
    
    // Main light for the scene
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 100_000f
    }
    
    // Model node holder
    var modelNodeState by remember { mutableStateOf<ModelNode?>(null) }
    
    // Load model
    LaunchedEffect(modelPath) {
        try {
            val file = File(modelPath)
            if (!file.exists()) {
                Log.w(TAG, "Model file does not exist: $modelPath")
                onError()
                return@LaunchedEffect
            }
            
            val instance = withContext(Dispatchers.IO) {
                try {
                    modelLoader.createModelInstance(modelPath)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create model instance: $modelPath", e)
                    null
                }
            }
            
            if (instance != null) {
                val node = ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 0.5f
                ).apply {
                    position = Position(0f, 0f, 0f)
                }
                modelNodeState = node
                onModelLoaded(node)
            } else {
                onError()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model: $modelPath", e)
            onError()
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            modelNodeState?.destroy()
        }
    }
    
    // Scene nodes
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
