# AR Obje Yerleştirme - Hit Testing Tasarım Dokümanı

**Proje:** ARSample - 3D Obje Ekleme/Çıkarma  
**Platform:** Kotlin Multiplatform (Android + iOS)  
**Tarih:** 2026-03-30  
**Versiyon:** 1.0

---

## Executive Summary

Bu doküman, AR uygulamasında kullanıcıların ekrana dokunarak 3D objeleri gerçek dünya yüzeylerine yerleştirmesi için **hit testing** (raycast) implementasyon stratejisini tanımlar. Android (ARCore) ve iOS (ARKit) platformları için unified bir yaklaşım sunar.

**Mevcut Durum:**
- ✅ Android ARView.kt'de hit testing kodu mevcut (SceneView 2.x + ARCore)
- ⚠️ iOS ARViewWrapper.kt'de deprecated `hitTest` API kullanılıyor
- ✅ ViewModel katmanı hazır (placeObject fonksiyonu)
- ❌ Edge case handling eksik (plane detection yoksa, model null vs.)

**Hedef:**
- Modern ARKit raycast API'ye geçiş (iOS)
- Consistent UX her iki platformda
- Robust error handling
- Performance optimization

---

## 1. Hit Testing Fundamentals

### 1.1 Hit Testing Nedir?

**Hit testing** (ARCore) / **Raycasting** (ARKit), ekrandaki 2D dokunma noktasından gerçek dünyada 3D bir ray (ışın) oluşturarak, bu ışının hangi yüzeyle kesiştiğini bulma işlemidir.

```
User Touch (2D screen) → Ray (3D world) → Intersection (3D pose)
     ↓                       ↓                    ↓
  (x: 500, y: 800)    Origin + Direction    Plane @ (0.5, 0.0, -1.2)
```

**Amaç:** Kullanıcı dokunduğunda, o noktadaki gerçek dünya koordinatlarını (x, y, z) ve yönelimini (rotation) bulmak.

---

## 2. Android ARCore Hit Testing (Mevcut Durum)

### 2.1 Mevcut Implementasyon Analizi

**Dosya:** `composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/ARView.kt`  
**Satır:** 113-151

#### Kod Akışı
```kotlin
onTouchEvent = { e, _ ->
    if (e.action == MotionEvent.ACTION_UP) {
        val frame = this.frame ?: return@onTouchEvent true
        val hitResults = frame.hitTest(e.x, e.y)
        
        hitResults.forEach { hit ->
            (hit.trackable as? Plane)?.let { _ ->
                modelPathToLoad?.let { path ->
                    val pose = hit.hitPose
                    onModelPlaced(path, pose.tx(), pose.ty(), pose.tz())
                    return@onTouchEvent true
                }
            }
        }
        
        if (hitResults.isEmpty()) {
            Log.d(TAG, "No plane detected at touch location")
        }
    }
    true
}
```

#### Analiz

| Aspect | Durum | Değerlendirme |
|--------|-------|--------------|
| **API Versiyonu** | `frame.hitTest()` - Classic API | ✅ Stabil, SceneView 2.x ile uyumlu |
| **Hit Result Filtering** | Sadece `Plane` trackable'ı kabul ediyor | ✅ Doğru - yüzeyler için ideal |
| **Model Kontrolü** | `modelPathToLoad` null check var | ✅ Null safety sağlanmış |
| **Error Handling** | Try-catch blokları mevcut | ✅ Crash prevention var |
| **User Feedback** | Sadece log mesajı | ❌ Kullanıcıya görsel feedback YOK |
| **Multiple Hit Results** | İlk Plane'de durur (`return`) | ✅ Performans açısından doğru |

#### Güçlü Yönler ✅
1. **Robust error handling:** Frame null check, hit test exception handling
2. **Plane filtering:** Sadece yüzey (Plane) trackable'larını kabul eder
3. **Early return:** İlk geçerli hit'te durur (gereksiz loop yok)

#### İyileştirme Alanları ⚠️
1. **User feedback eksik:** Plane bulunmadığında kullanıcıya visual cue verilmiyor
2. **Model validation eksik:** Model dosyası mevcut mu kontrolü yok
3. **Haptic feedback:** Başarılı yerleştirmede titreşim yok
4. **Placement indicator:** Kullanıcı nereye tıklayacağını bilmiyor

---

### 2.2 ARCore Hit Result Types

