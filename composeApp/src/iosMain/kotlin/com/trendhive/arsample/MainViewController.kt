package com.trendhive.arsample

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController { 
    // Koin handles DI - see App.kt for ViewModels injection via koinInject()
    App()
}