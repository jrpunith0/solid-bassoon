package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCrimson

/**
 * Filter presets and ColorMatrix transformations for video clips.
 */
enum class VideoFilterPreset(val displayName: String, val description: String) {
    NONE("Original", "Natural vibrant colors"),
    GRAYSCALE("Grayscale", "Classic monochrome B&W"),
    SEPIA("Sepia", "Warm vintage nostalgic tone"),
    HIGH_CONTRAST("High Contrast", "Punchy deep shadows & vibrant highlights"),
    CYBERPUNK("Cyberpunk", "Neon magenta & electric cyan"),
    WARM("Warm Sunset", "Golden hour rich warmth"),
    COOL("Cinematic Teal", "Moody teal and blue tint")
}

/**
 * Transition effects between video clips on the timeline.
 */
enum class VideoTransitionEffect(val displayName: String, val icon: String, val description: String) {
    NONE("None", "⚡", "Instant cut between clips"),
    DISSOLVE("Dissolve", "✨", "Smooth cross-dissolve fade"),
    SLIDE_LEFT("Slide Left", "⬅️", "Incoming clip pushes from right to left"),
    SLIDE_RIGHT("Slide Right", "➡️", "Incoming clip pushes from left to right"),
    FADE_TO_BLACK("Fade to Black", "⬛", "Dip to black transition"),
    CROSS_ZOOM("Cross Zoom", "🔍", "Dynamic whip zoom transition"),
    WHIP_PAN("Whip Pan", "💨", "High-speed camera motion blur pan")
}

/**
 * Platform safe zone overlay configurations.
 */
enum class SafeZoneType(val label: String, val platform: String) {
    NONE("None", "No overlay guides"),
    SHORTS("YouTube Shorts", "Shorts 9:16 safe area & action buttons"),
    REELS("Instagram Reels", "Reels UI buttons, captions & audio tag"),
    TIKTOK("TikTok", "TikTok right sidebar & bottom sound ticker")
}

object VideoColorMatrixHelper {
    fun getColorMatrix(preset: VideoFilterPreset): ColorMatrix? {
        return when (preset) {
            VideoFilterPreset.NONE -> null
            VideoFilterPreset.GRAYSCALE -> ColorMatrix().apply { setToSaturation(0f) }
            VideoFilterPreset.SEPIA -> ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                )
            )
            VideoFilterPreset.HIGH_CONTRAST -> ColorMatrix(
                floatArrayOf(
                    1.4f, 0f,   0f,   0f, -40f,
                    0f,   1.4f, 0f,   0f, -40f,
                    0f,   0f,   1.4f, 0f, -40f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            VideoFilterPreset.CYBERPUNK -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0f,   0.3f, 0f, 20f,
                    0f,   0.9f, 0.1f, 0f, -10f,
                    0.2f, 0.2f, 1.4f, 0f, 30f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            VideoFilterPreset.WARM -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.1f, 0f,   0f, 15f,
                    0f,   1.1f, 0f,   0f, 10f,
                    0f,   0f,   0.9f, 0f, -15f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
            VideoFilterPreset.COOL -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0f,   0f,   0f, -10f,
                    0f,   1.05f, 0.1f, 0f, 5f,
                    0f,   0.1f, 1.3f, 0f, 25f,
                    0f,   0f,   0f,   1f, 0f
                )
            )
        }
    }
}

/**
 * Filter selection panel with interactive previews.
 */
@Composable
fun VideoFilterSelectionPanel(
    selectedFilter: VideoFilterPreset,
    onSelectFilter: (VideoFilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().testTag("video_filter_panel")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = null,
                        tint = StudioCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Color Grading & Filters",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = selectedFilter.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioCrimson
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("filter_chips_row")
            ) {
                items(VideoFilterPreset.values()) { preset ->
                    val isSelected = selectedFilter == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectFilter(preset) },
                        label = {
                            Text(
                                text = preset.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioCrimson.copy(alpha = 0.18f),
                            selectedLabelColor = StudioCrimson
                        ),
                        modifier = Modifier.testTag("filter_${preset.name.lowercase()}")
                    )
                }
            }
        }
    }
}

/**
 * Safe Zone overlay guide rendered over the video player view.
 */
