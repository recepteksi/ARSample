# AR Hit Testing - Implementation Guide

**Konkret kod örnekleri ve adım adım uygulama rehberi**

---

## 1. iOS ARKit Raycast Migration

### ❌ Mevcut Kod (Deprecated)

```kotlin
// composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/ARViewWrapper.kt
val results = view.hitTest(location, ARHitTestResultTypeExistingPlaneUsingExtent)
```

**Sorun:** `hitTest(_:types:)` iOS 14'ten beri deprecated.

---

### ✅ Yeni Kod (Modern Raycast)

```kotlin
// composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/ARViewWrapper.kt

@OptIn(ExperimentalForeignApi::class)
@Composable
fun ARViewWrapper(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onPlacementFailed: ((String) -> Unit)? = null  // ← NEW: Error callback
) {
    var arView by remember { mutableStateOf<ARView?>(null) }
    
    val tapHandler = remember {
        TapHandler { gesture ->
            arView?.let { view ->
                // Validate model selection first
                if (modelPathToLoad == null) {
                    onPlacementFailed?.invoke("Please select an object first")
                    return@TapHandler
                }
                
                val location = gesture.locationInView(view)
                
                // ✅ Modern raycast API
                val query = ARRaycastQueryFromPoint(
                    origin = location,
                    allowing = ARRaycastTargetEstimatedPlane,  // Fast placement
                    alignment = ARRaycastTargetAlignmentAny     // Horizontal + Vertical
                )
                
                val results = view.raycastQueryFromPoint(
                    point = location,
                    allowing = ARRaycastTargetEstimatedPlane,
                    alignment = ARRaycastTargetAlignmentAny
                )
                
                if (results.isNotEmpty()) {
                    // Get first result
                    (results.firstOrNull() as? ARRaycastResult)?.let { result ->
                        try {
                            val transform = result.worldTransform
                            
                            // Extract position
                            val x = transform.columns[3].x
                            val y = transform.columns[3].y
                            val z = transform.columns[3].z
                            
                            println("iOS: Placing model at x=$x, y=$y, z=$z")
                            
                            // ✅ Trigger haptic feedback
                            triggerHapticFeedback()
                            
                            // Call placement callback
                            onModelPlaced(modelPathToLoad, x, y, z)
                            
                        } catch (e: Exception) {
                            println("Error extracting transform: ${e.message}")
                            onPlacementFailed?.invoke("Failed to place object")
                        }
                    }
                } else {
                    // No surface detected
                    println("No surface detected at tap location")
                    onPlacementFailed?.invoke("No surface detected. Move device to scan.")
                }
            }
        }
    }
    
    // ... rest of the code
}

// Helper function for haptic feedback
@OptIn(ExperimentalForeignApi::class)
private fun triggerHapticFeedback() {
    val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyleMedium)
    generator.prepare()
    generator.impactOccurred()
}
```

**Değişiklikler:**
1. ✅ `hitTest` → `raycastQueryFromPoint` migration
2. ✅ Model validation eklendi
3. ✅ Haptic feedback eklendi
4. ✅ Error callback eklendi
5. ✅ `estimatedPlane` kullanımı (hızlı placement)

---

## 2. Android Hit Testing İyileştirmeleri

### ✅ Güncellenmiş Kod

```kotlin
// composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/ARView.kt

@Composable
fun ARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onPlacementFailed: ((String) -> Unit)? = null  // ← NEW: Error callback
) {
    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    val currentNodes = remember { mutableMapOf<String, ModelNode>() }
    val hapticFeedback = LocalHapticFeedback.current
    
    // ✅ Tap debouncing
    var lastPlacementTime by remember { mutableStateOf(0L) }
    val PLACEMENT_DEBOUNCE_MS = 500L
    
    // ... existing code ...
    
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
                        // ✅ Debounce check
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastPlacementTime < PLACEMENT_DEBOUNCE_MS) {
                            Log.d(TAG, "Ignoring tap (debounce)")
                            return@onTouchEvent true
                        }
                        
                        // ✅ Model validation
                        if (modelPathToLoad == null) {
                            Log.w(TAG, "No model selected")
                            onPlacementFailed?.invoke("Please select an object first")
                            return@onTouchEvent true
                        }
                        
                        try {
                            // Get current frame
                            val frame = try {
                                this.frame ?: return@onTouchEvent true
                            } catch (ex: Exception) {
                                Log.e(TAG, "Frame unavailable: ${ex.message}")
                                onPlacementFailed?.invoke("AR session not ready")
                                return@onTouchEvent true
                            }

                            // Perform hit test
                            val hitResults = try {
                                frame.hitTest(e.x, e.y)
                            } catch (ex: Exception) {
                                Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                                onPlacementFailed?.invoke("Hit test failed")
                                return@onTouchEvent true
                            }

                            // Find first plane hit
                            hitResults.forEach { hit ->
                                (hit.trackable as? Plane)?.let { plane ->
                                    // ✅ Filter by plane type (optional)
                                    if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING ||
                                        plane.type == Plane.Type.VERTICAL) {
                                        
                                        val pose = hit.hitPose
                                        Log.d(TAG, "Model placed at: x=${pose.tx()}, y=${pose.ty()}, z=${pose.tz()}")
                                        
                                        // ✅ Update debounce time
                                        lastPlacementTime = currentTime
                                        
                                        // ✅ Trigger haptic feedback
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        
                                        // Call placement callback
                                        onModelPlaced(modelPathToLoad, pose.tx(), pose.ty(), pose.tz())
                                        return@onTouchEvent true
                                    }
                                }
                            }

                            // ✅ No plane detected - inform user
                            if (hitResults.isEmpty()) {
                                Log.d(TAG, "No plane detected at touch location")
                                onPlacementFailed?.invoke("No surface detected. Move device to scan.")
                            } else {
                                Log.d(TAG, "Hit result is not a plane")
                                onPlacementFailed?.invoke("Tap on a detected surface")
                            }
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Unexpected error during hit test: ${e.message}", e)
                            onPlacementFailed?.invoke("Unexpected error occurred")
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
```

