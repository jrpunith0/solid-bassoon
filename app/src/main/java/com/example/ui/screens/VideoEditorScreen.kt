package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarTaskEntity
import com.example.data.ContentProjectEntity
import com.example.data.CreatorSettingsEntity
import com.example.data.MediaItemEntity
import com.example.data.VideoMetadataEntity
import com.example.generator.ContentGenerator
import com.example.i18n.I18n
import com.example.ui.components.*
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val GOOGLE_VEO_STUDIO_URL = "https://www.imagine.art/video/create/google-veo-3-1?utm_campaign=G_I_Web_Kling_VID_OArt_Int_IC_4449&utm_id=24235473486&hsa_acc=5196594449&campaign_id=24235473486&gad_source=1&gad_campaignid=24235473486&gbraid=0AAAABEZD_eUkTniDNemXrqrC1afaN4nku&gclid=CjwKCAjwuobXBhBXEiwAYUaIlcf0RLgPMzFNvBDkRjwy0-E_XYYMxUlQPyP5m6TyGuzq0lwTG2WQSxoCgesQAvD_BwE"

data class EditableScene(
    val sceneNumber: Int,
    var title: String,
    var voiceover: String,
    var visualPrompt: String,
    var onScreenText: String,
    var durationSec: Int = 10,
    var bRollTag: String = "Cinematic Studio B-Roll",
    var captionSyncMs: Long = 0L
)

data class EditorStateSnapshot(
    val scenes: List<EditableScene>,
    val filter: VideoFilterPreset,
    val transition: VideoTransitionEffect,
    val trimStartMs: Long,
    val trimEndMs: Long
)

