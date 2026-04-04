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
private const val TAP_MAX_DURATION_MS = 200L

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
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit = { _, _ -> },
    onObjectPositionChanged: ((placedObjectId: String, x: Float, y: Float, z: Float) -> Unit)? = null,
    onDragStart: ((objectId: String) -> Unit)? = null,
    onDragMove: ((objectId: String, screenX: Float, screenY: Float) -> Unit)? = null,
    onDragEnd: ((objectId: String, screenX: Float, screenY: Float) -> Unit)? = null
) {
    // CRITICAL FIX: Use rememberUpdatedState to ensure callbacks always reference latest values
    // This prevents AndroidView factory closure from capturing stale lambda references
    val currentOnModelPlaced by rememberUpdatedState(onModelPlaced)
    val currentOnObjectScaleChanged by rememberUpdatedState(onObjectScaleChanged)
    val currentOnObjectPositionChanged by rememberUpdatedState(onObjectPositionChanged)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragMove by rememberUpdatedState(onDragMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentModelPath by rememberUpdatedState(modelPathToLoad)
    
    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    val currentNodes = remember { mutableMapOf<String, ModelNode>() }
    val dragOriginalScales = remember { mutableMapOf<String, Scale>() }
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
        
        // Remove file:// prefix if present
        val filePath = if (path.startsWith("file://")) {
            path.substring(7) // Remove "file://"
        } else {
            path
        }
        
        val file = File(filePath)
        if (!file.exists()) {
            Log.w(TAG, "Model file does not exist: $path (resolved to: $filePath)")
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
                            // Strip file:// prefix if present
                            val filePath = if (obj.arObjectId.startsWith("file://")) {
                                obj.arObjectId.substring(7)
                            } else {
                                obj.arObjectId
                            }
                            val file = File(filePath)
                            if (!file.exists()) {
                                Log.w(TAG, "Model file not found: ${obj.arObjectId} (resolved to: $filePath)")
                                return@withContext null
                            }
                            view.modelLoader.loadModelInstance(modelLocation)
                        } catch (e: Exception) {
                            Log.e(TAG, "IO error accessing model file: ${obj.arObjectId}", e)
                            null
                        }
                    }

                    if (modelInstance != null) {
                        val placedObjectId = obj.objectId
                        val modelNode = ModelNode(modelInstance).apply {
                            position = Position(obj.position.x, obj.position.y, obj.position.z)
                            scale = Scale(obj.scale, obj.scale, obj.scale)

                            // Enable SceneView built-in drag gesture support (long-press + drag)
                            isPositionEditable = true
                            isRotationEditable = false
                            isScaleEditable = false

                            onMoveBegin = { _, e ->
                                currentOnDragStart?.invoke(placedObjectId)
                                currentOnDragMove?.invoke(placedObjectId, e.x, e.y)

                                dragOriginalScales[placedObjectId] = scale
                                scale = Scale(scale.x * 1.1f, scale.y * 1.1f, scale.z * 1.1f)
                                true
                            }

                            onMove = { _, e, _ ->
                                currentOnDragMove?.invoke(placedObjectId, e.x, e.y)
                                true
                            }

                            onMoveEnd = { _, e ->
                                currentOnDragEnd?.invoke(placedObjectId, e.x, e.y)

                                dragOriginalScales.remove(placedObjectId)?.let { originalScale ->
                                    scale = originalScale
                                }
                                val pos = worldPosition
                                currentOnObjectPositionChanged?.invoke(placedObjectId, pos.x, pos.y, pos.z)
                            }
                        }
                        view.addChildNode(modelNode)
                        currentNodes[placedObjectId] = modelNode
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
                            currentOnObjectScaleChanged(nodeId, currentScale)
                            Log.d(TAG, "Scale ended for $nodeId with scale $currentScale")
                        }
                    }
                })

                onTouchEvent = touchEvent@{ e, _ ->
                    // Feed scale detector first so pinch-to-zoom works.
                    scaleGestureDetector?.onTouchEvent(e)

                    if (scaleGestureDetector?.isInProgress == true || e.pointerCount > 1) {
                        return@touchEvent true
                    }

                    // Let SceneView handle long-press drag gestures.
                    if (e.action != MotionEvent.ACTION_UP) {
                        return@touchEvent false
                    }

                    val isQuickTap = (e.eventTime - e.downTime) <= TAP_MAX_DURATION_MS
                    if (!isQuickTap) {
                        return@touchEvent false
                    }

                    val currentTime = System.currentTimeMillis()
                    if (!shouldProcessTap(currentTime)) {
                        return@touchEvent true
                    }

                    if (!isValidModelPath(currentModelPath)) {
                        Log.w(TAG, "Cannot place object: no valid model selected (currentModelPath=$currentModelPath)")
                        return@touchEvent false
                    }

                    val frame = getARFrameSafely(this)
                    if (frame == null) {
                        Log.w(TAG, "Cannot perform hit test: AR frame unavailable")
                        return@touchEvent false
                    }

                    val hitResults = try {
                        frame.hitTest(e.x, e.y)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                        emptyList()
                    }

                    if (hitResults.isEmpty()) {
                        Log.d(TAG, "No surfaces detected")
                        return@touchEvent false
                    }

                    val validHits = filterHitResults(hitResults)
                    if (validHits.isEmpty()) {
                        Log.d(TAG, "No valid planes found")
                        return@touchEvent false
                    }

                    val bestHit = validHits.first()
                    val plane = bestHit.trackable as Plane
                    val pose = bestHit.hitPose

                    Log.d(
                        TAG,
                        "Placing model on ${plane.type} plane at (${pose.tx()}, ${pose.ty()}, ${pose.tz()}) with scale $DEFAULT_SCALE"
                    )

                    currentModelPath?.let { path ->
                        currentOnModelPlaced(path, pose.tx(), pose.ty(), pose.tz(), DEFAULT_SCALE)
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
