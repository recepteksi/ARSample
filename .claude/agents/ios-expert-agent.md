---
name: ios-expert-agent
description: ARKit integration, iOS local storage, and platform-specific implementation research
type: reference
---

# iOS Expert Agent - Report

**Project:** ARSample - 3D Object Placement/Removal
**Date:** 2026-03-30
**Platform:** iOS (SwiftUI + ARKit/RealityKit)

---

## 1. ARKit & SwiftUI Integration

### 1.1 iOS 17+ - RealityView (Preferred)

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

### 1.2 RealityKit Model Loading

```swift
// USDZ model async loading
func loadModel(named fileName: String) async throws -> ModelEntity {
    guard let entity = try? await ModelEntity.loadModel(named: fileName) else {
        throw ARError.modelLoadingFailed
    }
    entity.generateCollisionShapes()
    return entity
}
```

### 1.3 WWDC 2025-2026 Updates (visionOS 26)

- **Object Manipulation API** - Enhanced touch with GestureComponent
- **Bidirectional Data Flow** - SwiftUI <-> RealityKit data binding
- **Spatial Anchors** - Cross-platform compatibility

---

## 2. Model Format Strategy

### 2.1 Format Comparison

| Format | Size | Quality | Load Time | Platform |
|--------|------|---------|-----------|----------|
| **USDZ** | Medium | Lossless | ~200ms (.rkassets) | iOS native |
| **GLB** | Small | Compressed | ~1.25s | Cross-platform |

### 2.2 GLB -> USDZ Conversion

```swift
// Conversion with Reality Converter CLI
// Terminal: realityconverter source.glb output.usdz

// For programmatic conversion:
import ModelIO

func convertGLBtoUSDZ(glbURL: URL, outputURL: URL) throws {
    let asset = MDLAsset(url: glbURL)
    asset.export(to: outputURL)
}
```

---

## 3. Local Storage Strategy

### 3.1 FileManager (3D Models)

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
│       ├── Application/
│       │   └── UseCases/
│       │       └── PlaceObjectUseCase.swift
│       ├── Domain/
│       │   └── Models/
│       │       └── ARModel.swift
│       └── Infrastructure/
│           ├── Persistence/
│           │   ├── Storage/
│           │   │   └── ModelStorageManager.swift
│           │   └── Repositories/
│           │       └── ARModelRepositoryImpl.swift
│           └── AR/
│               └── ARSessionManager.swift
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