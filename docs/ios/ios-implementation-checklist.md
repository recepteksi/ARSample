# iOS Implementation Checklist & Testing Plan

**Project:** ARSample - iOS AR Integration
**Agent:** iOS Expert Agent (Rapor)
**Date:** 2026-03-31

---

## PHASE 1: CRITICAL FIXES (Must Complete)

### 1.1 Parameter Passing Bridge

- [ ] **Update PlatformARView.ios.kt**
  - [ ] Add `placedObjects: List<PlacedObject>` parameter to actual function
  - [ ] Add `modelPathToLoad: String?` parameter to actual function
  - [ ] Pass both parameters to ARViewWrapper call
  - [ ] Verify Kotlin compilation succeeds
  - **File:** `/composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/PlatformARView.ios.kt`

- [ ] **Update ARViewWrapper.kt function signature**
  - [ ] Add `placedObjects: List<PlacedObject>` parameter
  - [ ] Add `modelPathToLoad: String? = null` parameter
  - [ ] Add `var remember` state for tracking these inputs
  - [ ] Verify function compiles without errors
  - **File:** `/composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/ARViewWrapper.kt` (Line 17)

### 1.2 Model Path Implementation

- [ ] **Replace hardcoded model path**
  - [ ] Remove: `onModelPlaced("default_model.usdz", x, y, z)`
  - [ ] Add: Pass actual selected model path (from `selectedObject?.modelUri`)
  - [ ] Verify onModelPlaced callback receives correct path
  - **File:** `/composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/ARViewWrapper.kt` (Line 42)

### 1.3 iOS Configuration

- [ ] **Update Info.plist with required permissions**
  - [ ] Add NSCameraUsageDescription key
  - [ ] Add UIRequiredDeviceCapabilities array with "arkit"
  - [ ] Verify plist is valid XML (no syntax errors)
  - [ ] Validate against iOS requirements
  - **File:** `/iosApp/iosApp/Info.plist`

### 1.4 Parameter Propagation Flow

- [ ] **Verify data flow from ARScreen**
  - [ ] Check ARScreen passes placedObjects to PlatformARView
  - [ ] Check ARScreen passes modelPathToLoad to PlatformARView
  - [ ] Trace to ensure parameters reach ARViewWrapper
  - [ ] Test with debug logging
  - **File:** `/composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/ui/screens/ARScreen.kt` (Lines 79-89)

---

## PHASE 2: FUNCTIONALITY RESTORATION (Critical)

### 2.1 Persisted Objects Restoration

- [ ] **Implement LaunchedEffect for object restoration**
  - [ ] Add LaunchedEffect(Unit) to restore persisted objects on first render
  - [ ] Call restorePersistedObjects() function
  - [ ] Handle errors gracefully (skip failed objects)
  - [ ] Test with 5+ previously placed objects
  - **Location:** ARViewWrapper.kt, after remember state initialization

- [ ] **Implement object visibility sync**
  - [ ] Add LaunchedEffect(placedObjects) to sync changes
  - [ ] Update map of rendered anchors
  - [ ] Handle object removal (detach from scene)
  - [ ] Test add/remove operations

### 2.2 Dynamic Model Loading

- [ ] **Implement model loading on selection**
  - [ ] Add LaunchedEffect(modelPathToLoad)
  - [ ] Load model when user selects from object list
  - [ ] Show loading indicator during load
  - [ ] Handle errors (file not found, invalid format)
  - [ ] Verify correct model appears on tap

- [ ] **Test model loading with different formats**
  - [ ] Test with USDZ files (native)
  - [ ] Test with GLB files (if conversion implemented)
  - [ ] Test with corrupted files (error handling)
  - [ ] Verify models load within acceptable time (<500ms)

### 2.3 AR Session Lifecycle

- [ ] **Implement proper session management**
  - [ ] Initialize ARWorldTrackingConfiguration on startup
  - [ ] Configure plane detection (horizontal + vertical)
  - [ ] Configure environment texturing
  - [ ] Pause session in onDispose
  - [ ] Test session pause/resume on app background

---

## PHASE 3: ENHANCED HIT TESTING

### 3.1 Modern Raycast API

- [ ] **Replace deprecated hitTest()**
  - [ ] Find all uses of ARHitTestResult...ExistingPlaneUsingExtent
  - [ ] Replace with ARRaycastQuery + raycast() pattern
  - [ ] Use ARRaycastTarget.existingPlaneUsingGeometry
  - [ ] Test on real device (hit testing requires actual planes)