**İyileştirmeler:**
1. ✅ Tap debouncing (500ms)
2. ✅ Model selection validation
3. ✅ Haptic feedback
4. ✅ Plane type filtering
5. ✅ Error callbacks
6. ✅ Detaylı logging

---

## 3. ARScreen UI Updates

### ✅ Error Feedback Entegrasyonu

```kotlin
// composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/ui/screens/ARScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARScreen(
    uiState: ARUiState,
    availableObjects: List<ARObject>,
    onSelectObject: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onImportObject: (uri: String, name: String, type: ModelType) -> Unit,
    onObjectPlaced: (objectId: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onObjectRemoved: (placedObjectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showObjectList by remember { mutableStateOf(false) }
    var showPlacedObjects by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    // ✅ NEW: Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val selectedObject = remember(uiState.selectedObjectId, availableObjects) {
        uiState.selectedObjectId?.let { id -> availableObjects.firstOrNull { it.id == id } }
    }

    Scaffold(
        // ... existing topBar ...
        
        // ✅ NEW: Snackbar host
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Platform-specific AR View
            PlatformARView(
                modifier = Modifier.fillMaxSize(),
                placedObjects = uiState.placedObjects,
                onModelPlaced = { _, x, y, z ->
                    uiState.selectedObjectId?.let { selectedId ->
                        onObjectPlaced(selectedId, x, y, z)
                    }
                },
                onModelRemoved = onObjectRemoved,
                modelPathToLoad = selectedObject?.modelUri,
                
                // ✅ NEW: Error callback
                onPlacementFailed = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            // ✅ NEW: Coaching overlay when no planes
            if (uiState.planesDetected == 0 && !uiState.isLoading) {
                ARCoachingOverlay(
                    message = "Move your device to detect surfaces",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // ✅ NEW: Instruction banner
            if (uiState.selectedObjectId == null && uiState.planesDetected > 0) {
                InstructionBanner(
                    message = "Select an object to place",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
            
            // ... rest of existing UI ...
        }
    }
}

// ✅ NEW: Coaching overlay component
@Composable
fun ARCoachingOverlay(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated scanning icon
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
            
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Point camera at floor or walls",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ✅ NEW: Instruction banner
@Composable
fun InstructionBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
```

---

## 4. ViewModel Updates

### ✅ AR State Management

```kotlin
// composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/viewmodel/ARViewModel.kt

data class ARUiState(
    val placedObjects: List<PlacedObject> = emptyList(),
    val selectedObjectId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val planesDetected: Int = 0,  // ✅ NEW: Track plane count
    val arSessionReady: Boolean = false  // ✅ NEW: Track AR state
)

class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase,
    private val getSceneUseCase: GetSceneUseCase,
    // ... other dependencies
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ARUiState())
    val uiState: StateFlow<ARUiState> = _uiState.asStateFlow()
    
    // ✅ NEW: Update plane count
    fun updatePlaneCount(count: Int) {
        _uiState.update { it.copy(planesDetected = count) }
    }
    
    // ✅ NEW: Update AR session state
    fun updateARSessionState(ready: Boolean) {
        _uiState.update { it.copy(arSessionReady = ready) }
    }
    
    fun placeObject(objectId: String, posX: Float, posY: Float, posZ: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // ✅ NEW: Validate AR session
                if (!_uiState.value.arSessionReady) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "AR session not ready. Please wait."
                        )
                    }
                    return@launch
                }
                
                val position = Position(posX, posY, posZ)
                placeObjectUseCase(objectId, position, Rotation.IDENTITY, Scale.IDENTITY)
                
                // Reload scene
                val scene = getSceneUseCase()
                _uiState.update { 
                    it.copy(
                        placedObjects = scene.placedObjects,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to place object"
                    )
                }
            }
        }
    }
    
    // ... rest of existing methods
}
```

