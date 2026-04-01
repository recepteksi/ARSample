package com.trendhive.arsample.presentation.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberModelFilePicker(
    onPickedUri: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Not all providers allow persistable permissions; temporary permission may still work.
            }
            onPickedUri(it.toString())
        }
    }

    return remember(launcher) {
        {
            // GLB is often reported as application/octet-stream, so we keep it permissive.
            launcher.launch(arrayOf("*/*"))
        }
    }
}

