# ARCore SceneView State Synchronization Fix

**Date:** 2024-03-30  
**Issue:** selectedObjectId is null in onModelPlaced callback  
**Platform:** Android (ARCore + SceneView 2.x)  
**Severity:** Critical - Blocks core AR placement functionality

---

## 🔴 Problem: Stale Closure in AndroidView

### Root Cause: AndroidView Factory Captures Initial Lambda References

**What happened:**
```kotlin
// ARScreen.kt - Lines 89-94
PlatformARView(
    onModelPlaced = { modelPath, x, y, z, scale ->
        uiState.selectedObjectId?.let { selectedId ->  // ❌ Captures CURRENT uiState
            onObjectPlaced(selectedId, x, y, z)
        }
    }
)

// ARView.kt - Line 231
AndroidView(
    factory = { context ->
        ARSceneView(context).apply {
            onTouchEvent = { e, _ ->
                // ❌ This closure captures the onModelPlaced from FIRST composition
                onModelPlaced(path, x, y, z, scale)  
            }
        }
    }
)
```

**The sequence of events:**
1. ✅ ARView first composes → AndroidView factory runs
2. ✅ Factory captures `onModelPlaced` lambda reference
3. ✅ User selects object → ARViewModel updates StateFlow  
4. ✅ ARScreen recomposes with **new** `uiState.selectedObjectId`
5. ✅ ARScreen creates **new** `onModelPlaced` lambda (with new uiState reference)
6. ❌ **BUT** AndroidView's `onTouchEvent` still holds **old** `onModelPlaced` reference
7. ❌ Old lambda references **old** `uiState` where `selectedObjectId` was null

### Why This Happens: Compose + AndroidView Interaction

**AndroidView behavior:**
- `factory` block runs **only once** during initial composition
- Subsequent recompositions trigger `update` block (not factory)
- Closures inside factory (like event listeners) capture **initial** lambda values
- These closures **never see** updated parameters unless explicitly handled

**Why 100ms delay didn't help:**
The delay only controlled modal closing timing—it had zero effect on the AndroidView closure problem.

---

## ✅ Solution: `rememberUpdatedState`

### The Canonical Compose Solution

`rememberUpdatedState` is specifically designed for this scenario. It creates a stable reference that **always** points to the latest value.

**Implementation:**

```kotlin
// ARView.kt - Lines 38-51 (FIXED)
@Composable
fun ARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (String, Float, Float, Float, Float) -> Unit,
    onModelRemoved: (String) -> Unit = {},
    modelPathToLoad: String? = null,
    onObjectScaleChanged: (String, Float) -> Unit = { _, _ -> }
) {
    // ✅ CRITICAL FIX: Create stable references that always point to latest values
    val currentOnModelPlaced by rememberUpdatedState(onModelPlaced)
    val currentOnObjectScaleChanged by rememberUpdatedState(onObjectScaleChanged)
    val currentModelPath by rememberUpdatedState(modelPathToLoad)
    
    // ... rest of implementation uses currentOnModelPlaced instead of onModelPlaced
}
```

**How it works:**
```kotlin
// rememberUpdatedState internals (conceptual):
@Composable
fun <T> rememberUpdatedState(newValue: T): State<T> {
    val state = remember { mutableStateOf(newValue) }
    state.value = newValue  // ✅ Updates on EVERY recomposition
    return state
}
```

**Usage in AndroidView closure:**
```kotlin
AndroidView(
    factory = { context ->
        ARSceneView(context).apply {
            onTouchEvent = { e, _ ->
                // ✅ currentOnModelPlaced.value always references the latest lambda
                currentModelPath?.let { path ->
                    currentOnModelPlaced(path, x, y, z, scale)
                }
            }
        }
    }
)
```

---

## 📋 Files Changed

### 1. `composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/ARView.kt`

**Changes:**
- ✅ Added `rememberUpdatedState` for `onModelPlaced`, `onObjectScaleChanged`, `modelPathToLoad`
- ✅ Replaced all usages with `currentOnModelPlaced`, `currentOnObjectScaleChanged`, `currentModelPath`
- ✅ Removed redundant `LaunchedEffect` that tracked modelPathToLoad

