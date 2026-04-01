# Android ARCore Hit Testing - Detaylı Analiz ve İyileştirme Raporu

**Tarih:** 2026-03-30  
**Proje:** ARSample - 3D Obje Ekleme/Çıkarma  
**Platform:** Android (ARCore + SceneView 2.1.0)  
**Analiz Edilen Dosya:** `composeApp/src/androidMain/kotlin/com/trendhive/arsample/ar/ARView.kt`

---

## 1. Executive Summary

### Mevcut Durum
✅ **İyi Yanlar:**
- ARCore SDK 1.48.0 (güncel)
- SceneView 2.1.0 kullanımı (stable versiyon)
- Plane detection (horizontal + vertical) aktif
- Depth mode otomatik algılama
- Environmental HDR lighting
- Manifest yapılandırması tam

⚠️ **Kritik Sorunlar:**
1. **Hit test sonuçları filtrelenmemiş** (distance, confidence, trackingState kontrolü yok)
2. **İlk valid hit result kullanılmıyor** (ARCore best practice)
3. **Anchor oluşturulmuyor** (pose doğrudan kullanılıyor - hatalı!)
4. **Plane tracking state kontrolü eksik**
5. **Performance optimization eksik** (her touch'ta tüm hit results işleniyor)

---

## 2. Mevcut Implementasyon Analizi

### 2.1. Hit Testing Kodu (Satır 113-151)

```kotlin
// MEVCUT KOD
onTouchEvent = { e, _ ->
    if (e.action == MotionEvent.ACTION_UP) {
        try {
            val frame = try {
                this.frame ?: return@onTouchEvent true
            } catch (ex: Exception) {
                Log.e(TAG, "Frame property unavailable")
                return@onTouchEvent true
            }

            val hitResults = try {
                frame.hitTest(e.x, e.y)
            } catch (ex: Exception) {
                Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                return@onTouchEvent true
            }

            hitResults.forEach { hit ->
                (hit.trackable as? Plane)?.let { _ ->
                    modelPathToLoad?.let { path ->
                        val pose = hit.hitPose
                        Log.d(TAG, "Model placed at: x=${pose.tx()}, y=${pose.ty()}, z=${pose.tz()}")
                        onModelPlaced(path, pose.tx(), pose.ty(), pose.tz())
                        return@onTouchEvent true
                    }
                }
            }

            if (hitResults.isEmpty()) {
                Log.d(TAG, "No plane detected at touch location")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during hit test: ${e.message}", e)
        }
    }
    true
}
```

### 2.2. Sorunların Detaylı Analizi

#### Problem #1: Anchor Kullanımı Eksik
**Kritik Seviye:** 🔴 YÜKSEK

**Sorun:**
```kotlin
val pose = hit.hitPose
onModelPlaced(path, pose.tx(), pose.ty(), pose.tz())
```

**Neden Hatalı:**
- ARCore'da `Pose` doğrudan kullanılmaz, **Anchor** kullanılır
- Anchor olmadan pose dünyayla birlikte kayar/değişir
- Model yerleştirme kararlı olmaz

**ARCore Best Practice:**
```kotlin
val anchor = hit.createAnchor()
val pose = anchor.pose
// anchor.detach() ile temizleme yap
```

---

#### Problem #2: Hit Result Filtreleme Yok
**Kritik Seviye:** 🔴 YÜKSEK

**Sorun:**
```kotlin
hitResults.forEach { hit ->
    (hit.trackable as? Plane)?.let { _ ->
        // İlk plane'i kullan
    }
}
```

**Neden Hatalı:**
- ARCore hit results **mesafeye göre sıralı** gelir (en yakından uzağa)
- Ancak plane'in tracking state'i, confidence değeri kontrol edilmiyor
- Çok uzak veya düşük kaliteli plane'ler de kabul ediliyor

**ARCore Best Practice:**
```kotlin
val firstHitResult = hitResults.firstOrNull { hit ->
    when (val trackable = hit.trackable) {
        is Plane -> {
            trackable.isPoseInPolygon(hit.hitPose) &&
            trackable.trackingState == TrackingState.TRACKING
        }
        is DepthPoint -> true // Depth API varsa öncelik ver
        else -> false
    }
}
```

---

#### Problem #3: Plane Tracking State Kontrolü Yok
**Kritik Seviye:** 🟡 ORTA

**Sorun:**
```kotlin
(hit.trackable as? Plane)?.let { _ ->
    // Plane'in state'i kontrol edilmiyor
}
```

**Neden Hatalı:**
- Plane'in state'i `PAUSED` veya `STOPPED` olabilir
- Bu durumlarda model yerleştirme hatalı olur

**Doğru Yaklaşım:**
```kotlin
(hit.trackable as? Plane)?.let { plane ->
    if (plane.trackingState != TrackingState.TRACKING) {
        return@forEach // Sadece tracking plane'leri kullan
    }
    if (!plane.isPoseInPolygon(hit.hitPose)) {
        return@forEach // Pose plane sınırları içinde değil
    }
    // Artık güvenli
}
```

---

#### Problem #4: Distance/Confidence Filtresi Yok
**Kritik Seviye:** 🟡 ORTA

**Sorun:**
- Hit result'un mesafesi kontrol edilmiyor
- Çok uzak objelere (>5 metre) placement yapılabilir
- Bu durum AR deneyimini bozar

**Önerilen Filtre:**
```kotlin
val MAX_PLACEMENT_DISTANCE = 3.0f // metre

val validHit = hitResults.firstOrNull { hit ->
    val distance = hit.distance
    distance > 0.1f && distance < MAX_PLACEMENT_DISTANCE &&
    (hit.trackable is Plane || hit.trackable is DepthPoint)
}
```

---

#### Problem #5: forEach Yerine firstOrNull Kullanılmalı
**Kritik Seviye:** 🟢 DÜŞÜK (Performance)

**Sorun:**
```kotlin
hitResults.forEach { hit ->
    // Birinci valid hit bulununca bile devam ediyor
}
```

**Neden Verimli Değil:**
- ARCore hit results zaten mesafeye göre sıralı
- İlk valid result bulununca döngüyü kırmalı

**Optimizasyon:**
```kotlin
val validHit = hitResults.firstOrNull { /* filter */ }
validHit?.let { hit ->
    // Process
}
```

---

## 3. ARCore Best Practices Uyumluluğu

| Best Practice | Durum | Açıklama |
|--------------|-------|----------|
| Anchor kullanımı | ❌ | Pose doğrudan kullanılıyor |
| Hit result filtreleme | ❌ | Distance, state kontrolü yok |
| Plane tracking state | ❌ | TrackingState kontrolü yok |
| isPoseInPolygon kontrolü | ❌ | Pose plane içinde mi kontrol edilmiyor |
| DepthPoint önceliği | ❌ | Depth API kullanılmıyor (ama config'de enabled) |
| firstOrNull kullanımı | ❌ | forEach kullanılıyor |
| Error handling | ✅ | Try-catch blokları var |
| Logging | ✅ | Debug logları mevcut |

**Skor: 2/8 (25%)**

---

## 4. SceneView 2.x API Kullanımı

### ✅ Doğru Kullanılanlar

1. **ARSceneView Initialization:**
```kotlin
ARSceneView(context).apply {
    sessionConfiguration = { session, config ->
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.depthMode = Config.DepthMode.AUTOMATIC
        }
    }
}
```
✅ Güncel SceneView 2.1.0 API'si  
✅ Depth mode support check yapılıyor  
✅ Environmental HDR aktif

2. **Frame Access:**
```kotlin
val frame = this.frame ?: return@onTouchEvent true
```
✅ SceneView 2.x'te `frame` property doğrudan erişilebilir

3. **Model Loading:**
```kotlin
view.modelLoader.loadModelInstance(modelLocation)
```
✅ SceneView 2.1.0 API'sine uygun

---

### ⚠️ Eksik/Hatalı Kullanımlar

1. **Anchor Management Yok:**
SceneView 2.x'te anchor management için `AnchorNode` kullanılmalı:

```kotlin
// Mevcut (Hatalı)
onModelPlaced(path, pose.tx(), pose.ty(), pose.tz())

// Olması Gereken
val anchor = hit.createAnchor()
val anchorNode = AnchorNode(engine = view.engine, anchor = anchor)
val modelNode = ModelNode(modelInstance, ...).apply {
    parent = anchorNode
}
view.addChildNode(anchorNode)
```

2. **onSessionUpdated Kullanılmıyor:**
SceneView 2.x'te `onSessionUpdated` callback kullanılarak plane updates'ler izlenebilir:

```kotlin
ARSceneView(context).apply {
    onSessionUpdated = { session, frame ->
        // Plane updates
        val updatedPlanes = frame.getUpdatedPlanes()
    }
}
```

---

## 5. Düzeltilmiş Implementasyon

### 5.1. Tam ARCore Best Practice Uyumlu Kod

```kotlin
package com.trendhive.arsample.ar

import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.*
import com.trendhive.arsample.domain.model.PlacedObject
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.math.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ARView"
private const val MAX_PLACEMENT_DISTANCE = 3.0f // metre
private const val MIN_PLACEMENT_DISTANCE = 0.2f // metre

@Composable
fun ARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, anchorId: String, posX: Float, posY: Float, posZ: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null
) {
    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    val currentNodes = remember { mutableMapOf<String, AnchorNode>() } // key: placedObjectId
    val anchors = remember { mutableMapOf<String, Anchor>() } // key: anchorId

    fun normalizeModelLocation(location: String): String {
        return when {
            location.startsWith("file://") -> location
            location.startsWith("/") -> "file://$location"
            else -> location
        }
    }

    // Sync placed objects with scene nodes
    LaunchedEffect(placedObjects, arSceneView) {
        val view = arSceneView ?: return@LaunchedEffect

        // Remove nodes that are no longer in the list
        val objectIds = placedObjects.map { it.objectId }.toSet()
        val idsToRemove = currentNodes.keys.filter { it !in objectIds }
        idsToRemove.forEach { id ->
            currentNodes[id]?.let { anchorNode ->
                anchors[anchorNode.anchor.hashCode().toString()]?.detach()
                view.removeChildNode(anchorNode)
            }
            currentNodes.remove(id)
        }

        // Add or update nodes from the list
        placedObjects.forEach { obj ->
            if (!currentNodes.containsKey(obj.objectId)) {
                val modelLocation = normalizeModelLocation(obj.arObjectId)

                try {
                    val modelInstance = withContext(Dispatchers.IO) {
                        try {
                            val file = File(obj.arObjectId)
                            if (!file.exists()) {
                                Log.w(TAG, "Model file not found: ${obj.arObjectId}")
                                return@withContext null
                            }
                            view.modelLoader.loadModelInstance(modelLocation)
                        } catch (e: Exception) {
                            Log.e(TAG, "IO error accessing model file: ${obj.arObjectId}", e)
                            null
                        }
                    }

                    if (modelInstance != null) {
                        // Create anchor at specified position (from database)
                        // Note: Bu kısım session restore için - initial placement için anchor hit test'ten gelecek
                        val pose = Pose.makeTranslation(
                            floatArrayOf(obj.position.x, obj.position.y, obj.position.z)
                        )
                        
                        // Anchor'u session'a ekle
                        val session = view.session
                        val anchor = session?.createAnchor(pose)
                        
                        if (anchor != null) {
                            val anchorNode = AnchorNode(engine = view.engine, anchor = anchor)
                            val modelNode = ModelNode(
                                modelInstance = modelInstance,
                                scaleToUnits = 0.5f
                            ).apply {
                                parent = anchorNode
                            }
                            view.addChildNode(anchorNode)
                            currentNodes[obj.objectId] = anchorNode
                            anchors[anchor.hashCode().toString()] = anchor
                            Log.d(TAG, "Model loaded successfully with anchor: ${obj.arObjectId}")
                        } else {
                            Log.e(TAG, "Failed to create anchor for model: ${obj.arObjectId}")
                        }
                    } else {
                        Log.e(TAG, "Failed to load model: modelInstance is null for ${obj.arObjectId}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading model ${obj.arObjectId}: ${e.message}", e)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Tüm anchor'ları detach et
            anchors.values.forEach { it.detach() }
            anchors.clear()
            currentNodes.clear()
            arSceneView?.destroy()
        }
    }

    AndroidView(
        factory = { context ->
            ARSceneView(context).apply {
                sessionConfiguration = { session, config ->
                    config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    
                    // Depth mode - öncelikli hit test için
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        config.depthMode = Config.DepthMode.AUTOMATIC
                        Log.d(TAG, "Depth mode enabled")
                    }
                    
                    // Instant Placement - hızlı placement için fallback
                    config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                }

                // Plane detection updates
                onSessionUpdated = { session, frame ->
                    // Optional: Plane güncelleme logları
                    val updatedPlanes = frame.getUpdatedPlanes()
                        .filter { it.trackingState == TrackingState.TRACKING }
                    
                    if (updatedPlanes.isNotEmpty()) {
                        Log.v(TAG, "Active tracking planes: ${updatedPlanes.size}")
                    }
                }

                onTouchEvent = { e, _ ->
                    if (e.action == MotionEvent.ACTION_UP && modelPathToLoad != null) {
                        try {
                            val frame = this.frame
                            if (frame == null) {
                                Log.w(TAG, "Frame is null, skipping hit test")
                                return@onTouchEvent true
                            }

                            // ARCore hit test
                            val hitResults = try {
                                frame.hitTest(e.x, e.y)
                            } catch (ex: Exception) {
                                Log.e(TAG, "Hit test failed: ${ex.message}", ex)
                                return@onTouchEvent true
                            }

                            // Filter ve prioritize hit results
                            val validHit = findBestHitResult(hitResults)

                            if (validHit != null) {
                                // Anchor oluştur (ARCore best practice)
                                val anchor = try {
                                    validHit.createAnchor()
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to create anchor: ${e.message}", e)
                                    return@onTouchEvent true
                                }

                                val pose = anchor.pose
                                val anchorId = anchor.hashCode().toString()
                                
                                anchors[anchorId] = anchor
                                
                                Log.d(
                                    TAG,
                                    "Model placed at: x=${pose.tx()}, y=${pose.ty()}, z=${pose.tz()}, " +
                                    "distance=${validHit.distance}, trackable=${validHit.trackable::class.simpleName}"
                                )
                                
                                onModelPlaced(
                                    modelPathToLoad,
                                    anchorId,
                                    pose.tx(),
                                    pose.ty(),
                                    pose.tz()
                                )
                            } else {
                                Log.d(TAG, "No valid placement surface found")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Unexpected error during hit test: ${e.message}", e)
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

/**
 * ARCore best practices'e göre en iyi hit result'u bul
 * 
 * Öncelik sırası:
 * 1. DepthPoint (en hassas)
 * 2. Plane (tracking state ve polygon içi kontrolü ile)
 * 3. Point (feature point)
 * 
 * Filtreleme kriterleri:
 * - Distance: MIN_PLACEMENT_DISTANCE ile MAX_PLACEMENT_DISTANCE arası
 * - Plane için: trackingState == TRACKING, isPoseInPolygon == true
 * - TrackingState check
 */
private fun findBestHitResult(hitResults: List<HitResult>): HitResult? {
    return hitResults.firstOrNull { hit ->
        // Distance check
        val distance = hit.distance
        if (distance < MIN_PLACEMENT_DISTANCE || distance > MAX_PLACEMENT_DISTANCE) {
            return@firstOrNull false
        }

        // Trackable type check
        when (val trackable = hit.trackable) {
            is DepthPoint -> {
                // Depth point en yüksek öncelik - en hassas yerleştirme
                Log.d(TAG, "Using DepthPoint at distance: $distance")
                true
            }
            is Plane -> {
                // Plane tracking state ve polygon kontrolü
                val isTracking = trackable.trackingState == TrackingState.TRACKING
                val isPoseInPolygon = trackable.isPoseInPolygon(hit.hitPose)
                
                if (isTracking && isPoseInPolygon) {
                    Log.d(TAG, "Using Plane (${trackable.type}) at distance: $distance")
                    true
                } else {
                    Log.v(TAG, "Plane rejected - tracking: $isTracking, inPolygon: $isPoseInPolygon")
                    false
                }
            }
            is Point -> {
                // Feature point - son seçenek
                val isTracking = trackable.trackingState == TrackingState.TRACKING
                if (isTracking) {
                    Log.d(TAG, "Using Point at distance: $distance")
                    true
                } else {
                    false
                }
            }
            is InstantPlacementPoint -> {
                // Instant placement - hızlı ama düşük kalite
                Log.d(TAG, "Using InstantPlacementPoint at distance: $distance")
                true
            }
            else -> {
                Log.v(TAG, "Unknown trackable type: ${trackable::class.simpleName}")
                false
            }
        }
    }
}
```

---

## 6. Domain Model Değişikliği

`PlacedObject` data class'ına `anchorId` field eklenmeli:

```kotlin
// domain/model/PlacedObject.kt

data class PlacedObject(
    val objectId: String,
    val arObjectId: String, // Model file path
    val anchorId: String,    // ✅ YENİ - ARCore anchor ID
    val position: Position3D,
    val rotation: Rotation3D = Rotation3D(0f, 0f, 0f),
    val scale: Scale3D = Scale3D(1f, 1f, 1f),
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 7. ViewModel Güncelleme

```kotlin
// presentation/ARViewModel.kt

fun placeModel(modelPath: String, anchorId: String, x: Float, y: Float, z: Float) {
    val newObject = PlacedObject(
        objectId = UUID.randomUUID().toString(),
        arObjectId = modelPath,
        anchorId = anchorId,  // ✅ YENİ
        position = Position3D(x, y, z)
    )
    
    viewModelScope.launch {
        repository.addPlacedObject(newObject)
    }
}

fun removeObject(anchorId: String) {
    viewModelScope.launch {
        repository.removePlacedObjectByAnchor(anchorId)
    }
}
```

---

## 8. Performance Optimizasyonları

### 8.1. Hit Test Debouncing

```kotlin
private var lastHitTestTime = 0L
private const val HIT_TEST_COOLDOWN_MS = 200L // 200ms

onTouchEvent = { e, _ ->
    if (e.action == MotionEvent.ACTION_UP) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastHitTestTime < HIT_TEST_COOLDOWN_MS) {
            return@onTouchEvent true // Çok sık hit test engelle
        }
        lastHitTestTime = currentTime
        
        // ... hit test logic
    }
    true
}
```

### 8.2. Frame Skip Optimization

```kotlin
onSessionUpdated = { session, frame ->
    // Her frame yerine 3 frame'de bir kontrol et
    if (frame.timestamp % 3 == 0L) {
        val updatedPlanes = frame.getUpdatedPlanes()
        // Process planes
    }
}
```

### 8.3. Model Loading Cache

```kotlin
private val modelInstanceCache = mutableMapOf<String, ModelInstance>()