- [ ] **Verify position extraction**
  - [ ] Extract x, y, z from matrix_float4x4 columns[3]
  - [ ] Test coordinate system consistency
  - [ ] Verify z-flip if needed for Vector3 model

### 3.2 Gesture Improvements

- [ ] **Enhance tap handler**
  - [ ] Add validation that model is selected before placing
  - [ ] Add feedback (visual or haptic) on tap
  - [ ] Handle edge cases (tapping off-plane)

- [ ] **Add optional: pinch and pan gestures**
  - [ ] Implement UIPinchGestureRecognizer for scaling
  - [ ] Implement UIPanGestureRecognizer for moving
  - [ ] Update model transform based on gestures

---

## PHASE 4: TESTING & VALIDATION

### 4.1 Unit Testing

- [ ] **Test parameter passing**
  - [ ] Mock ARViewWrapper with test data
  - [ ] Verify placedObjects parameter received
  - [ ] Verify modelPathToLoad parameter received

- [ ] **Test coordinate transformation**
  - [ ] Test Vector3 to SIMD3 conversion
  - [ ] Test ARKit matrix extraction
  - [ ] Verify coordinate system alignment

### 4.2 Integration Testing

- [ ] **Test on physical device**
  - [ ] Build and run on iPhone 6S+ (A9 minimum)
  - [ ] Test on iOS 13+ version
  - [ ] Verify camera permission flow
  - [ ] Check for ARKit availability errors

- [ ] **Test full user flow**
  - [ ] Launch app → grant camera permission
  - [ ] Wait for plane detection (should see plane outline)
  - [ ] Import or select model
  - [ ] Tap on detected plane to place model
  - [ ] Verify model appears at correct position
  - [ ] Close app and reopen → model still visible
  - [ ] Remove model → disappears from scene

### 4.3 Edge Cases

- [ ] **Test error scenarios**
  - [ ] Tap before model selected (should do nothing)
  - [ ] Tap before plane detected (should not crash)
  - [ ] Select model that doesn't exist (should handle gracefully)
  - [ ] Insufficient memory (show error message)
  - [ ] Device without ARKit support (show appropriate error)

- [ ] **Test performance**
  - [ ] Measure model load time (<500ms target)
  - [ ] Monitor memory usage (stay under 300MB)
  - [ ] Check frame rate (maintain 60 FPS)
  - [ ] Test with 10, 50, 100 placed objects

### 4.4 Device Testing Matrix

| Device | iOS Version | ARKit Support | Test Status |
|--------|-------------|---------------|-------------|
| iPhone 6S | 13.0+ | ✓ Basic | [ ] Pass/Fail |
| iPhone 7+ | 13.0+ | ✓ Basic | [ ] Pass/Fail |
| iPhone 12 | 14.0+ | ✓ + LiDAR | [ ] Pass/Fail |
| iPhone 13+ | 15.0+ | ✓ + LiDAR | [ ] Pass/Fail |
| iPad Air 2+ | 13.0+ | ✓ Basic | [ ] Pass/Fail |
| iPad Pro | 13.0+ | ✓ + LiDAR | [ ] Pass/Fail |

---

## PHASE 5: BUILD & DEPLOYMENT

### 5.1 Build Configuration

- [ ] **Gradle build**
  - [ ] Run `./gradlew :composeApp:assembleDebug` (Android check)
  - [ ] Verify iOS framework generates in build output
  - [ ] Check for compilation warnings

- [ ] **Xcode project**
  - [ ] Open `iosApp/iosApp.xcodeproj` in Xcode
  - [ ] Verify all targets build successfully
  - [ ] Check for framework linking errors
  - [ ] Verify Code Signing is configured

### 5.2 Release Build

- [ ] **Generate release build**
  - [ ] Update version number in Info.plist
  - [ ] Clean derived data
  - [ ] Run full build (Product → Build)
  - [ ] Generate archive for distribution
  - [ ] Test on multiple devices before release

### 5.3 Documentation

- [ ] **Update project documentation**
  - [ ] Document iOS-specific features (LiDAR, plane detection)
  - [ ] Add troubleshooting guide
  - [ ] Document minimum iOS version requirement (13.0)
  - [ ] Add device compatibility list

---

## PHASE 6: OPTIONAL ENHANCEMENTS

### 6.1 Advanced Features

