package com.example.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.ui.theme.*
import java.util.Locale

/**
 * Media3 Clipping Configuration Helper.
 * Creates an [androidx.media3.common.MediaItem] with explicit start and end boundary clipping.
 */
object Media3ClippingHelper {
    fun createClippedMediaItem(uri: String, startPositionMs: Long, endPositionMs: Long): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.parse(uri))
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startPositionMs.coerceAtLeast(0L))
                    .setEndPositionMs(endPositionMs.coerceAtLeast(startPositionMs + 500L))
                    .setStartsAtKeyFrame(false)
                    .build()
            )
            .build()
    }
}

/**
 * Professional Video Timeline Trimmer component using AndroidX Media3.
 *
 * Allows creators to:
 * - Select specific Start (In-point) and End (Out-point) timestamps for their video clips
 * - Visually scrub the timeline filmstrip with a live playhead indicator
 * - Fine-tune timestamps with millisecond precision (+/- 0.1s and +/- 1.0s buttons)
 * - Snap In/Out points to current playhead location
 * - Apply Media3 ClippingConfiguration directly to the video player
 * - Loop preview strictly within the trimmed boundaries
 */
@OptIn(UnstableApi::class)
@Composable
fun Media3TimelineTrimmer(
    videoDurationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    currentPlaybackPositionMs: Long = 0L,
    videoUri: String? = null,
    isLoopTrimActive: Boolean = false,
    onTrimChange: (startMs: Long, endMs: Long) -> Unit,
    onSeekToPosition: ((Long) -> Unit)? = null,
    onPreviewTrim: () -> Unit,
    onToggleLoopTrim: ((Boolean) -> Unit)? = null,
    onApplyMedia3Clipping: ((MediaItem) -> Unit)? = null,
    onResetTrim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val totalDuration = videoDurationMs.coerceAtLeast(1000L)

    val currentTrimmedMs = (trimEndMs - trimStartMs).coerceAtLeast(0L)
    val trimmedPercentage = ((currentTrimmedMs.toFloat() / totalDuration.toFloat()) * 100).toInt().coerceIn(0, 100)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("media3_timeline_trimmer"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF10141D)
        ),
        border = BorderStroke(1.dp, Color(0xFF252D3D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title, Media3 clipping badge, and Trimmed Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioCrimson.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Timeline Trimmer",
                            tint = StudioCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Media3 Timeline Trimmer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StudioPurple.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "ClippingConfig",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioPurpleLight,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Set precise in & out points for export & preview",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Trimmed Duration Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioCrimson.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("trimmer_duration_pill")
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formatPreciseTime(currentTrimmedMs),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = StudioCrimson
                        )
                        Text(
                            text = "$trimmedPercentage% of full clip",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // In & Out Timecode Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START (IN-POINT)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioAmber.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, StudioAmber.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "IN-POINT [",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = StudioAmber
                            )
                            Text(
                                text = formatPreciseTime(trimStartMs),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("trimmer_start_timecode")
                            )
                        }

                        // Snap In to Playhead
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val newStart = currentPlaybackPositionMs.coerceIn(0L, trimEndMs - 500L)
                                onTrimChange(newStart, trimEndMs)
                            },
                            modifier = Modifier.size(24.dp).testTag("trimmer_snap_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerticalAlignBottom,
                                contentDescription = "Snap In to Playhead",
                                tint = StudioAmber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // END (OUT-POINT)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "OUT-POINT ]",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = StudioCyan
                            )
                            Text(
                                text = formatPreciseTime(trimEndMs),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("trimmer_end_timecode")
                            )
                        }

                        // Snap Out to Playhead
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val newEnd = currentPlaybackPositionMs.coerceIn(trimStartMs + 500L, totalDuration)
                                onTrimChange(trimStartMs, newEnd)
                            },
                            modifier = Modifier.size(24.dp).testTag("trimmer_snap_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerticalAlignTop,
                                contentDescription = "Snap Out to Playhead",
                                tint = StudioCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // VISUAL TIMELINE FILMSTRIP & DUAL-HANDLE TRIM TRACK
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF090D14))
                    .border(BorderStroke(1.dp, Color(0xFF1E2638)), RoundedCornerShape(10.dp))
                    .testTag("trimmer_filmstrip_container")
            ) {
                val trackWidthPx = constraints.maxWidth.toFloat()
                val startFraction = (trimStartMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                val endFraction = (trimEndMs.toFloat() / totalDuration.toFloat()).coerceIn(startFraction + 0.01f, 1f)
                val playheadFraction = (currentPlaybackPositionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

                // Timeline Canvas (Filmstrip cells, shaded exclusion regions, trim window, playhead needle)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(totalDuration) {
                            detectTapGestures { offset ->
                                val clickedFraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                                val seekTarget = (clickedFraction * totalDuration).toLong()
                                onSeekToPosition?.invoke(seekTarget)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw filmstrip simulated cells
                    val cellCount = 10
                    val cellWidth = w / cellCount
                    for (i in 0 until cellCount) {
                        val cellLeft = i * cellWidth
                        // Film cell background
                        drawRect(
                            color = Color(0xFF141A26),
                            topLeft = Offset(cellLeft + 2f, 6f),
                            size = Size(cellWidth - 4f, h - 12f)
                        )
                        // Film sprocket ticks (top and bottom)
                        drawCircle(
                            color = Color(0xFF090D14),
                            radius = 2.5f,
                            center = Offset(cellLeft + cellWidth / 2f, 3f)
                        )
                        drawCircle(
                            color = Color(0xFF090D14),
                            radius = 2.5f,
                            center = Offset(cellLeft + cellWidth / 2f, h - 3f)
                        )
                    }

                    // 2. Active Trim Window Coordinates
                    val activeLeft = startFraction * w
                    val activeRight = endFraction * w
                    val activeWidth = (activeRight - activeLeft).coerceAtLeast(4f)

                    // Dimmed exclusion region (Before Start)
                    if (activeLeft > 0f) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.65f),
                            topLeft = Offset(0f, 0f),
                            size = Size(activeLeft, h)
                        )
                    }

                    // Dimmed exclusion region (After End)
                    if (activeRight < w) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.65f),
                            topLeft = Offset(activeRight, 0f),
                            size = Size(w - activeRight, h)
                        )
                    }

                    // Active Selected Clip Highlight Frame
                    drawRect(
                        color = StudioCrimson.copy(alpha = 0.15f),
                        topLeft = Offset(activeLeft, 0f),
                        size = Size(activeWidth, h)
                    )
                    // Top & Bottom boundary bars
                    drawRect(
                        color = StudioCrimson,
                        topLeft = Offset(activeLeft, 0f),
                        size = Size(activeWidth, 3f)
                    )
                    drawRect(
                        color = StudioCrimson,
                        topLeft = Offset(activeLeft, h - 3f),
                        size = Size(activeWidth, 3f)
                    )

                    // In-point boundary bracket [ (Amber)
                    drawRect(
                        color = StudioAmber,
                        topLeft = Offset(activeLeft, 0f),
                        size = Size(4f, h)
                    )
                    // Out-point boundary bracket ] (Cyan)
                    drawRect(
                        color = StudioCyan,
                        topLeft = Offset(activeRight - 4f, 0f),
                        size = Size(4f, h)
                    )

                    // 3. Playhead Needle
                    val playheadX = playheadFraction * w
                    drawLine(
                        color = Color.White,
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, h),
                        strokeWidth = 2.5f
                    )
                    // Needle head
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(playheadX, 4f)
                    )
                }

                // Drag gesture detector for Start and End handles
                // Start Handle (Left bracket [ )
                Box(
                    modifier = Modifier
                        .offset(x = (maxWidth.value * startFraction).dp - 12.dp)
                        .width(24.dp)
                        .fillMaxHeight()
                        .pointerInput(totalDuration, trimEndMs) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaFraction = dragAmount.x / trackWidthPx
                                val newStartFraction = (startFraction + deltaFraction).coerceIn(0f, (trimEndMs.toFloat() / totalDuration.toFloat()) - 0.02f)
                                val newStartMs = (newStartFraction * totalDuration).toLong()
                                onTrimChange(newStartMs, trimEndMs)
                                onSeekToPosition?.invoke(newStartMs)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        .testTag("trimmer_drag_start_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(StudioAmber)
                    )
                }

                // End Handle (Right bracket ] )
                Box(
                    modifier = Modifier
                        .offset(x = (maxWidth.value * endFraction).dp - 12.dp)
                        .width(24.dp)
                        .fillMaxHeight()
                        .pointerInput(totalDuration, trimStartMs) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaFraction = dragAmount.x / trackWidthPx
                                val newEndFraction = (endFraction + deltaFraction).coerceIn((trimStartMs.toFloat() / totalDuration.toFloat()) + 0.02f, 1f)
                                val newEndMs = (newEndFraction * totalDuration).toLong()
                                onTrimChange(trimStartMs, newEndMs)
                                onSeekToPosition?.invoke(newEndMs)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        .testTag("trimmer_drag_end_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(StudioCyan)
                    )
                }
            }

            // PRECISION NUDGE CONTROLS (-1s, -0.1s, +0.1s, +1s)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In-Point Fine Nudge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("In: ", fontSize = 10.sp, color = StudioAmber, fontWeight = FontWeight.Bold)
                    NudgeButton(label = "-1s", onClick = {
                        val newStart = (trimStartMs - 1000L).coerceAtLeast(0L)
                        onTrimChange(newStart, trimEndMs)
                        onSeekToPosition?.invoke(newStart)
                    })
                    NudgeButton(label = "-0.1s", onClick = {
                        val newStart = (trimStartMs - 100L).coerceAtLeast(0L)
                        onTrimChange(newStart, trimEndMs)
                        onSeekToPosition?.invoke(newStart)
                    })
                    NudgeButton(label = "+0.1s", onClick = {
                        val newStart = (trimStartMs + 100L).coerceAtMost(trimEndMs - 500L)
                        onTrimChange(newStart, trimEndMs)
                        onSeekToPosition?.invoke(newStart)
                    })
                    NudgeButton(label = "+1s", onClick = {
                        val newStart = (trimStartMs + 1000L).coerceAtMost(trimEndMs - 500L)
                        onTrimChange(newStart, trimEndMs)
                        onSeekToPosition?.invoke(newStart)
                    })
                }

                // Out-Point Fine Nudge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Out: ", fontSize = 10.sp, color = StudioCyan, fontWeight = FontWeight.Bold)
                    NudgeButton(label = "-1s", onClick = {
                        val newEnd = (trimEndMs - 1000L).coerceAtLeast(trimStartMs + 500L)
                        onTrimChange(trimStartMs, newEnd)
                        onSeekToPosition?.invoke(newEnd)
                    })
                    NudgeButton(label = "-0.1s", onClick = {
                        val newEnd = (trimEndMs - 100L).coerceAtLeast(trimStartMs + 500L)
                        onTrimChange(trimStartMs, newEnd)
                        onSeekToPosition?.invoke(newEnd)
                    })
                    NudgeButton(label = "+0.1s", onClick = {
                        val newEnd = (trimEndMs + 100L).coerceAtMost(totalDuration)
                        onTrimChange(trimStartMs, newEnd)
                        onSeekToPosition?.invoke(newEnd)
                    })
                    NudgeButton(label = "+1s", onClick = {
                        val newEnd = (trimEndMs + 1000L).coerceAtMost(totalDuration)
                        onTrimChange(trimStartMs, newEnd)
                        onSeekToPosition?.invoke(newEnd)
                    })
                }
            }

            // PRIMARY ACTIONS ROW: Preview Trimmed, Loop Toggle, Apply Media3 Clipping, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preview Trimmed Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPreviewTrim()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("trimmer_preview_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview Clip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Loop Trim Toggle
                if (onToggleLoopTrim != null) {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleLoopTrim(!isLoopTrimActive)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isLoopTrimActive) StudioPurple.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                            contentColor = if (isLoopTrimActive) StudioPurpleLight else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("trimmer_loop_toggle")
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isLoopTrimActive) "Loop: On" else "Loop", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Apply Media3 Clipping Button
                if (videoUri != null && onApplyMedia3Clipping != null) {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val clipped = Media3ClippingHelper.createClippedMediaItem(videoUri, trimStartMs, trimEndMs)
                            onApplyMedia3Clipping(clipped)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StudioEmerald.copy(alpha = 0.2f),
                            contentColor = StudioEmerald
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(38.dp)
                            .testTag("trimmer_apply_clipping_button")
                    ) {
                        Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Apply Clip", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Reset Button
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onResetTrim()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("trimmer_reset_button")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun NudgeButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.height(22.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

private fun formatPreciseTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    val millis = (ms % 1000) / 100
    return String.format(Locale.US, "%02d:%02d.%01d", minutes, seconds, millis)
}
