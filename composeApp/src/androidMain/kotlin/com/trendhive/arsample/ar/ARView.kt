package com.trendhive.arsample.ar

import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
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
import io.github.sceneview.math.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ARView"

// Hit test configuration constants
private const val HIT_TEST_MIN_DISTANCE = 0.1f
private const val HIT_TEST_MAX_DISTANCE = 10.0f
private const val HIT_TEST_MIN_CONFIDENCE = 0.5f
private const val TAP_DEBOUNCE_TIME_MS = 300L

// Scale configuration constants
private const val DEFAULT_SCALE = 0.3f
private const val MIN_SCALE = 0.1f
private const val MAX_SCALE = 5.0f

@Composable
fun ARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float, scale: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit = { _, _ -> }
) {
    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    val currentNodes = remember { mutableMapOf<String, ModelNode>() }
    var lastTapTime by remember { mutableStateOf(0L) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var currentScale by remember { mutableStateOf(1f) }
    var scaleGestureDetector by remember { mutableStateOf<ScaleGestureDetector?>(null) }

    fun normalizeModelLocation(location: String): String {
        return when {
            location.startsWith("file://") -> location
            location.startsWith("/") -> "file://$location"
            else -> location
        }
    }

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

    fun filterHitResults(hitResults: List<HitResult>): List<HitResult> {
        return hitResults.filter { hit ->
            val trackable = hit.trackable
            
            if (trackable !is Plane) {
                return@filter false
            }
            
            if (trackable.trackingState != TrackingState.TRACKING) {
                Log.d(TAG, "Plane not tracking, state: ${trackable.trackingState}")
                return@filter false
            }
            
            val pose = hit.hitPose
            val distance = Math.sqrt(
                (pose.tx() * pose.tx() + pose.ty() * pose.ty() + pose.tz() * pose.tz()).toDouble()
            ).toFloat()
            
            if (distance < HIT_TEST_MIN_DISTANCE || distance > HIT_TEST_MAX_DISTANCE) {
                Log.d(TAG, "Hit test distance out of range: $distance meters")
                return@filter false
            }
            
            if (trackable.subsumedBy != null) {
                Log.d(TAG, "Plane is subsumed by another plane")
                return@filter false
            }
            
            true
        }.sortedBy { hit ->
            val pose = hit.hitPose
            pose.tx() * pose.tx() + pose.ty() * pose.ty() + pose.tz() * pose.tz()
        }
    }

    fun getARFrameSafely(sceneView: ARSceneView): Frame? {
        return try {
            val session = sceneView.session
            if (session == null) {
                Log.w(TAG, "AR session is not initialized")
                return null
            }
            
            val frame = sceneView.frame
            if (frame == null) {
                Log.w(TAG, "AR frame is not available")
                return null
            }
            
            val camera = frame.camera
            if (camera.trackingState != TrackingState.TRACKING) {
                Log.w(TAG, "AR camera not tracking, state: ${camera.trackingState}")
                return null
            }
            
            frame
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get AR frame: ${e.message}", e)
            null
        }
    }

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

    LaunchedEffect(placedObjects, arSceneView) {
        val view = arSceneView ?: return@LaunchedEffect

        val objectIds = placedObjects.map { it.objectId }.toSet()
        val idsToRemove = currentNodes.keys.filter { it !in objectIds }
        idsToRemove.forEach { id ->
            currentNodes[id]?.let { node ->
                view.removeChildNode(node)
            }
            currentNodes.remove(id)
        }

        placedObjects.forEach { obj ->
            if (!currentNodes.containsKey(obj.objectId)) {
                val modelLocation = normalizeModelLocation(obj.arObjectId)

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
                            scale = Scale(obj.scale, obj.scale, obj.scale)
                        }
                        view.addChildNode(modelNode)
                        currentNodes[obj.objectId] = modelNode
                        Log.d(TAG, "Model loaded: ${obj.arObjectId} with scale ${obj.scale}")
                    } else {
                        Log.e(TAG, "Failed to load model: ${obj.arObjectId}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading model ${obj.arObjectId}: ${e.message}", e)
                }
            } else {
                currentNodes[obj.objectId]?.let { node ->
                    node.position = Position(obj.position.x, obj.position.y, obj.position.z)
                    node.scale = Scale(obj.scale, obj.scale, obj.scale)
                }
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

                scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        selectedNodeId?.let { nodeId ->
                            currentNodes[nodeId]?.let { node ->
                                val newScale = min(MAX_SCALE, max(MIN_SCALE, currentScale * detector.scaleFactor))
                                currentScale = newScale
                                node.scale = Scale(newScale, newScale, newScale)
                                Log.d(TAG, "Scaling object $nodeId to $newScale")
                            }
                        }
                        return true
                    }
                    
                    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                        selectedNodeId = currentNodes.entries.firstOrNull()?.key
                        selectedNodeId?.let { nodeId ->
                            currentNodes[nodeId]?.let { node ->
                                currentScale = node.scale.x
                            }
                        }
                        return true
                    }
                    
                    override fun onScaleEnd(detector: ScaleGestureDetector) {
                        selectedNodeId?.let { nodeId ->
                            onObjectScaleChanged(nodeId, currentScale)
                            Log.d(TAG, "Scale ended for $nodeId with scale $currentScale")
                        }
                    }
                })

                onTouchEvent = touchEvent@{ e, _ ->
                    scaleGestureDetector?.onTouchEvent(e)
                    
                    if (scaleGestureDetector?.isInProgress == true) {
                        return@touchEvent true
                    }
                    
                    if (e.action == MotionEvent.ACTION_UP) {
                        val currentTime = System.currentTimeMillis()
                        
                        if (!shouldProcessTap(currentTime)) {
                            return@touchEvent true
                        }
                        
                        if (!isValidModelPath(modelPathToLoad)) {
                            Log.w(TAG, "Cannot place object: no valid model selected")
                            return@touchEvent true
                        }
                        
                        val frame = getARFrameSafely(this)
                        if (frame == null) {
                            Log.w(TAG, "Cannot perform hit test: AR frame unavailable")
                            return@touchEvent true
                        }
                        
                        val hitResults = try {
                            frame.hitTest(e.x, e.y)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                            emptyList()
                        }
                        
                        if (hitResults.isEmpty()) {
                            Log.d(TAG, "No surfaces detected")
                            return@touchEvent true
                        }
                        
                        val validHits = filterHitResults(hitResults)
                        
                        if (validHits.isEmpty()) {
                            Log.d(TAG, "No valid planes found")
                            return@touchEvent true
                        }
                        
                        val bestHit = validHits.first()
                        val plane = bestHit.trackable as Plane
                        val pose = bestHit.hitPose
                        
                        Log.d(
                            TAG,
                            "Placing model on ${plane.type} plane at (${pose.tx()}, ${pose.ty()}, ${pose.tz()}) with scale $DEFAULT_SCALE"
                        )
                        
                        modelPathToLoad?.let { path ->
                            onModelPlaced(path, pose.tx(), pose.ty(), pose.tz(), DEFAULT_SCALE)
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
