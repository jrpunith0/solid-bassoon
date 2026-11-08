package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class PipelineStepState(val label: String, val color: Color) {
    PENDING("Waiting", Color(0xFF64748B)),
    RUNNING("Processing...", StudioAmber),
    COMPLETED("Completed", StudioEmerald),
    FAILED("Failed", StudioCrimson)
}

enum class PipelineStepId {
    STORY_GENERATION,
    IMAGE_GENERATION,
    NARRATION_AUDIO,
    FFMPEG_RENDERING
}

data class PipelineStageInfo(
    val stepId: PipelineStepId,
    val title: String,
    val titleKn: String,
    val description: String,
    val icon: ImageVector,
    var state: PipelineStepState = PipelineStepState.PENDING,
    var progress: Float = 0f,
    var detailMessage: String = "",
    var metricDetail: String = ""
)

/**
 * TaskStatusDashboard:
 * Uses a progress-tracked state machine to track and visualize the sequential progress
 * of 'Story Generation', 'Image Generation', 'Narration Audio Generation', and 'FFmpeg Rendering'.
 * Displays step progress bars, status badges, live pulse animations, and diagnostic cues.
 */
@Composable
fun TaskStatusDashboard(
    stages: List<PipelineStageInfo>,
    isKn: Boolean = false,
    overallTitle: String = "AI Story-to-Video Pipeline Progress",
    overallProgress: Float = 0f,
    onRetryStep: ((PipelineStepId) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_status_dashboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141E)),
        border = BorderStroke(1.dp, Color(0xFF232D40))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Overall Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioPurple.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = StudioPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isKn) "ಕಾರ್ಯ ಸ್ಥಿತಿ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್" else overallTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isKn) "4-ಹಂತಗಳ ಸ್ವಯಂಚಾಲಿತ ವೀಡಿಯೊ ಪೈಪ್‌ಲೈನ್" else "4-Stage Sequential AI Video Pipeline",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${(overallProgress * 100).toInt()}% READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (overallProgress >= 1.0f) StudioEmerald else StudioAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Overall Linear Progress Tracker
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { overallProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (overallProgress >= 1f) StudioEmerald else StudioCrimson,
                    trackColor = Color(0xFF1E2838)
                )
            }

            // Sequential Step Nodes
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                stages.forEachIndexed { index, stage ->
                    val isRunning = stage.state == PipelineStepState.RUNNING
                    val isDone = stage.state == PipelineStepState.COMPLETED
                    val isFailed = stage.state == PipelineStepState.FAILED

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (stage.state) {
                            PipelineStepState.COMPLETED -> StudioEmerald.copy(alpha = 0.1f)
                            PipelineStepState.RUNNING -> StudioAmber.copy(alpha = 0.12f)
                            PipelineStepState.FAILED -> StudioCrimson.copy(alpha = 0.12f)
                            PipelineStepState.PENDING -> Color(0xFF151C28)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (stage.state) {
                                PipelineStepState.COMPLETED -> StudioEmerald.copy(alpha = 0.4f)
                                PipelineStepState.RUNNING -> StudioAmber.copy(alpha = 0.6f)
                                PipelineStepState.FAILED -> StudioCrimson.copy(alpha = 0.6f)
                                PipelineStepState.PENDING -> Color(0xFF263248)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pipeline_step_${stage.stepId.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Step Number / Status Icon Circle
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (stage.state) {
                                                    PipelineStepState.COMPLETED -> StudioEmerald
                                                    PipelineStepState.RUNNING -> StudioAmber.copy(alpha = pulseAlpha)
                                                    PipelineStepState.FAILED -> StudioCrimson
                                                    PipelineStepState.PENDING -> Color(0xFF334155)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (stage.state) {
                                            PipelineStepState.COMPLETED -> Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            PipelineStepState.RUNNING -> Icon(
                                                Icons.Default.Sync,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            PipelineStepState.FAILED -> Icon(
                                                Icons.Default.Error,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            PipelineStepState.PENDING -> Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = if (isKn) stage.titleKn else stage.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = stage.description,
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                // Status Tag Badge
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = stage.state.color.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = stage.state.label.uppercase(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = stage.state.color,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (isFailed && onRetryStep != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { onRetryStep(stage.stepId) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Retry",
                                                tint = StudioCrimson,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic Progress Bar if Running
                            if (isRunning) {
                                LinearProgressIndicator(
                                    progress = { stage.progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = StudioAmber,
                                    trackColor = Color(0xFF263248)
                                )
                            }

                            // Metric details & feedback
                            if (stage.detailMessage.isNotBlank() || stage.metricDetail.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (stage.detailMessage.isNotBlank()) {
                                        Text(
                                            text = stage.detailMessage,
                                            fontSize = 9.sp,
                                            color = stage.state.color
                                        )
                                    }
                                    if (stage.metricDetail.isNotBlank()) {
                                        Text(
                                            text = stage.metricDetail,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
