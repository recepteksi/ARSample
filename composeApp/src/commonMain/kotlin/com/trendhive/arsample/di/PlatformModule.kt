package com.trendhive.arsample.di

import com.trendhive.arsample.infrastructure.persistence.local.ARObjectLocalDataSource
import com.trendhive.arsample.infrastructure.persistence.local.ARSceneDataStore
import com.trendhive.arsample.infrastructure.persistence.local.ModelFileStorage

/**
 * Platform-specific data sources provider
 * Implemented in androidMain and iosMain
 */
expect fun platformDataSourceModule(): org.koin.core.module.Module
