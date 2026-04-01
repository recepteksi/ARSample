# AR Hit Testing - Quick Reference

**⚡ TL;DR:** Android'de hit testing çalışıyor ama UX feedback eksik. iOS deprecated API kullanıyor, modern raycast'e geçmeli.

---

## 🎯 Problem Statement

| Platform | Durum | Öncelik |
|----------|-------|---------|
| **Android** | ✅ Hit test çalışıyor<br>❌ User feedback eksik | 🟡 UX iyileştirme |
| **iOS** | ⚠️ Deprecated `hitTest` API<br>❌ Coaching overlay yok | 🔴 API migration gerekli |
| **Common** | ❌ Edge case handling zayıf<br>❌ Model validation yok | 🔴 Kritik |

---

## 📋 Implementation Checklist

### 🔴 Critical (Must Have)

- [ ] **iOS:** Migrate `hitTest` → `raycast` API
  ```kotlin
  // OLD (deprecated)
  view.hitTest(location, ARHitTestResultTypeExistingPlaneUsingExtent)
  
  // NEW (modern)
  arView.raycast(
      from = location,
      allowing = ARRaycastTargetEstimatedPlane,
      alignment = ARRaycastTargetAlignmentHorizontal
  )
  ```

- [ ] **Both:** Model selection validation
  ```kotlin
  if (uiState.selectedObjectId == null) {
      showToast("Please select an object first")
      return
  }
  ```

- [ ] **Both:** Tap debouncing (prevent spam)
  ```kotlin
  private var lastTapTime = 0L
  private val DEBOUNCE_MS = 500L
  
  if (currentTime - lastTapTime < DEBOUNCE_MS) return
  ```

- [ ] **Both:** File validation before loading
  ```kotlin
  suspend fun validateModelFile(path: String): Boolean {
      val file = File(path)
      return file.exists() && file.length() > 0
  }
  ```

---

### 🟡 Important (Should Have)

- [ ] **Both:** Placement reticle (shows where user will tap)
- [ ] **Both:** Haptic feedback on successful placement
- [ ] **Both:** Coaching overlay (guide user to scan surfaces)
- [ ] **Both:** Placement animation (smooth scale-in)
- [ ] **Both:** Visual feedback for "no plane detected"

---

### 🟢 Nice to Have

- [ ] **Both:** Low light warning
- [ ] **Both:** Device stability check
- [ ] **Both:** Tracked raycast for drag & drop
- [ ] **Performance:** Object pooling
- [ ] **Performance:** Plane detection throttling

---

## 🏗️ Architecture

### Current Flow
```
User Tap → Hit Test → Check Plane → Place Object
                ↓ (if fail)
              Silent failure ❌
```

### Proposed Flow
```
User Tap → Validate Model → Hit Test → Validate Result → Visual Feedback → Place Object
              ↓                 ↓            ↓                   ↓
          Toast error      Toast error   Haptic feedback   Scale animation
```

---

## 🎨 UX Components

### 1. Placement Indicator (Reticle)
- **What:** Floating cursor showing tap target
- **When:** Continuously visible when plane detected
- **How:** Tracked raycast from screen center

### 2. Coaching Overlay
- **What:** Fullscreen guide for scanning
- **When:** No planes detected for >3 seconds
- **How:** Android custom / iOS ARCoachingOverlayView

### 3. State-Based Instructions
| State | Message | Icon |
|-------|---------|------|
| Initializing | "Starting AR..." | ⏳ |
| No planes | "Move device to scan" | 📱↕️ |
| No model | "Select an object" | ➕ |
| Ready | "Tap to place" | 👆 |

---

## 🐛 Edge Cases

| Case | Detection | Handling |
|------|-----------|----------|
| No model selected | `selectedObjectId == null` | Toast + return early |
| No plane detected | `hitResults.isEmpty()` | Coaching overlay |
| File missing | `!File(path).exists()` | Error toast + remove from list |
| Rapid taps | `currentTime - lastTap < 500ms` | Ignore (debounce) |
| AR not ready | `frame == null` | Queue action |

---

## 📊 Success Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Placement success rate | ~60% (guess) | >95% |
| Time to first placement | ~15s | <5s |
| User errors (wrong tap) | High | <10% |
| Frame rate | ~30 FPS | >30 FPS |

---

## 🚀 Implementation Priority

### Week 1: Critical Fixes
1. iOS raycast API migration (4h)
2. Model validation (2h)
3. Tap debouncing (1h)
4. Basic error toasts (2h)

### Week 2: UX Polish
1. Reticle implementation (6h)
2. Haptic feedback (2h)
3. Coaching overlay (4h)
4. Placement animation (2h)

### Week 3: Edge Cases
1. File validation (3h)
2. AR state management (4h)
3. Loading state handling (2h)

### Week 4: Performance
1. Hit test rate limiting (2h)
2. Plane detection optimization (3h)
3. Profiling & tuning (4h)

**Total:** ~40 hours

---

## 🔗 Key Code Locations

| File | Lines | What |
|------|-------|------|
| `ARView.kt` (Android) | 113-151 | Hit testing logic |
| `ARViewWrapper.kt` (iOS) | 27-63 | Deprecated hit test |
| `ARScreen.kt` | 79-89 | PlatformARView usage |
| `ARViewModel.kt` | - | Place object callback |

---

## 📚 API Quick Reference

### Android ARCore
```kotlin
// Hit test
val hits = frame.hitTest(x, y)

// Get pose
val pose = hit.hitPose
val position = Triple(pose.tx(), pose.ty(), pose.tz())

// Check trackable
when (hit.trackable) {
    is Plane -> { /* handle plane */ }
    else -> { /* ignore */ }
}
```

### iOS ARKit (Modern)
```kotlin
// Raycast
val results = arView.raycast(
    from = CGPointMake(x, y),
    allowing = ARRaycastTargetEstimatedPlane,
    alignment = ARRaycastTargetAlignmentHorizontal
)

// Get position
val transform = result.worldTransform
val position = Triple(
    transform.columns[3].x,
    transform.columns[3].y,
    transform.columns[3].z
)
```

---

## ⚠️ Common Pitfalls

1. **Forgetting anchor creation** → Objects drift over time
   - ✅ Always create anchor: `session.createAnchor(pose)`

2. **Using world coordinates directly** → Unstable placement
   - ✅ Use anchor-relative positioning

3. **No user feedback** → Confusion and frustration
   - ✅ Always show visual/haptic feedback

4. **Ignoring null checks** → Crashes
   - ✅ Validate frame, model, selected object

5. **Continuous hit testing** → Performance degradation
   - ✅ Rate limit to 10 FPS max

---

## 🎓 Learning Resources

1. **ARCore Hit Testing:** https://developers.google.com/ar/develop/hit-test
2. **ARKit Raycast:** https://developer.apple.com/documentation/arkit
3. **Mobile AR Guide:** https://www.reality-atlas.com/learn/arkit-arcore-mobile-ar-development-guide
4. **Performance Tips:** https://developers.google.com/ar/develop/performance

---

## ✅ Definition of Done

- [ ] iOS uses modern raycast API (no deprecation warnings)
- [ ] All edge cases have error handling
- [ ] User sees visual feedback for every action
- [ ] Placement success rate >90% in testing
- [ ] Frame rate stays >30 FPS with 5 objects
- [ ] Unit tests for hit test logic
- [ ] Manual testing completed on both platforms

---

**Created:** 2026-03-30  
**Status:** ✅ Ready for Implementation  
**Owner:** Main Developer Agent
