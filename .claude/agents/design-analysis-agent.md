---
name: design-analysis-agent
description: Web araştırması, AR örnekleri, tasarım dokümanı
type: reference
---

# Design & Analysis Agent

**Proje:** ARSample - 3D Obje Ekleme/Çıkarma
**Platform:** Kotlin Multiplatform (Android + iOS)
**Tarih:** 2026-03-30

---

## Görev

Web araştırması yaparak ARCore/ARKit uygulamaları için en iyi pratikleri, 3D model formatlarını ve Clean Architecture + DDD pattern önerilerini toplamak.

---

## Sorumluluklar

### 1. ARCore/ARKit Best Practices Araştırması

- SceneView, ARCore Kotlin API'leri
- ARKit + SwiftUI entegrasyonu
- Plane detection, hit testing, anchoring

### 2. 3D Model Formatları Araştırması

| Format | Platform | Sıkıştırma | Animasyon | Runtime Destek |
|--------|----------|------------|-----------|----------------|
| **GLB** | Her platform | Draco | Evet | En iyi |
| **USDZ** | iOS/macOS | Kayıpsız | Evet | Quick Look, ARKit |
| **FBX** | DCC Pipeline | Hayır | Evet | Dönüşüm gerekli |

**Tercih Edilen Format:** glTF 2.0 / GLB

**Gerekçe:**
- Evrensel destek (Google, Apple, Microsoft, Facebook)
- Kompakt dosya boyutları, Draco sıkıştırma
- Cross-platform uyumluluk

### 3. Polygon Budget (Gerçek Dünya Testleri)

| Cihaz | Hedef Polygon | Dosya Boyutu |
|-------|---------------|--------------|
| iPhone 14 (A15) | 150K yüzey | < 5 MB |
| iPhone 12 (A14) | 90K yüzey | < 3 MB |
| Android Orta | 50-65K yüzey | < 8 MB |
| WebAR | 50K yüzey | < 4 MB |

---

## Clean Architecture Katman Organizasyonu

```
composeApp/src/
├── commonMain/kotlin/com/trendhive/arsample/
│   ├── domain/
│   │   ├── model/           # Domain Entities
│   │   ├── repository/      # Abstract Repository Interfaces
│   │   └── usecase/         # Business Logic
│   ├── data/
│   │   ├── repository/      # Concrete Implementations
│   │   ├── local/           # Local Storage
│   │   └── mapper/          # Data <-> Domain Mappers
│   └── presentation/
│       ├── viewmodel/       # ViewModels
│       ├── ui/screens/      # Compose UI Screens
│       └── ui/components/   # Reusable Components
├── androidMain/kotlin/.../
│   ├── data/repository/     # ARCore implementation
│   └── ar/                 # ARCore Session, ARSceneView
└── iosMain/kotlin/.../
    ├── data/repository/     # ARKit implementation
    └── ar/                 # ARView Wrapper
```

---

## Domain Entity'leri

| Entity | Açıklama |
|--------|----------|
| `ARObject` | Import edilmiş 3D model - Metadata (isim, URI, type) |
| `ARScene` | Sahne - PlacedObject'lerin listesi |
| `PlacedObject` | Sahneye yerleştirilmiş obje (pozisyon, rotasyon, scale) |

---

## Use Case'ler

| Use Case | Sorumluluk |
|----------|------------|
| `ImportObjectUseCase` | Dosya URI'sinden yeni ARObject oluşturur |
| `GetAllObjectsUseCase` | Tüm kayıtlı objeleri döndürür |
| `DeleteObjectUseCase` | ARObject'i siler |
| `PlaceObjectInSceneUseCase` | ARObject'i sahnede belirtilen pozisyona ekler |
| `RemoveObjectFromSceneUseCase` | Objeyi sahneden çıkartır |
| `UpdateObjectTransformUseCase` | Objenin pozisyon/rotasyon/scale güncelle |
| `GetSceneUseCase` | Aktif sceneyi döndürür |
| `SaveSceneUseCase` | Sceneyi local storage'a kaydeder |

---

## Local Storage Stratejisi

| Veri | Storage | Sebep |
|------|---------|-------|
| Scene metadata (obje listesi, transform) | DataStore (JSON serialization) | Hızlı erişim, tip güvenli |
| 3D Model dosyaları | Internal/External File Storage | Büyük dosyalar için uygun |

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

## AR Scene Management Prensipleri

### Anchoring Sistemi
- Obje yerleştirmek için `Anchor` kullanılmalı (raw world coordinates DEĞİL)
- ARCore: `Anchor`, `PlaneDetector`, `HitResult`
- ARKit: `ARAnchor`, `ARRaycastResult`

### Raycast Stratejisi

| Tip | Kullanım Senaryosu |
|-----|-------------------|
| **One-shot raycast** | Obje taşıma (drag) - anlık pozisyon gerekli |
| **Tracked raycast** | Sabit obje yerleştirme - sürekli iyileştirme gerekli |
| **Instant placement** | Hızlı yerleştirme - yüzey beklemeden |

---

## Önemli Notlar

1. **Anchor Kullanımı Zorunlu:** Obje pozisyonları mutlaka Anchor üzerine kurulmalı
2. **GLB Varsayılan Format:** Cross-platform uyumluluk için GLB kullanılmalı
3. **DataStore Persistence:** Scene verisi her değişiklikte otomatik kaydedilmeli
4. **Object Pooling:** Performans için obje havuzu kullanılmalı
5. **Error Boundaries:** Her platform için hata yönetimi sağlanmalı

---

## Kaynaklar

- [Placing objects and handling 3D interaction | Apple Developer Documentation](https://developer.apple.com/documentation/arkit/placing-objects-and-handling-3d-interaction)
- [ARKit and ARCore Mobile AR Development Guide 2026 | Reality Atlas](https://www.reality-atlas.com/learn/arkit-arcore-mobile-ar-development-guide)
- [Performance considerations | ARCore | Google for Developers](https://developers.google.com/ar/develop/performance)
- [Hit-tests place virtual objects in the real world | ARCore | Google for Developers](https://developers.google.com/ar/develop/hit-test)
- [Working with Anchors | ARCore | Google for Developers](https://developers.google.com/ar/develop/anchors)