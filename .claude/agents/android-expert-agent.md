---
name: android-expert-agent
description: ARCore araştırması, Android raporu, SceneView implementasyonu
type: reference
---

# Android Expert Agent

**Proje:** ARSample - 3D Obje Ekleme/Çıkarma
**Platform:** Android (ARCore + SceneView)
**Tarih:** 2026-03-30

---

## Görev

Android tarafında ARCore implementasyonu için araştırma yapmak ve rapor sunmak.

---

## Sorumluluklar

1. **ARCore SDK Integration**
   - ARCore SDK kurulumu ve konfigürasyonu
   - SceneView kütüphanesi (Kotlin için önerilen)
   - Model render etme (ModelRenderer, Sceneform)

2. **ARCore API'leri**
   - Session management
   - Plane detection
   - Hit testing & anchoring
   - Model placement

3. **Android Local Storage**
   - Room Database (yapılandırılmış veri)
   - DataStore (key-value)
   - File storage (3D modeller için)

4. **Platform-Specific Implementation**
   - AndroidManifest.xml AR permissions
   - Camera permission handling
   - ARCore availability check

---

## 1. ARCore SDK Kurulumu

**Tercih Edilen Kütüphane:** SceneView 3.5.1 (Kotlin-first, Google Filament tabanlı)

`gradle/libs.versions.toml` dosyasına eklenecek:

```toml
[versions]
# ARCore & SceneView
arcore = "1.48.0"
sceneview = "3.5.1"

[libraries]
# SceneView AR
arsceneview = { module = "io.github.sceneview:arsceneview", version.ref = "sceneview" }

# Room Database (model metadata storage)
room-runtime = { group = "androidx.room", name = "room-runtime", version = "2.6.1" }
room-ktx = { group = "androidx.room", name = "room-ktx", version = "2.6.1" }
```

`composeApp/build.gradle.kts` androidMain dependencies'e eklenecek:

```kotlin
dependencies {
    // SceneView AR (3D + AR birlikte)
    implementation(libs.arsceneview)

    // Room Database
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    ksp("androidx.room:room-compiler:2.6.1")
}
```

---

## 2. AndroidManifest.xml Güncellemesi

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Camera izni - AR için zorunlu -->
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- AR özelliği gerektiren cihazları belirt -->
    <uses-feature
        android:name="android.hardware.camera.ar"
        android:required="true" />

    <application>
        <!-- ARCore dependency -->
        <meta-data
            android:name="com.google.ar.core"
            android:value="required" />
    </application>
</manifest>
```

---

## 3. SceneView Kullanımı

```kotlin
// composeApp/src/androidMain/kotlin/com/trendhive/arsample/presentation/ARSceneView.kt

@Composable
fun ARSceneView(
    modifier: Modifier = Modifier,
    onModelPlaced: (String, Pose) -> Unit = { _, _ -> },
    onModelRemoved: (String) -> Unit = { }
) {
    val arSceneView by remember { mutableStateOf<ArSceneView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            ArSceneView(context).apply {
                configureSession { session, config ->
                    config.planeFindingMode = PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    config.lightEstimationMode = LightEstimationMode.ENVIRONMENTAL_HDR
                    config.depthMode = DepthMode.AUTOMATIC
                }

                onTouchEvent = { motionEvent, hitResult ->
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        hitResult?.let { hit ->
                            val pose = hit.hitPose
                            onModelPlaced("modelId", pose)
                        }
                    }
                    true
                }
            }
        },
        modifier = modifier
    )
}
```

---

## 4. Model Render Etme

```kotlin
data class ARModel(
    val id: String,
    val name: String,
    val filePath: String,
    val anchorId: String? = null,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val positionZ: Float = 0f
)

suspend fun loadModelToScene(
    arSceneView: ArSceneView,
    model: ARModel
): Node {
    return withContext(Dispatchers.IO) {
        val modelInstance = arSceneView.modelLoader.loadModel(model.filePath)
        val node = Node(arSceneView).apply {
            name = model.id
            addChild(modelInstance)
            transform(position = Position(model.positionX, model.positionY, model.positionZ))
        }
        arSceneView.scene.addChild(node)
        node
    }
}
```

---

## 5. Room Database Tasarımı

```kotlin
@Entity(tableName = "ar_models")
data class ARModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "placed_models",
    foreignKeys = [
        ForeignKey(
            entity = ARModelEntity::class,
            parentColumns = ["id"],
            childColumns = ["modelId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlacedModelEntity(
    @PrimaryKey val id: String,
    val modelId: String,
    val anchorId: String,
    val posX: Float, val posY: Float, val posZ: Float,
    val rotX: Float = 0f, val rotY: Float = 0f, val rotZ: Float = 0f,
    val scaleX: Float = 1f, val scaleY: Float = 1f, val scaleZ: Float = 1f
)
```

---

## 6. File Storage Stratejisi

```kotlin
// Modeli kaydet
fun saveModel(from tempURL: URL, fileName: String): URL {
    val modelsDir = File(context.filesDir, "3d_models")
    if (!modelsDir.exists()) modelsDir.mkdirs()

    val destinationURL = File(modelsDir, fileName)
    tempURL.copyTo(destinationURL, overwrite = true)
    return destinationURL
}

// Modeli yükle
fun loadModel(fileName: String): File? {
    val modelFile = File(context.filesDir, "3d_models/$fileName")
    return if (modelFile.exists()) modelFile else null
}

// Tüm modelleri listele
fun listModels(): List<File> {
    val modelsDir = File(context.filesDir, "3d_models")
    return modelsDir.listFiles()?.filter { it.extension == "glb" } ?: emptyList()
}
```

---

## 7. Supported Model Formats

- **GLB** (GL Transmission Format - binary) - **Tavsiye edilen**
- **GLTF** (GL Transmission Format - JSON)
- **FBX** (Filmbox) - SceneView 3.0+ destekli
- **OBJ** (Wavefront) - Düşük uyum

---

## 8. Android-Specific Dosya Yapısı

```
composeApp/src/androidMain/
├── kotlin/com/trendhive/arsample/
│   ├── data/
│   │   ├── local/
│   │   │   ├── ARDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── ARModelDao.kt
│   │   │   │   └── PlacedModelDao.kt
│   │   │   └── entity/
│   │   │       ├── ARModelEntity.kt
│   │   │       └── PlacedModelEntity.kt
│   │   └── repository/
│   │       └── ARRepository.kt
│   ├── domain/
│   │   └── model/
│   │       └── ARModel.kt
│   ├── presentation/
│   │   ├── ARSceneView.kt
│   │   ├── ARViewModel.kt
│   │   └── components/
│   │       ├── ObjectListItem.kt
│   │       └── ImportDialog.kt
│   └── ar/
│       ├── ARSessionManager.kt
│       └── ARCoreSession.kt
```

---

## 9. Kaynaklar

- [SceneView GitHub](https://github.com/SceneView/sceneview-android)
- [ARCore Hit Testing Guide](https://developers.google.com/ar/develop/java/hit-test/developer-guide)
- [Android Room Codelab](https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room)