ARCore 4 farklı hit result tipi döndürür (mesafe yakın → uzak sıralı):

| Tip | Açıklama | Use Case | Orientation |
|-----|----------|----------|-------------|
| **Depth** | LiDAR/Depth API ile 3D geometry | Her yüzey (duvar, masa, koltuk) | Surface normal'e dik |
| **Plane** | Tespit edilen horizontal/vertical yüzeyler | Zemin/duvar yerleştirme | Plane normal'e dik |
| **Feature Point** | Görsel özellik noktaları | Arbitrary surface yerleştirme | Estimated surface normal |
| **Instant Placement** | Hızlı, tahmini derinlik | UX için hızlı placement | Gravity'e zıt (+Y yukarı) |

**Mevcut kod sadece `Plane` kullanıyor** → ✅ Doğru tercih (en stabil)

**Öneri:** Plane bulunamazsa fallback olarak **Instant Placement** eklenebilir.

---

## 3. iOS ARKit Hit Testing (Mevcut Durum + Modernizasyon)

### 3.1 Mevcut Implementasyon Analizi

**Dosya:** `composeApp/src/iosMain/kotlin/com/trendhive/arsample/ar/ARViewWrapper.kt`  
**Satır:** 27-63

#### Mevcut Kod
```kotlin
val results = view.hitTest(location, ARHitTestResultTypeExistingPlaneUsingExtent)
if (results.isNotEmpty()) {
    (results.firstOrNull() as? ARHitTestResult)?.let { firstResult ->
        val transform = firstResult.worldTransform
        val x = transform.columns[3].x
        val y = transform.columns[3].y
        val z = transform.columns[3].z
        onModelPlaced(pathToLoad, x, y, z)
    }
}
```

#### Critical Issue ⚠️

**`hitTest(_:types:)` API'si iOS 14'ten itibaren DEPRECATED!**

Apple'ın önerisi: **`raycast(from:allowing:alignment:)` API'sine geçiş yapın.**

#### Deprecation Nedenleri
1. **Terminology:** "Hit test" çok genel, "raycast" daha açıklayıcı
2. **Capability:** Raycast API daha güçlü query options sunar
3. **Tracked Raycasts:** Sürekli güncellenen raycast desteği var (tracked placement)

---

### 3.2 Modern ARKit Raycast API (Önerilen)

#### 3.2.1 One-Shot Raycast (Tek Tıklama)

```swift
// Swift örneği (Kotlin/Native'e uyarlanmalı)
let results = arView.raycast(
    from: screenPoint,
    allowing: .estimatedPlane,  // veya .existingPlaneGeometry
    alignment: .horizontal       // veya .any
)

if let firstResult = results.first {
    let position = firstResult.worldTransform.columns.3
    placeObject(at: position)
}
```

**Kotlin/Native için:**
```kotlin
val results = arView.raycast(
    from = CGPointMake(location.x, location.y),
    allowing = ARRaycastTargetEstimatedPlane,  // veya ARRaycastTargetExistingPlaneGeometry
    alignment = ARRaycastTargetAlignmentHorizontal
)
```

#### 3.2.2 Tracked Raycast (Sürekli Güncelleme)

Kullanıcı bir yüzeyi drag ederken sürekli pozisyon güncellemesi için:

```swift
let trackedRaycast = arSession.trackedRaycast(
    from: screenPoint,
    allowing: .estimatedPlane,
    alignment: .horizontal
) { results in
    // Her frame'de pozisyon güncellemesi
    if let result = results.first {
        updateObjectPosition(result.worldTransform)
    }
}

// İptal etmek için:
trackedRaycast?.stopTracking()
```

**Use Case:** Obje taşıma (drag & drop) özelliği için ideal.

---

### 3.3 ARKit Raycast Target Types

| Target Type | Açıklama | Use Case |
|------------|----------|----------|
| **existingPlaneGeometry** | Tam tespit edilmiş plane'ler (yüksek güven) | Kesin yerleştirme gerekiyorsa |
| **existingPlaneInfinite** | Plane sınırlarının ötesi (extrapolation) | Geniş yüzeyler için |
| **estimatedPlane** | Tahmini yüzey (visual features based) | Hızlı placement, plane henüz yoksa |

**Öneri:** `estimatedPlane` kullan → Plane detection'ı beklemeden hızlı placement.

---

## 4. Unified Hit Testing Strategy

### 4.1 Platform Abstraction

