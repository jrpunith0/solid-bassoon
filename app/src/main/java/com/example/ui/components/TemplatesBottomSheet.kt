package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCrimson

/**
 * Layout preset definition for videos.
 */
data class VideoLayoutPreset(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val iconEmoji: String,
    val defaultText: String,
    val bannerStyle: String
)

val DEFAULT_LAYOUT_PRESETS = listOf(
    VideoLayoutPreset(
        id = "hook",
        title = "The Viral Hook",
        category = "Opening",
        description = "Attention-grabbing top banner with pulsating badge to maximize first 3-second retention.",
        iconEmoji = "🔥",
        defaultText = "STOP SCROLLING! 🚨 Watch this before doing anything else",
        bannerStyle = "CrimsonTopBanner"
    ),
    VideoLayoutPreset(
        id = "lower_third",
        title = "Pro Lower Third",
        category = "Overlay",
        description = "Sleek creator branding bar with channel name, verification badge & topic title.",
        iconEmoji = "🏷️",
        defaultText = "Creator Studio AI • @viralcreator • Official Guide",
        bannerStyle = "LowerThirdBar"
    ),
    VideoLayoutPreset(
        id = "end_screen",
        title = "Engagement End Screen",
        category = "Closing",
        description = "High-converting closing card prompting subscription, comments, and links in bio.",
        iconEmoji = "🔔",
        defaultText = "SUBSCRIBE & TAP LINK IN BIO FOR FULL BLUEPRINT 🚀",
        bannerStyle = "ClosingCTA"
    ),
    VideoLayoutPreset(
        id = "split_screen",
        title = "Split Screen Reaction",
        category = "Format",
        description = "Dual-pane layout with 50/50 comparison or commentary reaction over B-roll.",
        iconEmoji = "📱",
        defaultText = "Expectation vs Reality 💡",
        bannerStyle = "DualPane"
    ),
    VideoLayoutPreset(
        id = "meme_quote",
        title = "Meme Header Bar",
        category = "Social",
        description = "White letterbox bar at top with bold black headline text for TikTok & Twitter virality.",
        iconEmoji = "💬",
        defaultText = "Nobody talks about this one feature... 🤯",
        bannerStyle = "MemeLetterbox"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesBottomSheet(
    selectedPresetId: String?,
    onSelectPreset: (VideoLayoutPreset) -> Unit,
    onDismiss: () -> Unit
) {
    var templateMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    val openCreatorPresets = androidx.compose.runtime.remember {
        com.example.data.opencreator.OpenCreatorCatalog.videoPresets.map { vp ->
            VideoLayoutPreset(
                id = vp.id,
                title = vp.titleEn,
                category = if (vp.isVertical) "9:16 Vertical" else "16:9 Cinema",
                description = vp.descriptionEn,
                iconEmoji = if (vp.isVertical) "📱" else "🎬",
                defaultText = "${vp.titleEn} • ${vp.cameraMovement}",
                bannerStyle = if (vp.isVertical) "VerticalCinema" else "WidescreenCinema"
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("templates_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = StudioCrimson)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Video Templates & OpenCreator", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = StudioCrimson, fontWeight = FontWeight.Bold)
                }
            }

            // Mode Selector
            TabRow(
                selectedTabIndex = templateMode,
                modifier = Modifier.fillMaxWidth(),
                divider = {}
            ) {
                Tab(
                    selected = templateMode == 0,
                    onClick = { templateMode = 0 },
                    text = { Text("Graphic Layouts (5)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = templateMode == 1,
                    onClick = { templateMode = 1 },
                    text = { Text("OpenCreator Presets (28)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Text(
                text = if (templateMode == 0)
                    "Apply pre-built visual presets and graphic overlay layouts to your active scene or timeline."
                else
                    "Imported directly from OpenCreator repository: 28 cinematic motion templates and framing presets.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val activeList = if (templateMode == 0) DEFAULT_LAYOUT_PRESETS else openCreatorPresets

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeList.size) { idx ->
                    val preset = activeList[idx]
                    val isSelected = selectedPresetId == preset.id
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StudioCrimson.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, StudioCrimson) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPreset(preset) }
                            .testTag("template_card_${preset.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(preset.iconEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) StudioCrimson else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StudioCrimson.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = preset.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StudioCrimson,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "\"${preset.defaultText}\"",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioCrimson)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
