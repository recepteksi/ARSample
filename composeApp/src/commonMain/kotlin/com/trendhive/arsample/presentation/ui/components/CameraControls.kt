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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Icon
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
        targetValue = if (isRecording) 4f else 50f,
        animationSpec = tween(durationMillis = 200),
        label = "inner_shape"
    )
    
    // Inner size relative to outer - larger when idle, smaller square when recording
    val innerSizeRatio by animateFloatAsState(
        targetValue = if (isRecording) 0.35f else 0.65f,
        animationSpec = tween(durationMillis = 200),
        label = "inner_size"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(
                width = 3.dp,
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
        // Inner red circle/square - size calculated from parent
        Box(
            modifier = Modifier
                .fillMaxSize(innerSizeRatio)
                .clip(RoundedCornerShape(if (isRecording) 4.dp else 50.dp))
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
    val timeString = "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    
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
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Complete camera controls bar with photo capture, record button, and gallery access.
 * Clean centered layout with symmetrical spacing.
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent background for better visibility
        Surface(
            modifier = Modifier
                .wrapContentSize(),
            shape = RoundedCornerShape(40.dp),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                CameraControlButton(
                    onClick = onOpenGallery,
                    enabled = !isRecording,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Open Gallery",
                        tint = if (isRecording) Color.Gray else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Photo capture button
                CameraControlButton(
                    onClick = onCapturePhoto,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture Photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Main record button (center, larger)
                CameraStyleRecordButton(
                    isRecording = isRecording,
                    onToggleRecording = onToggleRecording,
                    modifier = Modifier.size(72.dp)
                )
                
                // Placeholder for symmetry (or switch camera if available)
                if (onSwitchCamera != null) {
                    CameraControlButton(
                        onClick = onSwitchCamera,
                        enabled = !isRecording,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Switch Camera",
                            tint = if (isRecording) Color.Gray else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
                
                // Empty spacer for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
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