Her iki platformda consistent davranış için ortak interface:

```kotlin
// commonMain
expect class ARHitTestResult {
    val position: Position3D
    val rotation: Quaternion?
    val trackableType: TrackableType
}

enum class TrackableType {
    PLANE_HORIZONTAL,
    PLANE_VERTICAL,
    FEATURE_POINT,
    ESTIMATED_PLANE
}

data class Position3D(val x: Float, val y: Float, val z: Float)
```

#### Android Implementation
```kotlin
// androidMain
actual class ARHitTestResult(private val hit: com.google.ar.core.HitResult) {
    actual val position: Position3D
        get() = Position3D(
            hit.hitPose.tx(),
            hit.hitPose.ty(),
            hit.hitPose.tz()
        )
    
    actual val trackableType: TrackableType
        get() = when (hit.trackable) {
            is Plane -> {
                val plane = hit.trackable as Plane
                if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING)
                    TrackableType.PLANE_HORIZONTAL
                else TrackableType.PLANE_VERTICAL
            }
            else -> TrackableType.FEATURE_POINT
        }
}
```

#### iOS Implementation
```kotlin
// iosMain
actual class ARHitTestResult(private val result: ARRaycastResult) {
    actual val position: Position3D
        get() {
            val transform = result.worldTransform
            return Position3D(
                transform.columns[3].x,
                transform.columns[3].y,
                transform.columns[3].z
            )
        }
    
    actual val trackableType: TrackableType
        get() = when (result.target) {
            ARRaycastTargetExistingPlaneGeometry -> TrackableType.PLANE_HORIZONTAL
            ARRaycastTargetEstimatedPlane -> TrackableType.ESTIMATED_PLANE
            else -> TrackableType.FEATURE_POINT
        }
}
```

---

### 4.2 Hit Testing Workflow (Her İki Platform)

```mermaid
graph TD
    A[User Touch Event] --> B{Model Selected?}
    B -->|No| C[Show "Select object first" toast]
    B -->|Yes| D[Perform Hit Test/Raycast]
    D --> E{Results Found?}
    E -->|No| F[Show "No surface detected" toast]
    E -->|Yes| G{First Result is Plane?}
    G -->|Yes| H[Extract Position/Rotation]
    G -->|No| I{Allow Feature Points?}
    I -->|Yes| H
    I -->|No| F
    H --> J[Validate Model File]
    J --> K{File Exists?}
    K -->|No| L[Show error toast]
    K -->|Yes| M[Play Haptic Feedback]
    M --> N[Call onModelPlaced]
    N --> O[Show placement success animation]
```

---

## 5. Edge Cases & Error Handling

### 5.1 Edge Case Matrix

| Scenario | Current Behavior | Proposed Behavior | Priority |
|----------|------------------|-------------------|----------|
| **Model not selected** | Placement ignored (silent) | Toast: "Select an object first" | 🔴 High |
| **No plane detected** | Log only | Toast + coaching overlay | 🔴 High |
| **Model file missing** | Crash on load | Error toast + remove from list | 🔴 High |
| **Multiple rapid taps** | Multiple objects placed | Debounce 500ms | 🟡 Medium |
| **Touch during loading** | May crash/undefined | Disable touch when loading | 🟡 Medium |
| **AR session not ready** | Null pointer exception | Queue action until ready | 🟡 Medium |
| **Low light conditions** | Poor tracking | Show "Improve lighting" message | 🟢 Low |
| **Device movement** | Jittery placement | Delay placement until stable | 🟢 Low |

---

### 5.2 Error Handling Implementation

#### 5.2.1 Model Selection Check
```kotlin
// ARScreen.kt - onModelPlaced callback
onModelPlaced = { _, x, y, z ->
    if (uiState.selectedObjectId == null) {
        // Show toast
        scope.launch {
            snackbarHostState.showSnackbar("Please select an object first")
        }
        return@PlatformARView
    }
    
    onObjectPlaced(uiState.selectedObjectId, x, y, z)
}
```

#### 5.2.2 Plane Detection Feedback (Android)
```kotlin
// ARView.kt
if (hitResults.isEmpty()) {
    Log.d(TAG, "No plane detected at touch location")
    
    // Visual feedback
    onPlacementFailed?.invoke("Move device to detect surfaces")
    
    // Show coaching overlay
    showCoachingOverlay = true
}
```

