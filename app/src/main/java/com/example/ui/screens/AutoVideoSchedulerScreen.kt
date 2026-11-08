package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.i18n.I18n
import com.example.ui.components.AccountLoginDialog
import com.example.ui.components.PlatformBadge
import com.example.ui.components.UserLoginDialog
import com.example.ui.components.VideoGenerationProgressIndicator
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.*
import com.example.util.YouTubeStudioAppLinkCard
import com.example.util.YouTubeStudioLinkHelper

@Composable
fun AutoVideoSchedulerScreen(
    currentLanguage: String,
    settings: CreatorSettingsEntity,
    autoVideoJobs: List<AutoVideoJobEntity>,
    promptTemplates: List<PromptTemplateEntity>,
    isSchedulerActive: Boolean,
    countdownSeconds: Int,
    isGeneratingNow: Boolean,
    isBatchReprocessing: Boolean = false,
    batchReprocessTotal: Int = 0,
    batchReprocessCurrent: Int = 0,
    batchReprocessProgress: Float = 0f,
    batchReprocessStatus: String = "",
    onReprocessLast100: () -> Unit = {},
    onReprocessAll: () -> Unit = {},
    onReprocessJob: (AutoVideoJobEntity) -> Unit = {},
    onCancelBatchReprocess: () -> Unit = {},
    onToggleScheduler: (Boolean) -> Unit,
    onTriggerAutoRun: (String, String, String, String) -> Unit,
    onDeleteJob: (AutoVideoJobEntity) -> Unit,
    onClearAllJobs: () -> Unit,
    onAddPrompt: (PromptTemplateEntity) -> Unit,
    onDeletePrompt: (PromptTemplateEntity) -> Unit,
    onIncrementPromptUsage: (Long) -> Unit,
    onSaveSettings: (CreatorSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        if (isKn) "⚡ 10-ನಿಮಿಷದ ಶೆಡ್ಯೂಲರ್" else "⚡ 10-Min Scheduler",
        if (isKn) "📚 ಎಲ್ಲಾ ಪ್ರಾಂಪ್ಟ್‌ಗಳು" else "📚 All Prompts Library",
        if (isKn) "🔑 ಲಾಗಿನ್ & ಸಾಮಾಜಿಕ ಖಾತೆಗಳು" else "🔑 Social & User Logins"
    )

    // Scheduler Form State
    var topicInput by remember { mutableStateOf("Top 3 Camera Hacks for Going Viral") }
    var selectedPlatform by remember { mutableStateOf("YouTube") }
    var selectedFormat by remember { mutableStateOf("9:16 Shorts") }
    var showAddPromptDialog by remember { mutableStateOf(false) }
    var showUserLoginDialog by remember { mutableStateOf(false) }
    var selectedPlatformForLogin by remember { mutableStateOf<String?>(null) }
    var previewVideoJob by remember { mutableStateOf<AutoVideoJobEntity?>(null) }

    // Prompt search & filter
    var promptSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = if (isKn) "10-ನಿಮಿಷದ ಆಟೋ ವೀಡಿಯೊ ಜನರೇಟರ್ & ಅಪ್‌ಲೋಡ್" else "10-MIN AUTO VIDEO GENERATOR & UPLOAD",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isKn) "Google AI Studio Gemini ಸ್ಕ್ರಿಪ್ಟ್ + 3 ದೃಶ್ಯ ಪ್ರಾಂಪ್ಟ್‌ಗಳು + YouTube, Instagram, Facebook ಗೆ ನೇರ ಆಟೋ-ಅಪ್‌ಲೋಡ್"
                    else "Automated Gemini script engine, 3 visual scene prompts & direct upload to YouTube, Instagram & Facebook every 10 minutes",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // TAB 1: 10-MIN AUTO VIDEO ENGINE & SCHEDULER
        if (selectedTabIndex == 0) {
            // Circular progress indicator during 10-minute generation
            item {
                VideoGenerationProgressIndicator(
                    isVisible = isGeneratingNow,
                    statusText = "10-Minute Video Engine Running...",
                    stepText = "Generating script & 3 visual scenes, then uploading",
                    actionType = "render"
                )
            }

            // Live Scheduler Banner & Countdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("scheduler_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Switch Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (isSchedulerActive) StudioEmerald else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isSchedulerActive) "10-MINUTE LOOP: ACTIVE" else "10-MINUTE LOOP: PAUSED",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (isSchedulerActive) StudioEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Background Auto-Video Generator & Uploader",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isSchedulerActive,
                                onCheckedChange = { onToggleScheduler(it) },
                                modifier = Modifier.testTag("scheduler_toggle_switch")
                            )
                        }

                        HorizontalDivider()

                        // Countdown Timer Clock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF1E1B4B)
                                        )
                                    )
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isKn) "ಮುಂದಿನ ಆಟೋ ವೀಡಿಯೊ ರನ್ ಇನ್ನಷ್ಟು ಕಾಲದಲ್ಲಿ:" else "NEXT AUTO VIDEO GENERATION IN:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                val minutes = countdownSeconds / 60
                                val seconds = countdownSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioAmber
                                )
                                Text(
                                    text = "Target: $selectedPlatform • Format: $selectedFormat",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Button(
                                onClick = {
                                    onTriggerAutoRun(topicInput, selectedPlatform, selectedFormat, currentLanguage)
                                },
                                enabled = !isGeneratingNow,
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("run_now_button")
                            ) {
                                if (isGeneratingNow) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generating...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RUN NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Auto-Upload Targets
                        Text(
                            text = if (isKn) "ಅಪ್‌ಲೋಡ್ ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಗುರಿ:" else "Auto-Upload Target Platforms:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("YouTube", "Instagram", "Facebook", "All").forEach { p ->
                                FilterChip(
                                    selected = selectedPlatform == p,
                                    onClick = { selectedPlatform = p },
                                    label = { Text(p, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("platform_chip_$p")
                                )
                            }
                        }

                        // Video Format
                        Text(
                            text = if (isKn) "ವೀಡಿಯೊ ಸ್ವರೂಪ:" else "Video Format:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("9:16 Shorts", "16:9 Long-Form").forEach { f ->
                                FilterChip(
                                    selected = selectedFormat == f,
                                    onClick = { selectedFormat = f },
                                    label = { Text(f, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Topic Prompt Input
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            label = { Text(if (isKn) "ವೀಡಿಯೊ ವಿಷಯ ಪ್ರಾಂಪ್ಟ್ (Topic Prompt)" else "Active Video Generation Topic Prompt") },
                            modifier = Modifier.fillMaxWidth().testTag("auto_video_topic_input"),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    val randomSamples = listOf(
                                        "Top 3 smartphone camera tricks that look like DSLR",
                                        "How to double your retention on YouTube Shorts",
                                        "AI tools every video creator should know in 2026",
                                        "ಕನ್ನಡದಲ್ಲಿ ಆನ್‌ಲೈನ್ ಗಳಿಕೆಯ ನೈಜ ದಾರಿಗಳು",
                                        "Stop making this 1 huge mistake in your video audio",
                                        "5 psychological tricks to make videos viral"
                                    )
                                    topicInput = randomSamples.random()
                                }) {
                                    Icon(Icons.Default.Shuffle, contentDescription = "Randomize topic")
                                }
                            }
                        )
                    }
                }
            }

            // Architecture / Pipeline Indicator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🤖 PIPELINE: Google AI Studio Gemini + TTS + FFmpeg + Social APIs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioPurple
                        )
                        Text(
                            text = "• Gemini 3.5 Flash generates SEO title, description, voiceover script & 3 visual scene prompts\n• Text-To-Speech renders voice track • FFmpeg compiles 1080p MP4 • Auto-uploaded directly to channel",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // YouTube Studio App Linking Card (Direct User Request)
            item {
                YouTubeStudioAppLinkCard(
                    currentLanguage = currentLanguage,
                    channelName = settings.channelName,
                    customStudioUrl = settings.youtubeStudioUrl
                )
            }

            // Past Auto-Generated & Uploaded Videos Feed
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isKn) "ಆಟೋ-ಅಪ್‌ಲೋಡ್ ಮಾಡಿದ ವೀಡಿಯೊಗಳು (${autoVideoJobs.size})"
                            else "AUTO-UPLOADED VIDEOS (${autoVideoJobs.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onReprocessLast100,
                                enabled = !isBatchReprocessing,
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("reprocess_last_100_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reprocess 100", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = onReprocessAll,
                                enabled = !isBatchReprocessing,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("reprocess_all_button")
                            ) {
                                Text("All", fontSize = 10.sp)
                            }

                            if (autoVideoJobs.isNotEmpty()) {
                                TextButton(onClick = onClearAllJobs, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                    Text("Clear", fontSize = 11.sp, color = Color.Red)
                                }
                            }
                        }
                    }

                    // Live Batch Reprocessing Banner
                    if (isBatchReprocessing) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_reprocess_banner"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reprocessing Batch ($batchReprocessCurrent / $batchReprocessTotal)",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onCancelBatchReprocess,
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(26.dp)
                                            .testTag("cancel_reprocess_button")
                                    ) {
                                        Text("Cancel", fontSize = 10.sp, color = Color.Red)
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { batchReprocessProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )

                                Text(
                                    text = batchReprocessStatus,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            if (autoVideoJobs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ready for Next 10-Minute Auto Video",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Click 'RUN NOW' or leave the timer running to auto-generate & upload.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(autoVideoJobs, key = { it.id }) { job ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("auto_job_${job.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PlatformBadge(platform = job.platform)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(job.format, fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StudioEmerald.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "✔ ${job.status}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioEmerald
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteJob(job) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Text(
                                text = job.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "🎙 Voiceover Script: \"${job.scriptText}\"",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 15.sp
                            )

                            // 3 Visual Scene Prompts Preview
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🎨 3 Generated Visual Scene Prompts:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioPurple)
                                    Text("1️⃣ ${job.visualPrompt1}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("2️⃣ ${job.visualPrompt2}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("3️⃣ ${job.visualPrompt3}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ID: ${job.uploadId} • ${job.durationSec}s",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { previewVideoJob = job },
                                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("preview_auto_job_${job.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Preview", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onReprocessJob(job) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("reprocess_job_${job.id}")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Reprocess", fontSize = 9.sp)
                                    }

                                    if (job.platform.equals("YouTube", ignoreCase = true)) {
                                        Button(
                                            onClick = {
                                                YouTubeStudioLinkHelper.openYouTubeStudioApp(context, job.uploadId)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(30.dp)
                                                .testTag("open_yt_studio_app_${job.id}")
                                        ) {
                                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Studio App", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val fullPkg = "Title: ${job.title}\n\nScript:\n${job.scriptText}\n\nVisual Prompts:\n1. ${job.visualPrompt1}\n2. ${job.visualPrompt2}\n3. ${job.visualPrompt3}\n\nDescription:\n${job.description}"
                                            clipboardManager.setText(AnnotatedString(fullPkg))
                                            Toast.makeText(context, "Copied video package!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Copy", fontSize = 9.sp)
                                    }

                                    Button(
                                        onClick = { openUrl(job.uploadUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Open", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: ALL PROMPTS LIBRARY ("add all prompt this app")
        if (selectedTabIndex == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isKn) "ಎಲ್ಲಾ ಪ್ರಾಂಪ್ಟ್‌ಗಳ ಲೈಬ್ರರಿ" else "ALL PROMPTS LIBRARY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isKn) "ಆಟೋ ಜನರೇಟರ್‌ಗಾಗಿ ಯಾವುದೇ ಪ್ರಾಂಪ್ಟ್ ಆಯ್ಕೆಮಾಡಿ" else "Pick, customize & run viral prompts in 1-click",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddPromptDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_prompt_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isKn) "ಹೊಸ ಪ್ರಾಂಪ್ಟ್" else "Add Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = promptSearchQuery,
                    onValueChange = { promptSearchQuery = it },
                    placeholder = { Text("Search prompts by title, category, tags...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("prompt_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Category Chips
            item {
                val categories = listOf("All", "Viral Shorts", "10-Min Video", "Reels", "Hooks", "Visual Prompts", "YouTube SEO", "Kannada Culture", "Facebook")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }

            val filteredPrompts = promptTemplates.filter { p ->
                val matchesSearch = promptSearchQuery.isBlank() ||
                        p.title.contains(promptSearchQuery, ignoreCase = true) ||
                        p.category.contains(promptSearchQuery, ignoreCase = true) ||
                        p.tags.contains(promptSearchQuery, ignoreCase = true) ||
                        p.templateText.contains(promptSearchQuery, ignoreCase = true)

                val matchesCat = selectedCategoryFilter == "All" ||
                        p.category.equals(selectedCategoryFilter, ignoreCase = true)

                matchesSearch && matchesCat
            }

            items(filteredPrompts, key = { it.id }) { prompt ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("prompt_card_${prompt.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StudioPurple.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(prompt.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioPurple)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text(prompt.platform, fontSize = 10.sp) },
                                    modifier = Modifier.height(24.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${prompt.usageCount} runs",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeletePrompt(prompt) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(15.dp))
                                }
                            }
                        }

                        Text(
                            text = prompt.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = prompt.templateText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        if (prompt.sampleTopic.isNotBlank()) {
                            Text(
                                text = "Sample Topic: \"${prompt.sampleTopic}\"",
                                fontSize = 10.sp,
                                color = StudioAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    topicInput = prompt.sampleTopic.ifBlank { prompt.title }
                                    selectedTabIndex = 0
                                    onIncrementPromptUsage(prompt.id)
                                    Toast.makeText(context, "Loaded into 10-Minute Auto Generator!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.5f).height(34.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("USE IN AUTO GENERATOR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(prompt.templateText))
                                    Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: SOCIAL & USER LOGINS (YouTube, Facebook, Instagram Login)
        if (selectedTabIndex == 2) {
            item {
                Text(
                    text = if (isKn) "ಸಾಮಾಜಿಕ & ಕ್ರಿಯೇಟರ್ ಲಾಗಿನ್ ಖಾತೆಗಳು" else "SOCIAL & CREATOR ACCOUNT LOGINS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Creator Studio User Login Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("user_account_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(StudioCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = settings.channelName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = settings.youTubeEmail.ifBlank { "punithjr2@gmail.com" },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Status: ACTIVE & VERIFIED",
                                fontSize = 11.sp,
                                color = StudioEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showUserLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Switch / Login", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // YouTube Account Card
            item {
                SocialAccountItemCard(
                    platform = "YouTube",
                    color = YouTubeRed,
                    icon = Icons.Default.PlayArrow,
                    isConnected = settings.isYouTubeConnected,
                    handle = settings.youTubeHandle,
                    email = settings.youTubeEmail,
                    onManage = { selectedPlatformForLogin = "YouTube" }
                )
            }

            // Instagram Account Card
            item {
                SocialAccountItemCard(
                    platform = "Instagram",
                    color = InstagramGradientPink,
                    icon = Icons.Default.CameraAlt,
                    isConnected = settings.isInstagramConnected,
                    handle = settings.instagramHandle,
                    email = "Connected for Reels",
                    onManage = { selectedPlatformForLogin = "Instagram" }
                )
            }

            // Facebook Account Card
            item {
                SocialAccountItemCard(
                    platform = "Facebook",
                    color = FacebookBlue,
                    icon = Icons.Default.ThumbUp,
                    isConnected = settings.isFacebookConnected,
                    handle = settings.facebookPageName,
                    email = "Connected for Videos & Posts",
                    onManage = { selectedPlatformForLogin = "Facebook" }
                )
            }

            // API Keys & Gemini Config Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google AI Studio (Gemini API) Security", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(
                            text = "Connected with Server-Side Gemini API key. Ready to generate high-engaging video scripts, visual prompts & auto-upload without client-side key exposure.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Add Prompt Dialog
    if (showAddPromptDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Viral Shorts") }
        var newTemplate by remember { mutableStateOf("") }
        var newTopic by remember { mutableStateOf("") }
        var newPlatform by remember { mutableStateOf("All") }

        Dialog(onDismissRequest = { showAddPromptDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add New Creator Prompt", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Prompt Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Category (e.g. Viral Shorts, Hooks)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newTemplate,
                        onValueChange = { newTemplate = it },
                        label = { Text("Prompt Instructions (Use {topic} placeholder)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = newTopic,
                        onValueChange = { newTopic = it },
                        label = { Text("Default Sample Topic") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddPromptDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank() && newTemplate.isNotBlank()) {
                                    onAddPrompt(
                                        PromptTemplateEntity(
                                            title = newTitle,
                                            category = newCategory,
                                            templateText = newTemplate,
                                            sampleTopic = newTopic,
                                            platform = newPlatform,
                                            tags = newCategory.lowercase(),
                                            isFavorite = true,
                                            usageCount = 0
                                        )
                                    )
                                    showAddPromptDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald)
                        ) {
                            Text("Save Prompt")
                        }
                    }
                }
            }
        }
    }

    // User Login Dialog
    if (showUserLoginDialog) {
        UserLoginDialog(
            settings = settings,
            onLoginSuccess = { updated ->
                onSaveSettings(updated)
                showUserLoginDialog = false
            },
            onDismiss = { showUserLoginDialog = false }
        )
    }

    // Social Platform Account Dialog
    selectedPlatformForLogin?.let { platform ->
        AccountLoginDialog(
            platform = platform,
            settings = settings,
            currentLanguage = currentLanguage,
            onSaveSettings = { updated ->
                onSaveSettings(updated)
                selectedPlatformForLogin = null
            },
            onDismiss = { selectedPlatformForLogin = null }
        )
    }

    // Auto Video Job Interactive Preview Dialog
    if (previewVideoJob != null) {
        val job = previewVideoJob!!
        Dialog(onDismissRequest = { previewVideoJob = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_video_preview_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${job.platform} • ${job.format} • ${job.durationSec}s",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { previewVideoJob = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    var selectedModalScene by remember { mutableIntStateOf(0) }
                    val activePrompts = remember(job) {
                        listOf(job.visualPrompt1, job.visualPrompt2, job.visualPrompt3).filter { it.isNotBlank() }
                    }

                    VideoPlayerView(
                        videoUri = if (job.uploadUrl.startsWith("http") && job.uploadUrl.endsWith(".mp4")) job.uploadUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        title = job.title,
                        aspectRatio = if (job.format.contains("9:16")) 9f / 16f else 16f / 9f,
                        autoPlay = true,
                        sceneMarkers = listOf("Scene 1", "Scene 2", "Scene 3"),
                        visualPrompts = activePrompts,
                        scriptText = job.scriptText,
                        onSceneSelect = { selectedModalScene = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Scene Stepper & Narration Listen Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (0 until activePrompts.size.coerceAtLeast(3)).forEach { idx ->
                                Surface(
                                    onClick = { selectedModalScene = idx },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedModalScene == idx) StudioPurple else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                        Text(
                                            "Scene ${idx + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = if (selectedModalScene == idx) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedModalScene == idx) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Listen Narration Button
                        FilledTonalButton(
                            onClick = {
                                com.example.generator.StoryToVideoPipeline.playSceneNarration(
                                    context = context,
                                    voiceoverText = job.scriptText,
                                    audioFilePath = null,
                                    language = currentLanguage
                                )
                                Toast.makeText(context, "Playing narration...", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Listen TTS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Visual prompts card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("🎨 3 Generated Visual Scene Prompts:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioPurple)
                            if (job.visualPrompt1.isNotBlank()) Text("1️⃣ ${job.visualPrompt1}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (job.visualPrompt2.isNotBlank()) Text("2️⃣ ${job.visualPrompt2}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (job.visualPrompt3.isNotBlank()) Text("3️⃣ ${job.visualPrompt3}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Voiceover Script preview
                    if (job.scriptText.isNotBlank()) {
                        Text(
                            text = "🎙 Voiceover Script: \"${job.scriptText}\"",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (job.platform.equals("YouTube", true)) {
                            Button(
                                onClick = {
                                    YouTubeStudioLinkHelper.openYouTubeStudioApp(context, job.uploadId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Studio App", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val fullPkg = "Title: ${job.title}\n\nScript:\n${job.scriptText}\n\nVisual Prompts:\n1. ${job.visualPrompt1}\n2. ${job.visualPrompt2}\n3. ${job.visualPrompt3}"
                                clipboardManager.setText(AnnotatedString(fullPkg))
                                Toast.makeText(context, "Copied video package!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text("Copy Pkg", fontSize = 10.sp)
                        }

                        Button(
                            onClick = { previewVideoJob = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("Close", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialAccountItemCard(
    platform: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isConnected: Boolean,
    handle: String,
    email: String,
    onManage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("social_card_${platform.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = platform,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isConnected) StudioEmerald.copy(alpha = 0.2f) else StudioAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isConnected) StudioEmerald else StudioAmber
                            )
                        }
                    }
                    Text(
                        text = if (isConnected) handle else "Not authorized for auto-upload",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = email,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onManage,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(if (isConnected) "Manage" else "Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
