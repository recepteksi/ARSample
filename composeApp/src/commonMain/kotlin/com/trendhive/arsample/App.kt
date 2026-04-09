package com.trendhive.arsample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trendhive.arsample.domain.model.MediaItem
import com.trendhive.arsample.presentation.ui.screens.ARScreen
import com.trendhive.arsample.presentation.ui.screens.GalleryScreen
import com.trendhive.arsample.presentation.ui.screens.ObjectGalleryScreen
import com.trendhive.arsample.presentation.ui.screens.ObjectListScreen
import com.trendhive.arsample.presentation.viewmodel.ARViewModel
import com.trendhive.arsample.presentation.viewmodel.GalleryViewModel
import com.trendhive.arsample.presentation.viewmodel.ObjectListViewModel
import org.koin.compose.koinInject

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.AR(null)) }

            // Inject ViewModels via Koin (only inject when needed to avoid premature initialization)
            val objectListViewModel: ObjectListViewModel = koinInject()
            val objectListUiState by objectListViewModel.uiState.collectAsState()
            
            val arViewModel: ARViewModel = koinInject()
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
                is Screen.ObjectGallery -> {
                    ObjectGalleryScreen(
                        uiState = objectListUiState,
                        onObjectClick = { arObject ->
                            objectListViewModel.clearImportSuccess()
                            currentScreen = Screen.AR(arObject.id)
                        },
                        onObjectDelete = { id ->
                            objectListViewModel.deleteObject(id)
                        },
                        onImportClick = { uri, name, type ->
                            objectListViewModel.importObject(uri, name, type)
                        },
                        onNavigateBack = { currentScreen = Screen.AR(null) },
                        onNavigateToAR = { currentScreen = Screen.AR(null) }
                    )
                }
                is Screen.Gallery -> {
                    // Lazy inject GalleryViewModel only when Gallery screen is shown
                    // This prevents premature MediaRepository access before user navigates to Gallery
                    val galleryViewModel: GalleryViewModel = koinInject()
                    val galleryUiState by galleryViewModel.uiState.collectAsState()
                    
                    GalleryScreen(
                        uiState = galleryUiState,
                        onNavigateBack = { currentScreen = Screen.AR(null) },
                        onPhotoClick = { galleryViewModel.selectMedia(MediaItem.Photo(it)) },
                        onVideoClick = { galleryViewModel.selectMedia(MediaItem.Video(it)) },
                        onDeletePhoto = { galleryViewModel.deletePhoto(it) },
                        onDeleteVideo = { galleryViewModel.deleteVideo(it) },
                        onFilterChange = { galleryViewModel.setFilter(it) },
                        onClosePreview = { galleryViewModel.closePreview() },
                        onClearError = { galleryViewModel.clearError() }
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
                        onNavigateBack = { currentScreen = Screen.ObjectGallery },
                        onImportObject = { uri, name, type ->
                            objectListViewModel.importObject(uri, name, type)
                        },
                        onObjectPlaced = { objectId, x, y, z ->
                            arViewModel.placeObject(
                                objectId = objectId,
                                position = com.trendhive.arsample.domain.model.Vector3(x, y, z)
                            )
                        },
                        onObjectRemoved = { arViewModel.removeObject(it) },
                        onObjectDeleted = { objectListViewModel.deleteObject(it) },
                        onObjectPositionChanged = { placedObjectId, x, y, z ->
                            arViewModel.updateObjectPosition(placedObjectId, x, y, z)
                        },
                        onDragStart = { objectId, touchX, touchY ->
                            arViewModel.onDragStart(
                                objectId = objectId,
                                touchPosition = com.trendhive.arsample.domain.model.ScreenPosition(touchX, touchY)
                            )
                        },
                        onDragUpdate = { newX, newY, newZ, screenX, screenY, isOverTrash ->
                            arViewModel.onDragUpdate(
                                newPosition = com.trendhive.arsample.domain.model.Vector3(newX, newY, newZ),
                                screenPosition = com.trendhive.arsample.domain.model.ScreenPosition(screenX, screenY),
                                isOverTrashZone = isOverTrash,
                                trashProgress = if (isOverTrash) 1f else 0f
                            )
                        },
                        onDragEnd = {
                            arViewModel.onDragEnd()
                        },
                        onToggleRecording = {
                            arViewModel.toggleRecording()
                        },
                        onClearRecordingState = {
                            arViewModel.clearRecordingState()
                        },
                        onCapturePhoto = {
                            arViewModel.requestCapture()
                        },
                        onOpenGallery = {
                            currentScreen = Screen.Gallery
                        }
                    )
                }
            }
        }
    }
}

sealed class Screen {
    data object ObjectList : Screen()
    data object ObjectGallery : Screen()
    data object Gallery : Screen()
    data class AR(val selectedObjectId: String?) : Screen()
}
