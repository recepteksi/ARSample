package com.trendhive.arsample.ar

import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.trendhive.arsample.domain.model.PlacedObject
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.math.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ARView"

// Hit test configuration constants
private const val HIT_TEST_MIN_DISTANCE = 0.1f // Minimum distance in meters
private const val HIT_TEST_MAX_DISTANCE = 10.0f // Maximum distance in meters
private const val HIT_TEST_MIN_CONFIDENCE = 0.5f // Minimum plane tracking confidence
private const val TAP_DEBOUNCE_TIME_MS = 300L // Minimum time between taps in milliseconds

@Composable
fun ARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null
) {
    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    val currentNodes = remember { mutableMapOf<String, ModelNode>() } // key: placedObjectId
    var lastTapTime by remember { mutableStateOf(0L) } // For tap debouncing

    /**
     * Normalize model file location to file:// URI format
     */
    fun normalizeModelLocation(location: String): String {
        return when {
            location.startsWith("file://") -> location
            location.startsWith("/") -> "file://$location"
            else -> location
        }
    }

    /**
     * Validate model path before attempting to place it
     */
    fun isValidModelPath(path: String?): Boolean {
        if (path.isNullOrBlank()) {
            Log.w(TAG, "Model path is null or blank")
            return false
        }
        
        val file = File(path)
        if (!file.exists()) {
            Log.w(TAG, "Model file does not exist: $path")
            return false
        }
        
        if (!file.canRead()) {
            Log.w(TAG, "Model file is not readable: $path")
            return false
        }
        
        return true
    }

    /**
     * Filter hit results based on distance and plane tracking confidence
     */
    fun filterHitResults(hitResults: List<HitResult>): List<HitResult> {
        return hitResults.filter { hit ->
            val trackable = hit.trackable
            
            // Only accept hits on planes
            if (trackable !is Plane) {
                return@filter false
            }
            
            // Check if plane is being tracked
            if (trackable.trackingState != TrackingState.TRACKING) {
                Log.d(TAG, "Plane not tracking, state: ${trackable.trackingState}")
                return@filter false
            }
            
            // Calculate distance from camera
            val pose = hit.hitPose
            val distance = Math.sqrt(
                (pose.tx() * pose.tx() + pose.ty() * pose.ty() + pose.tz() * pose.tz()).toDouble()
            ).toFloat()
            
            // Filter by distance range
            if (distance < HIT_TEST_MIN_DISTANCE || distance > HIT_TEST_MAX_DISTANCE) {
                Log.d(TAG, "Hit test distance out of range: $distance meters")
                return@filter false
            }
            
            // Check plane subsumed status (if plane is merged into another)
            if (trackable.subsumedBy != null) {
                Log.d(TAG, "Plane is subsumed by another plane")
                return@filter false
            }
            
            true
        }.sortedBy { hit ->
            // Sort by distance, closest first
            val pose = hit.hitPose
            pose.tx() * pose.tx() + pose.ty() * pose.ty() + pose.tz() * pose.tz()
        }
    }

    /**
     * Attempt to retrieve the current ARCore frame safely
     */
    fun getARFrameSafely(sceneView: ARSceneView): Frame? {
        return try {
            // Check if AR session is active
            val session = sceneView.session
            if (session == null) {
                Log.w(TAG, "AR session is not initialized")
                return null
            }
            
            // Check session tracking state
            val camera = sceneView.arSession?.currentFrame?.camera
            if (camera?.trackingState != TrackingState.TRACKING) {
                Log.w(TAG, "AR camera not tracking, state: ${camera?.trackingState}")
                return null
            }
            
            // Get frame
            sceneView.frame
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get AR frame: ${e.message}", e)
            null
        }
    }

    /**
     * Handle tap debouncing to prevent rapid consecutive placements
     */
    fun shouldProcessTap(currentTime: Long): Boolean {
        val timeSinceLastTap = currentTime - lastTapTime
        return if (timeSinceLastTap >= TAP_DEBOUNCE_TIME_MS) {
            lastTapTime = currentTime
            true
        } else {
            Log.d(TAG, "Tap ignored due to debouncing (${timeSinceLastTap}ms since last tap)")
            false
        }
    }

    // Sync placed objects with scene nodes
    LaunchedEffect(placedObjects, arSceneView) {
        val view = arSceneView ?: return@LaunchedEffect

        // Remove nodes that are no longer in the list
        val objectIds = placedObjects.map { it.objectId }.toSet()
        val idsToRemove = currentNodes.keys.filter { it !in objectIds }
        idsToRemove.forEach { id ->
            currentNodes[id]?.let { node ->
                view.removeChildNode(node)
            }
            currentNodes.remove(id)
        }

        // Add or update nodes from the list
        placedObjects.forEach { obj ->
            if (!currentNodes.containsKey(obj.objectId)) {
                // In SceneView 2.x, we use ModelNode for 3D models
                val modelLocation = normalizeModelLocation(obj.arObjectId)

                // Load model asynchronously to avoid UI blocking
                try {
                    val modelInstance = withContext(Dispatchers.IO) {
                        try {
                            val file = File(obj.arObjectId)
                            if (!file.exists()) {
                                Log.w(TAG, "Model file not found: ${obj.arObjectId}")
                                return@withContext null
                            }
                            view.modelLoader.loadModelInstance(modelLocation)
                        } catch (e: Exception) {
                            Log.e(TAG, "IO error accessing model file: ${obj.arObjectId}", e)
                            null
                        }
                    }

                    if (modelInstance != null) {
                        val modelNode = ModelNode(modelInstance).apply {
                            position = Position(obj.position.x, obj.position.y, obj.position.z)
                        }
                        view.addChildNode(modelNode)
                        currentNodes[obj.objectId] = modelNode
                        Log.d(TAG, "Model loaded successfully: ${obj.arObjectId}")
                    } else {
                        Log.e(TAG, "Failed to load model: modelInstance is null for ${obj.arObjectId}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading model ${obj.arObjectId}: ${e.message}", e)
                }
            } else {
                // Update position if needed
                currentNodes[obj.objectId]?.position =
                    Position(obj.position.x, obj.position.y, obj.position.z)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            arSceneView?.destroy()
        }
    }

    AndroidView(
        factory = { context ->
            ARSceneView(context).apply {
                sessionConfiguration = { session, config ->
                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        config.depthMode = Config.DepthMode.AUTOMATIC
                    }
                }

                onTouchEvent = { e, _ ->
                    if (e.action == MotionEvent.ACTION_UP) {
                        val currentTime = System.currentTimeMillis()
                        
                        // Debounce rapid taps
                        if (!shouldProcessTap(currentTime)) {
                            return@onTouchEvent true
                        }
                        
                        // Validate that a model is selected before attempting placement
                        if (!isValidModelPath(modelPathToLoad)) {
                            Log.w(TAG, "Cannot place object: no valid model selected")
                            return@onTouchEvent true
                        }
                        
                        try {
                            // Safely retrieve the AR frame
                            val frame = getARFrameSafely(this)
                            if (frame == null) {
                                Log.w(TAG, "Cannot perform hit test: AR frame unavailable")
                                return@onTouchEvent true
                            }
                            
                            // Perform hit test
                            val hitResults = try {
                                frame.hitTest(e.x, e.y)
                            } catch (ex: Exception) {
                                Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                                return@onTouchEvent true
                            }
                            
                            if (hitResults.isEmpty()) {
                                Log.d(TAG, "No surfaces detected at touch location. Try moving the device to scan for surfaces.")
                                return@onTouchEvent true
                            }
                            
                            // Filter hit results based on quality and distance
                            val validHits = filterHitResults(hitResults)
                            
                            if (validHits.isEmpty()) {
                                Log.d(TAG, "No valid planes found at touch location after filtering")
                                return@onTouchEvent true
                            }
                            
                            // Use the best (closest, highest confidence) hit result
                            val bestHit = validHits.first()
                            val plane = bestHit.trackable as Plane
                            val pose = bestHit.hitPose
                            
                            // Log placement details for debugging
                            Log.d(
                                TAG,
                                "Placing model on ${plane.type} plane at position: " +
                                "x=${pose.tx()}, y=${pose.ty()}, z=${pose.tz()}, " +
                                "plane extent: ${plane.extentX}x${plane.extentZ}"
                            )
                            
                            // Notify placement with validated path
                            modelPathToLoad?.let { path ->
                                onModelPlaced(path, pose.tx(), pose.ty(), pose.tz())
                            }
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Unexpected error during object placement: ${e.message}", e)
                        }
                    }
                    true
                }
                arSceneView = this
            }
        },
        modifier = modifier,
        update = {}
    )
}
