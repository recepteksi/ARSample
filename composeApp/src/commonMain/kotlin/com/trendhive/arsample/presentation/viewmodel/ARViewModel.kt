package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.domain.model.ARScene
import com.trendhive.arsample.domain.model.PlacedObject
import com.trendhive.arsample.domain.model.Quaternion
import com.trendhive.arsample.domain.model.Vector3
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
    val selectedObjectId: String? = null
)

class ARViewModel(
    private val placeObjectUseCase: PlaceObjectInSceneUseCase,
    private val removeObjectUseCase: RemoveObjectFromSceneUseCase,
    private val getSceneUseCase: GetSceneUseCase,
    private val saveSceneUseCase: SaveSceneUseCase,
    private val sceneRepository: ARSceneRepository
) : androidx.lifecycle.ViewModel() {

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
        _uiState.value = _uiState.value.copy(selectedObjectId = objectId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}