package com.trendhive.arsample.ar

import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.trendhive.arsample.domain.model.PlacedObject
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "ARView"

// Hit test configuration constants
private const val HIT_TEST_MIN_DISTANCE = 0.1f
private const val HIT_TEST_MAX_DISTANCE = 10.0f
private const val HIT_TEST_MIN_CONFIDENCE = 0.5f

// Placement gesture configuration
private const val LONG_PRESS_THRESHOLD_MS = 500L
private const val TAP_DEBOUNCE_TIME_MS = 300L
private const val TAP_MAX_DURATION_MS = 200L

// Scale configuration constants
private const val DEFAULT_SCALE = 0.3f
private const val MIN_SCALE = 0.1f
private const val MAX_SCALE = 5.0f

// Drag gesture configuration constants
private const val DRAG_SLOP_PX = 24f  // Movement threshold in pixels to start drag
private const val DRAG_LONG_PRESS_MS = 150L  // Time threshold in ms to recognize as potential drag
private const val DRAG_FEEDBACK_SCALE_MULTIPLIER = 1.1f  // Visual feedback scale during drag

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
    
    val coroutineScope = rememberCoroutineScope()

    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    val currentNodes = remember { mutableMapOf<String, ModelNode>() }

    // Long-press placement state
    var touchDownTimeMs by remember { mutableStateOf<Long?>(null) }
    var touchDownPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var holdProgress by remember { mutableStateOf(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    var isHoldActive by remember { mutableStateOf(false) }
    var isHoldCancelled by remember { mutableStateOf(false) }
    
    val dragOriginalScales = remember { mutableMapOf<String, Scale>() }
    var lastTapTime by remember { mutableStateOf(0L) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var currentScale by remember { mutableStateOf(1f) }
    var scaleGestureDetector by remember { mutableStateOf<ScaleGestureDetector?>(null) }
    
    // Custom drag state - because SceneView's built-in gesture detection doesn't work reliably
    // We must manually handle drag by tracking state and updating position via ARCore hit tests
    var isDragging by remember { mutableStateOf(false) }
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    var dragTouchDownTime by remember { mutableStateOf(0L) }
    var dragTouchDownPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var dragStartNodePosition by remember { mutableStateOf<Position?>(null) }
    var dragLastScreenX by remember { mutableStateOf<Float?>(null) }
    var dragLastScreenY by remember { mutableStateOf<Float?>(null) }
    
    // Helper function to reset drag state
    fun resetDragState() {
        isDragging = false
        draggedNodeId = null
        dragStartNodePosition = null
        dragTouchDownPosition = null
        dragTouchDownTime = 0L
        dragLastScreenX = null
        dragLastScreenY = null
    }
    
    // Helper function to restore dragged node's original state
    fun restoreDraggedNodeState(nodeId: String) {
        dragStartNodePosition?.let { startPos ->
            currentNodes[nodeId]?.position = startPos
        }
        dragOriginalScales.remove(nodeId)?.let { originalScale ->
            currentNodes[nodeId]?.scale = originalScale
        }
    }

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

    fun cancelHoldFeedback() {
        holdJob?.cancel()
        holdJob = null
        touchDownTimeMs = null
        touchDownPosition = null
        holdProgress = 0f
        isHoldActive = false
        isHoldCancelled = false
    }

    /**
     * Projects a 3D world position to 2D screen coordinates using ARCore camera.
     * Returns null if the point is behind the camera or projection fails.
     */
    fun worldToScreen(view: ARSceneView, frame: Frame, worldX: Float, worldY: Float, worldZ: Float): Pair<Float, Float>? {
        return try {
            val camera = frame.camera
            if (camera.trackingState != TrackingState.TRACKING) {
                return null
            }
            
            val viewWidth = view.width.toFloat()
            val viewHeight = view.height.toFloat()
            if (viewWidth <= 0 || viewHeight <= 0) return null
            
            // Get projection and view matrices from ARCore camera
            val projectionMatrix = FloatArray(16)
            val viewMatrix = FloatArray(16)
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
            camera.getViewMatrix(viewMatrix, 0)
            
            // Combine into view-projection matrix
            val vpMatrix = FloatArray(16)
            android.opengl.Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
            
            // Transform world point to clip space
            val worldPoint = floatArrayOf(worldX, worldY, worldZ, 1f)
            val clipPoint = FloatArray(4)
            android.opengl.Matrix.multiplyMV(clipPoint, 0, vpMatrix, 0, worldPoint, 0)
            
            // Check if point is behind camera (w <= 0 means behind)
            if (clipPoint[3] <= 0.001f) {
                return null
            }
            
            // Perspective divide to get NDC (Normalized Device Coordinates)
            val ndcX = clipPoint[0] / clipPoint[3]
            val ndcY = clipPoint[1] / clipPoint[3]
            
            // Convert NDC to screen coordinates
            // NDC is in range [-1, 1], convert to [0, width] and [0, height]
            val screenX = (ndcX + 1f) * 0.5f * viewWidth
            val screenY = (1f - ndcY) * 0.5f * viewHeight  // Y is flipped in screen space
            
            Pair(screenX, screenY)
        } catch (e: Exception) {
            Log.e(TAG, "worldToScreen failed: ${e.message}", e)
            null
        }
    }
    
    /**
     * Checks if the given screen coordinates hit any existing ModelNode.
     * Returns the objectId if hit, null otherwise.
     * 
     * Uses screen-space distance checking which works correctly for the entire
     * model, not just the base. This fixes the issue where touching the top
     * of tall models didn't trigger drag.
     */
    fun hitTestNode(view: ARSceneView, x: Float, y: Float): String? {
        return try {
            val frame = getARFrameSafely(view) ?: return null
            
            // Screen-space hit detection: project each node to screen and check 2D distance
            // This works regardless of where on the model the user touches
            var closestObjectId: String? = null
            var closestDistanceSq = Float.MAX_VALUE
            
            // Hit radius in screen pixels - generous to account for model size
            val hitRadiusPx = 150f
            val hitRadiusSq = hitRadiusPx * hitRadiusPx
            
            for ((objectId, node) in currentNodes) {
                val nodePos = node.worldPosition
                
                // Project node's base position to screen
                val screenPos = worldToScreen(view, frame, nodePos.x, nodePos.y, nodePos.z)
                if (screenPos != null) {
                    val dx = x - screenPos.first
                    val dy = y - screenPos.second
                    val distanceSq = dx * dx + dy * dy
                    
                    // Check if within hit radius and closer than previous candidates
                    if (distanceSq < hitRadiusSq && distanceSq < closestDistanceSq) {
                        closestDistanceSq = distanceSq
                        closestObjectId = objectId
                    }
                }
                
                // Also check a point above the base (approximating model center/top)
                // Assume average model height of ~0.3m for the scaled models
                val modelHeight = 0.3f * node.scale.y
                val topScreenPos = worldToScreen(view, frame, nodePos.x, nodePos.y + modelHeight, nodePos.z)
                if (topScreenPos != null) {
                    val dx = x - topScreenPos.first
                    val dy = y - topScreenPos.second
                    val distanceSq = dx * dx + dy * dy
                    
                    if (distanceSq < hitRadiusSq && distanceSq < closestDistanceSq) {
                        closestDistanceSq = distanceSq
                        closestObjectId = objectId
                    }
                }
            }
            
            if (closestObjectId != null) {
                Log.d(TAG, "Screen-space hit detected on object $closestObjectId (distSq=${closestDistanceSq})")
            }
            
            closestObjectId
        } catch (e: Exception) {
            Log.e(TAG, "Node hit test failed: ${e.message}", e)
            null
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

                            // Note: We use custom drag handling in onTouchEvent instead of
                            // SceneView's built-in gestures which don't work reliably.
                            // Keep these flags enabled as they may help with node selection.
                            isPositionEditable = true
                            isRotationEditable = false
                            isScaleEditable = false

                            // Fallback callbacks - may not fire with our custom touch handling
                            // but kept for potential edge cases
                            onMoveBegin = { _, e ->
                                Log.d(TAG, "SceneView onMoveBegin for $placedObjectId (fallback)")
                                currentOnDragStart?.invoke(placedObjectId)
                                currentOnDragMove?.invoke(placedObjectId, e.x, e.y)

                                dragOriginalScales[placedObjectId] = scale
                                scale = Scale(scale.x * DRAG_FEEDBACK_SCALE_MULTIPLIER, scale.y * DRAG_FEEDBACK_SCALE_MULTIPLIER, scale.z * DRAG_FEEDBACK_SCALE_MULTIPLIER)
                                true
                            }

                            onMove = { _, e, _ ->
                                currentOnDragMove?.invoke(placedObjectId, e.x, e.y)
                                true
                            }

                            onMoveEnd = { _, e ->
                                Log.d(TAG, "SceneView onMoveEnd for $placedObjectId (fallback)")
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

    val indicatorSize = 48.dp
    val indicatorRadiusPx = with(LocalDensity.current) { (indicatorSize / 2).roundToPx() }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                val touchSlopPx = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

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

                    fun performHitTestAndPlace(x: Float, y: Float) {
                        if (!isValidModelPath(currentModelPath)) {
                            Log.w(TAG, "Cannot place object: no valid model selected (currentModelPath=$currentModelPath)")
                            return
                        }

                        val frame = getARFrameSafely(this)
                        if (frame == null) {
                            Log.w(TAG, "Cannot perform hit test: AR frame unavailable")
                            return
                        }

                        val hitResults = try {
                            frame.hitTest(x, y)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                            emptyList()
                        }

                        if (hitResults.isEmpty()) {
                            Log.d(TAG, "No surfaces detected")
                            return
                        }

                        val validHits = filterHitResults(hitResults)

                        if (validHits.isEmpty()) {
                            Log.d(TAG, "No valid planes found")
                            return
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
                    }

                    onTouchEvent = touchEvent@{ e, _ ->
                        scaleGestureDetector?.onTouchEvent(e)

                        if (scaleGestureDetector?.isInProgress == true || e.pointerCount > 1) {
                            if (isHoldActive) {
                                cancelHoldFeedback()
                            }
                            // Cancel any ongoing drag if multi-touch detected
                            if (isDragging) {
                                Log.d(TAG, "Drag cancelled due to multi-touch")
                                // Restore original position
                                draggedNodeId?.let { nodeId ->
                                    restoreDraggedNodeState(nodeId)
                                }
                                resetDragState()
                            }
                            return@touchEvent true
                        }

                        when (e.action) {
                            MotionEvent.ACTION_DOWN -> {
                                val hitObjectId = hitTestNode(this, e.x, e.y)
                                
                                if (hitObjectId != null) {
                                    // Touch on existing object - start potential drag
                                    Log.d(TAG, "Touch DOWN on object $hitObjectId - starting drag detection")
                                    cancelHoldFeedback()
                                    
                                    dragTouchDownTime = System.currentTimeMillis()
                                    dragTouchDownPosition = e.x to e.y
                                    draggedNodeId = hitObjectId
                                    isDragging = false  // Not dragging yet, just tracking
                                    
                                    // Store node's starting position for potential drag
                                    currentNodes[hitObjectId]?.let { node ->
                                        dragStartNodePosition = node.position
                                    }
                                    
                                    return@touchEvent true  // CONSUME - we're handling this
                                }
                                
                                // Touch on empty space - handle placement
                                isHoldActive = true
                                isHoldCancelled = false
                                touchDownTimeMs = System.currentTimeMillis()
                                touchDownPosition = e.x to e.y
                                holdProgress = 0f

                                holdJob?.cancel()
                                holdJob = coroutineScope.launch {
                                    while (isHoldActive && !isHoldCancelled) {
                                        val downTime = touchDownTimeMs ?: break
                                        val elapsed = System.currentTimeMillis() - downTime
                                        holdProgress = min(1f, elapsed.toFloat() / LONG_PRESS_THRESHOLD_MS.toFloat())

                                        if (holdProgress >= 1f) break
                                        delay(16)
                                    }
                                }
                                true
                            }

                            MotionEvent.ACTION_MOVE -> {
                                // Check if we're tracking a node touch (potential or active drag)
                                val nodeId = draggedNodeId
                                if (nodeId != null) {
                                    val startPos = dragTouchDownPosition
                                    if (startPos != null) {
                                        val dx = e.x - startPos.first
                                        val dy = e.y - startPos.second
                                        val distance = hypot(dx, dy)
                                        val elapsed = System.currentTimeMillis() - dragTouchDownTime
                                        
                                        // Check if should start dragging (either moved enough or held long enough)
                                        if (!isDragging && (distance > DRAG_SLOP_PX || elapsed > DRAG_LONG_PRESS_MS)) {
                                            isDragging = true
                                            dragLastScreenX = e.x  // Initialize last position for delta calculation
                                            dragLastScreenY = e.y
                                            Log.d(TAG, "Drag STARTED for object $nodeId (distance=$distance, elapsed=${elapsed}ms)")
                                            
                                            // Visual feedback: scale up
                                            currentNodes[nodeId]?.let { node ->
                                                dragOriginalScales[nodeId] = node.scale
                                                node.scale = Scale(node.scale.x * DRAG_FEEDBACK_SCALE_MULTIPLIER, node.scale.y * DRAG_FEEDBACK_SCALE_MULTIPLIER, node.scale.z * DRAG_FEEDBACK_SCALE_MULTIPLIER)
                                            }
                                            
                                            currentOnDragStart?.invoke(nodeId)
                                        }
                                        
                                        // If dragging, update node position
                                        if (isDragging) {
                                            val frame = getARFrameSafely(this)
                                            frame?.let { f ->
                                                try {
                                                    val hitResults = f.hitTest(e.x, e.y)
                                                    val validHits = filterHitResults(hitResults)
                                                    
                                                    currentNodes[nodeId]?.let { node ->
                                                        if (validHits.isNotEmpty()) {
                                                            val bestHit = validHits.first()
                                                            val pose = bestHit.hitPose
                                                            node.position = Position(pose.tx(), pose.ty(), pose.tz())
                                                            Log.d(TAG, "Drag MOVE: updated position to (${pose.tx()}, ${pose.ty()}, ${pose.tz()})")
                                                        } else {
                                                            // No valid plane hit - use screen delta movement
                                                            // Project finger movement to world XZ plane (horizontal)
                                                            val cam = f.camera
                                                            if (cam.trackingState == TrackingState.TRACKING) {
                                                                val camPose = cam.pose
                                                                val oldPos = node.position
                                                                
                                                                // Calculate distance from camera to object (for scaling)
                                                                val objDx = oldPos.x - camPose.tx()
                                                                val objDz = oldPos.z - camPose.tz()
                                                                val horizontalDist = kotlin.math.sqrt(objDx*objDx + objDz*objDz)
                                                                
                                                                // Get screen delta from last position (not absolute)
                                                                val startPos = dragTouchDownPosition
                                                                val lastX = dragLastScreenX ?: startPos?.first ?: e.x
                                                                val lastY = dragLastScreenY ?: startPos?.second ?: e.y
                                                                val screenDeltaX = e.x - lastX
                                                                val screenDeltaY = e.y - lastY
                                                                
                                                                // Store current position for next frame
                                                                dragLastScreenX = e.x
                                                                dragLastScreenY = e.y
                                                                
                                                                // Convert screen pixels to world units
                                                                // Scale factor: pixels to meters (adjust based on distance)
                                                                val screenWidth = this.width.toFloat()
                                                                val pixelToWorld = (horizontalDist * 0.002f).coerceIn(0.001f, 0.01f)
                                                                
                                                                // Get camera's right and forward vectors (horizontal only)
                                                                val rightVec = camPose.getXAxis()
                                                                val forwardVec = camPose.getZAxis()
                                                                
                                                                // Project camera forward to XZ plane (ignore Y component for horizontal movement)
                                                                val forwardXZ = floatArrayOf(-forwardVec[0], 0f, -forwardVec[2])
                                                                val forwardLen = kotlin.math.sqrt(forwardXZ[0]*forwardXZ[0] + forwardXZ[2]*forwardXZ[2])
                                                                if (forwardLen > 0.001f) {
                                                                    forwardXZ[0] /= forwardLen
                                                                    forwardXZ[2] /= forwardLen
                                                                }
                                                                
                                                                // Calculate world movement from screen delta
                                                                // Screen X -> world right direction
                                                                // Screen Y -> world forward direction (into screen)
                                                                val worldDeltaX = rightVec[0] * screenDeltaX * pixelToWorld + 
                                                                                  forwardXZ[0] * screenDeltaY * pixelToWorld
                                                                val worldDeltaZ = rightVec[2] * screenDeltaX * pixelToWorld + 
                                                                                  forwardXZ[2] * screenDeltaY * pixelToWorld
                                                                
                                                                // Apply delta to current position (keep Y height unchanged)
                                                                val newX = oldPos.x + worldDeltaX
                                                                val newY = oldPos.y  // Keep Y (height) the same
                                                                val newZ = oldPos.z + worldDeltaZ
                                                                
                                                                node.position = Position(newX, newY, newZ)
                                                                Log.d(TAG, "Drag MOVE (fallback): delta=(${screenDeltaX}, ${screenDeltaY}) -> world=($newX, $newY, $newZ)")
                                                            }
                                                        }
                                                    }
                                                } catch (ex: Exception) {
                                                    Log.e(TAG, "Hit test during drag failed: ${ex.message}", ex)
                                                }
                                            }
                                            
                                            currentOnDragMove?.invoke(nodeId, e.x, e.y)
                                        }
                                    }
                                    return@touchEvent true
                                }
                                
                                // Fallback: placement hold tracking
                                if (!isHoldActive) {
                                    return@touchEvent false
                                }
                                
                                val start = touchDownPosition
                                if (start != null) {
                                    val dx = e.x - start.first
                                    val dy = e.y - start.second
                                    if (hypot(dx, dy) > touchSlopPx) {
                                        Log.d(TAG, "Hold cancelled due to movement (>${touchSlopPx}px)")
                                        isHoldCancelled = true
                                        cancelHoldFeedback()
                                    }
                                }
                                true
                            }

                            MotionEvent.ACTION_UP -> {
                                // Check if we were tracking a node drag
                                val nodeId = draggedNodeId
                                if (nodeId != null) {
                                    if (isDragging) {
                                        Log.d(TAG, "Drag ENDED for object $nodeId at screen (${e.x}, ${e.y})")
                                        
                                        // Restore original scale
                                        dragOriginalScales.remove(nodeId)?.let { originalScale ->
                                            currentNodes[nodeId]?.scale = originalScale
                                        }
                                        
                                        // Notify about final position
                                        currentNodes[nodeId]?.let { node ->
                                            val pos = node.worldPosition
                                            currentOnObjectPositionChanged?.invoke(nodeId, pos.x, pos.y, pos.z)
                                        }
                                        
                                        currentOnDragEnd?.invoke(nodeId, e.x, e.y)
                                    } else {
                                        // Was just a tap on the object, not a drag
                                        val elapsed = System.currentTimeMillis() - dragTouchDownTime
                                        Log.d(TAG, "Tap on object $nodeId (duration=${elapsed}ms) - not a drag")
                                        // Could trigger selection here if needed
                                    }
                                    
                                    // Reset drag state
                                    resetDragState()
                                    return@touchEvent true
                                }
                                
                                // Fallback: placement hold handling
                                if (!isHoldActive) {
                                    return@touchEvent false
                                }
                                
                                val downTime = touchDownTimeMs
                                val start = touchDownPosition
                                val holdDuration = if (downTime != null) System.currentTimeMillis() - downTime else 0L
                                val shouldPlace = isHoldActive && !isHoldCancelled && holdDuration >= LONG_PRESS_THRESHOLD_MS

                                cancelHoldFeedback()

                                if (shouldPlace && start != null) {
                                    performHitTestAndPlace(start.first, start.second)
                                } else {
                                    Log.d(TAG, "Ignoring tap: holdDuration=${holdDuration}ms")
                                }
                                true
                            }

                            MotionEvent.ACTION_CANCEL -> {
                                // Cancel any ongoing drag
                                val nodeId = draggedNodeId
                                if (nodeId != null) {
                                    Log.d(TAG, "Drag CANCELLED for object $nodeId")
                                    
                                    // Restore original position and scale
                                    if (isDragging) {
                                        restoreDraggedNodeState(nodeId)
                                    }
                                    
                                    resetDragState()
                                    return@touchEvent true
                                }
                                
                                if (isHoldActive) {
                                    cancelHoldFeedback()
                                    return@touchEvent true
                                }
                                false
                            }

                            else -> false  // Don't consume unknown events
                        }
                    }

                    arSceneView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {}
        )

        // Visual feedback: show a small determinate progress indicator under the finger.
        val holdPos = touchDownPosition
        if (isHoldActive && holdPos != null) {
            CircularProgressIndicator(
                progress = holdProgress,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            (holdPos.first - indicatorRadiusPx).roundToInt(),
                            (holdPos.second - indicatorRadiusPx).roundToInt()
                        )
                    }
                    .size(indicatorSize)
            )
        }
    }
}
