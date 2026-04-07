package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.DragState
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.ScreenPosition
import com.trendhive.arsample.domain.model.TrashZoneState
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.domain.model.currentTimeMillis
import com.trendhive.arsample.application.usecase.CapturePhotoUseCase
import com.trendhive.arsample.application.usecase.MoveObjectUseCase
import com.trendhive.arsample.application.usecase.PlaceObjectInSceneUseCase
import com.trendhive.arsample.application.usecase.RecordVideoUseCase
import com.trendhive.arsample.application.usecase.RemoveObjectFromSceneUseCase
import com.trendhive.arsample.application.usecase.GetSceneUseCase
import com.trendhive.arsample.application.usecase.SaveSceneUseCase
import com.trendhive.arsample.domain.repository.ARSceneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Sealed class representing photo capture states.
 */
sealed class CaptureState {
    object Idle : CaptureState()
    object Capturing : CaptureState()
    data class Success(val message: String) : CaptureState()
    data class Error(val message: String) : CaptureState()
}

/**
 * Sealed class representing video recording states.
 */
sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Stopping : RecordingState()
    data class Success(val message: String) : RecordingState()
    data class Error(val message: String) : RecordingState()
}

data class ARUiState(
    val currentScene: ARScene? = null,
    val placedObjects: List<PlacedObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedObjectId: String? = null,
    val dragState: DragState = DragState.Idle,
    val trashZoneState: TrashZoneState = TrashZoneState.Hidden,
    val captureState: CaptureState = CaptureState.Idle,
    val captureRequest: Boolean = false,
    val recordingState: RecordingState = RecordingState.Idle,
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Long = 0L
)

class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase,
    private val getSceneUseCase: GetSceneUseCase,
    private val saveSceneUseCase: SaveSceneUseCase,
    private val sceneRepository: ARSceneRepository,
    private val moveObjectUseCase: MoveObjectUseCase,
    private val capturePhotoUseCase: CapturePhotoUseCase? = null,
    private val recordVideoUseCase: RecordVideoUseCase? = null
) : androidx.lifecycle.ViewModel() {

    companion object {
        const val DRAG_THRESHOLD_MS = 150L
        const val DRAG_SLOP_DP = 8f
    }

    private val _uiState = MutableStateFlow(ARUiState())
    val uiState: StateFlow<ARUiState> = _uiState.asStateFlow()

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Timer job for recording duration
    private var recordingTimerJob: Job? = null

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
        _uiState.value = _uiState.value.copy(selectedObjectId = objectId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Update a placed object's position after it was moved in the native AR view.
     *
     * Triggered by SceneView's node drag callbacks (onMoveEnd).
     */
    fun updateObjectPosition(placedObjectId: String, x: Float, y: Float, z: Float) {
        val newPosition = Vector3(x, y, z)

        // Optimistic UI update (keeps state in sync with the dragged node).
        _uiState.value = _uiState.value.copy(
            currentScene = _uiState.value.currentScene?.let { scene ->
                scene.copy(
                    objects = scene.objects.map { obj ->
                        if (obj.objectId == placedObjectId) obj.copy(position = newPosition) else obj
                    }
                )
            },
            placedObjects = _uiState.value.placedObjects.map { obj ->
                if (obj.objectId == placedObjectId) obj.copy(position = newPosition) else obj
            }
        )

        // Persist & reconcile with repository state.
        moveObject(placedObjectId, newPosition)
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
            else -> { /* No action needed for other states */ }
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

    // ==================== Photo Capture Operations ====================

    /**
     * Request a photo capture from the AR view.
     * This triggers the capture flow in PlatformARView.
     */
    fun requestCapture() {
        if (capturePhotoUseCase == null) {
            _uiState.value = _uiState.value.copy(
                captureState = CaptureState.Error("Photo capture not available")
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            captureState = CaptureState.Capturing,
            captureRequest = true
        )
    }

    /**
     * Handle the captured photo data from the AR view.
     * Called by the UI when PixelCopy/snapshot completes.
     */
    fun onPhotoCaptured(imageData: ByteArray?) {
        // Reset capture request
        _uiState.value = _uiState.value.copy(captureRequest = false)
        
        if (imageData == null) {
            _uiState.value = _uiState.value.copy(
                captureState = CaptureState.Error("Failed to capture photo")
            )
            return
        }
        
        if (capturePhotoUseCase == null) {
            _uiState.value = _uiState.value.copy(
                captureState = CaptureState.Error("Photo capture not available")
            )
            return
        }
        
        viewModelScope.launch {
            capturePhotoUseCase.invoke(imageData).fold(
                onSuccess = { photo ->
                    _uiState.value = _uiState.value.copy(
                        captureState = CaptureState.Success("Photo saved")
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        captureState = CaptureState.Error(e.message ?: "Failed to save photo")
                    )
                }
            )
        }
    }

    /**
     * Clear the capture state (dismiss toast/snackbar).
     */
    fun clearCaptureState() {
        _uiState.value = _uiState.value.copy(captureState = CaptureState.Idle)
    }

    // ==================== Video Recording Operations ====================

    /**
     * Start the recording duration timer.
     * Updates recordingDurationSeconds every second.
     */
    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            var seconds = 0L
            while (isActive) {
                _uiState.value = _uiState.value.copy(recordingDurationSeconds = seconds)
                delay(1000)
                seconds++
            }
        }
    }

    /**
     * Stop the recording duration timer.
     */
    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        _uiState.value = _uiState.value.copy(recordingDurationSeconds = 0L)
    }

    /**
     * Start video recording.
     */
    fun startRecording() {
        if (recordVideoUseCase == null) {
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.Error("Video recording not available")
            )
            return
        }

        viewModelScope.launch {
            recordVideoUseCase.startRecording().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        recordingState = RecordingState.Recording,
                        isRecording = true
                    )
                    startRecordingTimer()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        recordingState = RecordingState.Error(e.message ?: "Failed to start recording"),
                        isRecording = false
                    )
                }
            )
        }
    }

    /**
     * Stop video recording.
     */
    fun stopRecording() {
        if (recordVideoUseCase == null) {
            _uiState.value = _uiState.value.copy(
                recordingState = RecordingState.Error("Video recording not available"),
                isRecording = false
            )
            return
        }

        stopRecordingTimer()
        _uiState.value = _uiState.value.copy(recordingState = RecordingState.Stopping)

        viewModelScope.launch {
            recordVideoUseCase.stopRecording().fold(
                onSuccess = { video ->
                    _uiState.value = _uiState.value.copy(
                        recordingState = RecordingState.Success("Video saved (${video.durationMs / 1000}s)"),
                        isRecording = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        recordingState = RecordingState.Error(e.message ?: "Failed to save video"),
                        isRecording = false
                    )
                }
            )
        }
    }

    /**
     * Toggle video recording state.
     */
    fun toggleRecording() {
        if (_uiState.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    /**
     * Clear the recording state (dismiss toast/snackbar).
     */
    fun clearRecordingState() {
        _uiState.value = _uiState.value.copy(recordingState = RecordingState.Idle)
    }
}