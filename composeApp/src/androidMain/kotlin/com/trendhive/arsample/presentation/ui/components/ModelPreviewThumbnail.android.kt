package com.trendhive.arsample.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Android implementation of model preview thumbnail.
 * 
 * NOTE: 3D SceneView preview temporarily disabled due to stability issues.
 * Shows a placeholder icon instead. 
 * TODO: Re-enable 3D preview after SceneView stability is resolved.
 */
@Composable
actual fun ModelPreviewThumbnail(
    modelPath: String,
    modifier: Modifier,
    autoRotate: Boolean
) {
    // Simple placeholder - 3D preview disabled for stability
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ViewInAr,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}
