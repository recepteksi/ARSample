package com.trendhive.arsample.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific composable to display a captured photo from a file path or content URI.
 * Android: loads and renders the image asynchronously.
 * iOS: shows a placeholder icon.
 *
 * [uri] may be a content:// URI string (MediaStore) or an absolute file path.
 */
@Composable
expect fun PhotoThumbnail(uri: String?, modifier: Modifier = Modifier)
