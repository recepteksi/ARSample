package com.trendhive.arsample.di

import com.trendhive.arsample.domain.repository.MediaRepository
import com.trendhive.arsample.infrastructure.persistence.local.ARObjectLocalDataSource
import com.trendhive.arsample.infrastructure.persistence.local.ARSceneDataStore
import com.trendhive.arsample.infrastructure.persistence.local.ModelFileStorage
import com.trendhive.arsample.infrastructure.persistence.local.ARObjectLocalDataSourceImpl
import com.trendhive.arsample.infrastructure.persistence.local.ARSceneDataStoreImpl
import com.trendhive.arsample.infrastructure.persistence.local.ModelFileStorageImpl
import com.trendhive.arsample.infrastructure.persistence.local.MediaRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformDataSourceModule() = module {
    single<ARObjectLocalDataSource> { 
        ARObjectLocalDataSourceImpl(androidContext().filesDir) 
    }
    single<ARSceneDataStore> { 
        ARSceneDataStoreImpl(androidContext().filesDir) 
    }
    single<ModelFileStorage> { 
        ModelFileStorageImpl(androidContext(), androidContext().filesDir) 
    }
    single<MediaRepository> {
        MediaRepositoryImpl(androidContext())
    }
}
