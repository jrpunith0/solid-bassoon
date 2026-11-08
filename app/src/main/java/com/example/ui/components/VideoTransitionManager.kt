package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Supported transition types for video timeline segments.
 */
enum class TimelineTransitionType(
    val id: String,
    val displayName: String,
    val shortLabel: String,
    val description: String,
    val icon: ImageVector,
    val defaultDurationSec: Float = 0.6f
) {
    NONE(
        id = "none",
        displayName = "Direct Cut",
        shortLabel = "Cut",
        description = "Standard instant cut between clips with no transition",
        icon = Icons.Default.ContentCut,
        defaultDurationSec = 0f
    ),
    FADE_IN(
        id = "fade_in",
        displayName = "Fade In",
        shortLabel = "Fade In",
        description = "Smoothly fades in from black at the beginning of the clip",
        icon = Icons.Default.BrightnessMedium,
        defaultDurationSec = 0.8f
    ),
    FADE_OUT(
        id = "fade_out",
        displayName = "Fade Out",
        shortLabel = "Fade Out",
        description = "Smoothly fades video to black at the end of the clip",
        icon = Icons.Default.BrightnessLow,
        defaultDurationSec = 0.8f
    ),
    CROSS_DISSOLVE(
        id = "cross_dissolve",
        displayName = "Cross-Dissolve",
        shortLabel = "Dissolve",
        description = "Blends and dissolves outgoing clip smoothly into incoming clip",
        icon = Icons.Default.AutoFixHigh,
        defaultDurationSec = 0.6f
    ),
    DIP_TO_BLACK(
        id = "dip_to_black",
        displayName = "Dip to Black",
        shortLabel = "Dip Black",
        description = "Fades outgoing clip to pure black, then fades in incoming clip",
        icon = Icons.Default.Contrast,
        defaultDurationSec = 0.8f
    ),
    WIPE_LEFT(
        id = "wipe_left",
        displayName = "Wipe / Slide Left",
        shortLabel = "Wipe",
        description = "Incoming segment pushes outgoing segment horizontally",
        icon = Icons.Default.ArrowBack,
        defaultDurationSec = 0.5f
    )
}

/**
 * Junction transition between two adjacent segments.
 */
data class SegmentTransitionItem(
    val junctionIndex: Int, // e.g. 0 between Scene 1 and Scene 2
    val fromSegmentName: String,
    val toSegmentName: String,
    var type: TimelineTransitionType = TimelineTransitionType.CROSS_DISSOLVE,
    var durationSec: Float = 0.6f
)

/**
 * Comprehensive Transition Manager for Video Timeline Segments.
 * Allows users to add fade-in, fade-out, cross-dissolve, and dip-to-black transitions
 * between trimmed segments in the video timeline.
 */
