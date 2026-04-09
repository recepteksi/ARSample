package com.trendhive.arsample.di

import com.trendhive.arsample.domain.repository.ARObjectRepository
import com.trendhive.arsample.domain.repository.ARSceneRepository
import com.trendhive.arsample.domain.repository.MediaRepository
import com.trendhive.arsample.infrastructure.persistence.repository.ARObjectRepositoryImpl
import com.trendhive.arsample.infrastructure.persistence.repository.ARSceneRepositoryImpl
import com.trendhive.arsample.infrastructure.persistence.mapper.ARObjectMapper
import com.trendhive.arsample.infrastructure.persistence.mapper.ARSceneMapper
import com.trendhive.arsample.application.usecase.CapturePhotoUseCase
import com.trendhive.arsample.application.usecase.DeletePhotoUseCase
import com.trendhive.arsample.application.usecase.DeleteVideoUseCase
import com.trendhive.arsample.application.usecase.GetPhotosUseCase
import com.trendhive.arsample.application.usecase.GetVideosUseCase
import com.trendhive.arsample.application.usecase.ImportObjectUseCase
import com.trendhive.arsample.application.usecase.GetAllObjectsUseCase
import com.trendhive.arsample.application.usecase.DeleteObjectUseCase
import com.trendhive.arsample.application.usecase.PlaceObjectInSceneUseCase
import com.trendhive.arsample.application.usecase.RemoveObjectFromSceneUseCase
import com.trendhive.arsample.application.usecase.GetSceneUseCase
import com.trendhive.arsample.application.usecase.SaveSceneUseCase
import com.trendhive.arsample.application.usecase.MoveObjectUseCase
import com.trendhive.arsample.application.usecase.RecordVideoUseCase
import com.trendhive.arsample.presentation.viewmodel.GalleryViewModel
import com.trendhive.arsample.presentation.viewmodel.ObjectListViewModel
import com.trendhive.arsample.presentation.viewmodel.ARViewModel
import org.koin.dsl.module

/**
 * Data layer module - Repository implementations and mappers
 */
val dataModule = module {
    // Mappers
    single { ARObjectMapper() }
    single { ARSceneMapper() }
    
    // Repositories
    single<ARObjectRepository> { 
        ARObjectRepositoryImpl(get(), get()) 
    }
    single<ARSceneRepository> { 
        ARSceneRepositoryImpl(get()) 
    }
    // Note: MediaRepository is provided by platformModule (platform-specific implementation)
}

/**
 * Application layer module - Use cases
 */
val applicationModule = module {
    // Object use cases
    factory { ImportObjectUseCase(get()) }
    factory { GetAllObjectsUseCase(get()) }
    factory { DeleteObjectUseCase(get()) }
    
    // Scene use cases
    factory { PlaceObjectInSceneUseCase(get(), get()) }
    factory { RemoveObjectFromSceneUseCase(get()) }
    factory { GetSceneUseCase(get()) }
    factory { SaveSceneUseCase(get()) }
    factory { MoveObjectUseCase(get()) }
    
    // Media use cases
    factory { CapturePhotoUseCase(get()) }
    factory { RecordVideoUseCase(get()) }
    factory { GetPhotosUseCase(get()) }
    factory { GetVideosUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { DeleteVideoUseCase(get()) }
}

/**
 * Presentation layer module - ViewModels
 */
val presentationModule = module {
    single { ObjectListViewModel(get(), get(), get()) }
    single {
        ARViewModel(
            placeObjectUseCase = get(),
            removeObjectUseCase = get(),
            getSceneUseCase = get(),
            saveSceneUseCase = get(),
            sceneRepository = get(),
            moveObjectUseCase = get(),
            capturePhotoUseCase = get(),
            recordVideoUseCase = get()
        )
    }
    // GalleryViewModel is registered as single to prevent multiple instances being created
    // on recomposition (factory would create a new instance each time koinInject() is called,
    // causing repeated loadMedia() calls and leaking CoroutineScope instances).
    single { GalleryViewModel(get(), get(), get(), get()) }
}

/**
 * Combined app modules
 */
val appModules = listOf(
    dataModule,
    applicationModule,
    presentationModule
)
