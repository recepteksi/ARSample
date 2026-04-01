# iOS Implementation Quick Reference Guide

## Critical Fixes Required

### 1. PlatformARView.ios.kt (Line 8-22)
**Current Problem:** Parameters not passed to ARViewWrapper

**Required Change:**
```kotlin
@Composable
actual fun PlatformARView(
    modifier: Modifier,
    placedObjects: List<PlacedObject>,
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit,
    modelPathToLoad: String?
) {
    // ADD THESE TWO PARAMETERS:
    ARViewWrapper(
        modifier = modifier,
        placedObjects = placedObjects,          // ← ADD THIS
        modelPathToLoad = modelPathToLoad,      // ← ADD THIS
        onModelPlaced = onModelPlaced,
        onModelRemoved = onModelRemoved
    )
}
```

---

### 2. ARViewWrapper.kt (Line 17-21)
**Current Problem:** Missing parameters in function signature

**Required Changes:**
```kotlin
@Composable
fun ARViewWrapper(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject>,           // ← ADD THIS
    modelPathToLoad: String? = null,             // ← ADD THIS
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {}
) {
    // Implementation changes needed...
}
```

---

### 3. ARViewWrapper.kt (Line 42)
**Current Problem:** Hardcoded model path

**Replace This:**
```kotlin
onModelPlaced("default_model.usdz", x, y, z)
```

**With This:**
```kotlin
// Use the selected model from parent Compose state
selectedObject?.modelUri?.let { modelPath ->
    onModelPlaced(modelPath, x, y, z)
}
```

---

### 4. Info.plist
**Current Problem:** Missing camera permission and AR device requirements

**Add These Keys:**
```xml
<key>NSCameraUsageDescription</key>
<string>Camera access is required to use AR features for placing and viewing 3D objects in your environment</string>

<key>UIRequiredDeviceCapabilities</key>
<array>
    <string>arkit</string>
</array>
```

---

## RealityKit API Quick Reference

### Model Loading
```kotlin
// USDZ (Native, Preferred)
val entity = ModelEntity.loadModel(contentsOf = nsURL)

// Setup collision
entity.generateCollisionShapes(recursive = true)

// Add to scene
val anchor = AnchorEntity(plane: .horizontal)
anchor.addChild(entity)
arView.scene.addAnchor(anchor)
```

### Hit Testing (Deprecated → Modern)
```kotlin
// ❌ OLD (Deprecated)
val results = arView.hitTest(location, ARHitTestResultTypeExistingPlaneUsingExtent)

// ✓ NEW (Recommended)
val query = arView.raycastQuery(
    from = screenPoint,
    allowing = ARRaycastTarget.existingPlaneUsingGeometry,
    alignment = ARRaycastQueryAlignment.horizontal
)
val result = arView.session.raycast(query).firstOrNull()
```

### Transform Access
```kotlin
// Extract position from matrix_float4x4
val transform = hitResult.worldTransform
val x = transform.columns[3].x
val y = transform.columns[3].y
val z = transform.columns[3].z
```

---

## Data Flow Architecture

```
User Actions:
├─ Select Model → ARViewModel.selectObject() → ARScreen(uiState.selectedObject)
├─ Place Model → ARViewWrapper.onTap → ARViewModel.placeObject() → ARUiState.placedObjects
└─ Remove Model → ARViewWrapper.onRemove → ARViewModel.removeObject() → ARUiState.placedObjects

Re-render Cycle:
├─ ARScreen receives updated uiState.placedObjects
├─ PlatformARView receives placedObjects + modelPathToLoad
├─ ARViewWrapper restores objects from placedObjects list
└─ User sees synchronized AR scene

Persistence:
└─ ARViewModel sends updates to Repository → Database/LocalStorage
```

---

## Key Kotlin/Native Patterns

### @OptIn(ExperimentalForeignApi::class)
Required for all direct Objective-C interop code in ARViewWrapper.

### @Export("methodName:")
Used to expose Kotlin methods as Objective-C selectors for gesture handlers.
```kotlin
class TapHandler : NSObject() {
    @Export("handleTap:")
    fun handleTap(gesture: UITapGestureRecognizer) { ... }
}
```

### Remember & LaunchedEffect
- `remember` - Survive recomposition
- `LaunchedEffect(Unit)` - Run once on first render
- `LaunchedEffect(modelPathToLoad)` - Re-run when parameter changes

---

## Coordinate System

ARKit uses column-major matrix layout:
```
matrix_float4x4 {
    columns[0] = right vector (X axis)
    columns[1] = up vector (Y axis)
    columns[2] = back vector (-Z direction)
    columns[3] = position (X, Y, Z, W=1.0)
}
```

**Consistency Check:**
- ARKit Z-forward = -Vector3 Z-forward (account for sign flip)

---

## Testing on Real Device

Requirements:
- iPhone 6S or newer (A9 chip minimum)
- iOS 13+
- Physical device (ARKit not in simulator for plane detection)

Procedure:
1. Build: `xcode iosApp/iosApp.xcodeproj`
2. Grant camera permission when prompted
3. Tap plane to place model
4. Check Console for ARKit session state messages

---

## Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "ARKit not available" | Old device or iOS < 13 | Check UIRequiredDeviceCapabilities |
| "Camera permission denied" | User declined | Check NSCameraUsageDescription in Info.plist |
| Model appears black | Missing collision shapes | Call `generateCollisionShapes()` |
| Model jitters | No plane detection | Ensure `planeDetection` is enabled in config |
| App crashes on orientationChange | AR session not paused | Implement lifecycle management |

---

## Memory Optimization Tips

1. **Don't load all models at once** - Load on demand when selected
2. **Use smaller USDZ files** - Pre-optimize 3D assets
3. **Dispose AR session on deactivate** - Call `arView.session.pause()` in onDispose
4. **Batch raycast queries** - Don't perform on every touch event
5. **Limit placed objects** - Consider cap around 50-100 active entities

---

## Next Steps After Critical Fixes

1. **Restore placed objects on app restart**
   - Load from persistence in initial LaunchedEffect
   - Call restorePlacedObjects() with snapshots

2. **Load new models dynamically**
   - Detect modelPathToLoad changes
   - Use ARModelLoader.loadModelForARView()
   - Show loading indicator to user

3. **Improve error handling**
   - Define ARSessionState enum
   - Update UI based on state (error message, permissions, loading)

4. **Upgrade hit testing**
   - Replace deprecated hitTest() with raycast()
   - Add multitouch support (pinch to scale, pan to move)

---

## Reference Files Location

- Main implementation: `/composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/`
- Configuration: `/iosApp/iosApp/Info.plist`
- Entry point: `/iosApp/iosApp/ContentView.swift`
- Data models: `/composeApp/src/commonMain/kotlin/com/trendhive/arsample/domain/model/`
- ViewModel: `/composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/viewmodel/ARViewModel.kt`

---

Generated: 2026-03-31 | iOS Expert Agent (Rapor)
