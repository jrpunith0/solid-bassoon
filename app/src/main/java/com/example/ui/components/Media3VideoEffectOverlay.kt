package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Common Video Effect Filters supported by Media3 Color Matrix & Shader API.
 */
enum class Media3VideoFilter(
    val id: String,
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val previewTint: Color,
    val blendMode: BlendMode
) {
    NONE(
        id = "none",
        displayName = "Original",
        description = "Standard unmodified camera color grade",
        icon = Icons.Default.FilterNone,
        previewTint = Color.Transparent,
        blendMode = BlendMode.SrcOver
    ),
    GRAYSCALE(
        id = "grayscale",
        displayName = "Grayscale",
        description = "Classic monochrome B&W with rich mid-tone contrast",
        icon = Icons.Default.MonochromePhotos,
        previewTint = Color(0xFF888888),
        blendMode = BlendMode.Saturation
    ),
    SEPIA(
        id = "sepia",
        displayName = "Sepia",
        description = "Warm vintage nostalgic tone with amber wash",
        icon = Icons.Default.FilterVintage,
        previewTint = Color(0xFF704214),
        blendMode = BlendMode.Color
    ),
    HIGH_CONTRAST(
        id = "high_contrast",
        displayName = "High Contrast",
        description = "Punchy deep shadows and amplified highlights",
        icon = Icons.Default.Contrast,
        previewTint = Color.Black,
        blendMode = BlendMode.Hardlight
    ),
    CYBERPUNK(
        id = "cyberpunk",
        displayName = "Cyberpunk",
        description = "Vibrant neon magenta highlights and electric cyan shadows",
        icon = Icons.Default.AutoAwesome,
        previewTint = Color(0xFFE000FF),
        blendMode = BlendMode.Color
    ),
    WARM(
        id = "warm",
        displayName = "Warm Sunset",
        description = "Rich golden hour radiant warmth and amber glow",
        icon = Icons.Default.WbSunny,
        previewTint = Color(0xFFFF9900),
        blendMode = BlendMode.Color
    ),
    COOL(
        id = "cool",
        displayName = "Cinematic Teal",
        description = "Moody Hollywood blockbuster teal and blue cast",
        icon = Icons.Default.AcUnit,
        previewTint = Color(0xFF00B4D8),
        blendMode = BlendMode.Color
    )
}

/**
 * Media3 Color Matrix calculations for real-time video effect application.
 */
