package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.YouTubeRed

/**
 * Circular progress indicator UI that displays a loading state whenever a video
 * is currently being generated, rendered, or uploaded, providing rich visual feedback
 * during long-running background tasks.
 */
@Composable
fun VideoGenerationProgressIndicator(
    isVisible: Boolean,
    statusText: String = "Generating AI Video Content...",
    stepText: String = "Step 2 of 4: Rendering Scenes",
    progress: Float? = null, // null for indeterminate
    actionType: String = "generate", // "generate", "render", "upload"
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(StudioCrimson, YouTubeRed)),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("video_generation_progress_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress Indicator with center icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    if (progress != null) {
                        CircularProgressIndicator(
                            progress = { progress },
                            color = StudioCrimson,
                            trackColor = StudioCrimson.copy(alpha = 0.2f),
                            strokeWidth = 4.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("circular_progress_indicator")
                        )
                    } else {
                        CircularProgressIndicator(
                            color = StudioCrimson,
                            strokeWidth = 4.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("circular_progress_indicator")
                        )
                    }

                    // Centered Context Icon
                    val icon = when (actionType) {
                        "upload" -> Icons.Default.CloudUpload
                        "render" -> Icons.Default.Movie
                        else -> Icons.Default.AutoAwesome
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StudioCrimson.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = StudioCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text Description & Status
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = StudioCrimson,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (actionType) {
                                "upload" -> "UPLOADING TO PLATFORM"
                                "render" -> "RENDERING TIMELINE"
                                else -> "AI GENERATING VIDEO"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioCrimson,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Text(
                        text = stepText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                if (onCancel != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
