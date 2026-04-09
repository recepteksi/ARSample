package com.trendhive.arsample.di

import com.trendhive.arsample.domain.repository.MediaRepository
import com.trendhive.arsample.infrastructure.persistence.local.ARObjectLocalDataSource
import com.trendhive.arsample.infrastructure.persistence.local.ARSceneDataStore
import com.trendhive.arsample.infrastructure.persistence.local.ModelFileStorage
import com.trendhive.arsample.infrastructure.persistence.local.ARObjectLocalDataSourceIOSImpl
import com.trendhive.arsample.infrastructure.persistence.local.ARSceneDataStoreIOSImpl
import com.trendhive.arsample.infrastructure.persistence.local.ModelFileStorageIOSImpl
import com.trendhive.arsample.infrastructure.persistence.local.MediaRepositoryIOSImpl
import org.koin.dsl.module

actual fun platformDataSourceModule() = module {
    single<ARObjectLocalDataSource> { ARObjectLocalDataSourceIOSImpl() }
    single<ARSceneDataStore> { ARSceneDataStoreIOSImpl() }
    single<ModelFileStorage> { ModelFileStorageIOSImpl() }
    single<MediaRepository> { MediaRepositoryIOSImpl() }
}
