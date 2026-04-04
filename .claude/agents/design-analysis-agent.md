---
name: design-analysis-agent
description: Web research, AR examples, design documentation
type: reference
---

# Design & Analysis Agent

**Project:** ARSample - 3D Object Placement/Removal
**Platform:** Kotlin Multiplatform (Android + iOS)
**Date:** 2026-03-30

---

## Mission

Conduct web research to collect best practices for ARCore/ARKit applications, 3D model formats, and Clean Architecture + DDD pattern recommendations.

---

## Responsibilities

### 1. ARCore/ARKit Best Practices Research

- SceneView, ARCore Kotlin API'leri
- ARKit + SwiftUI entegrasyonu
- Plane detection, hit testing, anchoring

### 2. 3D Model Formats Research

| Format | Platform | Compression | Animation | Runtime Support |
|--------|----------|-------------|-----------|-----------------|
| **GLB** | All platforms | Draco | Yes | Best |
| **USDZ** | iOS/macOS | Lossless | Yes | Quick Look, ARKit |
| **FBX** | DCC Pipeline | No | Yes | Conversion required |

**Preferred Format:** glTF 2.0 / GLB

**Rationale:**
- Universal support (Google, Apple, Microsoft, Facebook)
- Compact file sizes, Draco compression
- Cross-platform compatibility

### 3. Polygon Budget (Real-World Tests)

| Device | Target Polygon | File Size |
|--------|----------------|-----------|
| iPhone 14 (A15) | 150K faces | < 5 MB |
| iPhone 12 (A14) | 90K faces | < 3 MB |
| Android Mid | 50-65K faces | < 8 MB |
| WebAR | 50K faces | < 4 MB |

---

## DDD Principles (Eric Evans)

### Layer Responsibilities

**1. Domain Layer** (Pure Business Logic)
- Entities (ARObject, ARScene, PlacedObject)
- Value Objects (ModelUri, ObjectName)
- Repository Interfaces (ARObjectRepository, ARSceneRepository)
- Domain Exceptions
- NO dependencies on other layers
- NO frameworks, NO persistence code

**2. Application Layer** (Use Cases & Orchestration)
- Use Cases (business workflows)
- Application Services (complex orchestration)
- Input/Output DTOs
- Orchestrates domain objects
- Depends ONLY on domain layer

**3. Infrastructure Layer** (Technical Implementations)
- Repository Implementations
- Database access (DTOs, Mappers, DataSources)
- File system operations
- AR platform integrations (ARCore, ARKit)
- Depends on domain layer

**4. Presentation Layer** (User Interface)
- ViewModels
- UI Screens (Compose)
- UI Components
- Depends on application and domain layers

### Dependency Flow
```
Presentation → Application → Domain ← Infrastructure
```

---

## Clean Architecture Layer Organization

```
composeApp/src/
├── commonMain/kotlin/com/trendhive/arsample/
│   ├── domain/                          # Domain Layer (Pure Business Logic)
│   │   ├── model/                       # Entities & Value Objects
│   │   ├── repository/                  # Repository Interfaces ONLY
│   │   └── exception/                   # Domain Exceptions
│   ├── application/                     # Application Layer (NEW)
│   │   ├── usecase/                     # Business Workflows
│   │   ├── service/                     # Complex Orchestration
│   │   └── dto/                         # Input/Output DTOs
│   ├── infrastructure/                  # Infrastructure Layer
│   │   └── persistence/
│   │       ├── repository/              # Repository Implementations
│   │       ├── datasource/              # Data Sources (interfaces)
│   │       ├── dto/                     # Database DTOs
│   │       └── mapper/                  # DTO ↔ Model Mappers
│   └── presentation/                    # Presentation Layer
│       ├── viewmodel/                   # ViewModels
│       └── ui/screens/                  # Compose UI Screens
├── androidMain/kotlin/.../
│   ├── infrastructure/persistence/      # Android implementations
│   └── ar/                              # ARCore Session, ARSceneView
└── iosMain/kotlin/.../
    ├── infrastructure/persistence/      # iOS implementations
    └── ar/                              # ARView Wrapper
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `ARObject` | Imported 3D model - Metadata (name, URI, type) |
| `ARScene` | Scene - List of PlacedObjects |
| `PlacedObject` | Object placed in scene (position, rotation, scale) |

---

## Use Cases

| Use Case | Responsibility |
|----------|----------------|
| `ImportObjectUseCase` | Creates new ARObject from file URI |
| `GetAllObjectsUseCase` | Returns all saved objects |
| `DeleteObjectUseCase` | Deletes ARObject |
| `PlaceObjectInSceneUseCase` | Adds ARObject to scene at specified position |
| `RemoveObjectFromSceneUseCase` | Removes object from scene |
| `UpdateObjectTransformUseCase` | Updates object position/rotation/scale |
| `GetSceneUseCase` | Returns active scene |
| `SaveSceneUseCase` | Saves scene to local storage |

---

## Local Storage Strategy

| Data | Storage | Reason |
|------|---------|--------|
| Scene metadata (object list, transform) | DataStore (JSON serialization) | Fast access, type-safe |
| 3D Model files | Internal/External File Storage | Suitable for large files |

---

## UI/UX Akışı

```
App Launch
    ↓
Object List Screen (kayıtlı objeler listesi)
    ↓
[+] Butonu → File Picker → Import Dialog
    ↓
AR Screen (obje ekleme/çıkarma)
    ↓
Coaching Overlay (AR öğretici)
    ↓
Placement → Touch to place → Haptic feedback
```

---

## AR Scene Management Principles

### Anchoring System
- Use `Anchor` for object placement (NOT raw world coordinates)
- ARCore: `Anchor`, `PlaneDetector`, `HitResult`
- ARKit: `ARAnchor`, `ARRaycastResult`

### Raycast Strategy

| Type | Use Case |
|------|----------|
| **One-shot raycast** | Object dragging - instant position needed |
| **Tracked raycast** | Static object placement - continuous refinement needed |
| **Instant placement** | Quick placement - without waiting for surface |

---

## Important Notes

1. **Anchor Usage Mandatory:** Object positions must be based on Anchors
2. **GLB Default Format:** GLB should be used for cross-platform compatibility
3. **DataStore Persistence:** Scene data should be auto-saved on every change
4. **Object Pooling:** Object pool should be used for performance
5. **Error Boundaries:** Error handling should be provided for each platform

---

## Resources

- [Placing objects and handling 3D interaction | Apple Developer Documentation](https://developer.apple.com/documentation/arkit/placing-objects-and-handling-3d-interaction)
- [ARKit and ARCore Mobile AR Development Guide 2026 | Reality Atlas](https://www.reality-atlas.com/learn/arkit-arcore-mobile-ar-development-guide)
- [Performance considerations | ARCore | Google for Developers](https://developers.google.com/ar/develop/performance)
- [Hit-tests place virtual objects in the real world | ARCore | Google for Developers](https://developers.google.com/ar/develop/hit-test)
- [Working with Anchors | ARCore | Google for Developers](https://developers.google.com/ar/develop/anchors)