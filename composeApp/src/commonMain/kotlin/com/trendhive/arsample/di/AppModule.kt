package com.trendhive.arsample.di

import com.trendhive.arsample.domain.repository.ARObjectRepository
import com.trendhive.arsample.domain.repository.ARSceneRepository
import com.trendhive.arsample.infrastructure.persistence.repository.ARObjectRepositoryImpl
import com.trendhive.arsample.infrastructure.persistence.repository.ARSceneRepositoryImpl
import com.trendhive.arsample.infrastructure.persistence.mapper.ARObjectMapper
import com.trendhive.arsample.infrastructure.persistence.mapper.ARSceneMapper
import com.trendhive.arsample.application.usecase.ImportObjectUseCase
import com.trendhive.arsample.application.usecase.GetAllObjectsUseCase
import com.trendhive.arsample.application.usecase.DeleteObjectUseCase
import com.trendhive.arsample.application.usecase.PlaceObjectInSceneUseCase
import com.trendhive.arsample.application.usecase.RemoveObjectFromSceneUseCase
import com.trendhive.arsample.application.usecase.GetSceneUseCase
import com.trendhive.arsample.application.usecase.SaveSceneUseCase
import com.trendhive.arsample.application.usecase.MoveObjectUseCase
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
}

/**
 * Presentation layer module - ViewModels
 */
val presentationModule = module {
    factory { ObjectListViewModel(get(), get(), get()) }
    factory { ARViewModel(get(), get(), get(), get(), get(), get()) }
}

/**
 * Combined app modules
 */
val appModules = listOf(
    dataModule,
    applicationModule,
    presentationModule
)
