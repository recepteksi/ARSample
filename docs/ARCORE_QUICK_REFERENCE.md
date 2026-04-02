# ARCore SceneView Quick Reference

**For:** Android AR Development with Compose + SceneView  
**Platform:** Android (ARCore + SceneView 2.x)  
**Last Updated:** 2024-03-30

---

## 🚨 Critical Pattern: AndroidView + Callbacks

### ❌ WRONG - Stale Closure
```kotlin
@Composable
fun MyARView(
    onEvent: (String) -> Unit,
    config: Config
) {
    AndroidView(
        factory = { context ->
            ARSceneView(context).apply {
                onTouchEvent = { e, _ ->
                    onEvent("tap")  // ❌ Captures initial onEvent reference
                }
            }
        }
    )
}
```

### ✅ CORRECT - rememberUpdatedState
```kotlin
@Composable
fun MyARView(
    onEvent: (String) -> Unit,
    config: Config
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentConfig by rememberUpdatedState(config)
    
    AndroidView(
        factory = { context ->
            ARSceneView(context).apply {
                onTouchEvent = { e, _ ->
                    currentOnEvent("tap")  // ✅ Always latest reference
                }
            }
        }
    )
}
```

**Rule of Thumb:**  
> Any parameter used in AndroidView factory closures **MUST** be wrapped with `rememberUpdatedState`

---

## 📐 SceneView Configuration Template

```kotlin
ARSceneView(context).apply {
    // Session configuration
    sessionConfiguration = { session, config ->
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        
        // Optional: Depth mode (requires ToF sensor)
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.depthMode = Config.DepthMode.AUTOMATIC
        }
    }
    
    // Touch event handling
    onTouchEvent = { motionEvent, _ ->
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            val frame = getARFrameSafely(this)
            val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
            
            val validHits = filterHitResults(hitResults)
            validHits.firstOrNull()?.let { hit ->
                val pose = hit.hitPose
                currentOnModelPlaced(pose.tx(), pose.ty(), pose.tz())
            }
        }
        true
    }
}
```

---

## 🎯 Hit Test Best Practices

### Safe Frame Retrieval
```kotlin
fun getARFrameSafely(sceneView: ARSceneView): Frame? {
    return try {
        val session = sceneView.session ?: return null
        val frame = sceneView.frame ?: return null
        val camera = frame.camera
        
        if (camera.trackingState != TrackingState.TRACKING) {
            return null
        }
        
        frame
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get AR frame: ${e.message}")
        null
    }
}
```

### Quality Filtering
```kotlin
fun filterHitResults(hitResults: List<HitResult>?): List<HitResult> {
    return hitResults?.filter { hit ->
        val trackable = hit.trackable
        
        // Only accept tracked planes
        trackable is Plane &&
        trackable.trackingState == TrackingState.TRACKING &&
        
        // Ignore merged planes
        trackable.subsumedBy == null &&
        
        // Distance bounds (0.1m - 10m)
        hit.distance in 0.1f..10.0f
        
    }?.sortedBy { it.distance }  // Closest first
        ?: emptyList()
}
```

---

## 🗂️ Model Loading Pattern

### Async Loading
```kotlin
LaunchedEffect(modelPath) {
    val view = arSceneView ?: return@LaunchedEffect
    
    try {
        val modelInstance = withContext(Dispatchers.IO) {
            val file = File(modelPath)
            if (!file.exists()) {
                Log.w(TAG, "Model not found: $modelPath")
                return@withContext null
            }
            view.modelLoader.loadModelInstance(normalizeModelLocation(modelPath))
        }
        
        if (modelInstance != null) {
            val modelNode = ModelNode(modelInstance).apply {
                position = Position(x, y, z)
                scale = Scale(scale, scale, scale)
            }
            view.addChildNode(modelNode)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error loading model: ${e.message}")
    }
}
```

### Path Normalization
```kotlin
fun normalizeModelLocation(location: String): String {
    return when {
        location.startsWith("file://") -> location
        location.startsWith("/") -> "file://$location"
        else -> location
    }
}
```

### Model Path Validation
```kotlin
fun isValidModelPath(path: String?): Boolean {
    if (path.isNullOrBlank()) return false
    
    val filePath = path.removePrefix("file://")
    val file = File(filePath)
    
    return file.exists() && file.canRead()
}
```

---

## 🎮 Touch Gesture Handling

### Tap Debouncing
```kotlin
private const val TAP_DEBOUNCE_TIME_MS = 300L
var lastTapTime by remember { mutableStateOf(0L) }

fun shouldProcessTap(currentTime: Long): Boolean {
    val timeSinceLastTap = currentTime - lastTapTime
    return if (timeSinceLastTap >= TAP_DEBOUNCE_TIME_MS) {
        lastTapTime = currentTime
        true
    } else {
        false
    }
}
```

### Pinch-to-Scale
```kotlin
val scaleGestureDetector = ScaleGestureDetector(
    context,
    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            selectedNode?.let { node ->
                val newScale = (currentScale * detector.scaleFactor)
                    .coerceIn(MIN_SCALE, MAX_SCALE)
                node.scale = Scale(newScale, newScale, newScale)
            }
            return true
        }
        
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            currentScale = selectedNode?.scale?.x ?: 1f
            return true
        }
    }
)

// In onTouchEvent:
scaleGestureDetector.onTouchEvent(motionEvent)
```