@Composable
fun VideoEditorScreen(
    currentLanguage: String,
    settings: CreatorSettingsEntity,
    videoDrafts: List<VideoMetadataEntity> = emptyList(),
    onSaveProject: (ContentProjectEntity) -> Unit,
    onSaveVideoDraft: (VideoMetadataEntity) -> Unit = {},
    onUpdateDraftStatus: (Long, String) -> Unit = { _, _ -> },
    onDeleteDraft: (VideoMetadataEntity) -> Unit = {},
    onScheduleTask: (CalendarTaskEntity) -> Unit,
    onSyncToGitHub: (String, String) -> Unit = { _, _ -> },
    onShowGlobalProgress: (title: String, subtitle: String, actionType: String, progress: Float?, onCancel: (() -> Unit)?) -> Unit = { _, _, _, _, _ -> },
    onUpdateGlobalProgress: (progress: Float?, subtitle: String?) -> Unit = { _, _ -> },
    onHideGlobalProgress: () -> Unit = {},
    onAddMediaToLibrary: (MediaItemEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isKn = currentLanguage == I18n.LANG_KN

    val clipboardManager = LocalClipboardManager.current
    var showDraftsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showTransitionsSheet by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showThumbnailDialog by remember { mutableStateOf(false) }
    var showVeoDialog by remember { mutableStateOf(false) }

    var topic by remember { mutableStateOf("Top 5 AI Tools That Will Replace Creators in 2026") }
    var selectedFormat by remember { mutableStateOf("16:9 Long-Form") }
    var selectedTone by remember { mutableStateOf("Cinematic & Fast-Paced") }

    // Generation / Rendering state
    var isGenerating by remember { mutableStateOf(false) }
    var isRendering by remember { mutableStateOf(false) }
    var progressStatusText by remember { mutableStateOf("") }
    var progressStepText by remember { mutableStateOf("") }
    var currentProgressFraction by remember { mutableStateOf<Float?>(null) }
    var actionType by remember { mutableStateOf("generate") }

    // Video preview URI
    var previewVideoUri by remember {
        mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    }

    // Video Trimming state
    var videoDurationMs by remember { mutableLongStateOf(60000L) }
    var trimStartMs by remember { mutableLongStateOf(0L) }
    var trimEndMs by remember { mutableLongStateOf(60000L) }
    var isTrimPreviewActive by remember { mutableStateOf(false) }
    var currentPlaybackPositionMs by remember { mutableLongStateOf(0L) }

    // Filter, Transition & Safe Zone state
    var selectedFilter by remember { mutableStateOf(VideoFilterPreset.NONE) }
    var selectedTransition by remember { mutableStateOf(VideoTransitionEffect.DISSOLVE) }
    var transitionDurationSec by remember { mutableFloatStateOf(0.6f) }
    var selectedSafeZone by remember { mutableStateOf(SafeZoneType.NONE) }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }

    // Audio & Keyframes state
    var masterVolume by remember { mutableFloatStateOf(0.85f) }
    var fadeInSec by remember { mutableFloatStateOf(1.0f) }
    var fadeOutSec by remember { mutableFloatStateOf(1.5f) }
    var isAutoDuckingEnabled by remember { mutableStateOf(true) }

    // Media3 Effect & Transition States
    var activeEffectFilter by remember { mutableStateOf(Media3VideoFilter.NONE) }
    var filterIntensity by remember { mutableFloatStateOf(0.85f) }
    var isSplitCompareActive by remember { mutableStateOf(false) }

    var segmentTransitions by remember {
        mutableStateOf(
            listOf(
                SegmentTransitionItem(0, "Scene 1", "Scene 2", TimelineTransitionType.CROSS_DISSOLVE, 0.6f),
                SegmentTransitionItem(1, "Scene 2", "Scene 3", TimelineTransitionType.FADE_OUT, 0.8f)
            )
        )
    }
    var selectedJunctionIndex by remember { mutableIntStateOf(0) }

    // Multi-track active tab
    var selectedToolTab by remember { mutableIntStateOf(0) } // 0: Scenes, 1: Trimmer, 2: Filters, 3: Transitions, 4: Audio, 5: Multi-Track

    // Editable scenes list
    var scenes by remember {
        mutableStateOf(
            listOf(
                EditableScene(
                    sceneNumber = 1,
                    title = "The Viral Hook",
                    voiceover = "If you are still creating content manually in 2026, you are already falling behind. Here is why.",
                    visualPrompt = "Hyper-realistic futuristic creator studio with glowing holographic AI timelines, 8k resolution, cinematic lighting",
                    onScreenText = "STOP DOING THIS IN 2026 🛑",
                    durationSec = 5,
                    bRollTag = "Holographic Timeline Close-Up",
                    captionSyncMs = 0L
                ),
                EditableScene(
                    sceneNumber = 2,
                    title = "The Problem",
                    voiceover = "Top channels are publishing 10 videos a week because automated generative pipelines do 90% of the heavy lifting.",
                    visualPrompt = "Close-up of high-speed timeline scrubbing with AI sound waves and video cut points appearing dynamically",
                    onScreenText = "10 Videos / Week Formula 📈",
                    durationSec = 10,
                    bRollTag = "Fast Editing Screen Capture",
                    captionSyncMs = 5000L
                ),
                EditableScene(
                    sceneNumber = 3,
                    title = "The AI Solution",
                    voiceover = "Here are the 3 workflows you need: automated script synthesis, dynamic B-roll tagging, and automatic caption hooks.",
                    visualPrompt = "Split screen showing 3 AI video engines rendering simultaneously with clean UI metrics",
                    onScreenText = "The 3 AI Workflows ⚡",
                    durationSec = 15,
                    bRollTag = "Triple Split-Screen Demonstration",
                    captionSyncMs = 15000L
                ),
                EditableScene(
                    sceneNumber = 4,
                    title = "Call to Action",
                    voiceover = "Hit subscribe and check the link below to get the complete production package and GitHub repository.",
                    visualPrompt = "Cinematic YouTube subscribe button animation with glowing crimson accents and creator profile badge",
                    onScreenText = "SUBSCRIBE & LINK IN BIO 🔔",
                    durationSec = 8,
                    bRollTag = "Subscribe CTA & Outro Card",
                    captionSyncMs = 30000L
                )
            )
        )
    }

    var selectedSceneIndex by remember { mutableIntStateOf(0) }

    // Undo / Redo Stack Manager
    val undoStack = remember { mutableStateListOf<EditorStateSnapshot>() }
    val redoStack = remember { mutableStateListOf<EditorStateSnapshot>() }

    fun captureSnapshot() {
        val current = EditorStateSnapshot(
            scenes = scenes.map { it.copy() },
            filter = selectedFilter,
            transition = selectedTransition,
            trimStartMs = trimStartMs,
            trimEndMs = trimEndMs
        )
        undoStack.add(current)
        redoStack.clear()
        if (undoStack.size > 20) {
            undoStack.removeAt(0)
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.removeAt(undoStack.lastIndex)
            val current = EditorStateSnapshot(
                scenes = scenes.map { it.copy() },
                filter = selectedFilter,
                transition = selectedTransition,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs
            )
            redoStack.add(current)

            scenes = last.scenes
            selectedFilter = last.filter
            selectedTransition = last.transition
            trimStartMs = last.trimStartMs
            trimEndMs = last.trimEndMs
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "Undone last edit", Toast.LENGTH_SHORT).show()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            val current = EditorStateSnapshot(
                scenes = scenes.map { it.copy() },
                filter = selectedFilter,
                transition = selectedTransition,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs
            )
            undoStack.add(current)

            scenes = next.scenes
            selectedFilter = next.filter
            selectedTransition = next.transition
            trimStartMs = next.trimStartMs
            trimEndMs = next.trimEndMs
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "Redone edit", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleAiGenerateVideo() {
        captureSnapshot()
        scope.launch {
            isGenerating = true
            actionType = "generate"
            progressStatusText = "AI Engine: Writing script & analyzing viral hooks..."
            progressStepText = "Step 1 of 3: Natural language generation"
            currentProgressFraction = 0.25f
            onShowGlobalProgress(
                "AI Video Generation API Request",
                "Writing multi-scene script & generating storyboard...",
                "generate",
                0.25f
            ) {
                isGenerating = false
                currentProgressFraction = null
                onHideGlobalProgress()
            }
            delay(900)

            progressStatusText = "AI Engine: Creating scene-by-scene storyboard prompts..."
            progressStepText = "Step 2 of 3: Synthesizing visual cues & B-Roll"
            currentProgressFraction = 0.65f
            onUpdateGlobalProgress(0.65f, "Creating scene-by-scene storyboard prompts & B-roll visual cues...")
            delay(1000)

            val autoContent = ContentGenerator.generateAutoVideo(
                topic = topic,
                platform = if (selectedFormat.contains("9:16")) "Instagram" else "YouTube",
                language = currentLanguage,
                format = selectedFormat
            )

            val generatedScenes = mutableListOf<EditableScene>()
            generatedScenes.add(
                EditableScene(
                    sceneNumber = 1,
                    title = "Hook Scene",
                    voiceover = autoContent.script.lines().firstOrNull() ?: "Attention creators!",
                    visualPrompt = autoContent.visualPrompts.getOrElse(0) { "Cinematic studio shot" },
                    onScreenText = "MUST WATCH 🔥",
                    durationSec = 6,
                    bRollTag = "Viral Opening Hook Visual",
                    captionSyncMs = 0L
                )
            )
            generatedScenes.add(
                EditableScene(
                    sceneNumber = 2,
                    title = "Core Breakdown",
                    voiceover = autoContent.description.take(160),
                    visualPrompt = autoContent.visualPrompts.getOrElse(1) { "Engaging tutorial view" },
                    onScreenText = "The Secret Trick 💡",
                    durationSec = 12,
                    bRollTag = "Tutorial Action & Workflow Cues",
                    captionSyncMs = 6000L
                )
            )
            generatedScenes.add(
                EditableScene(
                    sceneNumber = 3,
                    title = "Call to Action",
                    voiceover = "Follow for daily creator automation tools and tips!",
                    visualPrompt = autoContent.visualPrompts.getOrElse(2) { "Subscribe and like button" },
                    onScreenText = "Follow for More! 🚀",
                    durationSec = 6,
                    bRollTag = "Animated Subscribe & Like Button",
                    captionSyncMs = 18000L
                )
            )

            scenes = generatedScenes
            selectedSceneIndex = 0

            progressStatusText = "Finalizing video timeline..."
            progressStepText = "Step 3 of 3: Preparing interactive player preview"
            currentProgressFraction = 1.0f
            onUpdateGlobalProgress(1.0f, "Preparing interactive VideoPlayerView preview...")
            delay(400)

            isGenerating = false
            currentProgressFraction = null
            onHideGlobalProgress()
            Toast.makeText(context, "AI Video Script & Timeline Generated!", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleRenderVideo() {
        scope.launch {
            isRendering = true
            actionType = "render"
            progressStatusText = "Rendering Timeline: Compiling ${scenes.size} video scenes..."
            progressStepText = "Rendering audio track, captions & transitions"
            currentProgressFraction = 0.3f
            onShowGlobalProgress(
                "Video Rendering Engine API",
                "Compiling ${scenes.size} timeline scenes & rendering audio...",
                "render",
                0.3f
            ) {
                isRendering = false
                currentProgressFraction = null
                onHideGlobalProgress()
            }
            delay(1200)

            progressStatusText = "Applying color grading and audio loudness normalization..."
            progressStepText = "Encoding MP4 (1080p 60fps)"
            currentProgressFraction = 0.75f
            onUpdateGlobalProgress(0.75f, "Encoding MP4 (1080p 60fps) & applying color grading...")
            delay(1100)

            progressStatusText = "Render complete! Loading into VideoPlayerView..."
            progressStepText = "Ready for playback preview"
            currentProgressFraction = 1.0f
            onUpdateGlobalProgress(1.0f, "Render complete! Loading into Media3 VideoPlayerView...")
            delay(500)

            previewVideoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            isRendering = false
            currentProgressFraction = null
            onHideGlobalProgress()
            Toast.makeText(context, "🎬 Video Rendered! Preview active in PlayerView.", Toast.LENGTH_LONG).show()
        }
    }

    fun handleSaveProject() {
        val totalSec = scenes.sumOf { it.durationSec }
        val project = ContentProjectEntity(
            title = topic,
            topic = topic,
            platform = if (selectedFormat.contains("9:16")) "Instagram" else "YouTube",
            format = selectedFormat,
            status = "Production",
            scriptText = scenes.joinToString("\n\n") { "Scene ${it.sceneNumber} (${it.title}): ${it.voiceover}" },
            hookText = scenes.firstOrNull()?.voiceover ?: "",
            ctaText = scenes.lastOrNull()?.voiceover ?: "",
            captionText = "Watch full video on $topic. Created with Creator Studio AI.",
            hashtagsText = "#CreatorStudio #VideoEditor #AI #ContentCreation",
            duration = "${totalSec}s"
        )
        onSaveProject(project)
        Toast.makeText(context, "Project saved to Content Studio!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("video_editor_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Title Bar with Undo / Redo & Pro Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isKn) "ವೀಡಿಯೊ ಸಂಪಾದಕ ಮತ್ತು AI ಜನರೇಟರ್" else "Video Editor & AI Generator",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ExoPlayer preview, dual-trimmer, ColorMatrix filters & multi-track",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Undo Button
                    IconButton(
                        onClick = { undo() },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.size(32.dp).testTag("editor_undo_button")
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Redo Button
                    IconButton(
                        onClick = { redo() },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier.size(32.dp).testTag("editor_redo_button")
                    ) {
                        Icon(
                            Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = { showDraftsDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("open_video_drafts_button")
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Drafts (${videoDrafts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Circular progress indicator during video generation or rendering
        item {
            VideoGenerationProgressIndicator(
                isVisible = isGenerating || isRendering,
                statusText = progressStatusText,
                stepText = progressStepText,
                progress = currentProgressFraction,
                actionType = actionType,
                onCancel = {
                    isGenerating = false
                    isRendering = false
                }
            )
        }

        // Primary Responsive Video Player Preview (Media3 PlayerView + Aspect Ratio Toggling)
        item {
            val currentAspect = remember(selectedFormat) {
                VideoAspectRatio.fromFormatString(selectedFormat)
            }

            ResponsiveVideoPreview(
                videoUri = previewVideoUri,
                title = topic,
                selectedAspectRatio = currentAspect,
                onAspectRatioChange = { newRatio ->
                    selectedFormat = when (newRatio) {
                        VideoAspectRatio.SHORTS_9_16 -> "9:16 Shorts/Reels"
                        VideoAspectRatio.LANDSCAPE_16_9 -> "16:9 Long-Form"
                        VideoAspectRatio.SQUARE_1_1 -> "1:1 Square"
                        VideoAspectRatio.PORTRAIT_4_5 -> "4:5 Social"
                        VideoAspectRatio.CINEMA_21_9 -> "21:9 Cinema"
                    }
                },
                filterPreset = selectedFilter,
                safeZoneType = selectedSafeZone,
                onSafeZoneChange = { selectedSafeZone = it },
                trimBounds = if (isTrimPreviewActive) Pair(trimStartMs, trimEndMs) else null,
                masterVolume = masterVolume,
                fadeInSec = fadeInSec,
                fadeOutSec = fadeOutSec,
                sceneMarkers = scenes.map { "S${it.sceneNumber}" },
                visualPrompts = scenes.map { it.visualPrompt },
                scriptText = scenes.firstOrNull()?.voiceover ?: "",
                onPositionChange = { pos -> currentPlaybackPositionMs = pos },
                onMarkInPoint = { inMs ->
                    trimStartMs = inMs
                    isTrimPreviewActive = true
                    Toast.makeText(context, "Marked In-point: ${inMs / 1000}s", Toast.LENGTH_SHORT).show()
                },
                onMarkOutPoint = { outMs ->
                    trimEndMs = outMs
                    isTrimPreviewActive = true
                    Toast.makeText(context, "Marked Out-point: ${outMs / 1000}s", Toast.LENGTH_SHORT).show()
                },
                onPickLocalVideo = { localUri ->
                    previewVideoUri = localUri
                    Toast.makeText(context, "Loaded local video file!", Toast.LENGTH_SHORT).show()
                },
                onSceneSelect = { index ->
                    if (index in scenes.indices) {
                        selectedSceneIndex = index
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_video_player")
            )
        }

        // Quick Tools Toolbar: Trimmer, Transitions, Templates, Thumbnail Grabber, Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Templates button
                FilledTonalButton(
                    onClick = { showTemplatesSheet = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("open_templates_button")
                ) {
                    Icon(Icons.Default.DashboardCustomize, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Templates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Transitions button
                FilledTonalButton(
                    onClick = { showTransitionsSheet = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("open_transitions_button")
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(selectedTransition.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Thumbnail Grabber
                FilledTonalButton(
                    onClick = { showThumbnailDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("grab_thumbnail_button")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thumbnail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Google Veo 3.1 AI Button
                FilledTonalButton(
                    onClick = { showVeoDialog = true },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = StudioPurple.copy(alpha = 0.15f),
                        contentColor = StudioPurple
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(36.dp).testTag("open_veo_dialog_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Veo 3.1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Export Settings Menu
                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("open_export_dialog_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tool Tabs (Scenes, Trimmer, Color Filter, Audio Envelopes, Multi-Track)
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedToolTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                listOf(
                    "🎬 Scenes (${scenes.size})",
                    "✂️ Trimmer",
                    "🎨 Filters & Effects",
                    "🔀 Transitions",
                    "🔊 Audio & Ducking",
                    "🎚️ Multi-Track"
                ).forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedToolTab == idx,
                        onClick = {
                            selectedToolTab = idx
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        text = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedToolTab == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // TOOL TAB 0: SCENES & AI GENERATOR
        if (selectedToolTab == 0) {
            // Google Veo 3.1 AI Studio Link & Presets Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, StudioPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("google_veo_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(StudioPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MovieFilter,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Google Veo 3.1 AI Video",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StudioPurple.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "IMAGINE.ART",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = StudioPurple,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Google DeepMind Veo 3.1 Generative Video Studio",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Link and generate cinematic scenes with Google Veo 3.1 & Kling. Copy active scene prompts directly to Veo 3.1 or load generated renders into the editor timeline.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_VEO_STUDIO_URL))
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Opening Google Veo 3.1 Studio (Imagine.art)...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                modifier = Modifier.weight(1f).testTag("launch_google_veo_button")
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch Veo 3.1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val promptToCopy = if (selectedSceneIndex in scenes.indices) {
                                        scenes[selectedSceneIndex].visualPrompt.ifBlank { topic }
                                    } else {
                                        topic
                                    }
                                    clipboardManager.setText(AnnotatedString(promptToCopy))
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_VEO_STUDIO_URL))
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Copied prompt! Opening Google Veo 3.1 Studio...", Toast.LENGTH_LONG).show()
                                },
                                border = BorderStroke(1.dp, StudioPurple),
                                modifier = Modifier.weight(1.2f).testTag("copy_scene_prompt_veo_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioPurple, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Prompt & Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPurple)
                            }
                        }

                        // Quick Import Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { showVeoDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import Veo 3.1 Video / Samples", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioEmerald)
                            }

                            Text(
                                text = "Veo 3.1 & Kling 60fps",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // AI Generator Configuration Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("ai_video_generator_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Generator Prompt",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            label = { Text("Video Topic / Core Idea") },
                            modifier = Modifier.fillMaxWidth().testTag("editor_topic_input"),
                            singleLine = false,
                            maxLines = 2
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("16:9 Long-Form", "9:16 Shorts/Reels", "1:1 Square", "4:5 Social", "21:9 Cinema").forEach { fmt ->
                                val isSel = selectedFormat == fmt
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedFormat = fmt },
                                    label = { Text(fmt, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when {
                                                fmt.contains("9:16") -> Icons.Default.Smartphone
                                                fmt.contains("1:1") -> Icons.Default.CropSquare
                                                fmt.contains("4:5") -> Icons.Default.CropPortrait
                                                fmt.contains("21:9") -> Icons.Default.Movie
                                                else -> Icons.Default.Tv
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { handleAiGenerateVideo() },
                                enabled = !isGenerating && !isRendering,
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                modifier = Modifier.weight(1f).testTag("editor_ai_generate_button")
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generating...")
                                } else {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Generate")
                                }
                            }

                            FilledTonalButton(
                                onClick = { handleRenderVideo() },
                                enabled = !isGenerating && !isRendering,
                                modifier = Modifier.weight(1f).testTag("editor_render_video_button")
                            ) {
                                if (isRendering) {
                                    CircularProgressIndicator(color = StudioCrimson, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rendering...")
                                } else {
                                    Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Render Video")
                                }
                            }
                        }
                    }
                }
            }

            // Scene Timeline Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Timeline Scenes (${scenes.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Duration: ${scenes.sumOf { it.durationSec }}s",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("editor_timeline_row")
                ) {
                    itemsIndexed(scenes) { index, scene ->
                        val isSelected = selectedSceneIndex == index
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioCrimson else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .clickable {
                                    selectedSceneIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .testTag("scene_chip_$index")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Scene ${scene.sceneNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = scene.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${scene.durationSec}s",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Active Scene Detail Editor
            if (selectedSceneIndex in scenes.indices) {
                val activeScene = scenes[selectedSceneIndex]
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().testTag("active_scene_editor_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Editing Scene ${activeScene.sceneNumber}: ${activeScene.title}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = StudioCrimson.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${activeScene.durationSec} sec",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioCrimson,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = activeScene.voiceover,
                                onValueChange = { newVo ->
                                    captureSnapshot()
                                    scenes = scenes.mapIndexed { idx, s ->
                                        if (idx == selectedSceneIndex) s.copy(voiceover = newVo) else s
                                    }
                                },
                                label = { Text("Voiceover Script") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )

                            OutlinedTextField(
                                value = activeScene.onScreenText,
                                onValueChange = { newText ->
                                    scenes = scenes.mapIndexed { idx, s ->
                                        if (idx == selectedSceneIndex) s.copy(onScreenText = newText) else s
                                    }
                                },
                                label = { Text("On-Screen Caption / Hook Text") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = activeScene.visualPrompt,
                                onValueChange = { newPrompt ->
                                    scenes = scenes.mapIndexed { idx, s ->
                                        if (idx == selectedSceneIndex) s.copy(visualPrompt = newPrompt) else s
                                    }
                                },
                                label = { Text("Visual Prompt for AI Video Generator") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(activeScene.visualPrompt))
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_VEO_STUDIO_URL))
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Copied Scene ${activeScene.sceneNumber} prompt! Opening Veo 3.1...", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("send_scene_to_veo_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioPurple, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Scene to Veo 3.1 (Imagine.art)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPurple)
                                }
                            }

                            // Duration Slider
                            Text(
                                text = "Scene Duration: ${activeScene.durationSec}s",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = activeScene.durationSec.toFloat(),
                                onValueChange = { newDur ->
                                    scenes = scenes.mapIndexed { idx, s ->
                                        if (idx == selectedSceneIndex) s.copy(durationSec = newDur.toInt()) else s
                                    }
                                },
                                valueRange = 3f..30f,
                                steps = 26,
                                colors = SliderDefaults.colors(thumbColor = StudioCrimson, activeTrackColor = StudioCrimson),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // TOOL TAB 1: MEDIA3 TIMELINE TRIMMER
        if (selectedToolTab == 1) {
            item {
                Media3TimelineTrimmer(
                    videoDurationMs = videoDurationMs,
                    trimStartMs = trimStartMs,
                    trimEndMs = trimEndMs,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    videoUri = previewVideoUri,
                    isLoopTrimActive = isTrimPreviewActive,
                    onTrimChange = { start, end ->
                        trimStartMs = start
                        trimEndMs = end
                    },
                    onSeekToPosition = { pos ->
                        currentPlaybackPositionMs = pos
                    },
                    onPreviewTrim = {
                        isTrimPreviewActive = true
                        Toast.makeText(context, "Looping within trimmed boundaries (${trimStartMs / 1000}s - ${trimEndMs / 1000}s)", Toast.LENGTH_SHORT).show()
                    },
                    onToggleLoopTrim = { active ->
                        isTrimPreviewActive = active
                    },
                    onApplyMedia3Clipping = { _ ->
                        isTrimPreviewActive = true
                        Toast.makeText(context, "Media3 Clipping configuration applied to clip!", Toast.LENGTH_LONG).show()
                    },
                    onResetTrim = {
                        trimStartMs = 0L
                        trimEndMs = videoDurationMs
                        isTrimPreviewActive = false
                        Toast.makeText(context, "Trim boundaries reset", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // TOOL TAB 2: COLOR GRADING & FILTERS & SHADER EFFECTS
        if (selectedToolTab == 2) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    VideoFilterSelectionPanel(
                        selectedFilter = selectedFilter,
                        onSelectFilter = { filter ->
                            captureSnapshot()
                            selectedFilter = filter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )

                    VideoEffectControlPanel(
                        selectedFilter = activeEffectFilter,
                        filterIntensity = filterIntensity,
                        isSplitScreen = isSplitCompareActive,
                        onSelectFilter = { eff ->
                            activeEffectFilter = eff
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onChangeIntensity = { filterIntensity = it },
                        onToggleSplitScreen = { isSplitCompareActive = it },
                        onResetFilters = {
                            activeEffectFilter = Media3VideoFilter.NONE
                            filterIntensity = 0.85f
                            isSplitCompareActive = false
                        }
                    )
                }
            }
        }

        // TOOL TAB 3: TIMELINE TRANSITIONS (FADE IN, FADE OUT, CROSS-DISSOLVE)
        if (selectedToolTab == 3) {
            item {
                VideoTransitionManager(
                    transitions = segmentTransitions,
                    selectedJunctionIndex = selectedJunctionIndex,
                    onSelectJunction = { idx ->
                        selectedJunctionIndex = idx
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onUpdateTransition = { idx, type, dur ->
                        captureSnapshot()
                        segmentTransitions = segmentTransitions.mapIndexed { i, item ->
                            if (i == idx) item.copy(type = type, durationSec = dur) else item
                        }
                        Toast.makeText(context, "Set junction ${idx + 1} to ${type.displayName} (${dur}s)", Toast.LENGTH_SHORT).show()
                    },
                    onApplyToAll = { type, dur ->
                        captureSnapshot()
                        segmentTransitions = segmentTransitions.map {
                            it.copy(type = type, durationSec = dur)
                        }
                        Toast.makeText(context, "Applied ${type.displayName} to all timeline cuts!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // TOOL TAB 4: AUDIO & DUCKING
        if (selectedToolTab == 4) {
            item {
                AudioKeyframeEditor(
                    fadeInSec = fadeInSec,
                    fadeOutSec = fadeOutSec,
                    masterVolume = masterVolume,
                    isAutoDuckingEnabled = isAutoDuckingEnabled,
                    onChangeFadeIn = { fadeInSec = it },
                    onChangeFadeOut = { fadeOutSec = it },
                    onChangeMasterVolume = { masterVolume = it },
                    onToggleAutoDucking = { isAutoDuckingEnabled = it }
                )
            }
        }

        // TOOL TAB 5: MULTI-TRACK TIMELINE
        if (selectedToolTab == 5) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("multi_track_timeline_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Multi-Track Timeline Mixer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("5 Active Layers", fontSize = 11.sp, color = StudioEmerald, fontWeight = FontWeight.Bold)
                        }

                        // Track 1: Primary Video
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎬 V1: Primary Clip", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                            Text("Main 1080p Interview Footage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Track 2: B-Roll Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioPurple.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎞️ V2: B-Roll", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPurple, modifier = Modifier.width(110.dp))
                            Text("Layered Hologram Cues & Cuts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Track 3: Voiceover Audio
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StudioEmerald.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎙️ A1: Voiceover", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioEmerald, modifier = Modifier.width(110.dp))
                            Text("100% Vol • Dynamic Speech Track", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Track 4: Background Music
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF38BDF8).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎵 A2: Music", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.width(110.dp))
                            Text("${(masterVolume * 100).toInt()}% Vol • Auto-Ducked (-12dB)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Track 5: Captions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💬 T1: Captions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.width(110.dp))
                            Text("Auto-Synced Karaoke Kinetic Subtitles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Action Buttons: Save Project, Save Draft, Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { handleSaveProject() },
                    modifier = Modifier.weight(1f).testTag("editor_save_project_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Project", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val totalSec = scenes.sumOf { it.durationSec }
                        val draft = VideoMetadataEntity(
                            title = topic,
                            description = "Scenes: ${scenes.joinToString(" | ") { it.title }}",
                            topic = topic,
                            format = selectedFormat,
                            resolution = "1080p",
                            fps = 60,
                            durationSec = totalSec,
                            fileUri = previewVideoUri,
                            platform = if (selectedFormat.contains("9:16")) "Instagram" else "YouTube",
                            draftStatus = "Draft",
                            isDraft = true,
                            completionProgress = 0.5f,
                            aspectRatio = if (selectedFormat.contains("9:16")) "9:16" else "16:9"
                        )
                        onSaveVideoDraft(draft)
                    },
                    modifier = Modifier.weight(1f).testTag("editor_save_draft_button")
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Draft", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = {
                        val task = CalendarTaskEntity(
                            title = topic,
                            platform = if (selectedFormat.contains("9:16")) "Instagram" else "YouTube",
                            contentType = if (selectedFormat.contains("9:16")) "Reel" else "Video",
                            status = "Scheduled",
                            scheduledDate = "2026-10-20",
                            scheduledTime = "18:00",
                            notes = "Generated with AI Video Editor (${scenes.size} scenes)"
                        )
                        onScheduleTask(task)
                        Toast.makeText(context, "Added to Content Calendar!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).testTag("editor_schedule_calendar_button")
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule", fontSize = 12.sp)
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showDraftsDialog) {
        VideoDraftsManagerDialog(
            videoDrafts = videoDrafts,
            currentTopic = topic,
            currentFormat = selectedFormat,
            onSaveCurrentAsDraft = { draft -> onSaveVideoDraft(draft) },
            onLoadDraft = { draft ->
                topic = draft.topic.ifBlank { draft.title }
                selectedFormat = draft.format
                if (draft.fileUri.isNotBlank()) {
                    previewVideoUri = draft.fileUri
                }
                Toast.makeText(context, "Loaded draft: \"${draft.title}\"", Toast.LENGTH_SHORT).show()
            },
            onUpdateDraftStatus = onUpdateDraftStatus,
            onDeleteDraft = onDeleteDraft,
            onDismiss = { showDraftsDialog = false }
        )
    }

    if (showExportDialog) {
        VideoExportDialog(
            videoTitle = topic,
            videoUriOrUrl = previewVideoUri,
            onDismiss = { showExportDialog = false }
        )
    }

    if (showTransitionsSheet) {
        VideoTransitionBottomSheet(
            currentTransition = selectedTransition,
            transitionDurationSec = transitionDurationSec,
            onSelectTransition = { trans ->
                captureSnapshot()
                selectedTransition = trans
                Toast.makeText(context, "Applied ${trans.displayName} transition!", Toast.LENGTH_SHORT).show()
            },
            onChangeDuration = { transitionDurationSec = it },
            onDismiss = { showTransitionsSheet = false }
        )
    }

    if (showTemplatesSheet) {
        TemplatesBottomSheet(
            selectedPresetId = selectedTemplateId,
            onSelectPreset = { preset ->
                captureSnapshot()
                selectedTemplateId = preset.id
                // Apply layout preset headline to active scene
                if (selectedSceneIndex in scenes.indices) {
                    scenes = scenes.mapIndexed { idx, s ->
                        if (idx == selectedSceneIndex) s.copy(onScreenText = preset.defaultText) else s
                    }
                }
                Toast.makeText(context, "Applied ${preset.title} preset!", Toast.LENGTH_SHORT).show()
                showTemplatesSheet = false
            },
            onDismiss = { showTemplatesSheet = false }
        )
    }

    if (showThumbnailDialog) {
        ThumbnailGrabberDialog(
            currentTimestampMs = currentPlaybackPositionMs,
            defaultTopic = topic,
            onSaveThumbnail = { media ->
                onAddMediaToLibrary(media)
            },
            onDismiss = { showThumbnailDialog = false }
        )
    }

    if (showVeoDialog) {
        GoogleVeoStudioDialog(
            currentTopic = topic,
            activeScenePrompt = scenes.getOrNull(selectedSceneIndex)?.visualPrompt ?: topic,
            onLoadVideoUrl = { newVideoUrl ->
                previewVideoUri = newVideoUrl
                onAddMediaToLibrary(
                    MediaItemEntity(
                        title = "Veo 3.1 Render - $topic",
                        category = "Videos",
                        fileType = "video/mp4",
                        uriOrPath = newVideoUrl,
                        durationFormatted = "${scenes.sumOf { it.durationSec }}s",
                        sizeFormatted = "45 MB",
                        tags = "Veo3.1,ImagineArt,GoogleAI,DeepMind"
                    )
                )
                Toast.makeText(context, "🎬 Veo 3.1 video loaded into Editor Player!", Toast.LENGTH_SHORT).show()
                showVeoDialog = false
            },
            onDismiss = { showVeoDialog = false }
        )
    }
}