#### 5.2.3 File Validation
```kotlin
// Before loading model
suspend fun validateModelFile(path: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val file = File(path)
        if (!file.exists()) {
            Log.e(TAG, "Model file not found: $path")
            return@withContext false
        }
        
        if (file.length() == 0L) {
            Log.e(TAG, "Model file is empty: $path")
            return@withContext false
        }
        
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error validating model file: ${e.message}", e)
        false
    }
}
```

#### 5.2.4 Tap Debouncing
```kotlin
private var lastPlacementTime = 0L
private val PLACEMENT_DEBOUNCE_MS = 500L

fun onTapDetected(x: Float, y: Float) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastPlacementTime < PLACEMENT_DEBOUNCE_MS) {
        Log.d(TAG, "Ignoring tap (debounce)")
        return
    }
    lastPlacementTime = currentTime
    
    performHitTest(x, y)
}
```

---

## 6. UX Enhancements

### 6.1 Visual Feedback Components

#### 6.1.1 Placement Indicator (Reticle)

Kullanıcıya "nereye tıklayacağını" gösteren dinamik bir cursor:

**Android (SceneView):**
```kotlin
val reticleNode = ModelNode().apply {
    modelInstance = modelLoader.loadModelInstance("models/reticle.glb")
    isVisible = false
}

// Her frame'de hit test yap
onFrame = { frameTime ->
    val frame = this.frame ?: return@onFrame
    val center = Point(width / 2f, height / 2f)
    
    val hitResults = frame.hitTest(center.x, center.y)
    hitResults.firstOrNull { (it.trackable as? Plane) != null }?.let { hit ->
        reticleNode.position = Position(
            hit.hitPose.tx(),
            hit.hitPose.ty(),
            hit.hitPose.tz()
        )
        reticleNode.isVisible = true
    } ?: run {
        reticleNode.isVisible = false
    }
}
```

**iOS (ARKit):**
```swift
// ARView'da sürekli raycast
trackedRaycast = arSession.trackedRaycast(
    from: screenCenter,
    allowing: .estimatedPlane,
    alignment: .horizontal
) { results in
    guard let result = results.first else {
        reticleNode.isHidden = true
        return
    }
    
    reticleNode.isHidden = false
    reticleNode.simdWorldTransform = result.worldTransform
}
```

---

#### 6.1.2 Coaching Overlay

Kullanıcıya plane detection için guidance:

**Android (Custom Overlay):**
```kotlin
@Composable
fun ARCoachingOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_scan_phone),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Move your device to detect surfaces",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
```

**iOS (Native ARCoachingOverlayView):**
```kotlin
// UIKit bridge
val coachingOverlay = ARCoachingOverlayView()
coachingOverlay.session = arView.session
coachingOverlay.goal = ARCoachingGoalHorizontalPlane
coachingOverlay.activatesAutomatically = true
arView.addSubview(coachingOverlay)
```

---

#### 6.1.3 Haptic Feedback

Başarılı placement'ta:

**Android:**
```kotlin
val hapticFeedback = LocalHapticFeedback.current

fun onObjectPlaced() {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
}
```

**iOS:**
```kotlin
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyleMedium

val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyleMedium)
generator.prepare()

fun onObjectPlaced() {
    generator.impactOccurred()
}
```

---

#### 6.1.4 Placement Animation

Obje aniden belirir yerine smooth scale-in:

```kotlin
// ModelNode extension (Android)
fun ModelNode.animatePlacement(duration: Long = 300L) {
    scale = Scale(0f, 0f, 0f)
    
    animate(duration) {
        scale = Scale(1f, 1f, 1f)
    }
}

// RealityKit (iOS) - Scale animation
entity.scale = [0, 0, 0]
entity.move(
    to: Transform(scale: [1, 1, 1]),
    relativeTo: entity.parent,
    duration: 0.3,
    timingFunction: .easeOut
)
```

---

### 6.2 User Guidance

#### State-Based UI Messages

| AR State | User Instruction | Icon |
|----------|------------------|------|
| **Initializing** | "Starting AR session..." | Loading spinner |
| **No planes detected** | "Move your device to scan the area" | Phone tilt icon |
| **Plane detected, no model** | "Select an object to place" | Plus icon |
| **Model selected** | "Tap on a surface to place" | Tap hand icon |
| **Object placed** | "Tap to place more objects" | Checkmark |
| **Low light** | "Find a better lit area" | Light bulb icon |

