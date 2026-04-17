package com.trendhive.arsample.presentation.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
import java.io.File

private const val TAG = "PhotoThumbnail"

@Composable
actual fun PhotoThumbnail(uri: String?, modifier: Modifier) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uri) {
        if (uri.isNullOrBlank()) {
            bitmap = null
            return@LaunchedEffect
        }

        bitmap = withContext(Dispatchers.IO) {
            try {
                val parsedUri = Uri.parse(uri)

                // Handle content:// URIs
                if (parsedUri.scheme == "content") {
                    val inputStream = context.contentResolver.openInputStream(parsedUri)
                    if (inputStream == null) {
                        Log.w(TAG, "Failed to open content URI: $uri")
                        return@withContext null
                    }
                    inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }
                // Handle file:// and absolute paths
                else {
                    val filePath = if (uri.startsWith("file://")) {
                        uri.substring(7)
                    } else {
                        uri
                    }

                    val file = File(filePath)
                    if (!file.exists()) {
                        Log.w(TAG, "File does not exist: $filePath")
                        return@withContext null
                    }

                    BitmapFactory.decodeFile(filePath)?.asImageBitmap()
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Malformed image data for URI: $uri", e)
                null
            } catch (e: SecurityException) {
                Log.e(TAG, "Permission denied for URI: $uri", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode bitmap for URI: $uri", e)
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