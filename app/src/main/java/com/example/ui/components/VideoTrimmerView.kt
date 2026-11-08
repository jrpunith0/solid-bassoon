package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCrimson
import java.util.Locale

/**
 * Trimming slider UI component that allows users to select start and end timestamps
 * for a video clip with precision timecodes, duration, and boundary enforcement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTrimmerView(
    videoDurationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onTrimChange: (startMs: Long, endMs: Long) -> Unit,
    onPreviewTrim: () -> Unit,
    onResetTrim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val totalMs = videoDurationMs.coerceAtLeast(1000L)

    var currentRange by remember(trimStartMs, trimEndMs, videoDurationMs) {
        mutableStateOf(
            (trimStartMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)..(trimEndMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        )
    }

    val trimmedDurationMs = (trimEndMs - trimStartMs).coerceAtLeast(0L)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().testTag("video_trimmer_view")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = StudioCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Video Clip Trimmer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioCrimson.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Duration: ${formatTimecode(trimmedDurationMs)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCrimson,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Timecode badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("START TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatTimecode(trimStartMs),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("END TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatTimecode(trimEndMs),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // RangeSlider for Start & End timestamps
            RangeSlider(
                value = currentRange,
                onValueChange = { range ->
                    // Enforce minimum clip duration of 1 second
                    val minSpread = (1000f / totalMs.toFloat()).coerceAtMost(0.5f)
                    val correctedStart = range.start.coerceIn(0f, 1f - minSpread)
                    val correctedEnd = range.endInclusive.coerceIn(correctedStart + minSpread, 1f)
                    currentRange = correctedStart..correctedEnd

                    val newStartMs = (correctedStart * totalMs).toLong()
                    val newEndMs = (correctedEnd * totalMs).toLong()
                    onTrimChange(newStartMs, newEndMs)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioCrimson,
                    activeTrackColor = StudioCrimson,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trimmer_range_slider")
            )

            // Trim Actions Row: Preview Trim Loop & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPreviewTrim()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("preview_trim_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview Trimmed Clip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        currentRange = 0f..1f
                        onResetTrim()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("reset_trim_button")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    val millis = (ms % 1000) / 100
    return String.format(Locale.US, "%02d:%02d.%01d", minutes, seconds, millis)
}
