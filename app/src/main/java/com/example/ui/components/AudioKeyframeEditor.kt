package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCrimson

/**
 * Data structure representing a volume keyframe at a given timeline position.
 */
data class AudioKeyframe(
    val timestampMs: Long,
    val volume: Float // 0.0f to 1.0f
)

/**
 * Audio adjustment tool that allows creators to adjust volume,
 * set smooth fade-in and fade-out durations, and manage volume keyframes.
 */
@Composable
fun AudioKeyframeEditor(
    fadeInSec: Float,
    fadeOutSec: Float,
    masterVolume: Float,
    isAutoDuckingEnabled: Boolean,
    onChangeFadeIn: (Float) -> Unit,
    onChangeFadeOut: (Float) -> Unit,
    onChangeMasterVolume: (Float) -> Unit,
    onToggleAutoDucking: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().testTag("audio_keyframe_editor")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = StudioCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Audio Envelopes & Keyframes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${(masterVolume * 100).toInt()}% Vol",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Visual Volume Curve Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val baseVolY = h * (1f - masterVolume.coerceIn(0.1f, 1f))
                    val inWidth = (w * (fadeInSec / 10f)).coerceAtMost(w * 0.4f)
                    val outWidth = (w * (fadeOutSec / 10f)).coerceAtMost(w * 0.4f)

                    val path = Path().apply {
                        moveTo(0f, h)
                        lineTo(inWidth, baseVolY)
                        lineTo(w - outWidth, baseVolY)
                        lineTo(w, h)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFFFF3366),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw keyframe nodes
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(0f, h))
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(inWidth, baseVolY))
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(w - outWidth, baseVolY))
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(w, h))
                }
            }

            // Master Volume
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Master Background Music Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(masterVolume * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = masterVolume,
                    onValueChange = onChangeMasterVolume,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = StudioCrimson, activeTrackColor = StudioCrimson),
                    modifier = Modifier.fillMaxWidth().testTag("master_volume_slider")
                )
            }

            // Fade-In & Fade-Out Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fade-In", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${String.format("%.1f", fadeInSec)}s", fontSize = 11.sp, color = StudioCrimson, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fadeInSec,
                        onValueChange = onChangeFadeIn,
                        valueRange = 0f..5f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = StudioCrimson, activeTrackColor = StudioCrimson),
                        modifier = Modifier.testTag("fade_in_slider")
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fade-Out", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${String.format("%.1f", fadeOutSec)}s", fontSize = 11.sp, color = StudioCrimson, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fadeOutSec,
                        onValueChange = onChangeFadeOut,
                        valueRange = 0f..5f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = StudioCrimson, activeTrackColor = StudioCrimson),
                        modifier = Modifier.testTag("fade_out_slider")
                    )
                }
            }

            // Auto-Ducking Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAutoDucking(!isAutoDuckingEnabled) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Ducking Background Music", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Automatically lower music volume by -12dB when speech voiceover is active",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAutoDuckingEnabled,
                    onCheckedChange = onToggleAutoDucking,
                    modifier = Modifier.testTag("auto_ducking_switch")
                )
            }
        }
    }
}
