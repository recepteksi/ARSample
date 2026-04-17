package com.trendhive.arsample.presentation.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun PhotoThumbnail(uri: String?, modifier: Modifier) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uri) {
        if (uri == null) {
            bitmap = null
            return@LaunchedEffect
        }
        bitmap = withContext(Dispatchers.IO) {
            try {
                val parsedUri = Uri.parse(uri)
                if (parsedUri.scheme == "content") {
                    context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } else {
                    // Absolute file path
                    BitmapFactory.decodeFile(uri)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Last captured photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
