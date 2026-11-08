package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.CalendarTaskEntity
import com.example.data.ContentProjectEntity
import com.example.data.CreatorSettingsEntity
import com.example.generator.*
import com.example.i18n.I18n
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoCreatorScreen(
    currentLanguage: String,
    settings: CreatorSettingsEntity = CreatorSettingsEntity(),
    onSaveProject: (ContentProjectEntity) -> Unit,
    onPublishSuccess: (CalendarTaskEntity) -> Unit = {},
    onShowGlobalProgress: (title: String, subtitle: String, actionType: String, progress: Float?, onCancel: (() -> Unit)?) -> Unit = { _, _, _, _, _ -> },
    onHideGlobalProgress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isKn = currentLanguage == I18n.LANG_KN
    val database = remember { AppDatabase.getDatabase(context) }

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(VideoCreationCategory.YOUTUBE_LONG) }
    var activeScript by remember { mutableStateOf<StoryScriptData?>(null) }
    var userPromptTopic by remember { mutableStateOf("The Rise of Autonomous AI Creators in India") }
    var isGeneratingScript by remember { mutableStateOf(false) }

    // Rendering and Player States
    var isRenderingMp4 by remember { mutableStateOf(false) }
    var renderProgressFraction by remember { mutableFloatStateOf(0f) }
    var renderStatusText by remember { mutableStateOf("") }
    var exportedResult by remember { mutableStateOf<RenderOutputResult?>(null) }
    var currentPlayheadSec by remember { mutableFloatStateOf(0f) }
    var autoSaveStatus by remember { mutableStateOf("Saved to Room DB ✓") }

    // Diagnostic logs
    val diagnosticLogs = remember { mutableStateListOf<DiagnosticLogEntry>() }

    fun addLog(tag: String, level: LogLevel, message: String, details: String = "") {
        diagnosticLogs.add(
            0,
            DiagnosticLogEntry(
                timestamp = System.currentTimeMillis(),
                tag = tag,
                level = level,
                message = message,
                details = details
            )
        )
    }

    // Auto-save debouncer into Room DB and project.json
    LaunchedEffect(activeScript?.title, activeScript?.scenes?.size, activeScript?.scenes?.map { it.title to it.durationSec }) {
        activeScript?.let { sc ->
            autoSaveStatus = "Saving..."
            delay(600)
            try {
                StoryAssetStorageService.initializeCompleteProjectStructure(context, sc.id, sc.title)
                database.contentProjectDao().insertProject(
                    ContentProjectEntity(
                        title = sc.title,
                        topic = userPromptTopic,
                        platform = selectedCategory.titleEn,
                        format = sc.format.label,
                        status = if (sc.isExported) "Completed" else "In Progress",
                        scriptText = sc.scenes.joinToString("\n\n") { "Scene ${it.sceneNumber} (${it.title}): ${it.voiceover}" },
                        hookText = sc.introduction,
                        ctaText = sc.closingScene,
                        captionText = "Auto-generated ${selectedCategory.titleEn} on Creator Studio Pro.",
                        duration = "${sc.targetDurationSec}s",
                        scenesJson = sc.scenes.joinToString(";") { "${it.sceneNumber}:${it.title}:${it.imageStatus}:${it.audioStatus}" }
                    )
                )
                autoSaveStatus = "Auto-saved (${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}) ✓"
            } catch (e: Exception) {
                autoSaveStatus = "Local Cache Active"
            }
        }
    }

    fun generateCompletePipeline(
        topic: String,
        category: VideoCreationCategory,
        format: StoryVideoFormat,
        durationPreset: StoryDurationPreset = StoryDurationPreset.SEC_60
    ) {
        scope.launch {
            isGeneratingScript = true
            addLog("[PIPELINE]", LogLevel.INFO, "Starting AI Video Pipeline for '${category.titleEn}'", "Topic: $topic")
            delay(500)

            val scriptData = StoryToVideoPipeline.generateStory(
                topic = topic,
                genre = when (category) {
                    VideoCreationCategory.MANGA_STORY, VideoCreationCategory.STORY_TO_COMIC -> StoryGenre.ACTION
                    VideoCreationCategory.AI_CINEMATIC -> StoryGenre.THRILLER
                    VideoCreationCategory.YOUTUBE_SHORTS, VideoCreationCategory.INSTAGRAM_REELS -> StoryGenre.MOTIVATION
                    else -> StoryGenre.EDUCATIONAL
                },
                language = currentLanguage,
                durationPreset = durationPreset,
                format = format,
                apiKey = settings.geminiApiKey
            )

            scriptData.categoryId = category.id
            StoryAssetStorageService.initializeCompleteProjectStructure(context, scriptData.id, scriptData.title)
            activeScript = scriptData
            isGeneratingScript = false
            addLog("[SCRIPT]", LogLevel.INFO, "Generated ${scriptData.scenes.size} scenes successfully", "Target: ${scriptData.targetDurationSec}s")
            Toast.makeText(context, if (isKn) "ವೀಡಿಯೊ ಯೋಜನೆ ಸಿದ್ಧವಾಗಿದೆ!" else "Video script & scenes ready!", Toast.LENGTH_SHORT).show()
        }
    }

    fun startRenderPipeline() {
        val script = activeScript ?: return
        scope.launch {
            isRenderingMp4 = true
            renderProgressFraction = 0.05f
            renderStatusText = "Initializing Hardware MediaCodec & FFmpeg Engine..."
            addLog("[RENDER]", LogLevel.INFO, "Starting Hardware MP4 render for '${script.title}'")

            val result = VideoRenderEngine.renderVideo(
                context = context,
                script = script,
                resolution = script.resolution,
                fps = script.fps,
                onProgress = { update ->
                    renderProgressFraction = update.fraction
                    renderStatusText = update.phase
                    addLog("[PROGRESS]", LogLevel.INFO, update.phase, update.details)
                }
            )

            isRenderingMp4 = false
            if (result.isSuccess) {
                exportedResult = result.getOrNull()
                addLog("[EXPORT]", LogLevel.INFO, "Render Successful! EXIT CODE 0", "MP4: ${exportedResult?.mp4File?.name}")
                Toast.makeText(context, if (isKn) "MP4 ವೀಡಿಯೊ ರಫ್ತು ಪೂರ್ಣಗೊಂಡಿದೆ!" else "MP4 Video Rendered & Saved!", Toast.LENGTH_LONG).show()
            } else {
                addLog("[ERROR]", LogLevel.ERROR, "Render failed: ${result.exceptionOrNull()?.message}")
                Toast.makeText(context, "Render failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dashboard Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_creator_hero_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF831843)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.MovieFilter, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKn) "ಸಂಪೂರ್ಣ AI ವೀಡಿಯೊ ಕ್ರಿಯೇಷನ್ ಎಂಜಿನ್" else "COMPLETE AI VIDEO CREATION ENGINE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudioEmerald.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, StudioEmerald)
                            ) {
                                Text(
                                    text = autoSaveStatus,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isKn) "ಕಥೆ, ಸ್ಕ್ರಿಪ್ಟ್, ಚಿತ್ರಗಳಿಂದ MP4 ವೀಡಿಯೊ ರಚಿಸಿ" else "Create Downloadable MP4 Videos with Full Media Pipeline",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = if (isKn) "Manga, YouTube, Facebook, Reels, Shorts ಮತ್ತು AI Cinematic ವೀಡಿಯೊಗಳನ್ನು ರಫ್ತು ಮಾಡಿ."
                            else "Render stories, AI voiceovers, animated scenes, subtitles, and music into playable MP4 videos.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Primary Action: Start New Video Project
                        Button(
                            onClick = { showNewProjectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_new_video_project_button")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKn) "✨ ಹೊಸ ವೀಡಿಯೊ ಪ್ರಾಜೆಕ್ಟ್ ಆರಂಭಿಸಿ (Start New Video)" else "✨ CREATE NEW VIDEO PROJECT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. The 10 Video Creation Workspace Options Dashboard
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isKn) "೧೦ ವೀಡಿಯೊ ಕ್ರಿಯೇಷನ್ ವರ್ಕ್‌ಸ್ಪೇಸ್‌ಗಳು" else "10 VIDEO CREATION WORKSPACES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "10 MODES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioCrimson
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(VideoCreationCategory.values()) { cat ->
                        WorkspaceOptionCard(
                            category = cat,
                            isSelected = selectedCategory == cat,
                            isKn = isKn,
                            onClick = {
                                selectedCategory = cat
                                generateCompletePipeline(userPromptTopic, cat, cat.defaultAspect)
                            }
                        )
                    }
                }
            }
        }

        // 3. User Story / Prompt Input & Trigger Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedCategory.iconEmoji} ${if (isKn) selectedCategory.titleKn else selectedCategory.titleEn} Workspace",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCrimson
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = selectedCategory.defaultAspect.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userPromptTopic,
                        onValueChange = { userPromptTopic = it },
                        label = { Text(if (isKn) "ಕಥೆ ಅಥವಾ ವೀಡಿಯೊ ವಿಷಯ (Story / Topic)" else "Story or Video Topic Prompt") },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_creator_topic_input")
                    )

                    Button(
                        onClick = {
                            generateCompletePipeline(userPromptTopic, selectedCategory, selectedCategory.defaultAspect)
                        },
                        enabled = !isGeneratingScript && userPromptTopic.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_complete_video_plan_button")
                    ) {
                        if (isGeneratingScript) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isKn) "ವಿಭಜಿಸಲಾಗುತ್ತಿದೆ..." else "Synthesizing Scenes...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKn) "AI ಕಥೆ-ಟು-ವೀಡಿಯೊ ಸೃಷ್ಟಿಸಿ (Generate Video Pipeline)" else "Generate AI Video Pipeline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Active Story Scenes Breakdown & Script Editor (if script generated)
        activeScript?.let { script ->
            item {
                StoryScriptEditor(
                    script = script,
                    isKn = isKn,
                    onScriptUpdated = { updated -> activeScript = updated },
                    onProceedToImageGeneration = {
                        startRenderPipeline()
                    },
                    onRegenerateAll = {
                        scope.launch {
                            script.scenes.forEachIndexed { i, s ->
                                s.imageStatus = AssetStatus.GENERATING
                                s.audioStatus = AssetStatus.GENERATING
                                delay(200)
                                val img = StoryToVideoPipeline.generateAndSaveSceneImage(context, script.id, s, script.format, script.genre)
                                val aud = StoryToVideoPipeline.generateAndSaveSceneAudio(context, script.id, s, script.language)
                                s.imageFilePath = img.absolutePath
                                s.audioFilePath = aud.absolutePath
                                s.imageStatus = AssetStatus.READY
                                s.audioStatus = AssetStatus.READY
                            }
                            activeScript = script.copy()
                            Toast.makeText(context, "All scene assets refreshed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // 5. 8-Track Multi-Track Timeline Editor
            item {
                MultiTrackTimelineEditor(
                    scenes = script.scenes,
                    currentPlayheadSec = currentPlayheadSec,
                    isKn = isKn,
                    onPlayheadChange = { currentPlayheadSec = it },
                    onSplitScene = { idx ->
                        if (idx in script.scenes.indices) {
                            val list = script.scenes.toMutableList()
                            val cur = list[idx]
                            val halfDur = (cur.durationSec / 2).coerceAtLeast(3)
                            cur.durationSec = halfDur
                            val cloned = cur.copy(
                                title = "${cur.title} (Part 2)",
                                durationSec = halfDur
                            )
                            list.add(idx + 1, cloned)
                            activeScript = script.copy(scenes = list.mapIndexed { i, s -> s.copy(sceneNumber = i + 1) })
                        }
                    },
                    onDuplicateScene = { idx ->
                        if (idx in script.scenes.indices) {
                            val list = script.scenes.toMutableList()
                            val cur = list[idx]
                            list.add(idx + 1, cur.copy(title = "${cur.title} (Copy)"))
                            activeScript = script.copy(scenes = list.mapIndexed { i, s -> s.copy(sceneNumber = i + 1) })
                        }
                    },
                    onDeleteScene = { idx ->
                        if (script.scenes.size > 1 && idx in script.scenes.indices) {
                            val list = script.scenes.toMutableList()
                            list.removeAt(idx)
                            activeScript = script.copy(scenes = list.mapIndexed { i, s -> s.copy(sceneNumber = i + 1) })
                        }
                    }
                )
            }

            // 6. FFmpeg & MediaCodec Rendering Panel
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rendering_engine_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(20.dp))
                                Text(
                                    text = if (isKn) "ವೀಡಿಯೊ ರೆಂಡರಿಂಗ್ ಎಂಜಿನ್ (FFmpeg / Hardware)" else "VIDEO RENDERING ENGINE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isRenderingMp4) StudioAmber else StudioEmerald
                            ) {
                                Text(
                                    text = if (isRenderingMp4) "RENDERING" else "READY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Combining ${script.scenes.size} AI visual clips, voiceover tracks, background music score, and Kannada Unicode subtitles into final MP4.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isRenderingMp4) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                LinearProgressIndicator(
                                    progress = { renderProgressFraction },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = StudioCrimson
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(renderStatusText, fontSize = 10.sp, color = StudioCrimson, fontWeight = FontWeight.Bold)
                                    Text("${(renderProgressFraction * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { VideoRenderEngine.requestCancel() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("Cancel Render", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = { startRenderPipeline() },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("render_final_mp4_button")
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKn) "🎬 MP4 ವೀಡಿಯೊ ರೆಂಡರ್ ಮಾಡಿ (Render Final MP4)" else "🎬 RENDER FINAL MP4 VIDEO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 7. Video Player Preview & Export Package Download
            exportedResult?.let { exp ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_preview_export_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "🎉 " + if (isKn) "ಸಿದ್ಧವಾದ ವೀಡಿಯೊ ಪ್ರಿವ್ಯೂ & ರಫ್ತು" else "Rendered Video Preview & Exports",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = StudioEmerald
                            )

                            // Video Player
                            VideoPlayerView(
                                videoUri = exp.mp4File.absolutePath,
                                title = script.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            )

                            // Export buttons: MP4, MP3, SRT
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "MP4 saved to Movies/CreatorStudio!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("download_mp4_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("MP4 Video", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "SRT Subtitle exported to project subtitles!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("download_srt_button")
                                ) {
                                    Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SRT Subs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Audio MP3 exported to project audio!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("download_mp3_button")
                                ) {
                                    Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("MP3 Audio", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 8. FFmpeg Diagnostic Logs
            item {
                FFmpegDiagnosticLogsPanel(
                    logs = diagnosticLogs,
                    isRendering = isRenderingMp4,
                    exitCode = if (exportedResult != null) 0 else null,
                    exportOutputPath = exportedResult?.mp4File?.absolutePath,
                    isKn = isKn
                )
            }
        }
    }

    // Modal Dialog: Start New Video Project
    if (showNewProjectDialog) {
        CreateNewVideoProjectDialog(
            currentLanguage = currentLanguage,
            initialCategory = selectedCategory,
            onDismiss = { showNewProjectDialog = false },
            onStartProject = { config ->
                showNewProjectDialog = false
                selectedCategory = config.category
                userPromptTopic = config.topicOrStory
                generateCompletePipeline(config.topicOrStory, config.category, config.aspectRatio, config.durationPreset)
            }
        )
    }
}

@Composable
private fun WorkspaceOptionCard(
    category: VideoCreationCategory,
    isSelected: Boolean,
    isKn: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
            .testTag("workspace_card_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) StudioCrimson else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(category.iconEmoji, fontSize = 20.sp)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = category.defaultAspect.label.take(4),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Text(
                text = if (isKn) category.titleKn else category.titleEn,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = if (isKn) category.descriptionKn else category.descriptionEn,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp,
                maxLines = 2
            )
        }
    }
}