object Media3EffectColorHelper {
    fun createFilterMatrix(filter: Media3VideoFilter, intensity: Float): ColorMatrix? {
        val factor = intensity.coerceIn(0f, 1f)
        if (filter == Media3VideoFilter.NONE || factor <= 0f) return null

        return when (filter) {
            Media3VideoFilter.NONE -> null
            Media3VideoFilter.GRAYSCALE -> {
                ColorMatrix().apply { setToSaturation(1f - factor) }
            }
            Media3VideoFilter.SEPIA -> {
                val rFactor = 0.393f * factor + (1f - factor)
                val gFactor = 0.686f * factor + (1f - factor)
                val bFactor = 0.131f * factor + (1f - factor)
                ColorMatrix(
                    floatArrayOf(
                        rFactor, 0.769f * factor, 0.189f * factor, 0f, 0f,
                        0.349f * factor, gFactor, 0.168f * factor, 0f, 0f,
                        0.272f * factor, 0.534f * factor, bFactor, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            Media3VideoFilter.HIGH_CONTRAST -> {
                val scale = 1f + (0.6f * factor)
                val translate = -35f * factor
                ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            Media3VideoFilter.CYBERPUNK -> {
                ColorMatrix(
                    floatArrayOf(
                        1f + (0.3f * factor), 0f, 0.3f * factor, 0f, 20f * factor,
                        0f, 1f - (0.1f * factor), 0.1f * factor, 0f, -10f * factor,
                        0.2f * factor, 0.2f * factor, 1f + (0.4f * factor), 0f, 30f * factor,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            Media3VideoFilter.WARM -> {
                ColorMatrix(
                    floatArrayOf(
                        1f + (0.25f * factor), 0.1f * factor, 0f, 0f, 20f * factor,
                        0f, 1f + (0.1f * factor), 0f, 0f, 10f * factor,
                        0f, 0f, 1f - (0.15f * factor), 0f, -20f * factor,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            Media3VideoFilter.COOL -> {
                ColorMatrix(
                    floatArrayOf(
                        1f - (0.15f * factor), 0f, 0f, 0f, -15f * factor,
                        0f, 1f + (0.08f * factor), 0.1f * factor, 0f, 5f * factor,
                        0f, 0.1f * factor, 1f + (0.35f * factor), 0f, 30f * factor,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
        }
    }
}

/**
 * Real-time Video Effect Overlay for PlayerView.
 * Mounts directly on top of video playback with configurable intensity and optional split-screen comparison.
 */
@Composable
fun VideoEffectPlaybackOverlay(
    activeFilter: Media3VideoFilter,
    intensity: Float,
    isSplitScreenPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (activeFilter == Media3VideoFilter.NONE || intensity <= 0f) return

    val tintAlpha = (when (activeFilter) {
        Media3VideoFilter.GRAYSCALE -> 1.0f
        Media3VideoFilter.SEPIA -> 0.65f
        Media3VideoFilter.HIGH_CONTRAST -> 0.40f
        Media3VideoFilter.CYBERPUNK -> 0.45f
        Media3VideoFilter.WARM -> 0.40f
        Media3VideoFilter.COOL -> 0.45f
        Media3VideoFilter.NONE -> 0f
    } * intensity).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                val w = size.width
                val h = size.height

                // If split-screen is enabled, apply effect to right half only
                val effectWidth = if (isSplitScreenPreview) w / 2f else w
                val effectLeft = if (isSplitScreenPreview) w / 2f else 0f

                drawRect(
                    color = activeFilter.previewTint.copy(alpha = tintAlpha),
                    topLeft = Offset(effectLeft, 0f),
                    size = Size(effectWidth, h),
                    blendMode = activeFilter.blendMode
                )

                // Draw split-screen separator needle
                if (isSplitScreenPreview) {
                    drawLine(
                        color = Color.White,
                        start = Offset(w / 2f, 0f),
                        end = Offset(w / 2f, h),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
    ) {
        if (isSplitScreenPreview) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = "ORIGINAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StudioCrimson.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "${activeFilter.displayName.uppercase()} (${(intensity * 100).toInt()}%)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Control Panel for Video Effects and Filters.
 */
@Composable
fun VideoEffectControlPanel(
    selectedFilter: Media3VideoFilter,
    filterIntensity: Float,
    isSplitScreen: Boolean,
    onSelectFilter: (Media3VideoFilter) -> Unit,
    onChangeIntensity: (Float) -> Unit,
    onToggleSplitScreen: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_effect_control_panel"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10141D)),
        border = BorderStroke(1.dp, Color(0xFF222B3B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title and Split-Screen Toggle
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
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = StudioCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Media3 Video Effect Filters",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Grayscale, Sepia & High Contrast with real-time shader grading",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Split Screen Comparison Toggle
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSplitScreen(!isSplitScreen)
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSplitScreen) StudioAmber.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, if (isSplitScreen) StudioAmber else Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("toggle_split_screen_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Split View",
                            tint = if (isSplitScreen) StudioAmber else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSplitScreen) "Split: On" else "Before/After",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSplitScreen) StudioAmber else Color.White
                        )
                    }
                }
            }

            // Filter Preset Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Media3VideoFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectFilter(filter)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) StudioCrimson else Color(0xFF19202E),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) StudioCrimson else Color(0xFF2B364A)
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("filter_chip_${filter.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = filter.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Active Filter Description
            Text(
                text = selectedFilter.description,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            // Intensity Slider (Only when a filter is active)
            if (selectedFilter != Media3VideoFilter.NONE) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Intensity:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${(filterIntensity * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioCrimson
                        )
                    }

                    Slider(
                        value = filterIntensity,
                        onValueChange = onChangeIntensity,
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioCrimson,
                            activeTrackColor = StudioCrimson,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .testTag("filter_intensity_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Soft Grade",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        TextButton(
                            onClick = onResetFilters,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Reset to Original", fontSize = 10.sp, color = StudioAmber)
                        }

                        Text(
                            text = "Maximum Tint",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