---

## 5. Platform AR View Expect/Actual

### ✅ Updated Interface

```kotlin
// composeApp/src/commonMain/kotlin/com/trendhive/arsample/ar/PlatformARView.kt

@Composable
expect fun PlatformARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onPlacementFailed: ((String) -> Unit)? = null,  // ✅ NEW
    onPlaneCountChanged: ((Int) -> Unit)? = null,   // ✅ NEW
    onARSessionReady: ((Boolean) -> Unit)? = null   // ✅ NEW
)
```

### ✅ Android Implementation

```kotlin
// composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/PlatformARView.kt

@Composable
actual fun PlatformARView(
    modifier: Modifier,
    placedObjects: List<PlacedObject>,
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit,
    modelPathToLoad: String?,
    onPlacementFailed: ((String) -> Unit)?,
    onPlaneCountChanged: ((Int) -> Unit)?,
    onARSessionReady: ((Boolean) -> Unit)?
) {
    ARView(
        modifier = modifier,
        placedObjects = placedObjects,
        onModelPlaced = onModelPlaced,
        onModelRemoved = onModelRemoved,
        modelPathToLoad = modelPathToLoad,
        onPlacementFailed = onPlacementFailed,
        onPlaneCountChanged = onPlaneCountChanged,
        onARSessionReady = onARSessionReady
    )
}
```

### ✅ iOS Implementation

```kotlin
// composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/PlatformARView.kt

@Composable
actual fun PlatformARView(
    modifier: Modifier,
    placedObjects: List<PlacedObject>,
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit,
    modelPathToLoad: String?,
    onPlacementFailed: ((String) -> Unit)?,
    onPlaneCountChanged: ((Int) -> Unit)?,
    onARSessionReady: ((Boolean) -> Unit)?
) {
    ARViewWrapper(
        modifier = modifier,
        placedObjects = placedObjects,
        onModelPlaced = onModelPlaced,
        onModelRemoved = onModelRemoved,
        modelPathToLoad = modelPathToLoad,
        onPlacementFailed = onPlacementFailed,
        onPlaneCountChanged = onPlaneCountChanged,
        onARSessionReady = onARSessionReady
    )
}
```

---

## 6. Testing

### ✅ Unit Test Example

```kotlin
// composeApp/src/commonTest/kotlin/HitTestManagerTest.kt

class HitTestManagerTest {
    
    @Test
    fun `should debounce rapid taps`() {
        val manager = HitTestManager(debounceMs = 500)
        
        // First tap should be processed
        assertTrue(manager.shouldProcessTap())
        
        // Immediate second tap should be ignored
        Thread.sleep(100)
        assertFalse(manager.shouldProcessTap())
        
        // After debounce period, should be processed again
        Thread.sleep(450)
        assertTrue(manager.shouldProcessTap())
    }
    
    @Test
    fun `should validate model selection`() {
        val validator = PlacementValidator()
        
        // No model selected
        val result1 = validator.validate(selectedModel = null, planeDetected = true)
        assertFalse(result1.isValid)
        assertEquals("Please select an object first", result1.errorMessage)
        
        // Model selected but no plane
        val result2 = validator.validate(selectedModel = "cube.glb", planeDetected = false)
        assertFalse(result2.isValid)
        assertEquals("No surface detected", result2.errorMessage)
        
        // Valid case
        val result3 = validator.validate(selectedModel = "cube.glb", planeDetected = true)
        assertTrue(result3.isValid)
    }
}
```

---

## 7. Migration Checklist

### iOS Migration
- [ ] Replace `hitTest` with `raycastQueryFromPoint`
- [ ] Add `onPlacementFailed` callback
- [ ] Add haptic feedback
- [ ] Test on real device (iOS 15+)

### Android Improvements
- [ ] Add tap debouncing
- [ ] Add model validation
- [ ] Add haptic feedback
- [ ] Add error callbacks
- [ ] Test on different devices

### Common UI
- [ ] Add coaching overlay
- [ ] Add instruction banner
- [ ] Add snackbar for errors
- [ ] Update ViewModel for AR state
- [ ] Update PlatformARView interface

### Testing
- [ ] Write unit tests
- [ ] Manual testing on both platforms
- [ ] Performance profiling
- [ ] User acceptance testing

---

**Status:** ✅ Ready for Implementation  
**Estimated Time:** 12-16 hours  
**Priority:** 🔴 High
