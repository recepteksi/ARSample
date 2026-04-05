package com.trendhive.arsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.trendhive.arsample.infrastructure.persistence.local.*
import com.trendhive.arsample.infrastructure.persistence.repository.*
import com.trendhive.arsample.application.usecase.*
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate() and enableEdgeToEdge()
        installSplashScreen()
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Simple DI setup for Android
        val filesDir = applicationContext.filesDir
        val modelFileStorage = ModelFileStorageImpl(applicationContext, filesDir)
        val objectLocalDataSource = ARObjectLocalDataSourceImpl(filesDir)
        val sceneDataStore = ARSceneDataStoreImpl(filesDir)
        
        val objectRepository = ARObjectRepositoryImpl(objectLocalDataSource, modelFileStorage)
        val sceneRepository = ARSceneRepositoryImpl(sceneDataStore)

        val importObjectUseCase = ImportObjectUseCase(objectRepository)
        val getAllObjectsUseCase = GetAllObjectsUseCase(objectRepository)
        val deleteObjectUseCase = DeleteObjectUseCase(objectRepository)
        val placeObjectInSceneUseCase = PlaceObjectInSceneUseCase(sceneRepository, objectRepository)
        val removeObjectFromSceneUseCase = RemoveObjectFromSceneUseCase(sceneRepository)
        val getSceneUseCase = GetSceneUseCase(sceneRepository)
        val saveSceneUseCase = SaveSceneUseCase(sceneRepository)
        val moveObjectUseCase = MoveObjectUseCase(sceneRepository)

        setContent {
            App(
                importObjectUseCase = importObjectUseCase,
                getAllObjectsUseCase = getAllObjectsUseCase,
                deleteObjectUseCase = deleteObjectUseCase,
                placeObjectInSceneUseCase = placeObjectInSceneUseCase,
                removeObjectFromSceneUseCase = removeObjectFromSceneUseCase,
                getSceneUseCase = getSceneUseCase,
                saveSceneUseCase = saveSceneUseCase,
                moveObjectUseCase = moveObjectUseCase,
                sceneRepository = sceneRepository
            )
        }
    }
}