- [ ] **LiDAR Support**
  - [ ] Detect LiDAR availability
  - [ ] Enable depth-based improvements
  - [ ] Show LiDAR badge in UI for supported devices

- [ ] **Model Animations**
  - [ ] Load animations from USDZ files
  - [ ] Provide play/pause controls
  - [ ] Support animation selection

- [ ] **Physics Simulation**
  - [ ] Add gravity to placed objects
  - [ ] Implement object collision detection
  - [ ] Test stability with complex scenes

### 6.2 UI Improvements

- [ ] **AR Session State Indicator**
  - [ ] Show status (Initializing, Running, Error)
  - [ ] Display plane detection status
  - [ ] Show model loading progress

- [ ] **User Feedback**
  - [ ] Haptic feedback on successful placement
  - [ ] Visual feedback on hit detection
  - [ ] Toast messages for errors

---

## DEBUG LOGGING CHECKLIST

Add these logging points for troubleshooting:

```kotlin
// In ARViewWrapper.kt
Log.d("ARView", "ARViewWrapper initialized")
Log.d("ARView", "Received placedObjects: ${placedObjects.size}")
Log.d("ARView", "modelPathToLoad: $modelPathToLoad")
Log.d("ARView", "Session state: ${arView?.session?.currentFrame != null}")

// In hit testing
Log.d("ARView", "Raycast performed from: $screenPoint")
Log.d("ARView", "Hit result: ${result?.worldTransform?.columns?.get(3)}")
Log.d("ARView", "Model placed at position: ($x, $y, $z)")

// In model loading
Log.d("ARView", "Loading model: $filePath")
Log.d("ARView", "Model loaded successfully")
Log.d("ARView", "Model load failed: ${e.message}")
```

---

## VERIFICATION STEPS

After implementation, verify:

1. **Code Compiles**
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

2. **Project Builds in Xcode**
   - Open iosApp/iosApp.xcodeproj
   - Select target "iosApp"
   - Run Product → Build (or Cmd+B)

3. **App Launches on Device**
   - Select physical iOS device in Xcode
   - Click Play button to run
   - Wait for app to launch

4. **Parameters Are Passed**
   - Add temporary Log.d() statements
   - Check Xcode console for debug messages
   - Verify placedObjects and modelPathToLoad received

5. **AR Session Starts**
   - After launching, point device at flat surface
   - Look for plane visualization
   - Plane outlines should appear in light blue

6. **Model Selection Works**
   - Tap "Import" or select from object list
   - Selected model should be marked in UI
   - Selected model name shown in control panel

7. **Placement Works**
   - Select a model
   - Tap on detected plane
   - Model should appear at tap location
   - Close and reopen app → model persists

---

## COMMON ISSUES & SOLUTIONS

### Issue: "ARKit not available"
**Solution:**
- Check UIRequiredDeviceCapabilities includes "arkit"
- Verify testing on iOS 13+ device
- Check device is iPhone 6S or newer

### Issue: Plane detection not working
**Solution:**
- Move device slowly in circular motions
- Point at flat surfaces (floor, wall, table)
- Ensure adequate lighting
- Check ARWorldTrackingConfiguration.planeDetection is set

### Issue: Model appears black/invisible
**Solution:**
- Call `generateCollisionShapes()` after loading
- Check model file is valid USDZ
- Verify model is not scaled to 0
- Check z-position is not behind camera

### Issue: App crashes on permission denied
**Solution:**
- Add NSCameraUsageDescription to Info.plist
- Handle permission check before AR session
- Show user-friendly error message

### Issue: Memory pressure with many objects
**Solution:**
- Limit placed objects to ~50
- Unload distant objects
- Implement object pooling for frequently used models
- Monitor memory in Instruments

---

## SIGN-OFF CHECKLIST

When implementation is complete:

- [ ] All critical fixes implemented
- [ ] Code compiles without errors
- [ ] Builds in Xcode successfully
- [ ] Runs on physical iOS device
- [ ] Camera permission works
- [ ] Planes are detected
- [ ] Models can be placed
- [ ] Models persist after restart
- [ ] No runtime crashes observed
- [ ] Performance meets targets
- [ ] Documentation updated

---

**Implementation Owner:** [Assign Person]
**Code Review Required:** [ ] Yes [ ] No
**Testing Approval:** [ ] Pass [ ] Fail [ ] Pending

**Sign-off Date:** ____________
**Verified By:** ____________

---

Generated: 2026-03-31 | iOS Expert Agent (Rapor)
**Status:** Ready for Implementation
