package com.trendhive.arsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.trendhive.arsample.di.appModules
import com.trendhive.arsample.di.platformDataSourceModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        installSplashScreen()
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Koin DI only if not already started
        // This prevents crash on Activity recreation (e.g., configuration change)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger()
                androidContext(applicationContext)
                modules(platformDataSourceModule() + appModules)
            }
        }

        setContent {
            App()
        }
    }
}
