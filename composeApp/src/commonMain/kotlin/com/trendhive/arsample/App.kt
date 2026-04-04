package com.trendhive.arsample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trendhive.arsample.application.usecase.*
import com.trendhive.arsample.presentation.ui.screens.ARScreen
import com.trendhive.arsample.presentation.ui.screens.ObjectListScreen
import com.trendhive.arsample.presentation.viewmodel.ARViewModel
import com.trendhive.arsample.presentation.viewmodel.ObjectListViewModel

@Composable
fun App(
    importObjectUseCase: ImportObjectUseCase,
    getAllObjectsUseCase: GetAllObjectsUseCase,
    deleteObjectUseCase: DeleteObjectUseCase,
    placeObjectInSceneUseCase: PlaceObjectInSceneUseCase,
    removeObjectFromSceneUseCase: RemoveObjectFromSceneUseCase,
    getSceneUseCase: GetSceneUseCase,
    saveSceneUseCase: SaveSceneUseCase,
    moveObjectUseCase: MoveObjectUseCase,
    sceneRepository: com.trendhive.arsample.domain.repository.ARSceneRepository
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.AR(null)) }

            val objectListViewModel: ObjectListViewModel = viewModel {
                ObjectListViewModel(
                    getAllObjectsUseCase,
                    deleteObjectUseCase,
                    importObjectUseCase
                )
            }
            val objectListUiState by objectListViewModel.uiState.collectAsState()
            
            // Create ARViewModel once, outside the when block
            val arViewModel: ARViewModel = viewModel {
                ARViewModel(
                    placeObjectInSceneUseCase,
                    removeObjectFromSceneUseCase,
                    getSceneUseCase,
                    saveSceneUseCase,
                    sceneRepository,
                    moveObjectUseCase
                )
            }
            val arUiState by arViewModel.uiState.collectAsState()

            when (val screen = currentScreen) {
                is Screen.ObjectList -> {
                    ObjectListScreen(
                        uiState = objectListUiState,
                        onObjectSelected = { id ->
                            objectListViewModel.clearImportSuccess()
                            currentScreen = Screen.AR(id)
                        },
                        onStartAR = { currentScreen = Screen.AR(null) },
                        onImportClick = { uri, name, type ->
                            objectListViewModel.importObject(uri, name, type)
                        },
                        onDeleteObject = { objectListViewModel.deleteObject(it) }
                    )
                }
                is Screen.AR -> {
                    LaunchedEffect(screen.selectedObjectId) {
                        if (screen.selectedObjectId != null) {
                            arViewModel.selectObject(screen.selectedObjectId)
                        }
                    }

                    ARScreen(
                        uiState = arUiState,
                        availableObjects = objectListUiState.objects,
                        onSelectObject = { arViewModel.selectObject(it) },
                        onNavigateBack = { currentScreen = Screen.ObjectList },
                        onImportObject = { uri, name, type ->
                            objectListViewModel.importObject(uri, name, type)
                        },
                        onObjectPlaced = { objectId, x, y, z ->
                            arViewModel.placeObject(
                                objectId = objectId,
                                position = com.trendhive.arsample.domain.model.Vector3(x, y, z)
                            )
                        },
                        onObjectRemoved = { arViewModel.removeObject(it) }
                    )
                }
            }
        }
    }
}

sealed class Screen {
    data object ObjectList : Screen()
    data class AR(val selectedObjectId: String?) : Screen()
}
