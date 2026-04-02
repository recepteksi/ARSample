package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.DragState
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.ScreenPosition
import com.trendhive.arsample.domain.model.TrashZoneState
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.domain.usecase.MoveObjectUseCase
import com.trendhive.arsample.domain.usecase.PlaceObjectInSceneUseCase
import com.trendhive.arsample.domain.usecase.RemoveObjectFromSceneUseCase
import com.trendhive.arsample.domain.usecase.GetSceneUseCase
import com.trendhive.arsample.domain.usecase.SaveSceneUseCase
import com.trendhive.arsample.domain.repository.ARSceneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class ARUiState(
    val currentScene: ARScene? = null,
    val placedObjects: List<PlacedObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedObjectId: String? = null,
    val dragState: DragState = DragState.Idle,
    val trashZoneState: TrashZoneState = TrashZoneState.Hidden
)

class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase,
    private val getSceneUseCase: GetSceneUseCase,
    private val saveSceneUseCase: SaveSceneUseCase,
    private val sceneRepository: ARSceneRepository,
    private val moveObjectUseCase: MoveObjectUseCase
) : androidx.lifecycle.ViewModel() {

    companion object {
        const val DRAG_THRESHOLD_MS = 150L
        const val DRAG_SLOP_DP = 8f
    }

    private val _uiState = MutableStateFlow(ARUiState())
    val uiState: StateFlow<ARUiState> = _uiState.asStateFlow()

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        loadScene()
    }

    fun loadScene() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val scene = sceneRepository.getOrCreateDefaultScene()
                _uiState.value = _uiState.value.copy(
                    currentScene = scene,
                    placedObjects = scene.objects,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load scene"
                )
            }
        }
    }

    fun placeObject(
        objectId: String,
        position: Vector3,
        rotation: Quaternion = Quaternion.IDENTITY,
        scale: Float = 1f
    ) {
        val sceneId = _uiState.value.currentScene?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = placeObjectUseCase(sceneId, objectId, position, rotation, scale)
            result.fold(
                onSuccess = { scene ->
                    _uiState.value = _uiState.value.copy(
                        currentScene = scene,
                        placedObjects = scene.objects,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to place object"
                    )
                }
            )
        }
    }

    fun removeObject(placedObjectId: String) {
        val sceneId = _uiState.value.currentScene?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = removeObjectUseCase(sceneId, placedObjectId)
            result.fold(
                onSuccess = { scene ->
                    _uiState.value = _uiState.value.copy(
                        currentScene = scene,
                        placedObjects = scene.objects,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to remove object"
                    )
                }
            )
        }
    }

    fun selectObject(objectId: String?) {
        println("ARViewModel.selectObject: objectId=$objectId")
        _uiState.value = _uiState.value.copy(selectedObjectId = objectId)
        println("ARViewModel.selectObject: updated state, selectedObjectId=${_uiState.value.selectedObjectId}")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ==================== Drag Operations ====================

    /**
     * Start drag detection when touch begins on an object.
     * Transitions from Idle to Detecting state.
     */
    fun onDragStart(objectId: String, touchPosition: ScreenPosition) {
        val scene = _uiState.value.currentScene ?: return
        val placedObject = scene.objects.find { it.objectId == objectId } ?: return

        _uiState.value = _uiState.value.copy(
            dragState = DragState.Detecting(
                objectId = objectId,
                initialTouchPosition = touchPosition,
                objectStartPosition = placedObject.position,
                startTime = currentTimeMillis()
            ),
            trashZoneState = TrashZoneState.Visible
        )
    }

    /**
     * Update during drag movement.
     * Transitions from Detecting/Dragging to Dragging state.
     */
    fun onDragUpdate(
        newPosition: Vector3,
        screenPosition: ScreenPosition,
        isOverTrashZone: Boolean,
        trashProgress: Float = 0f
    ) {
        val currentState = _uiState.value.dragState
        if (currentState !is DragState.Detecting && currentState !is DragState.Dragging) return

        val objectId = when (currentState) {
            is DragState.Detecting -> currentState.objectId
            is DragState.Dragging -> currentState.objectId
            else -> return
        }

        _uiState.value = _uiState.value.copy(
            dragState = DragState.Dragging(
                objectId = objectId,
                currentPosition = newPosition,
                isOverTrashZone = isOverTrashZone,
                trashZoneProgress = trashProgress
            ),
            trashZoneState = if (isOverTrashZone) TrashZoneState.Hover(trashProgress) else TrashZoneState.Visible
        )
    }

    /**
     * End drag - either reposition or delete based on drop location.
     */
    fun onDragEnd() {
        when (val currentState = _uiState.value.dragState) {
            is DragState.Dragging -> {
                if (currentState.isOverTrashZone) {
                    // Delete the object
                    removeObject(currentState.objectId)
                } else {
                    // Reposition the object
                    moveObject(currentState.objectId, currentState.currentPosition)
                }
            }
            else -> { /* No action needed */ }
        }
        resetDragState()
    }

    /**
     * Cancel drag - restore original position.
     */
    fun onDragCancel() {
        resetDragState()
    }

    private fun resetDragState() {
        _uiState.value = _uiState.value.copy(
            dragState = DragState.Idle,
            trashZoneState = TrashZoneState.Hidden
        )
    }

    /**
     * Move object to new position using MoveObjectUseCase.
     */
    private fun moveObject(objectId: String, newPosition: Vector3) {
        val sceneId = _uiState.value.currentScene?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = moveObjectUseCase(sceneId, objectId, newPosition)
            result.fold(
                onSuccess = { scene ->
                    _uiState.value = _uiState.value.copy(
                        currentScene = scene,
                        placedObjects = scene.objects,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to move object"
                    )
                }
            )
        }
    }
}