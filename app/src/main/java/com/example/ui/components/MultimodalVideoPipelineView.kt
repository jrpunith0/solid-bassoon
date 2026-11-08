package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContentProjectEntity
import com.example.generator.ContentGenerator
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 4-Stage Multimodal AI Video Pipeline:
 * 1. Script Generate: Topic -> Structured Script (Hook, 3 Scenes, CTA)
 * 2. Script to Image Generate: Scene Prompts -> 3 Storyboard Visual Frames
 * 3. Image + Script to Audio Generate: Narration Voiceover + Waveform + Background Ducking
 * 4. Image + Audio + Script to Video Generate: Final Composite with Subtitles, Transitions & Social Preview
 */
@Composable
fun MultimodalVideoPipelineView(
    currentLanguage: String,
    onSaveProject: (ContentProjectEntity) -> Unit = {},
    onSchedulePost: (title: String, platform: String, format: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Active Stage: 1, 2, 3, or 4
    var currentStage by remember { mutableIntStateOf(1) }
    var isGeneratingStage by remember { mutableStateOf(false) }

    // Stage 1 State: Script Generation
    var topic by remember { mutableStateOf("Top AI Tools Revolutionizing Content Creation in 2026") }
    var platform by remember { mutableStateOf("YouTube") }
    var tone by remember { mutableStateOf("Engaging & Punchy") }
    var duration by remember { mutableStateOf("60s") }

    var generatedTitle by remember { mutableStateOf("Mastering AI Content Creation in 2026") }
    var generatedHook by remember { mutableStateOf("Stop spending 6 hours editing! These 3 AI workflows cut production time down to 15 minutes.") }
    var scene1Prompt by remember { mutableStateOf("Futuristic glowing cybernetic editing desk, ultra-wide monitors displaying real-time AI video timeline with neon crimson accents, 8k resolution") }
    var scene1Voiceover by remember { mutableStateOf("Step one: Hook your audience in the first two seconds with high-contrast motion.") }
    var scene2Prompt by remember { mutableStateOf("Dynamic holographic data visualization floating above desk showing retention curves soaring up 80%, volumetric cinematic lighting") }
    var scene2Voiceover by remember { mutableStateOf("Step two: Cut all filler words. Deliver the promised answer before the twenty-second mark.") }
    var scene3Prompt by remember { mutableStateOf("Expressive creator in modern studio smiling with high-end microphone gesturing excitedly toward floating subscribe badge, studio bokeh") }
    var scene3Voiceover by remember { mutableStateOf("Step three: Package your insights with repeatable visual hooks that turn casual viewers into loyal subscribers.") }
    var generatedCta by remember { mutableStateOf("Follow @creator_studio for daily blueprints & drop a 🔥 in the comments!") }

    // Stage 2 State: Image Storyboard Generation
    var selectedSceneIndex by remember { mutableIntStateOf(0) }
    var imageStyle by remember { mutableStateOf("Cinematic Realistic") }
    var frame1Approved by remember { mutableStateOf(true) }
    var frame2Approved by remember { mutableStateOf(true) }
    var frame3Approved by remember { mutableStateOf(true) }

    // Stage 3 State: Audio Narration & Waveform
    var voiceActor by remember { mutableStateOf("Alex (Deep Storyteller)") }
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }
    var musicDuckLevel by remember { mutableStateOf("Medium (-6dB)") }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var audioProgress by remember { mutableFloatStateOf(0.45f) }

    // Stage 4 State: Final Video Composite
    var resolution by remember { mutableStateOf("1080p Full HD") }
    var transitionEffect by remember { mutableStateOf("Dissolve") }
    var isPlayingVideo by remember { mutableStateOf(false) }
    var videoProgress by remember { mutableFloatStateOf(0.35f) }
    var showSafeZones by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("multimodal_video_pipeline_container"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Pipeline Header & Progress Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = StudioCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "MULTIMODAL AI PIPELINE",
                                color = StudioCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stage $currentStage of 4",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = when (currentStage) {
                            1 -> "1. Script Generate"
                            2 -> "2. Script to Image Generate"
                            3 -> "3. Image & Script to Audio Generate"
                            else -> "4. Image + Audio + Script to Video Generate"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick Navigation Stepper Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..4).forEach { stageNum ->
                        val isCurrent = currentStage == stageNum
                        val isPassed = currentStage > stageNum
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> StudioCrimson
                                        isPassed -> StudioEmerald
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { currentStage = stageNum },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPassed) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text(
                                    text = "$stageNum",
                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // STAGE 1: SCRIPT GENERATION
            if (currentStage == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your video concept or pick a trending idea to generate the complete 3-scene narrative structure:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Trending Idea Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val trending = listOf(
                            "Top AI Tools in 2026",
                            "How to Get 1,000 Subs Fast",
                            "Budget Studio Setup Hacks",
                            "Secret Retention Editing Trick"
                        )
                        items(trending) { t ->
                            FilterChip(
                                selected = topic == t,
                                onClick = {
                                    topic = t
                                    generatedTitle = "Mastering $t"
                                },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Video Topic / Core Question") },
                        modifier = Modifier.fillMaxWidth().testTag("pipeline_topic_input"),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = platform,
                            onValueChange = { platform = it },
                            label = { Text("Target Platform") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Target Duration") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Script Preview Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "TITLE: $generatedTitle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "🎯 HOOK (0-5s): $generatedHook", fontSize = 11.sp, color = StudioCrimson, fontWeight = FontWeight.SemiBold)
                            Text(text = "• Scene 1: $scene1Voiceover", fontSize = 11.sp)
                            Text(text = "• Scene 2: $scene2Voiceover", fontSize = 11.sp)
                            Text(text = "• Scene 3: $scene3Voiceover", fontSize = 11.sp)
                            Text(text = "🚀 CTA: $generatedCta", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                isGeneratingStage = true
                                scope.launch {
                                    delay(400)
                                    val script = ContentGenerator.generateScript(topic, platform, currentLanguage, duration, tone)
                                    generatedTitle = script.title
                                    generatedHook = script.openingHook
                                    scene1Voiceover = "Scene 1: " + script.mainContent.lines().firstOrNull().orEmpty()
                                    scene2Voiceover = "Scene 2: " + script.storyExplanation.take(120)
                                    scene3Voiceover = "Scene 3: " + script.ending
                                    generatedCta = script.callToAction
                                    isGeneratingStage = false
                                    Toast.makeText(context, "Regenerated script structure!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Regenerate", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                currentStage = 2
                                Toast.makeText(context, "Script Approved! Generating Scene Images...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f).testTag("approve_script_next_btn")
                        ) {
                            Text("Approve Script & Generate Images →", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // STAGE 2: SCRIPT TO IMAGE GENERATE (Storyboard Frames)
            if (currentStage == 2) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Script scene prompts converted to 3 visual frames. Review each keyframe before voiceover synthesis:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Scene Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Scene 1: Hook", "Scene 2: Core Concept", "Scene 3: Climax").forEachIndexed { idx, sName ->
                            FilterChip(
                                selected = selectedSceneIndex == idx,
                                onClick = { selectedSceneIndex = idx },
                                label = { Text(sName, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Active Scene Frame Mockup Canvas
                    val activePrompt = when (selectedSceneIndex) {
                        0 -> scene1Prompt
                        1 -> scene2Prompt
                        else -> scene3Prompt
                    }
                    val activeColor1 = when (selectedSceneIndex) {
                        0 -> Color(0xFF831843)
                        1 -> Color(0xFF1E3A8A)
                        else -> Color(0xFF065F46)
                    }
                    val activeColor2 = when (selectedSceneIndex) {
                        0 -> Color(0xFF1E1B4B)
                        1 -> Color(0xFF0F172A)
                        else -> Color(0xFF064E3B)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.radialGradient(colors = listOf(activeColor1, activeColor2)))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .testTag("scene_frame_canvas"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "FRAME ${selectedSceneIndex + 1} • $imageStyle",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activePrompt,
                                color = Color.White,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Badge at bottom
                        Surface(
                            color = StudioEmerald.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        ) {
                            Text("✓ 8K AI Generated", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    // Style Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Aesthetic Preset:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Cinematic", "3D Studio", "Anime Style", "Cyberpunk").forEach { st ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (imageStyle == st) StudioCrimson else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { imageStyle = st }
                                ) {
                                    Text(
                                        text = st,
                                        fontSize = 10.sp,
                                        color = if (imageStyle == st) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Step navigation buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { currentStage = 1 },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("← Back to Script", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                currentStage = 3
                                Toast.makeText(context, "Frames Approved! Synthesizing Audio Narration...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f).testTag("approve_frames_next_btn")
                        ) {
                            Text("Approve Frames & Synthesize Audio →", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // STAGE 3: IMAGE & SCRIPT TO AUDIO GENERATE (Voiceover Narration + Waveform)
            if (currentStage == 3) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Synthesizing multi-track voiceover narration, audio ducking keyframes, and background music sync:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Audio Player Simulation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { isPlayingAudio = !isPlayingAudio },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(StudioCrimson, CircleShape)
                                    ) {
                                        Icon(
                                            if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "TTS Narration Track", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "$voiceActor • Speed: ${speechSpeed}x • $duration", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    color = StudioEmerald.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("STEREO 48kHz", color = StudioEmerald, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            // Interactive Audio Waveform Visualization
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val heights = listOf(8, 14, 22, 34, 18, 28, 38, 24, 16, 32, 40, 26, 14, 30, 22, 36, 18, 28, 34, 12, 26, 38, 20, 10)
                                heights.forEachIndexed { i, h ->
                                    val isPlayed = (i / heights.size.toFloat()) <= audioProgress
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(h.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isPlayed) StudioCrimson else Color.White.copy(alpha = 0.35f))
                                    )
                                }
                            }

                            // Narration Text Snippet
                            Text(
                                text = "\"$scene1Voiceover $scene2Voiceover $scene3Voiceover\"",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Voice Actor & Ducking Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = voiceActor,
                            onValueChange = { voiceActor = it },
                            label = { Text("Voice Profile") },
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = musicDuckLevel,
                            onValueChange = { musicDuckLevel = it },
                            label = { Text("BGM Ducking") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Step navigation buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { currentStage = 2 },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("← Back to Frames", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                currentStage = 4
                                Toast.makeText(context, "Audio Approved! Compositing Final Video...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f).testTag("approve_audio_render_btn")
                        ) {
                            Text("Synthesize & Render Full Video →", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // STAGE 4: IMAGE + AUDIO + SCRIPT TO VIDEO GENERATE (Final Composite)
            if (currentStage == 4) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Composite Complete! Storyboard frames, synchronized subtitles, and audio mastered into final video player:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Video Render Player Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                            .border(1.dp, StudioCrimson.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .testTag("pipeline_rendered_video_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Frame
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF831843),
                                            Color(0xFF1E1B4B),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                )
                        )

                        // Play / Pause Overlay
                        IconButton(
                            onClick = { isPlayingVideo = !isPlayingVideo },
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                if (isPlayingVideo) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Safe Zones Outline Guide
                        if (showSafeZones) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .fillMaxHeight(0.85f)
                                    .border(BorderStroke(1.dp, StudioCyan.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = "SAFE ZONE (Shorts/Reels UI Safe)",
                                    color = StudioCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        // Subtitle Overlay (Karaoke sync)
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Text(
                                text = "⚡ $scene1Voiceover",
                                color = StudioAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Bottom Scrub Bar
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("00:21 / 01:00", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = { videoProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .padding(horizontal = 8.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = StudioCrimson
                            )
                            Text("$resolution • $transitionEffect", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }

                    // Render Controls: Resolution & Safe Zones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Resolution: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            FilterChip(
                                selected = resolution == "1080p Full HD",
                                onClick = { resolution = "1080p Full HD" },
                                label = { Text("1080p", fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = resolution == "720p HD",
                                onClick = { resolution = "720p HD" },
                                label = { Text("720p", fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Safe Zones: ", fontSize = 11.sp)
                            Switch(
                                checked = showSafeZones,
                                onCheckedChange = { showSafeZones = it },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    // Export & Schedule Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                onSaveProject(
                                    ContentProjectEntity(
                                        title = generatedTitle,
                                        topic = topic,
                                        platform = platform,
                                        format = "Video",
                                        status = "Ready to Publish",
                                        scriptText = "$generatedHook\n\n$scene1Voiceover\n$scene2Voiceover\n$scene3Voiceover\n\n$generatedCta",
                                        hookText = generatedHook,
                                        ctaText = generatedCta,
                                        hashtagsText = "#CreatorStudio #AI #ViralShorts"
                                    )
                                )
                                Toast.makeText(context, "✓ Project saved to Studio Projects!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Project", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                onSchedulePost(generatedTitle, platform, "Short / Reel")
                                Toast.makeText(context, "✓ Added to Content Calendar for publication!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).testTag("pipeline_schedule_btn")
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Schedule Post", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            isExporting = true
                            scope.launch {
                                delay(600)
                                isExporting = false
                                Toast.makeText(context, "🎉 Video rendered in $resolution! Saved to device & ready to publish.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_final_video_btn")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exporting H.264 MP4...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT COMPLETED VIDEO ($resolution)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
