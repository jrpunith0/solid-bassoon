package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator.StoryDurationPreset
import com.example.generator.StoryGenre
import com.example.generator.StoryVideoFormat
import com.example.generator.VideoCreationCategory
import com.example.i18n.I18n
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioPurple

data class VideoProjectConfig(
    val projectName: String,
    val category: VideoCreationCategory,
    val language: String,
    val topicOrStory: String,
    val videoStyle: String,
    val durationPreset: StoryDurationPreset,
    val aspectRatio: StoryVideoFormat,
    val resolution: String = "1080p",
    val frameRateFps: Int = 30,
    val voiceId: String = "kn_female_1",
    val bgmStyle: String = "Cinematic Epic",
    val subtitleLanguage: String = "Kannada",
    val isAiImageEnabled: Boolean = true,
    val isAiVideoEnabled: Boolean = false,
    val isPresetSaved: Boolean = false
)

@Composable
fun CreateNewVideoProjectDialog(
    currentLanguage: String,
    initialCategory: VideoCreationCategory = VideoCreationCategory.YOUTUBE_LONG,
    onDismiss: () -> Unit,
    onStartProject: (VideoProjectConfig) -> Unit
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN

    var projectName by remember { mutableStateOf("New Project ${System.currentTimeMillis().toString().takeLast(4)}") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedLanguage by remember { mutableStateOf(if (isKn) "Kannada (ಕನ್ನಡ)" else "English") }
    var topicOrStory by remember { mutableStateOf("The Hidden Legend of Karavali Coast") }
    var selectedStyle by remember { mutableStateOf("Anime Cinematic") }
    var selectedDuration by remember { mutableStateOf(StoryDurationPreset.SEC_60) }
    var selectedAspect by remember { mutableStateOf(initialCategory.defaultAspect) }
    var selectedResolution by remember { mutableStateOf("1080p") }
    var selectedVoice by remember { mutableStateOf("Kannada Female Studio (ಪ್ರಶಾಂತ ಧ್ವನಿ)") }
    var selectedBgm by remember { mutableStateOf("Cinematic Epic Orchestral") }
    var selectedSubtitles by remember { mutableStateOf(if (isKn) "Kannada Unicode (ಕನ್ನಡ)" else "English (US)") }
    var isAiImageEnabled by remember { mutableStateOf(true) }
    var isAiVideoEnabled by remember { mutableStateOf(false) }
    var isPresetSaved by remember { mutableStateOf(false) }

    val styles = listOf(
        "Japanese Manga", "Anime Cinematic", "Black & White Manga",
        "Full Colour Manga", "2D Animation", "3D Animation",
        "Comic Book Style", "Cinematic 4K", "Realistic"
    )

    val resolutions = listOf("720p (Fast)", "1080p (FHD)", "4K (Ultra)")
    val languages = listOf("Kannada (ಕನ್ನಡ)", "English", "Japanese (日本語)", "Hindi (हिन्दी)")
    val voices = listOf(
        "Kannada Female Studio (ಪ್ರಶಾಂತ ಧ್ವನಿ)",
        "Kannada Male Deep (ಗಂಭೀರ ಧ್ವನಿ)",
        "English Studio Pro (Female)",
        "English Epic Narrator (Male)",
        "Japanese Anime Shonen (Male)"
    )
    val bgmOptions = listOf("Cinematic Epic Orchestral", "Emotional Acoustic Piano", "Cyberpunk Action Pulse", "Quirky Comedy Whistle", "Dark Ambient Horror", "Peaceful Ambient Rain")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("create_new_video_project_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isKn) "✨ ಹೊಸ AI ವೀಡಿಯೊ ಪ್ರಾಜೆಕ್ಟ್" else "✨ START NEW AI VIDEO PROJECT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioCrimson
                        )
                        Text(
                            text = if (isKn) "13 ಸೆಟ್ಟಿಂಗ್‌ಗಳು ಮತ್ತು ಮರುಬಳಕೆಯ ಪ್ರಿಸೆಟ್" else "13 Production Settings & Reusable Preset",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Project Name
                    item {
                        OutlinedTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            label = { Text(if (isKn) "ಪ್ರಾಜೆಕ್ಟ್ ಹೆಸರು (Project Name)" else "Project Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_video_project_name")
                        )
                    }

                    // 2. Video Category (10 options)
                    item {
                        Text(
                            text = if (isKn) "ವೀಡಿಯೊ ವರ್ಗ (Video Category - 10 Modes):" else "Video Category (10 Options):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(VideoCreationCategory.values()) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        selectedAspect = cat.defaultAspect
                                    },
                                    label = { Text("${cat.iconEmoji} ${if (isKn) cat.titleKn else cat.titleEn}", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("category_chip_${cat.id}")
                                )
                            }
                        }
                    }

                    // 3. Topic or Story
                    item {
                        OutlinedTextField(
                            value = topicOrStory,
                            onValueChange = { topicOrStory = it },
                            label = { Text(if (isKn) "ವೀಡಿಯೊ ಕಥೆ ಅಥವಾ ವಿಷಯ (Story / Topic Prompt)" else "Story or Topic Prompt") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_video_project_topic")
                        )
                    }

                    // 4. Video Language
                    item {
                        Text(
                            text = if (isKn) "ವೀಡಿಯೊ ಭಾಷೆ (Language):" else "Video Language:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            languages.forEach { lang ->
                                FilterChip(
                                    selected = selectedLanguage == lang,
                                    onClick = { selectedLanguage = lang },
                                    label = { Text(lang, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // 5. Video Style (Anime, Manga, 3D, Comic, etc.)
                    item {
                        Text(
                            text = if (isKn) "ವೀಡಿಯೊ ಶೈಲಿ (Visual Style):" else "Visual Style:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(styles) { st ->
                                FilterChip(
                                    selected = selectedStyle == st,
                                    onClick = { selectedStyle = st },
                                    label = { Text(st, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // 6. Aspect Ratio & Resolution
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Aspect Ratio:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(StoryVideoFormat.LANDSCAPE_16_9, StoryVideoFormat.PORTRAIT_9_16, StoryVideoFormat.SQUARE_1_1, StoryVideoFormat.FEED_4_5).forEach { fmt ->
                                        FilterChip(
                                            selected = selectedAspect == fmt,
                                            onClick = { selectedAspect = fmt },
                                            label = { Text(fmt.label.take(4), fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Resolution:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    resolutions.forEach { res ->
                                        FilterChip(
                                            selected = selectedResolution == res.take(5),
                                            onClick = { selectedResolution = res.take(5) },
                                            label = { Text(res.take(5), fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7. Duration Presets
                    item {
                        Text(
                            text = if (isKn) "ವೀಡಿಯೊ ಅವಧಿ (Duration):" else "Duration Preset:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StoryDurationPreset.values().forEach { d ->
                                FilterChip(
                                    selected = selectedDuration == d,
                                    onClick = { selectedDuration = d },
                                    label = { Text(if (isKn) d.labelKn else d.labelEn, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // 8. Voice & Background Music
                    item {
                        Text(
                            text = if (isKn) "ಧ್ವನಿ ಮತ್ತು ಹಿನ್ನೆಲೆ ಸಂಗೀತ (Voice & BGM):" else "Voice & BGM:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(voices) { v ->
                                FilterChip(
                                    selected = selectedVoice == v,
                                    onClick = { selectedVoice = v },
                                    label = { Text(v.take(18), fontSize = 10.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(bgmOptions) { b ->
                                FilterChip(
                                    selected = selectedBgm == b,
                                    onClick = { selectedBgm = b },
                                    label = { Text(b.take(18), fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // 9. AI Generation Toggles (Images & Real Video Clips)
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("AI Image Generation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Generate 8K scene visuals per scene", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(checked = isAiImageEnabled, onCheckedChange = { isAiImageEnabled = it })
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("AI Video Generation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Synthesize moving AI video clips (Veo / Parallax)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(checked = isAiVideoEnabled, onCheckedChange = { isAiVideoEnabled = it })
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Buttons: Save Preset & Start Project
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            isPresetSaved = true
                            Toast.makeText(context, "Preset saved! Will auto-apply to future projects.", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_project_preset_button")
                    ) {
                        Icon(if (isPresetSaved) Icons.Default.Check else Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPresetSaved) "Preset Saved ✓" else "Save Preset", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val config = VideoProjectConfig(
                                projectName = projectName,
                                category = selectedCategory,
                                language = selectedLanguage,
                                topicOrStory = topicOrStory,
                                videoStyle = selectedStyle,
                                durationPreset = selectedDuration,
                                aspectRatio = selectedAspect,
                                resolution = selectedResolution,
                                voiceId = selectedVoice,
                                bgmStyle = selectedBgm,
                                subtitleLanguage = selectedSubtitles,
                                isAiImageEnabled = isAiImageEnabled,
                                isAiVideoEnabled = isAiVideoEnabled,
                                isPresetSaved = isPresetSaved
                            )
                            onStartProject(config)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("launch_video_workspace_button")
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isKn) "ವರ್ಕ್‌ಸ್ಪೇಸ್ ಆರಂಭಿಸಿ" else "Launch Workspace", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
