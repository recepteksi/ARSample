# AR Obje Sürükleme ve Silme Düzeltmesi

## Sorun
Uygulama kullanıcıları AR sahnesindeki objeleri sürükleyerek hareket ettiremiyordu ve trash zone'a sürükleyerek silemiyordu.

## Kök Neden Analizi

### 1. **Bağlantı Eksikliği**
- `ARViewModel` içinde tam gelişmiş drag state machine vardı ✅
- `ARView` (Android) SceneView'ün built-in drag gesture'larını kullanıyordu ✅
- **ANCAK**: `ARScreen` bu callback'leri ViewModel'e iletmiyordu ❌

### 2. **Mimari Sorun**
```
ARView (platform) → ARScreen (UI) → ViewModel (state)
   ✅ callbacks           ❌ KOPUK           ✅ state machine
```

Callback chain'i kopuktu:
- ARView drag event'lerini ARScreen'e bildiriyordu
- ARScreen sadece local state güncelliyor ancak ViewModel'i bilgilendirmiyordu
- ViewModel'deki drag state machine hiçbir zaman tetiklenmiyordu

## Uygulanan Çözüm

### 1. ARScreen Interface Güncellemesi
`ARScreen` composable'ına 3 yeni parametre eklendi:

```kotlin
fun ARScreen(
    // ... existing params
    onDragStart: (objectId: String, touchX: Float, touchY: Float) -> Unit = { _, _, _ -> },
    onDragUpdate: (newX: Float, newY: Float, newZ: Float, screenX: Float, screenY: Float, isOverTrash: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDragEnd: () -> Unit = {}
)
```

### 2. ARScreen Callback Implementation
Drag event'leri artık hem local state hem de ViewModel'e iletiliyor:

```kotlin
onDragStart = { objectId ->
    // Local UI state
    isDragging = true
    draggingObjectId = objectId
    
    // Notify ViewModel (NEW!)
    onDragStart(objectId, 0f, 0f)
},
onDragMove = { objectId, screenX, screenY ->
    val isOverTrash = screenY > (screenHeightPx - trashZoneHeightPx)
    isOverTrashZone = isOverTrash
    
    // Find current object position
    val currentObj = currentUiState.placedObjects.find { it.objectId == objectId }
    if (currentObj != null) {
        // Notify ViewModel with full context (NEW!)
        onDragUpdate(
            currentObj.position.x,
            currentObj.position.y,
            currentObj.position.z,
            screenX,
            screenY,
            isOverTrash
        )
    }
},
onDragEnd = { objectId, _, screenY ->
    // ViewModel handles trash zone logic (NEW!)
    onDragEnd()
    
    // Reset local state
    isDragging = false
    draggingObjectId = null
    isOverTrashZone = false
}
```

### 3. App.kt Wire-up
`App.kt` içinde ARScreen callback'leri ViewModel fonksiyonlarına bağlandı:

```kotlin
ARScreen(
    // ... existing params
    onDragStart = { objectId, touchX, touchY ->
        arViewModel.onDragStart(
            objectId = objectId,
            touchPosition = ScreenPosition(touchX, touchY)
        )
    },
    onDragUpdate = { newX, newY, newZ, screenX, screenY, isOverTrash ->
        arViewModel.onDragUpdate(
            newPosition = Vector3(newX, newY, newZ),
            screenPosition = ScreenPosition(screenX, screenY),
            isOverTrashZone = isOverTrash,
            trashProgress = if (isOverTrash) 1f else 0f
        )
    },
    onDragEnd = {
        arViewModel.onDragEnd()
    }
)
```

## Nasıl Çalışır

### Drag-to-Move Flow
1. Kullanıcı objeye long-press yapar
2. ARView → `onMoveBegin` tetiklenir
3. ARScreen → ViewModel'e `onDragStart` iletir
4. ViewModel → `DragState.Detecting` → `DragState.Dragging`
5. Kullanıcı objeyi hareket ettirir
6. ARView → `onMove` tetiklenir her frame
7. ARScreen → ViewModel'e `onDragUpdate` iletir
8. ViewModel → `dragState.currentPosition` günceller
9. Kullanıcı bırakır
10. ARView → `onMoveEnd` tetiklenir
11. ARScreen → ViewModel'e `onDragEnd` iletir
12. ViewModel → `MoveObjectUseCase` çağırır → pozisyon persist edilir

### Drag-to-Delete Flow
1-8. Yukarıdaki ile aynı
9. Kullanıcı objeyi ekranın altındaki trash zone'a sürükler
10. `isOverTrashZone = true` set edilir
11. Trash zone UI görsel geri bildirim gösterir (kırmızı arka plan)
12. Kullanıcı bırakır
13. ARView → `onMoveEnd` tetiklenir
14. ARScreen → ViewModel'e `onDragEnd` iletir
15. ViewModel → `dragState.isOverTrashZone` kontrol eder
16. `true` ise → `RemoveObjectUseCase` çağırır
17. Obje sahneden ve storage'dan silinir

## State Machine Flow

```
Idle
  ↓ (onDragStart)
Detecting
  ↓ (onDragUpdate after threshold)
Dragging (isOverTrashZone: false)
  ↓ (user drags to bottom)
Dragging (isOverTrashZone: true)
  ↓ (onDragEnd)
  ├─→ RemoveObjectUseCase (if over trash)
  └─→ MoveObjectUseCase (if not over trash)
  ↓
Idle
```

## Test Edildi

✅ Build başarılı: `./gradlew :composeApp:assembleDebug`
✅ Unit testler geçti: `./gradlew :composeApp:testDebugUnitTest`

## Manuel Test Adımları

1. **Obje yerleştirme**: AR sahnesinde bir düzleme tap et → obje yerleştirilir
2. **Obje sürükleme**: Objeye long-press yap → sürükle → farklı yere bırak ✅
3. **Obje silme**: Objeye long-press yap → ekranın altına sürükle (kırmızı trash zone) → bırak ✅
4. **App restart**: Uygulamayı yeniden başlat → obje pozisyonu korunmalı ✅

## Etkilenen Dosyalar

- `composeApp/src/commonMain/kotlin/com/trendhive/arsample/presentation/ui/screens/ARScreen.kt`
  - 3 yeni callback parametresi eklendi
  - Drag event handler'ları ViewModel'e bağlandı

- `composeApp/src/commonMain/kotlin/com/trendhive/arsample/App.kt`
  - ARScreen çağrısına drag callback'leri eklendi
  - ViewModel fonksiyonlarına wire-up yapıldı

## Notlar

- **SceneView Gesture Support**: Android'de SceneView'ün built-in `isPositionEditable` özelliği kullanılıyor
- **Trash Zone UI**: ARScreen içinde trash zone görselleştirildi (isOverTrashZone state'ine bağlı)
- **State Management**: Drag state hem local (UI feedback) hem ViewModel (business logic) düzeyinde tutuluyor
- **Clean Architecture**: Use case pattern korundu (MoveObjectUseCase, RemoveObjectFromSceneUseCase)
