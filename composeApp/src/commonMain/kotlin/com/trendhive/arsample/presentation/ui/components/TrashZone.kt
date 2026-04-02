package com.trendhive.arsample.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.trendhive.arsample.domain.model.TrashZoneState

/**
 * Animated trash zone component for drag-to-delete feature.
 *
 * When user drags an AR object, this trash zone appears at the bottom of the screen.
 * Dropping an object on it deletes the object.
 *
 * Visual specs:
 * - Normal size: 72dp × 72dp
 * - Hover size: scales to 1.2x
 * - Corner radius: 20dp
 * - Shadow: 4dp normal, 8dp on hover
 * - Background: surfaceVariant (normal), error red (hover)
 * - Entry animation: Spring bounce with fadeIn
 * - Idle animation: Subtle breathing pulse
 *
 * @param state The current state of the trash zone (Hidden, Visible, or Hover)
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun TrashZone(
    state: TrashZoneState,
    modifier: Modifier = Modifier
) {
    val isVisible = state != TrashZoneState.Hidden
    val isHovering = state is TrashZoneState.Hover

    // Scale animation with spring effect
    val scale by animateFloatAsState(
        targetValue = when {
            !isVisible -> 0f
            isHovering -> 1.2f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "trash_scale"
    )

    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isHovering -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        },
        animationSpec = tween(200),
        label = "trash_bg"
    )

    // Icon tint animation
    val iconTint by animateColorAsState(
        targetValue = when {
            isHovering -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "trash_icon"
    )

    // Breathing pulse animation when visible (not hovering)
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val finalScale = if (isVisible && !isHovering) scale * breathingScale else scale

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(150)) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(finalScale)
                .size(72.dp)
                .shadow(
                    elevation = if (isHovering) 8.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete zone",
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )

                // Show hint text when hovering
                if (isHovering) {
                    Text(
                        text = "Release",
                        style = MaterialTheme.typography.labelSmall,
                        color = iconTint,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