---

## 🔄 State Management Patterns

### ViewModel → Compose → ARView
```kotlin
// ViewModel (commonMain)
class ARViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ARUiState())
    val uiState: StateFlow<ARUiState> = _uiState.asStateFlow()
    
    fun selectObject(objectId: String?) {
        _uiState.value = _uiState.value.copy(selectedObjectId = objectId)
    }
}

// Screen (commonMain)
@Composable
fun ARScreen(uiState: ARUiState, onSelectObject: (String?) -> Unit) {
    PlatformARView(
        modelPathToLoad = uiState.selectedObjectId?.let { id ->
            availableObjects.firstOrNull { it.id == id }?.modelUri
        }
    )
}

// ARView (androidMain)
@Composable
fun ARView(modelPathToLoad: String?) {
    val currentModelPath by rememberUpdatedState(modelPathToLoad)
    
    AndroidView(factory = { /* Use currentModelPath */ })
}
```

---

## 🧹 Lifecycle Management

### Proper Cleanup
```kotlin
DisposableEffect(Unit) {
    onDispose {
        arSceneView?.destroy()
        currentNodes.clear()
    }
}
```

### Node Management
```kotlin
LaunchedEffect(placedObjects, arSceneView) {
    val view = arSceneView ?: return@LaunchedEffect
    
    // Remove nodes for deleted objects
    val objectIds = placedObjects.map { it.objectId }.toSet()
    val idsToRemove = currentNodes.keys.filter { it !in objectIds }
    
    idsToRemove.forEach { id ->
        currentNodes[id]?.let { node ->
            view.removeChildNode(node)
        }
        currentNodes.remove(id)
    }
    
    // Add/update nodes for current objects
    placedObjects.forEach { obj ->
        if (!currentNodes.containsKey(obj.objectId)) {
            // Load and add new node
        } else {
            // Update existing node position/scale
        }
    }
}
```

---

## 📊 Logging Strategy

### Development Logs
```kotlin
private const val TAG = "ARView"

Log.d(TAG, "Model path updated: $currentModelPath")
Log.d(TAG, "Placing model on ${plane.type} plane at (${pose.tx()}, ${pose.ty()}, ${pose.tz()})")
Log.d(TAG, "Hit test distance: $distance meters")
```

### Production Logs (Keep Minimal)
```kotlin
Log.w(TAG, "Model file does not exist: $path")
Log.e(TAG, "Failed to load model: ${e.message}", e)
Log.e(TAG, "Hit test failed: ${e.message}", e)
```

---

## ⚡ Performance Tips

1. **Model Loading**
   - Always load on `Dispatchers.IO`
   - Cache `ModelInstance` references
   - Validate paths before loading

2. **Hit Testing**
   - Filter results for quality
   - Debounce tap events (300ms)
   - Early return on invalid frames

3. **Node Management**
   - Reuse nodes instead of recreating
   - Remove nodes when objects deleted
   - Use `remember` for node maps

4. **State Updates**
   - Use `rememberUpdatedState` (NOT `remember { mutableStateOf }`)
   - Collect StateFlow with `collectAsState()`
   - Avoid unnecessary recompositions

---

## 🐛 Common Pitfalls

### ❌ Pitfall 1: Forgetting rememberUpdatedState
```kotlin
// BAD - Closure captures stale callback
AndroidView(factory = { context ->
    ARSceneView(context).apply {
        onTouchEvent = { e, _ -> onEvent(data) }  // ❌ Stale
    }
})

// GOOD
val currentOnEvent by rememberUpdatedState(onEvent)
AndroidView(factory = { context ->
    ARSceneView(context).apply {
        onTouchEvent = { e, _ -> currentOnEvent(data) }  // ✅
    }
})
```

### ❌ Pitfall 2: Using delay() for State Sync
```kotlin
// BAD - Treats symptom, not cause
onObjectSelected = {
    onSelectObject(it)
    launch {
        delay(100)  // ❌ Doesn't fix AndroidView closure
        showModal = false
    }
}

// GOOD - Fix the actual issue
val currentOnModelPlaced by rememberUpdatedState(onModelPlaced)
```

### ❌ Pitfall 3: Not Validating Model Paths
```kotlin
// BAD - Will crash if file doesn't exist
val modelInstance = view.modelLoader.loadModelInstance(path)  // ❌

// GOOD
if (isValidModelPath(path)) {
    val modelInstance = view.modelLoader.loadModelInstance(path)  // ✅
}
```

---

## 📚 Quick Links

- [ARCore Developer Guide](https://developers.google.com/ar/develop)
- [SceneView GitHub](https://github.com/SceneView/sceneview-android)
- [Compose AndroidView Docs](https://developer.android.com/jetpack/compose/interop/interop-apis)
- [rememberUpdatedState Docs](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#rememberUpdatedState)

---

**Keep this reference handy for all AR development!**
