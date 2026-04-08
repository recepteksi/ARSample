# ARCore Best Practices - Quick Reference

**Platform:** Android ARCore 1.48.0 + SceneView 2.1.0  
**Last Updated:** 2026-03-30

---

## 🎯 Hit Testing Golden Rules

### ✅ DO

```kotlin
// 1. Use firstOrNull with filtering
val validHit = hitResults.firstOrNull { hit ->
    hit.distance in 0.2f..3.0f && 
    (hit.trackable is DepthPoint || hit.trackable is Plane)
}

// 2. Always create Anchor
val anchor = validHit?.createAnchor()
val anchorNode = AnchorNode(engine, anchor!!)

// 3. Check Plane tracking state
(trackable as? Plane)?.let { plane ->
    plane.trackingState == TrackingState.TRACKING &&
    plane.isPoseInPolygon(hit.hitPose)
}

// 4. Prioritize hit result types
when (trackable) {
    is DepthPoint -> 1    // Highest accuracy
    is Plane -> 2         // Good accuracy
    is Point -> 3         // Medium accuracy
    is InstantPlacement -> 4  // Fast but low accuracy
}

// 5. Cleanup anchors on dispose
override fun onDispose() {
    anchors.forEach { it.detach() }
    anchors.clear()
}
```

### ❌ DON'T

```kotlin
// 1. Don't use pose directly (WRONG)
val pose = hit.hitPose
onModelPlaced(pose.tx(), pose.ty(), pose.tz())

// 2. Don't use forEach (WRONG - inefficient)
hitResults.forEach { hit ->
    // Processing all results even after finding valid one
}

// 3. Don't skip tracking state check (WRONG)
(trackable as? Plane)?.let { plane ->
    // Missing: plane.trackingState check
    val anchor = hit.createAnchor()
}

// 4. Don't ignore distance (WRONG)
hitResults.firstOrNull { hit ->
    hit.trackable is Plane  // Missing: distance check
}

// 5. Don't forget to detach anchors (WRONG - memory leak)
// Missing: anchor.detach() on cleanup
```

---

## 📐 Distance & Confidence Thresholds

| Metric | Min | Max | Reason |
|--------|-----|-----|--------|
| **Distance** | 0.2m | 3.0m | UX optimal, tracking reliable |
| **Plane Size** | 0.1m² | - | Too small planes unreliable |
| **Confidence** | 0.5 | 1.0 | Only medium-high confidence |
| **Anchor Count** | - | 50 | Performance limit |

---

## 🔄 Hit Result Priority Order

```kotlin
private fun findBestHitResult(hits: List<HitResult>): HitResult? {
    return hits.firstOrNull { hit ->
        val distance = hit.distance
        if (distance !in 0.2f..3.0f) return@firstOrNull false
        
        when (val trackable = hit.trackable) {
            // Priority 1: Most accurate
            is DepthPoint -> true
            
            // Priority 2: Good for floors/walls
            is Plane -> {
                trackable.trackingState == TrackingState.TRACKING &&
                trackable.isPoseInPolygon(hit.hitPose)
            }
            
            // Priority 3: Feature points
            is Point -> trackable.trackingState == TrackingState.TRACKING
            
            // Priority 4: Fast but inaccurate
            is InstantPlacementPoint -> true
            
            else -> false
        }
    }
}
```

---

## 🎨 SceneView 2.x Patterns

### Model Loading

```kotlin
// Async load with null check
val modelInstance = withContext(Dispatchers.IO) {
    val file = File(modelPath)
    if (!file.exists()) return@withContext null
    view.modelLoader.loadModelInstance("file://$modelPath")
}

modelInstance?.let { instance ->
    val modelNode = ModelNode(instance).apply {
        scaleToUnits = 0.5f
        parent = anchorNode
    }
}
```

### Anchor Management

```kotlin
// Create and track
val anchor = hit.createAnchor()
val anchorNode = AnchorNode(engine = view.engine, anchor = anchor)
view.addChildNode(anchorNode)
anchors[anchorId] = anchor

// Cleanup
fun removeAnchor(anchorId: String) {
    anchors[anchorId]?.detach()
    anchors.remove(anchorId)
    currentNodes.remove(anchorId)
}
```

### Session Configuration

```kotlin
ARSceneView(context).apply {
    sessionConfiguration = { session, config ->
        // Essential settings
        config.planeFindingMode = PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        config.lightEstimationMode = LightEstimationMode.ENVIRONMENTAL_HDR
        
        // Optional: Depth (if supported)
        if (session.isDepthModeSupported(DepthMode.AUTOMATIC)) {
            config.depthMode = DepthMode.AUTOMATIC
        }
        
        // Optional: Instant placement fallback
        config.instantPlacementMode = InstantPlacementMode.LOCAL_Y_UP
    }
}
```