**Implementation:**
```kotlin
// ARViewModel
sealed class ARState {
    object Initializing : ARState()
    object PlanesNotDetected : ARState()
    data class Ready(val planesCount: Int) : ARState()
    object ModelSelected : ARState()
    data class Error(val message: String) : ARState()
}

val arState: StateFlow<ARState>

// ARScreen.kt
when (uiState.arState) {
    is ARState.PlanesNotDetected -> {
        CoachingOverlay(message = "Move device to scan")
    }
    is ARState.Ready -> {
        if (uiState.selectedObjectId == null) {
            InstructionBanner(message = "Select an object")
        }
    }
}
```

---

## 7. Performance Optimization

### 7.1 Hit Testing Rate Limiting

Sürekli hit test yapmak yerine:

```kotlin
private var lastHitTestTime = 0L
private val HIT_TEST_INTERVAL_MS = 100L  // 10 FPS

fun shouldPerformHitTest(): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastHitTestTime < HIT_TEST_INTERVAL_MS) {
        return false
    }
    lastHitTestTime = now
    return true
}
```

### 7.2 Plane Detection Optimization

Plane detection gereksiz yere CPU tüketir:

**ARCore:**
```kotlin
// İlk plane detect edildikten sonra slow down
config.planeFindingMode = when {
    detectedPlanesCount == 0 -> Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
    detectedPlanesCount < 3 -> Config.PlaneFindingMode.HORIZONTAL
    else -> Config.PlaneFindingMode.DISABLED  // Enough planes
}
```

**ARKit:**
```swift
// Yeterli plane varsa detection'ı duraklat
if planeAnchors.count >= 3 {
    configuration.planeDetection = []
    session.run(configuration)
}
```

### 7.3 Memory Management

Placed objects çok olursa memory leak riski:

```kotlin
// Object pooling pattern
class ModelNodePool(private val maxSize: Int = 20) {
    private val availableNodes = mutableListOf<ModelNode>()
    
    fun acquire(modelPath: String): ModelNode? {
        return availableNodes.removeFirstOrNull() ?: createNew(modelPath)
    }
    
    fun release(node: ModelNode) {
        if (availableNodes.size < maxSize) {
            availableNodes.add(node)
        } else {
            node.destroy()
        }
    }
}
```

---

## 8. Testing Strategy

### 8.1 Unit Tests

```kotlin
class HitTestManagerTest {
    @Test
    fun `should return null when no plane detected`() {
        val results = emptyList<HitResult>()
        val manager = HitTestManager()
        
        val pose = manager.getPlacementPose(results)
        
        assertNull(pose)
    }
    
    @Test
    fun `should filter non-plane trackables`() {
        val results = listOf(
            mockHitResult(trackable = FeaturePoint()),
            mockHitResult(trackable = Plane())
        )
        
        val pose = manager.getPlacementPose(results)
        
        assertNotNull(pose)  // Should find the plane
    }
    
    @Test
    fun `should debounce rapid taps`() {
        val manager = HitTestManager(debounceMs = 500)
        
        assertTrue(manager.shouldProcessTap())   // 0ms
        assertFalse(manager.shouldProcessTap())  // 100ms
        // ... wait 500ms ...
        assertTrue(manager.shouldProcessTap())   // 600ms
    }
}
```

### 8.2 Integration Tests

**Android (Instrumented Test):**
```kotlin
@Test
fun testPlacement_whenPlaneDetected_objectIsPlaced() {
    // Given: AR session started, model selected
    launchARScreen()
    selectModel("cube.glb")
    waitForPlaneDetection(timeout = 5.seconds)
    
    // When: User taps on detected plane
    tapOnScreen(centerX, centerY)
    
    // Then: Object appears at correct position
    assertObjectPlaced()
    assertHapticFeedbackTriggered()
}
```

**iOS (XCTest):**
```swift
func testPlacement_withNoPlane_showsCoaching() {
    let arView = ARView()
    let wrapper = ARViewWrapper()
    
    // When: Session started but no planes
    wrapper.onModelPlaced = { _ in
        XCTFail("Should not place without plane")
    }
    
    // Then: Coaching overlay visible
    XCTAssertTrue(wrapper.coachingOverlay.isActive)
}
```

### 8.3 Manual Test Cases

