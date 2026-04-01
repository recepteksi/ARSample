package com.trendhive.arsample.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberModelFilePicker(
    onPickedUri: (String) -> Unit
): () -> Unit {
    // iOS file picker is not implemented yet.
    // This keeps the shared UI compiling for iOS targets.
    return remember { { /* no-op */ } }
}