---

## 🐛 Common Pitfalls

### 1. Frame Null Check

```kotlin
// ❌ WRONG
val frame = this.frame!!  // Can crash

// ✅ CORRECT
val frame = this.frame ?: run {
    Log.w(TAG, "Frame is null")
    return@onTouchEvent true
}
```

### 2. Pose in Polygon Check

```kotlin
// ❌ WRONG - Pose might be outside plane
(trackable as? Plane)?.let { plane ->
    val anchor = hit.createAnchor()
}

// ✅ CORRECT
(trackable as? Plane)?.let { plane ->
    if (plane.isPoseInPolygon(hit.hitPose)) {
        val anchor = hit.createAnchor()
    }
}
```

### 3. Anchor Lifecycle

```kotlin
// ❌ WRONG - Memory leak
val anchor = hit.createAnchor()
// Never detached!

// ✅ CORRECT
val anchor = hit.createAnchor()
anchors[anchorId] = anchor

// Later...
override fun onDispose() {
    anchors.values.forEach { it.detach() }
}
```

### 4. Distance Filtering

```kotlin
// ❌ WRONG - Can place at 10 meters
hitResults.firstOrNull { hit.trackable is Plane }

// ✅ CORRECT
hitResults.firstOrNull { hit ->
    hit.distance in 0.2f..3.0f && hit.trackable is Plane
}
```

---

## ⚡ Performance Tips

### 1. Hit Test Debouncing

```kotlin
private var lastHitTestTime = 0L
private const val COOLDOWN_MS = 200L

if (System.currentTimeMillis() - lastHitTestTime < COOLDOWN_MS) {
    return@onTouchEvent true
}
lastHitTestTime = System.currentTimeMillis()
```

### 2. Model Instance Caching

```kotlin
private val modelCache = mutableMapOf<String, ModelInstance>()

suspend fun loadModel(path: String): ModelInstance? {
    return modelCache.getOrPut(path) {
        withContext(Dispatchers.IO) {
            view.modelLoader.loadModelInstance(path)
        }
    }
}
```

### 3. Frame Skip for Plane Updates

```kotlin
onSessionUpdated = { session, frame ->
    // Process every 3rd frame
    if (frame.timestamp % 3 == 0L) {
        val planes = frame.getUpdatedPlanes()
        // ... process
    }
}
```

### 4. Limit Anchor Count

```kotlin
private const val MAX_ANCHORS = 50

fun addAnchor(anchor: Anchor) {
    if (anchors.size >= MAX_ANCHORS) {
        // Remove oldest anchor
        anchors.values.first().detach()
        anchors.remove(anchors.keys.first())
    }
    anchors[anchor.hashCode().toString()] = anchor
}
```

---

## 📱 Device Compatibility

### ARCore Support Check

```kotlin
fun isARCoreSupportedAndInstalled(context: Context): Boolean {
    return when (ArCoreApk.getInstance().checkAvailability(context)) {
        ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
        ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
        ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
            // Request install
            ArCoreApk.getInstance().requestInstall(activity, true)
            false
        }
        else -> false
    }
}
```

### Depth Mode Support

```kotlin
sessionConfiguration = { session, config ->
    val depthSupported = session.isDepthModeSupported(DepthMode.AUTOMATIC)
    Log.d(TAG, "Depth mode supported: $depthSupported")
    
    if (depthSupported) {
        config.depthMode = DepthMode.AUTOMATIC
    }
}
```

---

## 🧪 Testing Checklist

### Unit Tests
- [ ] `findBestHitResult` - distance filtering
- [ ] `findBestHitResult` - plane tracking state
- [ ] `findBestHitResult` - priority order
- [ ] Anchor lifecycle (create/detach)

### Integration Tests
- [ ] Hit test returns valid result
- [ ] Anchor creates successfully
- [ ] Model attaches to anchor
- [ ] Cleanup removes all anchors

### Manual QA
- [ ] Place on floor (horizontal)
- [ ] Place on wall (vertical)
- [ ] Reject too close (<20cm)
- [ ] Reject too far (>3m)
- [ ] Model stays anchored (not drifting)

---

## 📚 Quick Links

- [ARCore Hit Testing Guide](https://developers.google.com/ar/develop/java/hit-test/developer-guide)
- [ARCore Anchors](https://developers.google.com/ar/develop/anchors)
- [SceneView GitHub](https://github.com/SceneView/sceneview-android)
- [ARCore Samples](https://github.com/google-ar/arcore-android-sdk)

---

**Print this and keep next to your desk! 📋**
