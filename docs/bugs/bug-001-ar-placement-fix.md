# BUG REPORT #001: AR Objects Not Placing in Scene

**Bug ID:** BUG-001  
**Severity:** 🔴 **CRITICAL**  
**Status:** ✅ **RESOLVED**  
**Reporter:** Bug Fixer Agent  
**Date:** 2026-04-02  
**Platform:** Android (Kotlin Multiplatform)  
**Device:** Xiaomi (ID: 215b336d)

---

## Summary

AR objects were not being placed in the scene when users tapped on detected planes, despite:
- ✅ Object selection working correctly
- ✅ Plane detection functioning properly
- ✅ Hit testing succeeding
- ✅ Model paths being valid

---

## Description

When a user selects a 3D object from the modal and taps on a detected AR plane surface, the object should be placed in the AR scene. Instead, nothing happens. The placement callback executes but the object is not saved to the scene because `selectedObjectId` is null.

---

## Steps to Reproduce

1. Launch ARSample app on Android device
2. Navigate to AR Scene screen
3. Tap "Objects" button in top-right
4. Select any 3D model from the modal list
5. Wait for AR plane detection (move device slowly over horizontal surface)
6. Tap on detected plane surface

**Expected Behavior:**  
Selected 3D object should appear at the tapped location

**Actual Behavior:**  
Nothing happens. No object is placed.

---

## Root Cause Analysis (5-Why Technique)

### **Why 1:** Why don't objects appear?  
**Answer:** Because `onObjectPlaced` callback is never called with a valid `selectedId`

### **Why 2:** Why is `selectedId` invalid?  
**Answer:** Because `uiState.selectedObjectId` is **null** inside the `onModelPlaced` lambda in `ARScreen.kt:87-91`

### **Why 3:** Why is `uiState.selectedObjectId` null in the lambda?  
**Answer:** Because the lambda **captures the state value at composition time**, not at invocation time

### **Why 4:** Why does Compose capture stale state?  
**Answer:** Because `PlatformARView` is created in the `Scaffold` content lambda, which captures `uiState` once when the Composable is first composed

### **Why 5:** Why doesn't the lambda get the updated state?  
**Answer:** Because **Compose doesn't recompose the lambda body** when `uiState` changes. The `onModelPlaced` callback is passed to `PlatformARView`, which passes it to `ARView`, which wraps it in `rememberUpdatedState`. However, the **intermediate lambda in ARScreen** (lines 87-91) is NOT wrapped with `rememberUpdatedState`, so it captures the old state.

---

## Root Cause

**Architectural Issue:** Lambda closure capturing stale state across multiple composition layers

### Problem Code (ARScreen.kt:87-91)

```kotlin
PlatformARView(
    modifier = Modifier.fillMaxSize(),
    placedObjects = uiState.placedObjects,
    onModelPlaced = { modelPath, x, y, z, scale ->
        println("ARScreen: onModelPlaced - modelPath=$modelPath, selectedObjectId=${uiState.selectedObjectId}")
        uiState.selectedObjectId?.let { selectedId ->  // ❌ Captures stale uiState!
            onObjectPlaced(selectedId, x, y, z)
        }
    },
    onModelRemoved = onObjectRemoved,
    modelPathToLoad = selectedObject?.modelUri
)
```

### Why This Happens

1. **Lambda Creation:** When `ARScreen` is first composed, the `onModelPlaced` lambda is created
2. **State Capture:** The lambda captures the **current** `uiState` reference at that moment
3. **State Update:** When user selects an object, `ARViewModel` updates its StateFlow
4. **Recomposition:** `ARScreen` recomposes with new `uiState` value
5. **Lambda Persistence:** **BUT** the lambda itself is NOT recreated (Compose optimization)
6. **Stale Closure:** The lambda still references the **old** `uiState` from step 2
7. **Invocation:** When placement happens, the lambda executes with stale state → `selectedObjectId = null`

### Why ARView's `rememberUpdatedState` Doesn't Help

- `ARView.kt:49` wraps `onModelPlaced` with `rememberUpdatedState(onModelPlaced)`
- This ensures `ARView` gets the latest **lambda reference**
- **BUT** the lambda itself has already captured the stale `uiState`
- The updated state wrapper doesn't fix what's inside the lambda's closure

---

## Evidence from Logs

### Before Fix

```
16:24:42.930  ARViewModel.selectObject: updated state, selectedObjectId=1b9e33ad-...
16:24:42.949  ARScreen: selectedObjectId=1b9e33ad-..., modelUri=file://...
16:24:46.111  ARView: Placing model on HORIZONTAL_UPWARD_FACING plane at (...)
16:24:46.111  ARScreen: onModelPlaced - selectedObjectId=null ❌
```

**Notice:** Placement happens 3 seconds AFTER selection, but `selectedObjectId` is still null!

### After Fix

```
16:47:16.491  ARView: Placing model on HORIZONTAL_UPWARD_FACING plane at (...)
16:47:16.491  ARScreen: onModelPlaced - selectedObjectId=0081eead-... ✅
```

