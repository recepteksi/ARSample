# Drag-and-Drop & Delete Feature Design Document

**Version:** 1.0  
**Date:** 2026-03-30  
**Author:** Design Analysis Agent

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [UX/UI Design](#uxui-design)
3. [Interaction Flow](#interaction-flow)
4. [State Machine](#state-machine)
5. [Architecture Design](#architecture-design)
6. [Platform Implementation](#platform-implementation)
7. [Component Specifications](#component-specifications)
8. [Implementation Roadmap](#implementation-roadmap)

---

## Executive Summary

This document describes the design for implementing drag-to-move and drag-to-delete functionality for AR objects in the ARSample application. The feature enables users to:

1. **Drag to Reposition**: Move placed AR objects to new positions on detected surfaces
2. **Drag to Delete**: Delete objects by dragging them to a trash zone

### Key Design Principles

- **Immediate Feedback**: Visual feedback within 100ms of user action
- **Discoverability**: Trash icon appears contextually when dragging starts
- **Error Prevention**: Confirm destructive actions with visual/haptic feedback
- **Platform Consistency**: Same UX behavior on Android and iOS

---

## UX/UI Design

### 1. Trash Icon Appearance & Animation

#### Entry Animation (When Dragging Starts)
```
Timeline: 0ms → 250ms

0ms:    Trash icon spawns at bottom-center of screen
        - Scale: 0.0 → Invisible
        - Opacity: 0.0
        
50ms:   Begin spring animation
        - Scale: 0.0 → 1.2 (overshoot)
        - Opacity: 0.0 → 1.0
        
150ms:  Settle animation
        - Scale: 1.2 → 1.0
        
250ms:  Idle state - subtle breathing pulse
        - Scale: 1.0 ↔ 1.05 (loop)
```

#### Visual Specifications
```
┌─────────────────────────────────────────┐
│                                         │
│           AR Scene View                 │
│                                         │
│       ┌─────┐                          │
│       │ 🪑  │  ← Object being dragged  │
│       └─────┘                          │
│            \                            │
│             \                           │
│              \                          │
│               ↓                         │
│         ┌─────────┐                     │
│         │  🗑️    │  ← Trash Zone       │
│         │ Delete  │                     │
│         └─────────┘                     │
│                                         │
├─────────────────────────────────────────┤
│  [Back]                    [Import] [⋮] │
└─────────────────────────────────────────┘
```

**Trash Zone Specs:**
| Property | Value |
|----------|-------|
| Position | Bottom-center, 48dp from edge |
| Size (Normal) | 72dp × 72dp |
| Size (Hover) | 88dp × 88dp |
| Corner Radius | 20dp |
| Background (Normal) | Surface variant, 80% opacity |
| Background (Hover) | Error color, 90% opacity |
| Icon Size | 32dp |
| Drop Shadow | 4dp elevation |

### 2. Visual Feedback During Drag

#### Object Drag Visual States

**A. Normal State (Not Dragging)**
```kotlin
ObjectVisuals(
    scale = originalScale,
    opacity = 1.0f,
    shadowIntensity = 0.3f,
    outlineColor = null
)
```

**B. Dragging State (Not Over Trash)**
```kotlin
ObjectVisuals(
    scale = originalScale * 1.1f,     // 10% larger
    opacity = 0.85f,                   // Slightly transparent
    shadowIntensity = 0.6f,            // Enhanced shadow
    outlineColor = Color.White,        // Selection outline
    outlineWidth = 2.dp
)
```

**C. Hover Over Trash State**
```kotlin
ObjectVisuals(
    scale = originalScale * 0.8f,     // Shrink toward trash
    opacity = 0.5f,                    // Highly transparent
    shadowIntensity = 0.2f,
    outlineColor = Color.Red,          // Danger outline
    outlineWidth = 3.dp,
    tintColor = Color.Red.copy(alpha = 0.3f)  // Red tint overlay
)
```

#### Trail Effect During Drag
```
Object Position History (last 5 frames):
[P0] ← Current position
 ↑
[P1] ← 1 frame ago (opacity: 0.4)
 ↑
[P2] ← 2 frames ago (opacity: 0.3)
 ↑
[P3] ← 3 frames ago (opacity: 0.2)
 ↑
[P4] ← 4 frames ago (opacity: 0.1)
```

### 3. Trash Zone Hover Behavior

#### State Transitions
```
┌────────────────┐
│  IDLE STATE    │
│  (Invisible)   │
└───────┬────────┘
        │ onDragStart
        ▼
┌────────────────┐
│  VISIBLE       │
│  Scale: 1.0    │◄─────────────────┐
│  BG: Gray      │                  │
└───────┬────────┘                  │
        │ onEnterTrashZone          │ onExitTrashZone
        ▼                           │
┌────────────────┐                  │
│  HOVER STATE   │──────────────────┘
│  Scale: 1.2    │
│  BG: Red       │
│  Vibrate: Yes  │
└───────┬────────┘
        │ onDrop
        ▼
┌────────────────┐
│  DELETE ANIM   │
│  Shrink→0      │
│  Fade→0        │
└───────┬────────┘
        │ Animation complete
        ▼
┌────────────────┐
│  IDLE STATE    │
└────────────────┘
```

#### Hover Feedback
| Feedback Type | Description |
|---------------|-------------|
| **Visual** | Trash icon scales 1.0 → 1.2, background turns error red |
| **Haptic** | Light vibration (20ms) on enter, heavy (50ms) on drop |
| **Audio** | Optional: Subtle "crumple" sound on delete (accessibility toggle) |

### 4. Deletion Visual Effect

#### Animation Sequence (400ms total)
```
Phase 1 (0-100ms): Object shrinks toward trash center
    - Scale: current → 0.2
    - Position: current → trash center
    - Easing: EaseInCubic

Phase 2 (100-200ms): Particle burst
    - 8-12 particles spawn from object center
    - Particles fly outward with gravity
    - Color matches object's dominant color

Phase 3 (200-300ms): Trash "gulp" animation
    - Trash icon scale: 1.2 → 0.9 → 1.0 (quick squish)
    
Phase 4 (300-400ms): Cleanup
    - Remove object from scene
    - Trash icon fades out (if no more drags)
    - Success haptic feedback
```

---

## Interaction Flow

### 1. Gesture Differentiation

#### Touch Gesture Decision Tree
```
User touches object
        │
        ▼
    ┌───────────────┐
    │ Is touch on   │──── No ───► Ignore / AR Placement
    │ placed object?│
    └───────┬───────┘
            │ Yes
            ▼
    ┌───────────────┐
    │ Touch         │
    │ duration      │
    └───────┬───────┘
            │
     ┌──────┴──────┐
     │             │
   < 150ms      ≥ 150ms
     │             │
     ▼             ▼
┌─────────┐   ┌──────────┐
│ TAP     │   │ DRAG     │
│ Select  │   │ Begin    │
└─────────┘   └──────────┘
```

#### Gesture Parameters
| Parameter | Value | Description |
|-----------|-------|-------------|
| `TAP_THRESHOLD` | 150ms | Max duration for tap recognition |
| `DRAG_SLOP` | 8dp | Min movement to start drag |
| `LONG_PRESS_TIMEOUT` | 500ms | Duration for context menu (future) |
| `VELOCITY_THRESHOLD` | 100dp/s | Min velocity for fling gesture |

### 2. Drag Operation Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    COMPLETE DRAG FLOW                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  [1] Touch Down on Object                                   │
│      ├── Record initial touch position                      │
│      ├── Record object's current position                   │
│      └── Start drag detection timer (150ms)                 │
│                         │                                   │
│                         ▼                                   │
│  [2] Movement Detected (> DRAG_SLOP)                       │
│      ├── Enter DRAGGING state                               │
│      ├── Show trash icon with entry animation               │
│      ├── Begin object lift animation                        │
│      ├── Disable plane tap placement                        │
│      └── Start continuous hit testing                       │
│                         │                                   │
│                         ▼                                   │
│  [3] Continuous Drag (ACTION_MOVE)                         │
│      ├── Perform raycast from touch point                   │
│      ├── If valid surface → Move object to hit position     │
│      ├── If no surface → Keep last valid position           │
│      ├── Check if over trash zone                           │
│      └── Update visual feedback accordingly                 │
│                         │                                   │
│            ┌────────────┴────────────┐                      │
│            ▼                         ▼                      │
│  [4a] Touch Up (Normal)      [4b] Touch Up (Over Trash)    │
│       ├── Exit DRAGGING              ├── Play delete anim  │
│       ├── Commit new position        ├── Remove object     │
│       ├── Hide trash icon            ├── Hide trash icon   │
│       ├── Save to repository         ├── Haptic feedback   │
│       └── Re-enable placement        └── Update repository │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 3. Hit Testing During Drag

#### Android (ARCore/SceneView)
```kotlin
// One-shot raycast for drag operations
fun performDragHitTest(
    frame: Frame,
    screenX: Float,
    screenY: Float
): Position? {
    val hitResults = frame.hitTest(screenX, screenY)
    
    return hitResults
        .filterIsInstance<HitResult>()
        .filter { hit ->
            val trackable = hit.trackable
            // Accept planes and feature points
            (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) ||
            trackable is Point
        }
        .minByOrNull { hit ->
            // Prefer closest hit
            hit.distance
        }
        ?.let { hit ->
            val pose = hit.hitPose
            Position(pose.tx(), pose.ty(), pose.tz())
        }
}
```

#### iOS (ARKit)
```swift
// Raycast for drag operations
func performDragHitTest(
    arView: ARView,
    point: CGPoint
) -> SIMD3<Float>? {
    let results = arView.raycast(
        from: point,
        allowing: .estimatedPlane,
        alignment: .any
    )
    
    return results.first?.worldTransform.translation
}
```

---

## State Machine

### DragState Sealed Class Hierarchy

```kotlin
sealed class DragState {
    object Idle : DragState()
    
    data class Detecting(
        val objectId: String,
        val initialTouchPosition: Offset,
        val objectStartPosition: Vector3,
        val startTime: Long
    ) : DragState()
    
    data class Dragging(
        val objectId: String,
        val currentPosition: Vector3,
        val isOverTrashZone: Boolean,
        val trashZoneProgress: Float  // 0.0 - 1.0 for animation
    ) : DragState()
    
    data class Dropping(
        val objectId: String,
        val finalPosition: Vector3,
        val action: DropAction
    ) : DragState()
}

enum class DropAction {
    REPOSITION,  // Drop on valid surface
    DELETE,      // Drop on trash zone
    CANCEL       // Invalid drop location
}
```

### State Machine Diagram

```
                                ┌─────────────────────────┐
                                │         IDLE            │
                                │                         │
                                │  • No active drag       │
                                │  • Trash icon hidden    │
                                │  • Normal object render │
                                └───────────┬─────────────┘
                                            │
                                            │ onTouchDown(objectId)
                                            ▼
                                ┌─────────────────────────┐
                                │       DETECTING         │
                                │                         │
                                │  • Timer started        │
                                │  • Tracking movement    │
                                │  • Object highlighted   │
                                └───────────┬─────────────┘
                                            │
                         ┌──────────────────┼──────────────────┐
                         │                  │                  │
                   movement < slop    movement ≥ slop    timer expired
                   AND timer < 150ms  OR timer ≥ 150ms   no movement
                         │                  │                  │
                         ▼                  ▼                  ▼
                ┌──────────────┐   ┌─────────────────┐   ┌──────────┐
                │   → IDLE     │   │    DRAGGING     │   │ → IDLE   │
                │   (tap)      │   │                 │   │ (cancel) │
                └──────────────┘   │  • Object lifted│   └──────────┘
                                   │  • Trash visible│
                                   │  • Hit testing  │
                                   └────────┬────────┘
                                            │
                              ┌─────────────┴─────────────┐
                              │                           │
                         onMove()                    onTouchUp()
                              │                           │
                              ▼                           ▼
                    ┌──────────────────┐        ┌─────────────────┐
                    │ Update position  │        │    DROPPING     │
                    │ Check trash zone │        │                 │
                    │ → Stay DRAGGING  │        │  action based   │
                    └──────────────────┘        │  on location    │
                                                └────────┬────────┘
                                                         │
                                    ┌────────────────────┼────────────────────┐
                                    │                    │                    │
                            over trash zone       on valid surface      invalid
                                    │                    │                    │
                                    ▼                    ▼                    ▼
                            ┌─────────────┐    ┌─────────────────┐   ┌─────────────┐
                            │   DELETE    │    │   REPOSITION    │   │   CANCEL    │
                            │             │    │                 │   │             │
                            │ • Remove    │    │ • Update pos    │   │ • Restore   │
                            │ • Animate   │    │ • Save scene    │   │   position  │
                            │ • Feedback  │    │ • Feedback      │   │             │
                            └──────┬──────┘    └────────┬────────┘   └──────┬──────┘
                                   │                    │                   │
                                   └────────────────────┼───────────────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │      IDLE       │
                                               └─────────────────┘
```

---

## Architecture Design

### 1. New Domain Entities/Value Objects

#### DragOperation Value Object
```kotlin
// domain/model/DragOperation.kt
data class DragOperation(
    val objectId: String,
    val startPosition: Vector3,
    val currentPosition: Vector3,
    val state: DragOperationState
) {
    val displacement: Vector3
        get() = Vector3(
            currentPosition.x - startPosition.x,
            currentPosition.y - startPosition.y,
            currentPosition.z - startPosition.z
        )
    
    val distance: Float
        get() = displacement.length()
}

enum class DragOperationState {
    ACTIVE,
    COMPLETED,
    CANCELLED
}
```

#### TrashZone Value Object
```kotlin
// domain/model/TrashZone.kt
data class TrashZone(
    val screenBounds: Rect,  // Screen coordinates
    val isActive: Boolean = false
) {
    fun contains(screenPosition: Offset): Boolean {
        return screenBounds.contains(screenPosition)
    }
    
    fun distanceFromCenter(screenPosition: Offset): Float {
        val center = screenBounds.center
        return sqrt(
            (screenPosition.x - center.x).pow(2) +
            (screenPosition.y - center.y).pow(2)
        )
    }
    
    // Progress 0.0 (edge) to 1.0 (center) for hover animation
    fun hoverProgress(screenPosition: Offset): Float {
        if (!contains(screenPosition)) return 0f
        val maxDistance = minOf(screenBounds.width, screenBounds.height) / 2
        val distance = distanceFromCenter(screenPosition)
        return (1f - (distance / maxDistance)).coerceIn(0f, 1f)
    }
}
```

#### ObjectTransform Value Object (Enhanced)
```kotlin
// domain/model/ObjectTransform.kt
data class ObjectTransform(
    val position: Vector3,
    val rotation: Quaternion,
    val scale: Float
) {
    companion object {
        val IDENTITY = ObjectTransform(
            position = Vector3.ZERO,
            rotation = Quaternion.IDENTITY,
            scale = 1f
        )
    }
    
    fun lerp(target: ObjectTransform, progress: Float): ObjectTransform {
        return ObjectTransform(
            position = position.lerp(target.position, progress),
            rotation = rotation.slerp(target.rotation, progress),
            scale = scale + (target.scale - scale) * progress
        )
    }
}
```

### 2. New Use Cases

#### MoveObjectUseCase
```kotlin
// domain/usecase/MoveObjectUseCase.kt
class MoveObjectUseCase(
    private val sceneRepository: ARSceneRepository
) {
    /**
     * Moves a placed object to a new position within the scene.
     * 
     * @param sceneId The scene containing the object
     * @param objectId The placed object's ID
     * @param newPosition The target position in world coordinates
     * @return Result containing updated scene or error
     */
    suspend operator fun invoke(
        sceneId: String,
        objectId: String,
        newPosition: Vector3
    ): Result<ARScene> {
        // Validation
        require(objectId.isNotBlank()) { "Object ID cannot be blank" }
        
        val scene = sceneRepository.getScene(sceneId)
            ?: return Result.failure(IllegalArgumentException("Scene not found: $sceneId"))
        
        val objectExists = scene.objects.any { it.objectId == objectId }
        if (!objectExists) {
            return Result.failure(IllegalArgumentException("Object not found: $objectId"))
        }
        
        // Update object position
        val updatedObjects = scene.objects.map { obj ->
            if (obj.objectId == objectId) {
                obj.copy(position = newPosition)
            } else {
                obj
            }
        }
        
        val updatedScene = scene.copy(objects = updatedObjects)
        sceneRepository.saveScene(updatedScene)
        
        return Result.success(updatedScene)
    }
}
```

#### ValidateDragTargetUseCase
```kotlin
// domain/usecase/ValidateDragTargetUseCase.kt
class ValidateDragTargetUseCase {
    /**
     * Determines if a drag target position is valid for object placement.
     * 
     * @param position The target world position
     * @param surfaceNormal The normal vector of the detected surface (if any)
     * @return DragTargetValidation result
     */
    operator fun invoke(
        position: Vector3?,
        surfaceNormal: Vector3? = null
    ): DragTargetValidation {
        if (position == null) {
            return DragTargetValidation.Invalid(
                reason = "No valid surface detected"
            )
        }
        
        // Validate position is within reasonable bounds
        if (position.y < -10f || position.y > 10f) {
            return DragTargetValidation.Invalid(
                reason = "Position out of bounds"
            )
        }
        
        // Check surface angle (if normal provided)
        if (surfaceNormal != null) {
            val upVector = Vector3(0f, 1f, 0f)
            val angle = acos(surfaceNormal.dot(upVector))
            
            // Reject surfaces steeper than 45 degrees for horizontal placement
            if (angle > Math.PI / 4) {
                return DragTargetValidation.SteepSurface(
                    position = position,
                    surfaceAngle = angle.toFloat()
                )
            }
        }
        
        return DragTargetValidation.Valid(position)
    }
}

sealed class DragTargetValidation {
    data class Valid(val position: Vector3) : DragTargetValidation()
    data class SteepSurface(
        val position: Vector3,
        val surfaceAngle: Float
    ) : DragTargetValidation()
    data class Invalid(val reason: String) : DragTargetValidation()
}
```

### 3. ViewModel Drag State Management

#### Enhanced ARUiState
```kotlin
// presentation/viewmodel/ARUiState.kt
data class ARUiState(
    val currentScene: ARScene? = null,
    val placedObjects: List<PlacedObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedObjectId: String? = null,
    
    // NEW: Drag-related state
    val dragState: DragState = DragState.Idle,
    val trashZoneState: TrashZoneState = TrashZoneState.Hidden
)

sealed class DragState {
    object Idle : DragState()
    
    data class Dragging(
        val objectId: String,
        val originalPosition: Vector3,
        val currentPosition: Vector3,
        val isOverTrashZone: Boolean = false
    ) : DragState()
}

sealed class TrashZoneState {
    object Hidden : TrashZoneState()
    object Visible : TrashZoneState()
    data class Hover(val progress: Float) : TrashZoneState()  // 0.0 - 1.0
}
```

#### ARViewModel Extensions
```kotlin
// presentation/viewmodel/ARViewModel.kt
class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase,
    private val moveObjectUseCase: MoveObjectUseCase,  // NEW
    private val getSceneUseCase: GetSceneUseCase,
    private val saveSceneUseCase: SaveSceneUseCase,
    private val sceneRepository: ARSceneRepository
) : ViewModel() {
    
    // --- DRAG OPERATIONS ---
    
    /**
     * Called when user begins dragging an object.
     */
    fun startDrag(objectId: String, initialPosition: Vector3) {
        val currentState = _uiState.value
        
        // Verify object exists
        val obj = currentState.placedObjects.find { it.objectId == objectId }
            ?: return
        
        _uiState.value = currentState.copy(
            selectedObjectId = objectId,
            dragState = DragState.Dragging(
                objectId = objectId,
                originalPosition = initialPosition,
                currentPosition = initialPosition,
                isOverTrashZone = false
            ),
            trashZoneState = TrashZoneState.Visible
        )
    }
    
    /**
     * Called continuously during drag to update position.
     */
    fun updateDrag(newPosition: Vector3, isOverTrashZone: Boolean) {
        val currentDragState = _uiState.value.dragState
        if (currentDragState !is DragState.Dragging) return
        
        _uiState.value = _uiState.value.copy(
            dragState = currentDragState.copy(
                currentPosition = newPosition,
                isOverTrashZone = isOverTrashZone
            ),
            trashZoneState = if (isOverTrashZone) {
                TrashZoneState.Hover(progress = 1f)  // Could be animated
            } else {
                TrashZoneState.Visible
            }
        )
    }
    
    /**
     * Called when user releases the object.
     */
    fun endDrag() {
        val currentDragState = _uiState.value.dragState
        if (currentDragState !is DragState.Dragging) return
        
        viewModelScope.launch {
            if (currentDragState.isOverTrashZone) {
                // DELETE OPERATION
                handleDragToDelete(currentDragState.objectId)
            } else {
                // MOVE OPERATION
                handleDragToMove(
                    objectId = currentDragState.objectId,
                    newPosition = currentDragState.currentPosition
                )
            }
            
            // Reset drag state
            _uiState.value = _uiState.value.copy(
                dragState = DragState.Idle,
                trashZoneState = TrashZoneState.Hidden
            )
        }
    }
    
    /**
     * Called when user cancels drag (e.g., gesture cancelled).
     */
    fun cancelDrag() {
        val currentDragState = _uiState.value.dragState
        if (currentDragState !is DragState.Dragging) return
        
        // Object snaps back to original position (handled by UI)
        _uiState.value = _uiState.value.copy(
            dragState = DragState.Idle,
            trashZoneState = TrashZoneState.Hidden
        )
    }
    
    private suspend fun handleDragToMove(objectId: String, newPosition: Vector3) {
        val sceneId = _uiState.value.currentScene?.id ?: return
        
        moveObjectUseCase(sceneId, objectId, newPosition)
            .onSuccess { updatedScene ->
                _uiState.value = _uiState.value.copy(
                    currentScene = updatedScene,
                    placedObjects = updatedScene.objects
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to move object: ${error.message}"
                )
            }
    }
    
    private suspend fun handleDragToDelete(objectId: String) {
        val sceneId = _uiState.value.currentScene?.id ?: return
        
        removeObjectUseCase(sceneId, objectId)
            .onSuccess { updatedScene ->
                _uiState.value = _uiState.value.copy(
                    currentScene = updatedScene,
                    placedObjects = updatedScene.objects,
                    selectedObjectId = null
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete object: ${error.message}"
                )
            }
    }
}
```

### 4. UI Components

#### TrashZone Composable
```kotlin
// presentation/ui/components/TrashZone.kt
@Composable
fun TrashZone(
    state: TrashZoneState,
    modifier: Modifier = Modifier
) {
    val isVisible = state != TrashZoneState.Hidden
    val isHovering = state is TrashZoneState.Hover
    
    // Animation values
    val scale by animateFloatAsState(
        targetValue = when {
            !isVisible -> 0f
            isHovering -> 1.2f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "trash_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isHovering -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        },
        animationSpec = tween(200),
        label = "trash_bg"
    )
    
    val iconTint by animateColorAsState(
        targetValue = when {
            isHovering -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "trash_icon"
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(72.dp)
                .shadow(
                    elevation = if (isHovering) 8.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
                
                if (isHovering) {
                    Text(
                        text = "Release to delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = iconTint,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
```

#### DragOverlay Composable
```kotlin
// presentation/ui/components/DragOverlay.kt
@Composable
fun DragOverlay(
    dragState: DragState,
    trashZoneState: TrashZoneState,
    trashZoneBounds: (Rect) -> Unit,  // Report bounds to parent
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Trash Zone at bottom center
        TrashZone(
            state = trashZoneState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .onGloballyPositioned { coordinates ->
                    trashZoneBounds(
                        Rect(
                            coordinates.positionInRoot(),
                            coordinates.size.toSize()
                        )
                    )
                }
        )
        
        // Drag indicator overlay (if needed)
        if (dragState is DragState.Dragging) {
            DragIndicator(
                isOverTrash = dragState.isOverTrashZone
            )
        }
    }
}

@Composable
private fun DragIndicator(isOverTrash: Boolean) {
    // Subtle screen overlay when dragging
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (isOverTrash) {
                    Color.Red.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
    )
}
```

---

## Platform Implementation

### Android (SceneView/ARCore)

#### Enhanced ARView with Drag Support
```kotlin
// androidMain/ar/ARView.kt (additions)

@Composable
fun ARView(
    modifier: Modifier = Modifier,
    placedObjects: List<PlacedObject> = emptyList(),
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float, scale: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit = {},
    modelPathToLoad: String? = null,
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit = { _, _ -> },
    // NEW: Drag callbacks
    dragState: DragState = DragState.Idle,
    onDragStart: (objectId: String, position: Vector3) -> Unit = { _, _ -> },
    onDragUpdate: (position: Vector3, screenPosition: Offset) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    // ... existing state ...
    
    // NEW: Drag state tracking
    var isDragging by remember { mutableStateOf(false) }
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    var dragStartPosition by remember { mutableStateOf<Position?>(null) }
    var touchDownTime by remember { mutableStateOf(0L) }
    var touchDownPosition by remember { mutableStateOf(Offset.Zero) }
    
    // DRAG CONSTANTS
    val DRAG_SLOP = 24f  // pixels
    val DRAG_THRESHOLD_MS = 150L
    
    AndroidView(
        factory = { context ->
            ARSceneView(context).apply {
                // ... existing configuration ...
                
                onTouchEvent = touchEvent@{ e, hitResult ->
                    // Handle scale gesture first
                    scaleGestureDetector?.onTouchEvent(e)
                    if (scaleGestureDetector?.isInProgress == true) {
                        return@touchEvent true
                    }
                    
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Check if touching an existing node
                            val touchedNode = findNodeAtScreenPosition(e.x, e.y)
                            if (touchedNode != null) {
                                touchDownTime = System.currentTimeMillis()
                                touchDownPosition = Offset(e.x, e.y)
                                draggedNodeId = touchedNode.id
                                return@touchEvent true
                            }
                        }
                        
                        MotionEvent.ACTION_MOVE -> {
                            draggedNodeId?.let { nodeId ->
                                val currentPosition = Offset(e.x, e.y)
                                val distance = (currentPosition - touchDownPosition).getDistance()
                                val elapsed = System.currentTimeMillis() - touchDownTime
                                
                                // Check if should start dragging
                                if (!isDragging && 
                                    (distance > DRAG_SLOP || elapsed > DRAG_THRESHOLD_MS)) {
                                    isDragging = true
                                    val node = currentNodes[nodeId]
                                    if (node != null) {
                                        dragStartPosition = node.position
                                        val pos = Vector3(
                                            node.position.x,
                                            node.position.y,
                                            node.position.z
                                        )
                                        onDragStart(nodeId, pos)
                                        // Make node editable for built-in gestures
                                        node.isEditable = true
                                        node.isPositionEditable = true
                                    }
                                }
                                
                                // If dragging, update position
                                if (isDragging) {
                                    val frame = getARFrameSafely(this)
                                    frame?.let { f ->
                                        val hitResults = f.hitTest(e.x, e.y)
                                        val validHit = filterHitResults(hitResults).firstOrNull()
                                        
                                        if (validHit != null) {
                                            val pose = validHit.hitPose
                                            val newPos = Vector3(pose.tx(), pose.ty(), pose.tz())
                                            
                                            // Update node position visually
                                            currentNodes[nodeId]?.position = Position(
                                                pose.tx(), pose.ty(), pose.tz()
                                            )
                                            
                                            // Notify ViewModel
                                            onDragUpdate(newPos, currentPosition)
                                        }
                                    }
                                }
                                return@touchEvent true
                            }
                        }
                        
                        MotionEvent.ACTION_UP -> {
                            if (isDragging && draggedNodeId != null) {
                                currentNodes[draggedNodeId]?.let { node ->
                                    node.isEditable = false
                                    node.isPositionEditable = false
                                }
                                onDragEnd()
                                isDragging = false
                                draggedNodeId = null
                                dragStartPosition = null
                                return@touchEvent true
                            }
                            
                            // If not dragging, this was a tap
                            draggedNodeId?.let { nodeId ->
                                val elapsed = System.currentTimeMillis() - touchDownTime
                                if (elapsed < DRAG_THRESHOLD_MS) {
                                    // Handle as tap/selection
                                    // ... existing tap handling ...
                                }
                            }
                            
                            // Reset state
                            draggedNodeId = null
                            touchDownTime = 0L
                        }
                        
                        MotionEvent.ACTION_CANCEL -> {
                            if (isDragging) {
                                // Restore original position
                                draggedNodeId?.let { nodeId ->
                                    dragStartPosition?.let { startPos ->
                                        currentNodes[nodeId]?.position = startPos
                                    }
                                    currentNodes[nodeId]?.let { node ->
                                        node.isEditable = false
                                        node.isPositionEditable = false
                                    }
                                }
                                onDragCancel()
                            }
                            isDragging = false
                            draggedNodeId = null
                            dragStartPosition = null
                        }
                    }
                    
                    // ... existing tap-to-place logic for non-object touches ...
                    true
                }
                
                arSceneView = this
            }
        },
        modifier = modifier,
        update = { /* ... */ }
    )
}

// Helper function to find node at screen position
private fun ARSceneView.findNodeAtScreenPosition(x: Float, y: Float): ModelNode? {
    // Use collision system for hit testing against nodes
    val hitResult = collisionSystem?.hitTest(MotionEvent.obtain(
        0, 0, MotionEvent.ACTION_DOWN, x, y, 0
    ))
    return hitResult?.firstOrNull()?.node as? ModelNode
}
```

### iOS (ARKit) Implementation Notes

```kotlin
// iosMain/ar/ARViewWrapper.kt (conceptual additions)

// iOS drag handling uses UIGestureRecognizer
// Key gestures:
// - UILongPressGestureRecognizer: Detect drag start (150ms threshold)
// - UIPanGestureRecognizer: Handle drag movement
// - ARView.raycast(): Continuous hit testing during drag

/*
 iOS Implementation Strategy:
 
 1. Add gesture recognizers to ARView:
    - longPressRecognizer → starts drag after 150ms
    - panRecognizer → tracks drag movement
 
 2. Node hit testing:
    - arView.entity(at: point) → find touched entity
 
 3. Raycast during drag:
    - arView.raycast(from:allowing:alignment:)
    - Update entity position to raycast hit point
 
 4. State management via callbacks to shared ViewModel
*/
```

---

## Component Specifications

### Accessibility

| Feature | Implementation |
|---------|----------------|
| **Screen Reader** | Announce "Dragging [object name]" on drag start |
| **VoiceOver (iOS)** | Custom rotor action for "Move" and "Delete" |
| **TalkBack (Android)** | Accessibility events for state changes |
| **Haptic Feedback** | Configurable via accessibility settings |
| **Reduced Motion** | Skip particle effects, use fade instead |

### Performance Targets

| Metric | Target | Notes |
|--------|--------|-------|
| Touch Response | < 16ms | Single frame |
| Visual Update | 60 FPS | During drag |
| Hit Test | < 5ms | Per frame |
| Memory Overhead | < 2 MB | For drag state + animations |

### Error Handling

| Scenario | Response |
|----------|----------|
| Lost AR Tracking | Pause drag, show "Tracking Lost" indicator |
| No Valid Surface | Keep object at last valid position |
| Network Error (save) | Queue save, retry with exponential backoff |
| Low Memory | Disable particle effects |

---

## Implementation Roadmap

### Phase 1: Core Drag Infrastructure (3 days)
- [ ] Add DragState to ARUiState
- [ ] Implement MoveObjectUseCase
- [ ] Add drag callbacks to ARView signature
- [ ] Basic touch-to-drag detection

### Phase 2: Visual Feedback (2 days)
- [ ] TrashZone composable
- [ ] DragOverlay composable
- [ ] Object visual states during drag
- [ ] Entry/exit animations

### Phase 3: Gesture Refinement (2 days)
- [ ] Gesture differentiation (tap vs drag)
- [ ] Continuous hit testing during drag
- [ ] Edge cases (tracking lost, invalid surface)
- [ ] Haptic feedback integration

### Phase 4: Platform Parity (2 days)
- [ ] iOS gesture recognizer setup
- [ ] iOS raycast integration
- [ ] Cross-platform testing
- [ ] Performance optimization

### Phase 5: Polish & Testing (2 days)
- [ ] Accessibility implementation
- [ ] Reduced motion support
- [ ] Unit tests for use cases
- [ ] Integration tests for drag flow

---

## References

1. [SceneView Node.kt - Gesture Handling](https://github.com/sceneview/sceneview/blob/main/sceneview/src/main/java/io/github/sceneview/node/Node.kt)
2. [SceneView NodeGestureDelegate.kt](https://github.com/sceneview/sceneview/blob/main/sceneview/src/main/java/io/github/sceneview/node/NodeGestureDelegate.kt)
3. [Android Compose Drag Gestures](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling)
4. [ARCore Hit Testing](https://developers.google.com/ar/develop/hit-test)
5. [Material Design Gestures](https://m2.material.io/design/interaction/gestures.html)

---

*Document generated by Design Analysis Agent*
