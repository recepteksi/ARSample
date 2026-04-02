package com.trendhive.arsample.ar

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.trendhive.arsample.domain.model.PlacedObject
import platform.UIKit.UITapGestureRecognizer
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
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit = { _, _ -> }
) {
    var arView by remember { mutableStateOf<ARSCNView?>(null) }
    
    val tapHandler = remember {
        TapHandler { gesture ->
            val view = arView ?: run {
                println("$TAG: WARNING - ARView not initialized")
                return@TapHandler
            }
            
            val location = gesture.locationInView(view)
            println("$TAG: Tap detected")
            
            // Use ARKit hitTest API for plane detection
            try {
                val hitResults = view.hitTest(
                    location,
                    ARHitTestResultTypeExistingPlaneUsingExtent
                )
                
                if (hitResults.count().toInt() == 0) {
                    println("$TAG: No plane detected at tap location")
                    return@TapHandler
                }
                
                // Get first valid result
                val firstResult = hitResults.firstOrNull() as? ARHitTestResult
                if (firstResult == null) {
                    println("$TAG: WARNING - Hit test result not valid")
                    return@TapHandler
                }
                
                // Extract world transform from result
                val hitPose = firstResult.worldTransform
                
                // ARKit worldTransform is matrix_float4x4 (simd_float4x4)
                // columns is CPointer<Vector128VarOf<Vector128>>
                // Each column is 4 floats (16 bytes = 128 bits)
                // Translation is in column 3 (index 3)
                var posX = 0f
                var posY = 0f
                var posZ = 0f
                hitPose.useContents {
                    // columns is CPointer<Vector128VarOf<Vector128>>
                    // Reinterpret as float pointer to access individual values
                    // Matrix layout: [col0(4 floats), col1(4 floats), col2(4 floats), col3(4 floats)]
                    // Translation (col3): indices 12, 13, 14
                    val floatPtr = columns.reinterpret<kotlinx.cinterop.FloatVar>()
                    posX = floatPtr[12]
                    posY = floatPtr[13]
                    posZ = floatPtr[14]
                }

                val pathToPlace = modelPathToLoad ?: run {
                    println("$TAG: WARNING - No model path to load")
                    return@TapHandler
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
            
            // Add tap gesture recognizer
            val tapGesture = UITapGestureRecognizer(
                target = tapHandler,
                action = NSSelectorFromString("handleTap:")
            )
            view.addGestureRecognizer(tapGesture)
            println("$TAG: Tap gesture added")
            
            arView = view
            view
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class TapHandler(private val onTap: (UITapGestureRecognizer) -> Unit) : NSObject() {
    @ObjCAction
    fun handleTap(gesture: UITapGestureRecognizer) {
        onTap(gesture)
    }
}
