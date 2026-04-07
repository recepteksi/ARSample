package com.trendhive.arsample.ar

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.trendhive.arsample.domain.model.PlacedObject
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UILongPressGestureRecognizer
import platform.Foundation.NSSelectorFromString
import platform.ARKit.*
import platform.CoreGraphics.CGRectMake
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.useContents
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret

private const val TAG = "ARViewWrapper"

@OptIn(ExperimentalForeignApi::class)
@Composable
fun ARViewWrapper(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float, scale: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit = { _, _ -> },
    captureRequest: Boolean = false,
    onCaptureComplete: ((ByteArray?) -> Unit)? = null
) {
    var arView by remember { mutableStateOf<ARSCNView?>(null) }
    
    val longPressHandler = remember {
        LongPressHandler { recognizer ->
            if (recognizer.state != UIGestureRecognizerStateEnded) return@LongPressHandler

            val view = arView ?: run {
                println("$TAG: WARNING - ARView not initialized")
                return@LongPressHandler
            }

            val location = recognizer.locationInView(view)
            println("$TAG: Long press ended")

            // Use ARKit hitTest API for plane detection
            try {
                val hitResults = view.hitTest(
                    location,
                    ARHitTestResultTypeExistingPlaneUsingExtent
                )

                if (hitResults.count().toInt() == 0) {
                    println("$TAG: No plane detected at long-press location")
                    return@LongPressHandler
                }

                // Get first valid result
                val firstResult = hitResults.firstOrNull() as? ARHitTestResult
                if (firstResult == null) {
                    println("$TAG: WARNING - Hit test result not valid")
                    return@LongPressHandler
                }

                // Extract world transform from result
                val hitPose = firstResult.worldTransform

                // Translation is in column 3 (index 3)
                var posX = 0f
                var posY = 0f
                var posZ = 0f
                hitPose.useContents {
                    val floatPtr = columns.reinterpret<kotlinx.cinterop.FloatVar>()
                    posX = floatPtr[12]
                    posY = floatPtr[13]
                    posZ = floatPtr[14]
                }

                val pathToPlace = modelPathToLoad ?: run {
                    println("$TAG: WARNING - No model path to load")
                    return@LongPressHandler
                }

                val defaultScale = 0.3f // Match Android default scale
                println("$TAG: Placing model at position: x=$posX, y=$posY, z=$posZ, scale=$defaultScale")
                onModelPlaced(pathToPlace, posX, posY, posZ, defaultScale)
            } catch (e: Exception) {
                println("$TAG: ERROR - Hit test failed: ${e.message}")
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try {
                println("$TAG: Cleaning up AR session")
                arView?.session?.pause()
            } catch (e: Exception) {
                println("$TAG: WARNING - Error during cleanup: ${e.message}")
            }
        }
    }
    
    // Handle capture request
    LaunchedEffect(captureRequest) {
        if (captureRequest && onCaptureComplete != null) {
            val view = arView
            if (view == null) {
                onCaptureComplete(null)
                return@LaunchedEffect
            }
            
            try {
                // Capture snapshot of the AR view
                val snapshot = view.snapshot()
                if (snapshot == null) {
                    println("$TAG: Snapshot returned null")
                    onCaptureComplete(null)
                    return@LaunchedEffect
                }
                
                // Convert UIImage to JPEG data
                val jpegData = platform.UIKit.UIImageJPEGRepresentation(snapshot, 0.9)
                if (jpegData == null) {
                    println("$TAG: JPEG conversion returned null")
                    onCaptureComplete(null)
                    return@LaunchedEffect
                }
                
                // Convert NSData to ByteArray
                val bytes = jpegData.bytes
                val length = jpegData.length.toInt()
                if (bytes == null || length == 0) {
                    onCaptureComplete(null)
                    return@LaunchedEffect
                }
                
                val byteArray = ByteArray(length)
                kotlinx.cinterop.memScoped {
                    val ptr = bytes.reinterpret<kotlinx.cinterop.ByteVar>()
                    for (i in 0 until length) {
                        byteArray[i] = ptr[i]
                    }
                }
                
                println("$TAG: Captured photo with ${byteArray.size} bytes")
                onCaptureComplete(byteArray)
            } catch (e: Exception) {
                println("$TAG: ERROR - Capture failed: ${e.message}")
                onCaptureComplete(null)
            }
        }
    }
    
    UIKitView(
        factory = {
            println("$TAG: Creating ARSCNView")
            
            val view = ARSCNView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0))
            
            // Configure AR session
            val config = ARWorldTrackingConfiguration().apply {
                planeDetection = ARPlaneDetectionHorizontal or ARPlaneDetectionVertical
                println("$TAG: AR config: plane detection enabled")
            }
            
            // Start AR session
            view.session.runWithConfiguration(config)
            println("$TAG: AR session started")
            
            // Add long-press gesture recognizer (prevents accidental single-tap placement)
            val longPressGesture = UILongPressGestureRecognizer(
                target = longPressHandler,
                action = NSSelectorFromString("handleLongPress:")
            ).apply {
                minimumPressDuration = 0.5 // 500ms
            }
            view.addGestureRecognizer(longPressGesture)
            println("$TAG: Long press gesture added")
            
            arView = view
            view
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class LongPressHandler(private val onLongPress: (UILongPressGestureRecognizer) -> Unit) : NSObject() {
    @ObjCAction
    fun handleLongPress(gesture: UILongPressGestureRecognizer) {
        onLongPress(gesture)
    }
}
