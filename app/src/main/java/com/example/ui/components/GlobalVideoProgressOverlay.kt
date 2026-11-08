package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.YouTubeRed

/**
 * State representation for the global video generation & rendering progress overlay.
 */
data class GlobalVideoLoadingState(
    val isVisible: Boolean = false,
    val title: String = "Generating AI Video Content...",
    val subtitle: String = "Processing video frames & neural assets...",
    val actionType: String = "generate", // "generate", "render", "upload", "api"
    val progress: Float? = null, // null for indeterminate CircularProgressIndicator
    val cancellable: Boolean = true,
    val onCancel: (() -> Unit)? = null
)

/**
 * Global CircularProgressIndicator overlay in the UI that displays whenever an API request
 * for video generation or rendering is in progress.
 */
@Composable
fun GlobalVideoProgressOverlay(
    state: GlobalVideoLoadingState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.95f, animationSpec = tween(180)),
        modifier = modifier
    ) {
        // Prevent back press from dismissing underlying screens unexpectedly while API request is running
        BackHandler(enabled = state.isVisible) {
            state.onCancel?.invoke()
        }

        // Full screen dimmed backdrop (scrim) that blocks background interactions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Intercept touch events */ }
                .testTag("global_video_progress_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing animation for outer indicator glow and icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_alpha"
            )

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                tonalElevation = 10.dp,
                shadowElevation = 18.dp,
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(StudioCrimson, YouTubeRed, Color(0xFFFF8A80))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 420.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Tag Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StudioCrimson.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            // Pulsing active dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StudioCrimson.copy(alpha = dotAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (state.actionType.lowercase()) {
                                    "render" -> "RENDER ENGINE ACTIVE"
                                    "upload" -> "CLOUD SYNC ACTIVE"
                                    else -> "AI API REQUEST IN PROGRESS"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioCrimson,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Prominent CircularProgressIndicator with center dynamic state
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(92.dp)
                            .scale(pulseScale)
                    ) {
                        if (state.progress != null) {
                            // Determinate CircularProgressIndicator
                            CircularProgressIndicator(
                                progress = { state.progress.coerceIn(0f, 1f) },
                                strokeWidth = 6.dp,
                                color = StudioCrimson,
                                trackColor = StudioCrimson.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("global_circular_progress_indicator")
                            )

                            // Center percentage text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(state.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.actionType.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioCrimson
                                )
                            }
                        } else {
                            // Indeterminate CircularProgressIndicator
                            CircularProgressIndicator(
                                strokeWidth = 6.dp,
                                color = StudioCrimson,
                                trackColor = StudioCrimson.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("global_circular_progress_indicator")
                            )

                            // Center dynamic action icon
                            val icon = when (state.actionType.lowercase()) {
                                "render" -> Icons.Default.Movie
                                "upload" -> Icons.Default.CloudUpload
                                "api" -> Icons.Default.Sensors
                                else -> Icons.Default.AutoAwesome
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(StudioCrimson.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = StudioCrimson,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("global_loading_title")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle / Step description
                    Text(
                        text = state.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.testTag("global_loading_subtitle")
                    )

                    // Secondary horizontal track if progress is given
                    if (state.progress != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { state.progress.coerceIn(0f, 1f) },
                            color = StudioCrimson,
                            trackColor = StudioCrimson.copy(alpha = 0.18f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Simulated 3-stage pipeline badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProgressStepPill(
                            stepNumber = "1",
                            label = "API Script",
                            isActive = (state.progress ?: 0.3f) >= 0.1f
                        )
                        ProgressStepPill(
                            stepNumber = "2",
                            label = "Neural Scenes",
                            isActive = (state.progress ?: 0.5f) >= 0.45f
                        )
                        ProgressStepPill(
                            stepNumber = "3",
                            label = "Rendering",
                            isActive = (state.progress ?: 0.8f) >= 0.8f
                        )
                    }

                    // Cancel button
                    if (state.cancellable && state.onCancel != null) {
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedButton(
                            onClick = state.onCancel,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("global_loading_cancel_button")
                        ) {
                            Text(
                                text = "Cancel Request",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStepPill(
    stepNumber: String,
    label: String,
    isActive: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) StudioCrimson.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) StudioCrimson.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isActive) StudioCrimson else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) StudioCrimson else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
