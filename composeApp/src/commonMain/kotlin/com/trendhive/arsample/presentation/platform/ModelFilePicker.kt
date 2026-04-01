package com.trendhive.arsample.presentation.platform

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that launches a platform file picker.
 * When a file is picked, [onPickedUri] is called with the URI string.
 */
@Composable
expect fun rememberModelFilePicker(
    onPickedUri: (String) -> Unit
): () -> Unit

