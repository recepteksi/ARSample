package com.trendhive.arsample.presentation.viewmodel

import com.trendhive.arsample.domain.model.ARObject
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.application.usecase.DeleteObjectUseCase
import com.trendhive.arsample.application.usecase.GetAllObjectsUseCase
import com.trendhive.arsample.application.usecase.ImportObjectUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class ObjectListUiState(
    val objects: List<ARObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val importSuccess: Boolean = false
)

class ObjectListViewModel(
    private val getAllObjectsUseCase: GetAllObjectsUseCase,
    private val deleteObjectUseCase: DeleteObjectUseCase,
    private val importObjectUseCase: ImportObjectUseCase
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(ObjectListUiState())
    val uiState: StateFlow<ObjectListUiState> = _uiState.asStateFlow()

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        loadObjects()
    }

    fun loadObjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val objects = getAllObjectsUseCase()
                _uiState.value = _uiState.value.copy(
                    objects = objects,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load objects"
                )
            }
        }
    }

    fun importObject(uri: String, name: String, modelType: ModelType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, importSuccess = false)
            val result = importObjectUseCase(uri, name, modelType)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        importSuccess = true
                    )
                    loadObjects()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to import object"
                    )
                }
            )
        }
    }

    fun deleteObject(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                deleteObjectUseCase(id)
                loadObjects()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete object"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearImportSuccess() {
        _uiState.value = _uiState.value.copy(importSuccess = false)
    }
}