package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

/**
 * Custom control overlay for AndroidX Media3 [androidx.media3.ui.PlayerView].
 *
 * Provides dedicated video editing controls:
 * - Play / Pause toggle with high-visibility center button
 * - Precision Seek Backward 5s (-5000 ms) and Seek Forward 5s (+5000 ms)
 * - Mute / Unmute audio toggle with status indicator
 * - Interactive timeline scrubber with trim range markers
 * - Quick "Mark In" and "Mark Out" editor shortcuts
 * - Playback speed selector (0.5x to 2x)
 */
@Composable
fun PlayerViewCustomOverlay(
    isVisible: Boolean,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isMuted: Boolean,
    playbackSpeed: Float = 1.0f,
    title: String = "",
    trimBounds: Pair<Long, Long>? = null,
    onPlayPauseToggle: () -> Unit,
    onSeekRelative: (deltaMs: Long) -> Unit,
    onSeekTo: (positionMs: Long) -> Unit,
    onMuteToggle: () -> Unit,
    onSpeedCycle: () -> Unit = {},
    onMarkInPoint: ((Long) -> Unit)? = null,
    onMarkOutPoint: ((Long) -> Unit)? = null,
    onOpenLocalFile: (() -> Unit)? = null,
    sceneMarkers: List<String> = emptyList(),
    onSceneSelect: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
                .padding(10.dp)
                .testTag("player_view_custom_overlay")
        ) {
            // TOP BAR: Title, Source badge, Speed, Mute/Unmute, and File picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) StudioEmerald else StudioAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.ifBlank { "Media3 Video Player" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.testTag("player_overlay_title")
                    )
                }

                // Controls: Speed, Mute/Unmute, File
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Open Local Video File
                    if (onOpenLocalFile != null) {
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenLocalFile()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("player_overlay_pick_file")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open video file",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("File", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Playback Speed Cycle Pill
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSpeedCycle()
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("player_overlay_speed_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = StudioCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${playbackSpeed}x",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Prominent Mute / Unmute Button
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onMuteToggle()
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isMuted) StudioCrimson.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.18f),
                        border = BorderStroke(
                            1.dp,
                            if (isMuted) StudioCrimson else Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("player_overlay_mute_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute audio" else "Mute audio",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMuted) "MUTED" else "SOUND",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // CENTER TRANSPORT CONTROLS: Seek -5s, Play/Pause, Seek +5s
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek Backward 5s (-5000 ms)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeekRelative(-5000L)
                    },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_overlay_seek_back_5s")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Seek backward 5 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "-5s",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Primary Play / Pause Action Button
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayPauseToggle()
                    },
                    shape = CircleShape,
                    color = StudioCrimson,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("player_overlay_play_pause")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause video" else "Play video",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Seek Forward 5s (+5000 ms)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeekRelative(5000L)
                    },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_overlay_seek_fwd_5s")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh, // Forward visual counterpart
                            contentDescription = "Seek forward 5 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "+5s",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // BOTTOM BAR: Scene markers, Quick In/Out buttons, Timeline scrubber, and Monospace timecodes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Quick Trimming In/Out Mark Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scene markers chips
                    if (sceneMarkers.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            sceneMarkers.take(4).forEachIndexed { idx, marker ->
                                Surface(
                                    onClick = {
                                        val target = (durationMs / sceneMarkers.size) * idx
                                        onSeekTo(target)
                                        onSceneSelect?.invoke(idx)
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.height(18.dp)
                                ) {
                                    Text(
                                        text = marker,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // In / Out point quick set buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (onMarkInPoint != null) {
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onMarkInPoint(currentPositionMs)
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = StudioAmber.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .height(20.dp)
                                    .testTag("player_overlay_mark_in")
                            ) {
                                Text(
                                    text = "[ Set In",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioAmber,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (onMarkOutPoint != null) {
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onMarkOutPoint(currentPositionMs)
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = StudioCyan.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .height(20.dp)
                                    .testTag("player_overlay_mark_out")
                            ) {
                                Text(
                                    text = "Set Out ]",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Scrubber Track Slider
                val currentFraction = if (durationMs > 0) {
                    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Slider(
                    value = currentFraction,
                    onValueChange = { fraction ->
                        val targetMs = (fraction * durationMs).toLong()
                        onSeekTo(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = StudioCrimson,
                        activeTrackColor = StudioCrimson,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .testTag("player_overlay_scrubber")
                )

                // Timecode Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatOverlayTime(currentPositionMs),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("player_overlay_current_time")
                    )

                    if (trimBounds != null) {
                        Text(
                            text = "CLIP: ${formatOverlayTime(trimBounds.first)} ➔ ${formatOverlayTime(trimBounds.second)}",
                            color = StudioAmber,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("player_overlay_trim_bounds")
                        )
                    }

                    Text(
                        text = formatOverlayTime(durationMs),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("player_overlay_duration")
                    )
                }
            }
        }
    }
}

private fun formatOverlayTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
