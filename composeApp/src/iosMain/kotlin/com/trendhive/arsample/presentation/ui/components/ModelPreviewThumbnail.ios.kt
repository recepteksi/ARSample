package com.trendhive.arsample.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * iOS implementation of 3D model preview thumbnail.
 * 
 * Since SceneView is not available on iOS, this shows a placeholder
 * ViewInAr icon styled to match the preview aesthetic.
 * 
 * Future enhancement: Could use RealityKit via interop for actual 3D preview.
 */
@Composable
actual fun ModelPreviewThumbnail(
    modelPath: String,
    modifier: Modifier,
    autoRotate: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Use custom ViewInAr icon (same as AppIcons pattern)
        androidx.compose.material3.Icon(
            imageVector = ViewInArIconPreview,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}

/**
 * Custom ViewInAr icon for iOS (following AppIcons.ios.kt pattern)
 */
private val ViewInArIconPreview: ImageVector
    get() = ImageVector.Builder(
        name = "ViewInAr",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Main 3D cube outline
        path(fill = SolidColor(Color.Black)) {
            // Cube front face
            moveTo(3f, 4f)
            lineTo(3f, 10f)
            lineTo(5f, 10f)
            lineTo(5f, 6f)
            lineTo(9f, 6f)
            lineTo(9f, 4f)
            close()
            
            // Cube back corner (top-right)
            moveTo(15f, 4f)
            lineTo(15f, 6f)
            lineTo(19f, 6f)
            lineTo(19f, 10f)
            lineTo(21f, 10f)
            lineTo(21f, 4f)
            close()
            
            // Cube front corner (bottom-left)
            moveTo(3f, 14f)
            lineTo(3f, 20f)
            lineTo(9f, 20f)
            lineTo(9f, 18f)
            lineTo(5f, 18f)
            lineTo(5f, 14f)
            close()
            
            // Cube back corner (bottom-right)
            moveTo(15f, 18f)
            lineTo(15f, 20f)
            lineTo(21f, 20f)
            lineTo(21f, 14f)
            lineTo(19f, 14f)
            lineTo(19f, 18f)
            close()
            
            // Center 3D shape
            moveTo(12f, 8f)
            lineTo(8f, 10.5f)
            lineTo(8f, 15.5f)
            lineTo(12f, 18f)
            lineTo(16f, 15.5f)
            lineTo(16f, 10.5f)
            close()
            
            // Inner highlight
            moveTo(12f, 9.5f)
            lineTo(14.5f, 11f)
            lineTo(14.5f, 14f)
            lineTo(12f, 15.5f)
            lineTo(9.5f, 14f)
            lineTo(9.5f, 11f)
            close()
        }
    }.build()