@Composable
fun VideoTransitionManager(
    transitions: List<SegmentTransitionItem>,
    selectedJunctionIndex: Int,
    onSelectJunction: (Int) -> Unit,
    onUpdateTransition: (junctionIndex: Int, type: TimelineTransitionType, durationSec: Float) -> Unit,
    onApplyToAll: (TimelineTransitionType, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentJunction = transitions.getOrNull(selectedJunctionIndex) ?: transitions.firstOrNull()

    var activeType by remember(currentJunction) {
        mutableStateOf(currentJunction?.type ?: TimelineTransitionType.CROSS_DISSOLVE)
    }
    var activeDuration by remember(currentJunction) {
        mutableFloatStateOf(currentJunction?.durationSec ?: 0.6f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_transition_manager_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10141E)),
        border = BorderStroke(1.dp, Color(0xFF242E40))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title and Global Apply Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = StudioPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Timeline Transition Manager",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Fade-in, fade-out & cross-dissolves between clips",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onApplyToAll(activeType, activeDuration)
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = StudioPurple.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, StudioPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("transition_apply_all_button")
                ) {
                    Text(
                        text = "Apply to All Cuts",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPurpleLight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Junction Cut Selector Bar (Segments: [Scene 1] ──⚡── [Scene 2] ──✨── [Scene 3])
            Text(
                text = "SELECT TIMELINE CUT JUNCTION:",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = StudioPurpleLight
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                transitions.forEachIndexed { idx, junction ->
                    val isSelected = idx == selectedJunctionIndex
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectJunction(idx)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) StudioPurple else Color(0xFF182030),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) StudioPurple else Color(0xFF28344A)
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("transition_junction_$idx")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = junction.type.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else StudioAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${junction.fromSegmentName} ➔ ${junction.toSegmentName}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${junction.type.shortLabel} ${junction.durationSec}s)",
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Transition Type Selection Chips
            Text(
                text = "TRANSITION EFFECT TYPE:",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = StudioPurpleLight
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimelineTransitionType.entries.forEach { effect ->
                    val isChosen = activeType == effect
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeType = effect
                            if (effect.defaultDurationSec > 0f) {
                                activeDuration = effect.defaultDurationSec
                            }
                            currentJunction?.let {
                                onUpdateTransition(it.junctionIndex, effect, activeDuration)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChosen) StudioCrimson else Color(0xFF161D2B),
                        border = BorderStroke(1.dp, if (isChosen) StudioCrimson else Color(0xFF28344A)),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("transition_type_${effect.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = effect.icon,
                                contentDescription = null,
                                tint = if (isChosen) Color.White else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = effect.displayName,
                                fontSize = 10.sp,
                                fontWeight = if (isChosen) FontWeight.Black else FontWeight.Medium,
                                color = if (isChosen) Color.White else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Description of active effect
            Text(
                text = activeType.description,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            // Duration Slider & Fine Control
            if (activeType != TimelineTransitionType.NONE) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transition Duration:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${String.format("%.1f", activeDuration)}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioAmber
                        )
                    }

                    Slider(
                        value = activeDuration,
                        onValueChange = { dur ->
                            activeDuration = dur
                            currentJunction?.let {
                                onUpdateTransition(it.junctionIndex, activeType, dur)
                            }
                        },
                        valueRange = 0.2f..2.0f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioPurple,
                            activeTrackColor = StudioPurple,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .testTag("transition_duration_slider")
                    )

                    // Quick duration presets (0.3s, 0.5s, 0.8s, 1.0s, 1.5s)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.3f, 0.5f, 0.8f, 1.0f, 1.5f).forEach { sec ->
                            Surface(
                                onClick = {
                                    activeDuration = sec
                                    currentJunction?.let {
                                        onUpdateTransition(it.junctionIndex, activeType, sec)
                                    }
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = if (Math.abs(activeDuration - sec) < 0.05f) StudioPurple.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f).height(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${sec}s", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual Transition Rendering Overlay on top of the player.
 * Computes smooth Fade-In, Fade-Out, Dip-to-Black, or Cross-Dissolve opacities in real-time.
 */
@Composable
fun TransitionRendererOverlay(
    currentPlaybackPositionMs: Long,
    clipStartMs: Long,
    clipEndMs: Long,
    transitionType: TimelineTransitionType,
    transitionDurationSec: Float,
    modifier: Modifier = Modifier
) {
    if (transitionType == TimelineTransitionType.NONE || transitionDurationSec <= 0f) return

    val transDurationMs = (transitionDurationSec * 1000).toLong()
    val timeIntoClip = (currentPlaybackPositionMs - clipStartMs).coerceAtLeast(0L)
    val timeRemainingInClip = (clipEndMs - currentPlaybackPositionMs).coerceAtLeast(0L)

    var scrimAlpha = 0f

    when (transitionType) {
        TimelineTransitionType.FADE_IN -> {
            // Fade from black at clip beginning
            if (timeIntoClip < transDurationMs) {
                scrimAlpha = (1f - (timeIntoClip.toFloat() / transDurationMs.toFloat())).coerceIn(0f, 1f)
            }
        }
        TimelineTransitionType.FADE_OUT -> {
            // Fade to black at clip end
            if (timeRemainingInClip < transDurationMs) {
                scrimAlpha = (1f - (timeRemainingInClip.toFloat() / transDurationMs.toFloat())).coerceIn(0f, 1f)
            }
        }
        TimelineTransitionType.DIP_TO_BLACK -> {
            // Fade in at start and fade out at end
            if (timeIntoClip < transDurationMs) {
                scrimAlpha = (1f - (timeIntoClip.toFloat() / transDurationMs.toFloat())).coerceIn(0f, 1f)
            } else if (timeRemainingInClip < transDurationMs) {
                scrimAlpha = (1f - (timeRemainingInClip.toFloat() / transDurationMs.toFloat())).coerceIn(0f, 1f)
            }
        }
        TimelineTransitionType.CROSS_DISSOLVE -> {
            // Dissolve blend: gentle transparency shimmer
            if (timeIntoClip < transDurationMs) {
                scrimAlpha = (0.5f * (1f - (timeIntoClip.toFloat() / transDurationMs.toFloat()))).coerceIn(0f, 0.5f)
            }
        }
        TimelineTransitionType.WIPE_LEFT, TimelineTransitionType.NONE -> {}
    }

    if (scrimAlpha > 0.01f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
        )
    }
}
