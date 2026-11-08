package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.*
import com.example.data.AiPromptItemEntity
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.ui.theme.*

/**
 * AI PROMPT STUDIO – MULTI AI GENERATOR SCREEN
 * Features:
 * 1. AI Provider Selection (ChatGPT, Gemini, Runway, Kling AI, ElevenLabs, Custom)
 * 2. Smart Prompt Builder with 16+ parameters
 * 3. Provider-optimized outputs with negative prompt, camera, character consistency, and audio cues
 * 4. Multi-Provider Side-by-Side Prompt Comparison
 * 5. Prompt actions: Generate, Improve, Translate (KN/EN), Shorten, Expand, Copy, Download, Save
 * 6. Direct generation via connected API with graceful prompt copy fallback
 */
@Composable
fun AiPromptStudioScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    promptList: List<AiPromptItemEntity> = emptyList(),
    onSavePromptToLibrary: (AiPromptItemEntity) -> Unit = {},
    onDeletePromptFromLibrary: (AiPromptItemEntity) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var selectedStudioMode by remember { mutableStateOf(0) } // 0 = Builder, 1 = Multi-Provider Compare, 2 = Prompt Library
    var selectedProvider by remember { mutableStateOf(AiProvider.GEMINI) }

    // Prompt Builder Fields
    var storyTopic by remember {
        mutableStateOf(if (isKn) "ಒಂದು ಹಳ್ಳಿಯಲ್ಲಿ ನಡೆಯುವ ರಹಸ್ಯಮಯ ಕಥೆ ಮತ್ತು ನಿಗೂಢ ದೇವಾಲಯ" else "Mystery of the forgotten temple ruins at sunset")
    }
    var characterName by remember { mutableStateOf("Arjun") }
    var characterAppearance by remember { mutableStateOf("Indian youth, hazel eyes, sharp hair, saffron creator jacket") }
    var location by remember { mutableStateOf("Ancient mysterious village at sunset with stone temple ruins") }
    var sceneDescription by remember { mutableStateOf("Walking slowly towards an abandoned heritage library as twilight falls") }
    var moodEmotion by remember { mutableStateOf("Mysterious & Suspenseful") }
    var artStyle by remember { mutableStateOf("Cinematic 3D Animation") }
    var cameraAngle by remember { mutableStateOf("Wide establishing shot") }
    var cameraMovement by remember { mutableStateOf("Slow cinematic tracking push-in with subtle parallax") }
    var lighting by remember { mutableStateOf("Golden hour sunset rim lighting with atmospheric volumetric dusk mist") }
    var videoDurationSec by remember { mutableStateOf(60) }
    var aspectRatio by remember { mutableStateOf("16:9") }
    var outputQuality by remember { mutableStateOf("1080p Full HD") }
    var voiceStyle by remember { mutableStateOf("Deep cinematic narration with warm emotional cadence") }
    var musicSfx by remember { mutableStateOf("Low suspense cello drone, rustic temple wind chime, subtle crickets") }

    // Active Generated Package
    var generatedPackage by remember {
        mutableStateOf<ProviderPromptPackage?>(null)
    }

    // Comparison Packages Map
    var comparisonPackages by remember {
        mutableStateOf<Map<AiProvider, ProviderPromptPackage>>(emptyMap())
    }

    var isGeneratingPrompt by remember { mutableStateOf(false) }

    fun currentBuilderParams(): PromptBuilderParams = PromptBuilderParams(
        storyOrTopic = storyTopic,
        mainCharacter = characterName,
        characterAppearance = characterAppearance,
        location = location,
        sceneDescription = sceneDescription,
        moodAndEmotion = moodEmotion,
        artStyle = artStyle,
        cameraAngle = cameraAngle,
        cameraMovement = cameraMovement,
        lighting = lighting,
        actionMovement = "Explores the courtyard, looks upward at glowing ancient glyphs",
        videoDurationSec = videoDurationSec,
        aspectRatio = aspectRatio,
        outputQuality = outputQuality,
        language = if (isKn) "kn" else "en",
        voiceStyle = voiceStyle,
        musicSoundEffects = musicSfx
    )

    // Initial default generation
    LaunchedEffect(Unit) {
        generatedPackage = AiProviderManager.buildOptimizedPrompt(selectedProvider, currentBuilderParams())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner: AI Prompt Studio Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("ai_prompt_studio_header_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isKn) "AI ಪ್ರಾಂಪ್ಟ್ ಸ್ಟುಡಿಯೋ" else "AI Prompt Studio",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isKn) "ChatGPT, Gemini, Runway, Kling & ElevenLabs ಪ್ರಾಂಪ್ಟ್ ಜನರೇಟರ್" else "Multi-AI Generator & Provider-Optimized Engine",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCrimson.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "MULTI-PROVIDER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioCrimson,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Mode Tabs: Builder | Multi-Provider Comparison | Prompt Library
                TabRow(
                    selectedTabIndex = selectedStudioMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).testTag("prompt_studio_mode_tabs")
                ) {
                    Tab(
                        selected = selectedStudioMode == 0,
                        onClick = { selectedStudioMode = 0 },
                        text = { Text(if (isKn) "ಪ್ರಾಂಪ್ಟ್ ಬಿಲ್ಡರ್" else "Prompt Builder", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_prompt_builder")
                    )
                    Tab(
                        selected = selectedStudioMode == 1,
                        onClick = {
                            selectedStudioMode = 1
                            comparisonPackages = AiProviderManager.generateMultiProviderComparison(currentBuilderParams())
                        },
                        text = { Text(if (isKn) "ಮಲ್ಟಿ-ಪ್ರೊವೈಡರ್ ಹೋಲಿಕೆ" else "Compare Providers", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_compare_providers")
                    )
                    Tab(
                        selected = selectedStudioMode == 2,
                        onClick = { selectedStudioMode = 2 },
                        text = { Text(if (isKn) "ಪ್ರಾಂಪ್ಟ್ ಲೈಬ್ರರಿ" else "Prompt Library", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_prompt_library")
                    )
                }
            }
        }

        // MODE 0: PROMPT BUILDER
        if (selectedStudioMode == 0) {
            com.example.ui.components.PromptStudio(
                settings = settings,
                currentLanguage = currentLanguage,
                initialProvider = selectedProvider,
                onSavePrompt = onSavePromptToLibrary,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        // MODE 1: MULTI-PROVIDER COMPARISON
        if (selectedStudioMode == 1) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("comparison_header_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isKn) "ಮಲ್ಟಿ-ಪ್ರೊವೈಡರ್ ಪ್ರಾಂಪ್ಟ್ ಹೋಲಿಕೆ" else "Multi-Provider Prompt Comparison",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Button(
                            onClick = {
                                val allPromptsText = comparisonPackages.entries.joinToString("\n\n=== ${"=".repeat(20)}\n\n") { (provider, pkg) ->
                                    "PROVIDER: ${provider.displayName} (${pkg.model})\n\n${pkg.mainPrompt}"
                                }
                                clipboardManager.setText(AnnotatedString(allPromptsText))
                                Toast.makeText(context, "Copied all 5 provider prompts to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_all_prompts_btn")
                        ) {
                            Icon(Icons.Default.CopyAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isKn) "ಎಲ್ಲ ಪ್ರಾಂಪ್ಟ್ ನಕಲಿಸಿ" else "Copy All Prompts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Side-by-side optimized syntax tailored for ChatGPT, Google Gemini, Runway, Kling AI, and ElevenLabs from a single story input.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            comparisonPackages.forEach { (provider, pkg) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = BorderStroke(1.dp, Color(0xFF333333)),
                    modifier = Modifier.fillMaxWidth().testTag("compare_card_${provider.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = StudioCrimson) {
                                    Text(provider.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pkg.model, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(pkg.mainPrompt))
                                    Toast.makeText(context, "Copied ${provider.displayName} prompt!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = pkg.mainPrompt,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            maxLines = 8
                        )
                    }
                }
            }
        }

        // MODE 2: PROMPT LIBRARY
        if (selectedStudioMode == 2) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("prompt_library_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isKn) "ಉಳಿಸಲಾದ ಪ್ರಾಂಪ್ಟ್ ಲೈಬ್ರರಿ" else "Saved Prompt Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (promptList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isKn) "ಯಾವುದೇ ಪ್ರಾಂಪ್ಟ್ ಉಳಿಸಲಾಗಿಲ್ಲ. ಪ್ರಾಂಪ್ಟ್ ಬಿಲ್ಡರ್‌ನಲ್ಲಿ 'Save' ಟ್ಯಾಪ್ ಮಾಡಿ." else "No prompts saved yet. Generate a prompt and tap the bookmark icon to save here.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        promptList.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prompt.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${prompt.provider} • ${prompt.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(prompt.mainPrompt, maxLines = 2, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(prompt.mainPrompt))
                                            Toast.makeText(context, "Prompt copied!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { onDeletePromptFromLibrary(prompt) }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