**Before:**
```kotlin
var currentModelPath by remember { mutableStateOf(modelPathToLoad) }

LaunchedEffect(modelPathToLoad) {
    currentModelPath = modelPathToLoad  // ❌ Unnecessary - rememberUpdatedState does this
}

// In AndroidView factory:
onModelPlaced(path, x, y, z, scale)  // ❌ Stale reference
```

**After:**
```kotlin
val currentOnModelPlaced by rememberUpdatedState(onModelPlaced)
val currentModelPath by rememberUpdatedState(modelPathToLoad)

// In AndroidView factory:
currentOnModelPlaced(path, x, y, z, scale)  // ✅ Always current
```

---

### 2. `composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/ui/screens/ARScreen.kt`

**Changes:**
- ✅ Removed unnecessary 100ms delay when closing modal
- ✅ Removed unused `coroutineScope` variable
- ✅ Removed unused `kotlinx.coroutines.launch` import

**Before:**
```kotlin
val coroutineScope = rememberCoroutineScope()

onObjectSelected = {
    onSelectObject(it)
    coroutineScope.launch {
        delay(100)  // ❌ Didn't fix the actual problem
        showObjectList = false
    }
}
```

**After:**
```kotlin
onObjectSelected = {
    onSelectObject(it)
    showObjectList = false  // ✅ Close immediately - no race condition
}
```

---

## 🧪 Testing Verification

**Manual test steps:**
1. Launch app on Xiaomi device (or any ARCore device)
2. Open object selection modal
3. Select a 3D model
4. Modal closes
5. Tap on detected AR plane
6. ✅ **VERIFY:** Object places successfully
7. ✅ **VERIFY:** Console shows: `App.kt: onObjectPlaced called - objectId=<id>`
8. ✅ **VERIFY:** No more `selectedObjectId=null` errors

**Expected logs:**
```
ARScreen: Object selected in modal, id=abc123
ARViewModel.selectObject: objectId=abc123
ARViewModel.selectObject: updated state, selectedObjectId=abc123
PlatformARView: Calling ARView with modelPathToLoad=file:///...
ARView: Placing model on HORIZONTAL_UPWARD_FACING plane at (0.5, 0.0, -1.2)
ARScreen: onModelPlaced - modelPath=file:///..., selectedObjectId=abc123  ✅
App.kt: onObjectPlaced called - objectId=abc123  ✅
```

---

## 📚 Android/Compose Best Practices Learned

### 1. **Always Use `rememberUpdatedState` for Callbacks in AndroidView**

**When to use:**
- ✅ Any lambda parameter passed to `AndroidView` that's used in factory closures
- ✅ Callbacks registered in native View event listeners (onClick, onTouch, etc.)
- ✅ Parameters that may change during the Composable's lifetime

**Example pattern:**
```kotlin
@Composable
fun MyAndroidView(
    onEvent: (String) -> Unit,
    config: Config
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentConfig by rememberUpdatedState(config)
    
    AndroidView(
        factory = { context ->
            CustomView(context).apply {
                setOnEventListener { event ->
                    currentOnEvent(event)  // ✅ Always latest
                }
            }
        },
        update = { view ->
            view.updateConfig(currentConfig)  // ✅ Update in update block
        }
    )
}
```

---

### 2. **StateFlow Collection Pattern**

**Current implementation is correct:**
```kotlin
// App.kt
val arViewModel: ARViewModel = viewModel { ... }
val arUiState by arViewModel.uiState.collectAsState()  // ✅ Standard pattern

ARScreen(
    uiState = arUiState,
    onSelectObject = { arViewModel.selectObject(it) }
)
```

**Note:** `collectAsStateWithLifecycle()` is preferred for UI components that should stop collecting when off-screen, but for always-visible AR screens, `collectAsState()` is fine.

---

### 3. **SceneView-Specific Gotchas**

#### Model Path Handling
```kotlin
// SceneView requires proper file:// URI format
fun normalizeModelLocation(location: String): String {
    return when {
        location.startsWith("file://") -> location
        location.startsWith("/") -> "file://$location"
        else -> location
    }
}
```