| Test Case | Steps | Expected Result |
|-----------|-------|-----------------|
| **TC1: Basic placement** | 1. Start AR<br>2. Wait for plane<br>3. Select object<br>4. Tap plane | Object placed, haptic triggered |
| **TC2: No plane** | 1. Start AR<br>2. Tap immediately | Coaching overlay shows |
| **TC3: No model selected** | 1. Start AR<br>2. Tap plane without selecting | Toast: "Select object first" |
| **TC4: Rapid taps** | 1. Start AR<br>2. Tap rapidly 5 times | Only 1 object placed (debounce) |
| **TC5: Missing model file** | 1. Select object<br>2. Delete file from storage<br>3. Try to place | Error toast shown |

---

## 9. Implementation Roadmap

### Phase 1: Critical Fixes (Week 1)
- [ ] iOS: Migrate from deprecated `hitTest` to `raycast` API
- [ ] Add model selection validation (show toast if null)
- [ ] Implement tap debouncing (500ms)
- [ ] Add plane detection failure feedback

### Phase 2: UX Enhancements (Week 2)
- [ ] Implement placement indicator (reticle)
- [ ] Add haptic feedback on placement
- [ ] Create coaching overlay (Android custom + iOS native)
- [ ] Add placement success animation (scale-in)

### Phase 3: Edge Cases (Week 3)
- [ ] File validation before loading
- [ ] AR session state management
- [ ] Loading state handling (disable touch)
- [ ] Low light detection & feedback

### Phase 4: Performance (Week 4)
- [ ] Hit test rate limiting
- [ ] Plane detection optimization (disable after threshold)
- [ ] Object pooling for memory management
- [ ] Profile with Instruments (iOS) and Profiler (Android)

---

## 10. API Reference

### 10.1 Android ARCore

```kotlin
// Hit testing
val hitResults: List<HitResult> = frame.hitTest(x: Float, y: Float)

// Hit result properties
val pose: Pose = hitResult.hitPose
val x: Float = pose.tx()
val y: Float = pose.ty()
val z: Float = pose.tz()
val trackable: Trackable = hitResult.trackable

// Plane properties
val plane: Plane = trackable as Plane
val planeType: Plane.Type = plane.type  // HORIZONTAL_UPWARD_FACING, VERTICAL, ...
```

**Docs:** https://developers.google.com/ar/develop/hit-test

---

### 10.2 iOS ARKit

```kotlin
// Modern raycast (preferred)
val results: List<ARRaycastResult> = arView.raycast(
    from = CGPointMake(x, y),
    allowing = ARRaycastTargetEstimatedPlane,
    alignment = ARRaycastTargetAlignmentHorizontal
)

// Tracked raycast (continuous)
val trackedRaycast: ARTrackedRaycast = session.trackedRaycast(
    from = point,
    allowing = target,
    alignment = alignment,
    updateHandler = { results in
        // Called every frame
    }
)

// Extract position
val transform: matrix_float4x4 = result.worldTransform
val x = transform.columns[3].x
val y = transform.columns[3].y
val z = transform.columns[3].z
```

**Docs:** https://developer.apple.com/documentation/arkit/ar_hit_testing

---

## 11. Conclusion

Bu tasarım dokümanı, AR obje yerleştirme özelliği için kapsamlı bir rehber sunmaktadır:

**✅ Çözülen Sorunlar:**
- iOS deprecated API → Modern raycast migration
- Edge case handling → Robust error management
- Poor UX → Visual feedback + coaching
- Performance issues → Rate limiting + optimization

**📊 Metrikler (Hedef):**
- Placement success rate: >95%
- Time to first placement: <5 seconds
- User error rate: <10%
- Frame rate: >30 FPS (model visible)

**🔄 Sürekli İyileştirme:**
- User feedback collection (analytics)
- A/B testing (reticle vs. no reticle)
- Performance profiling (real devices)

---

## References

1. **ARCore Hit Testing:** https://developers.google.com/ar/develop/hit-test
2. **ARCore Performance:** https://developers.google.com/ar/develop/performance
3. **ARKit Raycast:** https://developer.apple.com/documentation/arkit/raycasting
4. **Reality Atlas Guide:** https://www.reality-atlas.com/learn/arkit-arcore-mobile-ar-development-guide
5. **ARKit Plane Detection:** https://developer.apple.com/documentation/arkit/tracking-and-visualizing-planes

---

**Hazırlayan:** Design & Analysis Agent  
**Onay:** Proje Mimarı  
**Durum:** ✅ Ready for Implementation
