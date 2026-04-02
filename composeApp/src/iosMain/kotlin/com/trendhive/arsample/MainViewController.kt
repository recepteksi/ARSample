package com.trendhive.arsample

import androidx.compose.ui.window.ComposeUIViewController
import com.trendhive.arsample.data.local.*
import com.trendhive.arsample.data.repository.*
import com.trendhive.arsample.domain.usecase.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController { 
    // Simple DI setup for iOS
    val modelFileStorage = ModelFileStorageIOSImpl()
    val objectLocalDataSource = ARObjectLocalDataSourceIOSImpl()
    val sceneDataStore = ARSceneDataStoreIOSImpl()
    
    val objectRepository = ARObjectRepositoryImpl(objectLocalDataSource, modelFileStorage)
    val sceneRepository = ARSceneRepositoryImpl(sceneDataStore)

    val importObjectUseCase = ImportObjectUseCase(objectRepository)
    val getAllObjectsUseCase = GetAllObjectsUseCase(objectRepository)
    val deleteObjectUseCase = DeleteObjectUseCase(objectRepository)
    val placeObjectInSceneUseCase = PlaceObjectInSceneUseCase(sceneRepository, objectRepository)
    val removeObjectFromSceneUseCase = RemoveObjectFromSceneUseCase(sceneRepository)
    val getSceneUseCase = GetSceneUseCase(sceneRepository)
    val saveSceneUseCase = SaveSceneUseCase(sceneRepository)

    App(
        importObjectUseCase = importObjectUseCase,
        getAllObjectsUseCase = getAllObjectsUseCase,
        deleteObjectUseCase = deleteObjectUseCase,
        placeObjectInSceneUseCase = placeObjectInSceneUseCase,
        removeObjectFromSceneUseCase = removeObjectFromSceneUseCase,
        getSceneUseCase = getSceneUseCase,
        saveSceneUseCase = saveSceneUseCase,
        sceneRepository = sceneRepository
    )
}