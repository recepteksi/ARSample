package com.trendhive.arsample.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.DragState
import com.trendhive.arsample.domain.model.TrashZoneState
import kotlin.math.sqrt

/**
 * Full-screen overlay for drag operations.
 * Shows TrashZone at bottom and provides visual feedback during drag.
 *
 * Visual behavior:
 * 1. Normal drag: Transparent overlay, TrashZone visible at bottom
 * 2. Hover over trash: Light red tint on entire screen, TrashZone highlighted
 * 3. Release on trash: Object deleted
 *
 * @param dragState Current drag state from ViewModel
 * @param trashZoneState Current trash zone state
 * @param onTrashZoneBoundsChanged Callback when trash zone bounds change (for hit detection)
 * @param modifier Optional modifier
 */
@Composable
fun DragOverlay(
    dragState: DragState,
    trashZoneState: TrashZoneState,
    onTrashZoneBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDragging = dragState is DragState.Dragging || dragState is DragState.Detecting
    val isOverTrash = (dragState as? DragState.Dragging)?.isOverTrashZone == true

    // Subtle red tint when hovering over trash
    val overlayColor by animateColorAsState(
        targetValue = when {
            isOverTrash -> Color.Red.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "overlay_color"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Screen overlay tint (only when over trash)
        if (isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor)
            )
        }

        // Trash zone at bottom center
        TrashZone(
            state = trashZoneState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp) // Above navigation area
                .onGloballyPositioned { coordinates ->
                    onTrashZoneBoundsChanged(coordinates.boundsInRoot())
                }
        )
    }
}

/**
 * Helper function to check if a screen position is within the trash zone.
 *
 * @param x X coordinate in screen pixels
 * @param y Y coordinate in screen pixels
 * @param trashZoneBounds The bounds of the trash zone in root coordinates
 * @return True if the position is within the trash zone bounds
 */
fun isPositionInTrashZone(
    x: Float,
    y: Float,
    trashZoneBounds: Rect
): Boolean {
    return trashZoneBounds.contains(Offset(x, y))
}

/**
 * Calculate hover progress based on distance from center.
 * Returns 0.0 at edge, 1.0 at center.
 *
 * This can be used to scale visual feedback based on how close
 * the dragged object is to the center of the trash zone.
 *
 * @param x X coordinate in screen pixels
 * @param y Y coordinate in screen pixels
 * @param trashZoneBounds The bounds of the trash zone in root coordinates
 * @return Progress value from 0.0 (at edge) to 1.0 (at center), or 0.0 if outside bounds
 */
fun calculateTrashZoneProgress(
    x: Float,
    y: Float,
    trashZoneBounds: Rect
): Float {
    if (!trashZoneBounds.contains(Offset(x, y))) {
        return 0f
    }

    val center = trashZoneBounds.center
    val maxDistance = minOf(trashZoneBounds.width, trashZoneBounds.height) / 2
    val dx = x - center.x
    val dy = y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    return (1f - (distance / maxDistance)).coerceIn(0f, 1f)
}