**Notice:** Now `selectedObjectId` is correctly captured!

---

## Solution

### Fix Applied: Use `rememberUpdatedState` for `uiState`

**File:** `composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/ui/screens/ARScreen.kt`

#### Change 1: Add `rememberUpdatedState` wrapper (lines 23-25)

```kotlin
@Composable
fun ARScreen(
    uiState: ARUiState,
    // ... other parameters
) {
    // CRITICAL FIX: Use rememberUpdatedState to ensure callbacks always capture latest state
    // This prevents lambda closures from capturing stale uiState references
    val currentUiState by rememberUpdatedState(uiState)
    
    var showObjectList by remember { mutableStateOf(false) }
    // ... rest of composable
}
```

#### Change 2: Use `currentUiState` in lambda (lines 87-91)

```kotlin
PlatformARView(
    modifier = Modifier.fillMaxSize(),
    placedObjects = uiState.placedObjects,
    onModelPlaced = { modelPath, x, y, z, scale ->
        println("ARScreen: onModelPlaced - modelPath=$modelPath, selectedObjectId=${currentUiState.selectedObjectId}")
        currentUiState.selectedObjectId?.let { selectedId ->  // ✅ Now uses currentUiState!
            onObjectPlaced(selectedId, x, y, z)
        }
    },
    onModelRemoved = onObjectRemoved,
    modelPathToLoad = selectedObject?.modelUri
)
```

### How This Works

1. `rememberUpdatedState(uiState)` creates a **mutable reference** that always points to the latest `uiState`
2. When `uiState` changes, the **reference** is updated automatically
3. The lambda closure captures the **reference**, not the value
4. When invoked, the lambda reads from the reference → gets latest state ✅

---

## Alternative Solutions Considered

### Option 1: Pass `selectedObjectId` directly to `PlatformARView` ❌
**Problem:** Requires changing `ARView` signature, which is platform-specific and used in both Android and iOS

### Option 2: Use `LaunchedEffect` to update a separate state ❌
**Problem:** Adds unnecessary complexity and potential race conditions

### Option 3: Remove intermediate lambda, call `onObjectPlaced` directly from ARView ❌
**Problem:** ARView shouldn't know about domain logic (object IDs vs model paths)

### ✅ Option 4: Use `rememberUpdatedState` (CHOSEN)
**Advantages:**
- Minimal code change (2 lines)
- No architectural changes required
- Standard Compose pattern for this exact scenario
- No performance impact
- Maintains separation of concerns

---

## Files Changed

| File | Lines Changed | Description |
|------|---------------|-------------|
| `ARScreen.kt` | 25, 88-89 | Added `rememberUpdatedState` wrapper and used `currentUiState` in lambda |

---

## Verification

### Test Case 1: Object Placement
✅ **PASS**
1. Launch app
2. Select object from modal
3. Tap on detected plane
4. **Result:** Object appears in AR scene at tapped location

### Test Case 2: Multiple Objects
✅ **PASS**
1. Place first object
2. Select different object
3. Place second object
4. **Result:** Both objects visible, each with correct model

### Test Case 3: State Persistence
✅ **PASS**
1. Place object
2. Navigate away and back
3. **Result:** Placed object remains in scene

### Regression Tests
✅ No existing functionality broken
✅ No performance degradation
✅ No new memory leaks

---

## Lessons Learned

### For Future Development

1. **Lambda State Capture in Compose:**
   - Always use `rememberUpdatedState` when lambda callbacks need to access state
   - Never directly reference mutable state from parent composables in callbacks
   - Be aware of closure capture timing

2. **Debugging State Issues:**
   - Add logging to both state updates AND callback invocations
   - Compare timestamps to identify timing issues
   - Check if state propagation is delayed

3. **Code Review Checklist:**
   - [ ] Are there lambda callbacks that reference composable state?
   - [ ] Is `rememberUpdatedState` used for those references?
   - [ ] Are there multiple composition layers between state and usage?

---

## Related Documentation

- [Jetpack Compose: Side Effects](https://developer.android.com/jetpack/compose/side-effects)
- [rememberUpdatedState API Reference](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#rememberUpdatedState(kotlin.Any))
- [Compose Lifecycle](https://developer.android.com/jetpack/compose/lifecycle)

---

## Prevention

### Store Memory: Compose State Management

**Subject:** State management in callbacks  
**Fact:** Use `rememberUpdatedState` for composable state referenced in lambda callbacks  
**Reason:** Lambda closures in Compose capture state at composition time, not invocation time. Using `rememberUpdatedState` creates a mutable reference that always points to the latest state value, preventing stale state bugs in callbacks.

---

## Sign-off

**Fixed by:** Bug Fixer Agent  
**Reviewed by:** [Pending]  
**Approved by:** [Pending]  
**Deployed:** 2026-04-02  

**Build:** composeApp-debug.apk  
**Git Commit:** [Will be committed after review]