@Composable
fun SafeZoneGuideOverlay(
    safeZoneType: SafeZoneType,
    modifier: Modifier = Modifier
) {
    if (safeZoneType == SafeZoneType.NONE) return

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Dashed safe margins
            val strokeDashed = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            when (safeZoneType) {
                SafeZoneType.SHORTS -> {
                    // YouTube Shorts: Top bar ~10%, Bottom title & sound ~22%, Right action bar ~15%
                    val topGutter = h * 0.08f
                    val bottomGutter = h * 0.20f
                    val rightGutter = w * 0.16f

                    // Draw safe box
                    drawRect(
                        color = Color(0xFFFF0033).copy(alpha = 0.7f),
                        topLeft = Offset(w * 0.04f, topGutter),
                        size = Size(w - rightGutter - w * 0.06f, h - topGutter - bottomGutter),
                        style = strokeDashed
                    )
                    // Shade right buttons column
                    drawRect(
                        color = Color.Black.copy(alpha = 0.25f),
                        topLeft = Offset(w - rightGutter, topGutter),
                        size = Size(rightGutter, h - topGutter - bottomGutter)
                    )
                    // Shade bottom title & sound
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(0f, h - bottomGutter),
                        size = Size(w, bottomGutter)
                    )
                }
                SafeZoneType.REELS -> {
                    // Instagram Reels: Top header ~9%, Bottom username & audio ~24%, Right buttons ~18%
                    val topGutter = h * 0.09f
                    val bottomGutter = h * 0.22f
                    val rightGutter = w * 0.18f

                    drawRect(
                        color = Color(0xFFE1306C).copy(alpha = 0.7f),
                        topLeft = Offset(w * 0.04f, topGutter),
                        size = Size(w - rightGutter - w * 0.06f, h - topGutter - bottomGutter),
                        style = strokeDashed
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        topLeft = Offset(w - rightGutter, topGutter),
                        size = Size(rightGutter, h - topGutter - bottomGutter)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(0f, h - bottomGutter),
                        size = Size(w, bottomGutter)
                    )
                }
                SafeZoneType.TIKTOK -> {
                    // TikTok: Top navigation ~11%, Bottom caption ~20%, Right interaction icons ~16%
                    val topGutter = h * 0.11f
                    val bottomGutter = h * 0.20f
                    val rightGutter = w * 0.16f

                    drawRect(
                        color = Color(0xFF00F2FE).copy(alpha = 0.7f),
                        topLeft = Offset(w * 0.04f, topGutter),
                        size = Size(w - rightGutter - w * 0.06f, h - topGutter - bottomGutter),
                        style = strokeDashed
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.25f),
                        topLeft = Offset(w - rightGutter, topGutter),
                        size = Size(rightGutter, h - topGutter - bottomGutter)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(0f, h - bottomGutter),
                        size = Size(w, bottomGutter)
                    )
                }
                SafeZoneType.NONE -> {}
            }
        }

        // Overlay Label badge
        Surface(
            shape = RoundedCornerShape(bottomEnd = 8.dp),
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioCrimson)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${safeZoneType.label} Safe Zone",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Bottom Sheet modal to select video transitions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTransitionBottomSheet(
    currentTransition: VideoTransitionEffect,
    transitionDurationSec: Float,
    onSelectTransition: (VideoTransitionEffect) -> Unit,
    onChangeDuration: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("transition_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = StudioCrimson)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Clip Transition", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Done", fontWeight = FontWeight.Bold, color = StudioCrimson)
                }
            }

            Text(
                text = "Apply transition effect between current clip and subsequent scene.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Transition Cards Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VideoTransitionEffect.values().forEach { effect ->
                    val isSelected = currentTransition == effect
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StudioCrimson.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, StudioCrimson) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTransition(effect) }
                            .testTag("transition_card_${effect.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(effect.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = effect.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) StudioCrimson else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = effect.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Duration Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Transition Duration", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${String.format("%.1f", transitionDurationSec)}s", fontSize = 12.sp, color = StudioCrimson, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = transitionDurationSec,
                    onValueChange = onChangeDuration,
                    valueRange = 0.2f..2.0f,
                    steps = 17,
                    colors = SliderDefaults.colors(thumbColor = StudioCrimson, activeTrackColor = StudioCrimson)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
