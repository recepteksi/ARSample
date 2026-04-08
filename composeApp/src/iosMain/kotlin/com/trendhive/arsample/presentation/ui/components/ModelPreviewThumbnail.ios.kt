package com.trendhive.arsample.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.QuickLook.*
import platform.SceneKit.*
import platform.UIKit.*
import platform.darwin.*

private const val TAG = "ModelPreviewThumbnail"

/**
 * iOS implementation of 3D model preview thumbnail.
 *
 * Strategy:
 *  - USDZ  -> SCNView (SceneKit) embedded via UIKitView. Auto-rotation via SCNAction.
 *  - GLB/GLTF -> QLThumbnailGenerator (QuickLook) produces async UIImage.
 *               SceneKit's GLB support via ModelIO is fragile in Kotlin/Native cinterop.
 *  - Fallback -> Placeholder icon for unknown formats or load failures.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ModelPreviewThumbnail(
    modelPath: String,
    modifier: Modifier,
    autoRotate: Boolean
) {
    val extension = modelPath.substringAfterLast('.', "").lowercase()
    when (extension) {
        "usdz" -> USDZPreview(modelPath = modelPath, modifier = modifier, autoRotate = autoRotate)
        "glb", "gltf" -> GLBThumbnailPreview(modelPath = modelPath, modifier = modifier)
        else -> PlaceholderPreview(modifier = modifier)
    }
}

// ---------------------------------------------------------------------------
// USDZ -> SCNView (SceneKit)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun USDZPreview(
    modelPath: String,
    modifier: Modifier,
    autoRotate: Boolean
) {
    val sceneRef = remember { mutableStateOf<SCNView?>(null) }

    DisposableEffect(modelPath, autoRotate) {
        sceneRef.value?.let { configureSceneView(it, modelPath, autoRotate) }
        onDispose {
            sceneRef.value?.scene?.rootNode?.removeAllActions()
        }
    }

    UIKitView(
        factory = {
            val scnView = SCNView(frame = CGRectMake(0.0, 0.0, 1.0, 1.0), options = null)
            scnView.backgroundColor = UIColor.clearColor
            scnView.autoenablesDefaultLighting = true
            scnView.antialiasingMode = SCNAntialiasingMode.SCNAntialiasingModeMultisampling4X
            scnView.allowsCameraControl = false
            configureSceneView(scnView, modelPath, autoRotate)
            sceneRef.value = scnView
            scnView
        },
        modifier = modifier.clip(RoundedCornerShape(8.dp))
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun configureSceneView(scnView: SCNView, modelPath: String, autoRotate: Boolean) {
    try {
        val fileURL = NSURL.fileURLWithPath(modelPath)
        val scene = SCNScene.sceneWithURL(fileURL, options = null, error = null)
        if (scene == null) {
            println("$TAG: SCNScene failed to load from $modelPath")
            return
        }

        scnView.scene = scene

        val cameraNode = SCNNode().apply { camera = SCNCamera() }
        cameraNode.position = SCNVector3Make(0f, 0.15f, 0.5f)
        scene.rootNode.addChildNode(cameraNode)
        scnView.pointOfView = cameraNode

        if (autoRotate) {
            val spin = SCNAction.repeatActionForever(
                SCNAction.rotateByX(0.0, 1.5, 0.0, duration = 3.0)
            )
            scene.rootNode.runAction(spin)
        } else {
            scene.rootNode.removeAllActions()
        }
    } catch (e: Exception) {
        println("$TAG: Exception configuring SCNView: ${e.message}")
    }
}

// ---------------------------------------------------------------------------
// GLB / GLTF -> QLThumbnailGenerator (QuickLook, iOS 13+)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun GLBThumbnailPreview(modelPath: String, modifier: Modifier) {
    val imageViewRef = remember { mutableStateOf<UIImageView?>(null) }

    LaunchedEffect(modelPath) {
        generateQLThumbnail(modelPath) { image ->
            val iv = imageViewRef.value ?: return@generateQLThumbnail
            if (image != null) {
                dispatch_async(dispatch_get_main_queue()) {
                    iv.image = image
                    iv.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                    iv.backgroundColor = UIColor.clearColor
                }
            }
        }
    }

    UIKitView(
        factory = {
            val container = UIView(frame = CGRectMake(0.0, 0.0, 1.0, 1.0))
            container.backgroundColor = UIColor.clearColor

            val imageView = UIImageView(frame = CGRectMake(0.0, 0.0, 1.0, 1.0))
            imageView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
            imageView.autoresizingMask =
                UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
            container.addSubview(imageView)
            imageViewRef.value = imageView
            container
        },
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun generateQLThumbnail(filePath: String, onResult: (UIImage?) -> Unit) {
    val fileURL = NSURL.fileURLWithPath(filePath)
    val request = QLThumbnailGenerator.Request(
        fileAt = fileURL,
        size = CGSizeMake(160.0, 160.0),
        scale = UIScreen.mainScreen.scale,
        representationTypes = QLThumbnailGeneratorRequestRepresentationTypeThumbnail
    )
    QLThumbnailGenerator.sharedGenerator.generateRepresentationsForRequest(request) { representation, _, error ->
        if (error != null) {
            println("$TAG: QLThumbnailGenerator error: ${error.localizedDescription}")
            dispatch_async(dispatch_get_main_queue()) { onResult(null) }
            return@generateRepresentationsForRequest
        }
        val image = representation?.uiImage
        dispatch_async(dispatch_get_main_queue()) { onResult(image) }
    }
}

// ---------------------------------------------------------------------------
// Fallback placeholder
// ---------------------------------------------------------------------------

@Composable
private fun PlaceholderPreview(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = ViewInArIconPreview,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}

private val ViewInArIconPreview: ImageVector
    get() = ImageVector.Builder(
        name = "ViewInAr",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 4f); lineTo(3f, 10f); lineTo(5f, 10f); lineTo(5f, 6f)
            lineTo(9f, 6f); lineTo(9f, 4f); close()

            moveTo(15f, 4f); lineTo(15f, 6f); lineTo(19f, 6f); lineTo(19f, 10f)
            lineTo(21f, 10f); lineTo(21f, 4f); close()

            moveTo(3f, 14f); lineTo(3f, 20f); lineTo(9f, 20f); lineTo(9f, 18f)
            lineTo(5f, 18f); lineTo(5f, 14f); close()

            moveTo(15f, 18f); lineTo(15f, 20f); lineTo(21f, 20f); lineTo(21f, 14f)
            lineTo(19f, 14f); lineTo(19f, 18f); close()

            moveTo(12f, 8f); lineTo(8f, 10.5f); lineTo(8f, 15.5f); lineTo(12f, 18f)
            lineTo(16f, 15.5f); lineTo(16f, 10.5f); close()

            moveTo(12f, 9.5f); lineTo(14.5f, 11f); lineTo(14.5f, 14f); lineTo(12f, 15.5f)
            lineTo(9.5f, 14f); lineTo(9.5f, 11f); close()
        }
    }.build()
