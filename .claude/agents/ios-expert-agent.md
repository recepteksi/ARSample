---
name: ios-expert-agent
description: ARKit entegrasyonu, iOS local storage ve platform-specific implementation araştırması
type: reference
---

# iOS Expert Agent - Rapor

**Proje:** ARSample - 3D Obje Ekleme/Çıkarma
**Tarih:** 2026-03-30
**Platform:** iOS (SwiftUI + ARKit/RealityKit)

---

## 1. ARKit & SwiftUI Entegrasyonu

### 1.1 iOS 17+ - RealityView (Tercih Edilen)

```swift
import SwiftUI
import RealityKit
import ARKit

struct ARViewContainer: UIViewRepresentable {
    func makeUIView(context: Context) -> ARView {
        let arView = ARView(frame: .zero)
        let config = ARWorldTrackingConfiguration()
        config.planeDetection = [.horizontal, .vertical]
        config.environmentTexturing = .automatic
        arView.session.run(config)
        return arView
    }

    func updateUIView(_ uiView: ARView, context: Context) {}
}

struct ARScreen: View {
    var body: some View {
        ARViewContainer()
            .ignoresSafeArea()
    }
}
```

### 1.2 RealityKit ile Model Yükleme

```swift
// USDZ model asenkron yükleme
func loadModel(named fileName: String) async throws -> ModelEntity {
    guard let entity = try? await ModelEntity.loadModel(named: fileName) else {
        throw ARError.modelLoadingFailed
    }
    entity.generateCollisionShapes()
    return entity
}
```

### 1.3 WWDC 2025-2026 Yenilikleri (visionOS 26)

- **Object Manipulation API** - GestureComponent ile gelişmiş dokunma
- **Bidirectional Data Flow** - SwiftUI <-> RealityKit veri bağlama
- **Spatial Anchors** - Cross-platform uyumluluk

---

## 2. Model Format Stratejisi

### 2.1 Format Karşılaştırması

| Format | Boyut | Kalite | Yükleme Süresi | Platform |
|--------|-------|--------|----------------|----------|
| **USDZ** | Medium | Kayıpsız | ~200ms (.rkassets) | iOS native |
| **GLB** | Küçük | Sıkıştırılmış | ~1.25s | Cross-platform |

### 2.2 GLB -> USDZ Dönüşümü

```swift
// Reality Converter CLI ile dönüşüm
// Terminal: realityconverter source.glb output.usdz

// Programatik dönüşüm için:
import ModelIO

func convertGLBtoUSDZ(glbURL: URL, outputURL: URL) throws {
    let asset = MDLAsset(url: glbURL)
    asset.export(to: outputURL)
}
```

---

## 3. Local Storage Stratejisi

### 3.1 FileManager (3D Modeller)

```swift
final class ModelStorageManager {
    static let shared = ModelStorageManager()
    private let fileManager = FileManager.default
    private let documentsDirectory: URL

    init() {
        documentsDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }

    func save(model data: Data, fileName: String) throws -> URL {
        let modelsDirectory = documentsDirectory.appendingPathComponent("ar_models", isDirectory: true)
        if !fileManager.fileExists(atPath: modelsDirectory.path) {
            try fileManager.createDirectory(at: modelsDirectory, withIntermediateDirectories: true)
        }
        let fileURL = modelsDirectory.appendingPathComponent(fileName)
        try data.write(to: fileURL)
        return fileURL
    }

    func listModels() -> [String] {
        let modelsDirectory = documentsDirectory.appendingPathComponent("ar_models")
        guard let contents = try? fileManager.contentsOfDirectory(atPath: modelsDirectory.path) else {
            return []
        }
        return contents.filter { $0.hasSuffix(".usdz") }
    }
}
```

### 3.2 SQLite.swift (Yapılandırılmış Veri)

```swift
import SQLite

final class ARSessionStorage {
    private var db: Connection?

    private let placements = Table("object_placements")
    private let id = Expression<Int64>("id")
    private let modelName = Expression<String>("model_name")
    private let positionX = Expression<Double>("pos_x")
    private let positionY = Expression<Double>("pos_y")
    private let positionZ = Expression<Double>("pos_z")

    func savePlacement(model: String, position: SIMD3<Float>) throws {
        let insert = placements.insert(
            modelName <- model,
            positionX <- Double(position.x),
            positionY <- Double(position.y),
            positionZ <- Double(position.z)
        )
        try db?.run(insert)
    }
}
```

---

## 4. Platform-Specific Implementation

### 4.1 Info.plist Yetkileri

```xml
<key>NSCameraUsageDescription</key>
<string>AR özellikleri için kameraya erişim gerekiyor</string>

<key>UIRequiredDeviceCapabilities</key>
<array>
    <string>arkit</string>
</array>
```

### 4.2 ARKit Availability Check

```swift
import ARKit

class ARSessionManager {
    static var isARSupported: Bool {
        ARWorldTrackingConfiguration.isSupported
    }

    static var supportsLiDAR: Bool {
        ARWorldTrackingConfiguration.supportsSceneReconstruction(.mesh)
    }
}
```

---

## 5. iOS-Specific Dosya Yapısı

```
iosApp/iosApp/
├── App/
│   └── ARSamplApp.swift
├── Features/
│   └── AR/
│       ├── Presentation/
│       │   ├── Views/
│       │   │   ├── ARViewContainer.swift
│       │   │   ├── ARScreen.swift
│       │   │   └── ObjectListView.swift
│       │   └── ViewModels/
│       │       ├── ARViewModel.swift
│       │       └── ObjectListViewModel.swift
│       ├── Domain/
│       │   ├── Models/
│       │   │   └── ARModel.swift
│       │   └── UseCases/
│       │       └── PlaceObjectUseCase.swift
│       └── Data/
│           ├── Storage/
│           │   └── ModelStorageManager.swift
│           └── Repositories/
│               └── ARModelRepositoryImpl.swift
├── Core/
│   ├── Extensions/
│   ├── Utilities/
│   └── Constants/
└── Resources/
    ├── Assets.xcassets
    └── Info.plist
```

---

## 6. Anchor & Hit Testing

```swift
func placeModel(at screenPoint: CGPoint) {
    guard let result = arView.raycast(from: screenPoint, allowing: .estimatedPlane, alignment: .horizontal).first else {
        return
    }

    let anchor = AnchorEntity(raycastResult: result)
    let modelEntity = try! loadModel(named: "chair.usdz")

    anchor.addChild(modelEntity)
    arView.scene.addAnchor(anchor)
}
```

---

## 7. Önemli Notlar

1. **USDZ Varsayılan** - iOS'te RealityKit USDZ kullanır, GLB dönüştürülmeli
2. **LiDAR Destek Kontrolü** - Her cihazda farklı capabilities olabilir
3. **Collision Shapes** - ModelEntity.generateCollisionShapes() zorunlu
4. **Memory Management** - Büyük modeller için async loading şart

---

## Kaynaklar

- [Apple ARKit Documentation](https://developer.apple.com/documentation/arkit)
- [RealityKit Entity Loading](https://developer.apple.com/documentation/realitykit/loading-entities-from-a-file)
- [WWDC25 SwiftUI and RealityKit](https://developer.apple.com/videos/play/wwdc2025/274/)
- [SQLite.swift Tutorial](https://blog.canopas.com/ios-persist-data-using-sqlite-swift-library-with-swiftui-example-c5baefc04334)
- [GLB to USDZ Conversion](https://www.modelo.io/damf/article/2024/05/03/1024/how-to-convert-glb-to-usdz-for-ar-quick-look)