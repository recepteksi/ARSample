package com.trendhive.arsample.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Camera-style record button with white outer ring and red inner circle.
 * When recording, inner circle becomes a red square (stop icon).
 * Includes press animation (scale down to 0.9f).
 */
@Composable
fun CameraStyleRecordButton(
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "record_button_scale"
    )
    
    // Animate inner shape transformation (circle to square)
    val innerCornerRadius by animateFloatAsState(
        targetValue = if (isRecording) 6f else 50f,
        animationSpec = tween(durationMillis = 200),
        label = "inner_shape"
    )
    
    val innerSize by animateFloatAsState(
        targetValue = if (isRecording) 24f else 52f,
        animationSpec = tween(durationMillis = 200),
        label = "inner_size"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(
                width = 4.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggleRecording
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner red circle/square
        Box(
            modifier = Modifier
                .size(innerSize.dp)
                .clip(RoundedCornerShape(innerCornerRadius.dp))
                .background(Color(0xFFFF0000))
        )
    }
}

/**
 * Recording timer display showing elapsed time in HH:MM:SS format.
 * Features a pulsing red dot indicator and semi-transparent red background.
 */
@Composable
fun RecordingTimerDisplay(
    durationSeconds: Long,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )
    
    // Format duration as HH:MM:SS
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFF0000).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pulsing red dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(dotAlpha)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            )
            
            // REC text
            Text(
                text = "REC",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )
            
            // Timer
            Text(
                text = timeString,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Secondary camera control button (for photo capture, gallery, camera switch).
 */
@Composable
fun CameraControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Complete camera controls bar with photo capture, record button, and gallery access.
 */
@Composable
fun CameraControlsBar(
    isRecording: Boolean,
    onCapturePhoto: () -> Unit,
    onToggleRecording: () -> Unit,
    onOpenGallery: () -> Unit,
    onSwitchCamera: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery button (bottom left)
        CameraControlButton(
            onClick = onOpenGallery,
            enabled = !isRecording
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = "Open Gallery",
                tint = if (isRecording) Color.Gray else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Photo capture button (left of record)
        CameraControlButton(
            onClick = onCapturePhoto,
            modifier = Modifier.offset(x = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Photo",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Main record button (center, larger)
        CameraStyleRecordButton(
            isRecording = isRecording,
            onToggleRecording = onToggleRecording
        )
        
        // Switch camera button (right of record) - placeholder for symmetry
        if (onSwitchCamera != null) {
            CameraControlButton(
                onClick = onSwitchCamera,
                modifier = Modifier.offset(x = (-16).dp),
                enabled = !isRecording
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Switch Camera",
                    tint = if (isRecording) Color.Gray else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            // Empty spacer for alignment
            Box(modifier = Modifier.size(56.dp).offset(x = (-16).dp))
        }
        
        // Empty spacer for symmetry with gallery button
        Box(modifier = Modifier.size(56.dp))
    }
}

/**
 * Recording border glow effect - subtle red border when recording.
 */
@Composable
fun BoxScope.RecordingBorderGlow(
    isRecording: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "border_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    AnimatedVisibility(
        visible = isRecording,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300)),
        modifier = Modifier.matchParentSize()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 3.dp,
                    color = Color.Red.copy(alpha = glowAlpha),
                    shape = RoundedCornerShape(0.dp)
                )
        )
    }
}