suspend fun loadModelInstance(path: String): ModelInstance? {
    return modelInstanceCache.getOrPut(path) {
        withContext(Dispatchers.IO) {
            view.modelLoader.loadModelInstance(normalizeModelLocation(path))
        }
    }
}
```

---

## 9. Testing Checklist

### Unit Tests
- [ ] `findBestHitResult` fonksiyonu - distance filtreleme
- [ ] `findBestHitResult` fonksiyonu - plane tracking state
- [ ] `findBestHitResult` fonksiyonu - priority order (Depth > Plane > Point)
- [ ] Anchor lifecycle (create, detach)
- [ ] Model instance caching

### Integration Tests
- [ ] ARSceneView initialization
- [ ] Plane detection başarılı
- [ ] Hit test sonucu anchor oluşturuyor
- [ ] Model yerleşiyor ve tracking çalışıyor
- [ ] Anchor detach sonrası cleanup

### Manual QA Tests
- [ ] Yatay yüzeye (masa) model yerleşiyor
- [ ] Dikey yüzeye (duvar) model yerleşiyor
- [ ] Çok yakın (<20cm) yerleştirme reddediliyor
- [ ] Çok uzak (>3m) yerleştirme reddediliyor
- [ ] Plane tracking kaybında model kaybolmuyor (anchor tracking)
- [ ] App restart sonrası modeller yüklenebiliyor (persistence)

---

## 10. Sonuç ve Öncelik Sırası

### Kritik (Hemen Yapılmalı)
1. ✅ **Anchor kullanımı implement et** - 2 saat
2. ✅ **`findBestHitResult` filtreleme fonksiyonu ekle** - 1 saat
3. ✅ **PlacedObject'e anchorId field ekle** - 30 dk
4. ✅ **AnchorNode ile model placement** - 1 saat

### Önemli (Bu Sprint'te)
5. ✅ **Plane tracking state kontrolü** - 30 dk
6. ✅ **Distance filtering (0.2m - 3m)** - 15 dk
7. ✅ **forEach yerine firstOrNull** - 10 dk
8. ✅ **Anchor cleanup (detach) on remove** - 30 dk

### Optimize (Sonraki Sprint)
9. ⏳ Hit test debouncing - 20 dk
10. ⏳ Model instance caching - 1 saat
11. ⏳ Frame skip optimization - 30 dk
12. ⏳ Unit tests yazılması - 3 saat

---

## 11. Referanslar

1. **ARCore Official Docs:**
   - [Hit Testing Guide](https://developers.google.com/ar/develop/java/hit-test/developer-guide)
   - [Anchors Best Practices](https://developers.google.com/ar/develop/anchors)

2. **SceneView 2.x:**
   - [GitHub Repository](https://github.com/SceneView/sceneview-android)
   - [ARSceneView Sample](https://github.com/SceneView/sceneview-android/tree/main/samples)

3. **ARCore Samples:**
   - [hello_ar_kotlin](https://github.com/google-ar/arcore-android-sdk/tree/master/samples/hello_ar_kotlin)

---

## 12. Ekler

### A. ARCore Hit Result Types Karşılaştırma

| Type | Accuracy | Speed | Use Case | Priority |
|------|----------|-------|----------|----------|
| **DepthPoint** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | Herhangi yüzey | 1 |
| **Plane** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Zemin/Duvar | 2 |
| **Point** | ⭐⭐⭐ | ⭐⭐⭐⭐ | Feature noktası | 3 |
| **InstantPlacement** | ⭐⭐ | ⭐⭐⭐⭐⭐ | Hızlı placement | 4 |

### B. Plane TrackingState Enum

```kotlin
enum class TrackingState {
    TRACKING,  // ✅ Kullanılabilir
    PAUSED,    // ⚠️ Geçici kayıp
    STOPPED    // ❌ Kullanılmaz
}
```

### C. SceneView 2.x vs 3.x Karşılaştırma

| Özellik | SceneView 2.1.0 (Mevcut) | SceneView 3.6.0 |
|---------|--------------------------|-----------------|
| ARCore Support | ✅ | ✅ |
| Compose Integration | ✅ | ✅ (Native Composable) |
| AnchorNode | ✅ | ✅ |
| Model Loading | `loadModelInstance` | `rememberModelInstance` |
| Stability | Stable | Stable |
| Breaking Changes | - | Yes (Migration needed) |

**Öneri:** SceneView 2.1.0 şu an yeterli. 3.x'e geçiş için MIGRATION.md incelenebilir.

---

**Rapor Sonu**  
*Bu rapor, mevcut ARCore implementasyonunun detaylı analizini ve production-ready hale getirmek için gerekli değişiklikleri içermektedir.*