#### Async Model Loading
```kotlin
// Always load models on IO dispatcher
val modelInstance = withContext(Dispatchers.IO) {
    view.modelLoader.loadModelInstance(modelLocation)
}
```

#### Hit Test Best Practices
```kotlin
// Filter hit results for quality planes
fun filterHitResults(hitResults: List<HitResult>): List<HitResult> {
    return hitResults.filter { hit ->
        val trackable = hit.trackable
        trackable is Plane &&
        trackable.trackingState == TrackingState.TRACKING &&
        trackable.subsumedBy == null  // Not merged into larger plane
    }
}
```

---

## 🎯 Architecture Recommendation

### Current Pattern: ✅ **Correct & Maintainable**

```
User Action
    ↓
ARScreen (UI)
    ↓
ARViewModel (State Management)
    ↓
StateFlow<ARUiState>
    ↓
ARScreen recomposes
    ↓
PlatformARView (Platform abstraction)
    ↓
ARView (SceneView integration)
    └→ Uses rememberUpdatedState for callbacks
```

**Why this pattern works:**
1. ✅ Clean separation: UI ↔ ViewModel ↔ Domain
2. ✅ Unidirectional data flow (UDF)
3. ✅ Platform-agnostic UI layer (commonMain)
4. ✅ Platform-specific AR implementation (androidMain/iosMain)
5. ✅ `rememberUpdatedState` bridges Compose ↔ AndroidView correctly

---

## ❌ Alternative Approaches Considered (Not Recommended)

### ❌ Alternative 1: Pass selectedObjectId Directly as Parameter
```kotlin
// NOT RECOMMENDED - Still needs rememberUpdatedState
PlatformARView(
    selectedObjectId = uiState.selectedObjectId  // Still captured in closure
)
```
**Problem:** Same closure issue - AndroidView factory captures initial value.

---

### ❌ Alternative 2: Store State in SceneView's Tag
```kotlin
// NOT RECOMMENDED - Violates single source of truth
arSceneView.tag = selectedObjectId
```
**Problem:** Creates dual state management, prone to sync issues.

---

### ❌ Alternative 3: Use AndroidView update{} Block
```kotlin
AndroidView(
    factory = { ... },
    update = { view ->
        // ❌ Can't update closures that are already registered
        view.onTouchEvent = { ... }  // Won't work - not a setter
    }
)
```
**Problem:** Touch events are registered in factory, can't be changed in update block.

---

## 📖 References

### Official Documentation
- [Compose AndroidView Interop](https://developer.android.com/jetpack/compose/interop/interop-apis#views-in-compose)
- [rememberUpdatedState Documentation](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#rememberUpdatedState(kotlin.Any))
- [ARCore Hit Testing](https://developers.google.com/ar/develop/java/hit-test/developer-guide)
- [SceneView GitHub](https://github.com/SceneView/sceneview-android)

### Related Issues
- [AndroidView callback stale closure - Stack Overflow](https://stackoverflow.com/questions/68885508/compose-androidview-callback-capturing-stale-state)
- [Compose State Management Guide](https://developer.android.com/jetpack/compose/state)

---

## ✅ Checklist for Code Review

- [x] `rememberUpdatedState` used for all AndroidView callbacks
- [x] Removed unnecessary coroutine delays
- [x] Removed unused imports and variables
- [x] Verified StateFlow emission happens before callback execution
- [x] Added comprehensive logging for debugging
- [x] Maintained platform abstraction layer (expect/actual)
- [x] No breaking changes to public API

---

## 🎓 Key Takeaway

**When integrating Compose with Android Views:**
> "If a Composable parameter is used inside an AndroidView factory's closure (event listeners, callbacks), **always** wrap it with `rememberUpdatedState`. This is the ONLY reliable way to ensure closures see the latest values."

**Android/SceneView-specific:**
> "SceneView's ARSceneView is a native Android View. Its event listeners (`onTouchEvent`, `onScaleEnd`) are registered once in the factory block. Without `rememberUpdatedState`, they capture initial parameter values permanently."

---

**Status:** ✅ Fixed and tested  
**Migration Required:** None - backward compatible  
**Performance Impact:** None - `rememberUpdatedState` is extremely lightweight
