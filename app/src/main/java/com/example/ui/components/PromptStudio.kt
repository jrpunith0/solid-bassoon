package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.StudioCrimson
import kotlinx.coroutines.launch

/**
 * PromptStudio Frontend Component
 * Allows creators to:
 * 1. Select an AI Provider (OpenAI, Gemini, Runway, Kling, ElevenLabs, Custom).
 * 2. Input detailed story and scene parameters (topic, characters, style, lighting, camera, audio).
 * 3. Generate an engine-optimized prompt tailored to that provider.
 * 4. Directly edit the generated prompt inside an inline code editor.
 * 5. Copy to clipboard with instant confirmation.
 * 6. Save prompt to the project's prompt library.
 */
@Composable
fun PromptStudio(
    settings: CreatorSettingsEntity,
    currentLanguage: String = I18n.LANG_EN,
    initialProvider: AiProvider = AiProvider.GEMINI,
    onSavePrompt: (AiPromptItemEntity) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isKn = currentLanguage == I18n.LANG_KN

    var selectedProvider by remember { mutableStateOf(initialProvider) }

    // Scene & Story Parameters State
    var storyTopic by remember {
        mutableStateOf(if (isKn) "ಒಂದು ಹಳ್ಳಿಯಲ್ಲಿ ನಡೆಯುವ ರಹಸ್ಯಮಯ ಕಥೆ" else "Mystery of the abandoned temple ruins at dusk")
    }
    var characterName by remember { mutableStateOf("Arjun") }
    var characterAppearance by remember { mutableStateOf("Indian youth, hazel eyes, sharp hair, saffron creator jacket") }
    var location by remember { mutableStateOf("Ancient misty village courtyard with weathered stone pillars") }
    var sceneDescription by remember { mutableStateOf("Stepping forward cautiously as ancient glowing runes begin to illuminate") }
    var moodEmotion by remember { mutableStateOf("Suspenseful & Epic") }
    var artStyle by remember { mutableStateOf("Cinematic 3D Animation") }
    var cameraAngle by remember { mutableStateOf("Wide establishing low-angle shot") }
    var cameraMovement by remember { mutableStateOf("Slow cinematic tracking push-in with subtle parallax") }
    var lighting by remember { mutableStateOf("Golden hour sunset rim lighting with atmospheric volumetric dust mist") }
    var videoDurationSec by remember { mutableStateOf(60) }
    var aspectRatio by remember { mutableStateOf("16:9") }
    var voiceStyle by remember { mutableStateOf("Deep cinematic narration with warm emotional cadence") }
    var musicSfx by remember { mutableStateOf("Low suspense cello drone, rustic temple flute, subtle crickets") }

    // Editable Prompt State (Allows users to edit the prompt before saving/copying)
    var editablePromptText by remember { mutableStateOf("") }
    var negativePromptText by remember { mutableStateOf("") }
    var activeModelName by remember { mutableStateOf("") }
    var isGeneratingPrompt by remember { mutableStateOf(false) }

    fun currentParams(): PromptBuilderParams = PromptBuilderParams(
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
        actionMovement = "Explores the courtyard, looking closely at glowing ancient carvings",
        videoDurationSec = videoDurationSec,
        aspectRatio = aspectRatio,
        outputQuality = "1080p Full HD",
        language = if (isKn) "kn" else "en",
        voiceStyle = voiceStyle,
        musicSoundEffects = musicSfx
    )

    fun generatePromptForSelectedProvider() {
        val pkg = AiProviderManager.buildOptimizedPrompt(selectedProvider, currentParams())
        editablePromptText = pkg.mainPrompt
        negativePromptText = pkg.negativePrompt
        activeModelName = pkg.model
    }

    // Auto-generate initial prompt on start
    LaunchedEffect(selectedProvider) {
        if (editablePromptText.isBlank()) {
            generatePromptForSelectedProvider()
        }
    }

    val isConnected = AiProviderManager.isProviderConnected(selectedProvider, settings)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prompt_studio_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. AI Provider Selection Header
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("prompt_studio_provider_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isKn) "AI ಪ್ರೊವೈಡರ್ ಆಯ್ಕೆ" else "Target AI Provider",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = selectedProvider.displayName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Connection status badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isConnected) Color(0xFF2EA44F).copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isConnected) "API CONNECTED" else "PROMPT ONLY (NO KEY)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isConnected) Color(0xFF2EA44F) else Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Provider Selection Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        AiProvider.GEMINI,
                        AiProvider.OPENAI,
                        AiProvider.RUNWAY,
                        AiProvider.KLING,
                        AiProvider.ELEVEN_LABS
                    ).forEach { provider ->
                        val isSelected = selectedProvider == provider
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedProvider = provider
                                generatePromptForSelectedProvider()
                            },
                            label = {
                                Text(
                                    text = provider.displayName.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("prompt_studio_provider_${provider.id}")
                        )
                    }
                }
            }
        }

        // 2. Story / Scene Parameters Form
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth().testTag("prompt_studio_form_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isKn) "ಕಥೆ ಮತ್ತು ದೃಶ್ಯದ ವಿವರಗಳು" else "Story & Scene Parameters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Story / Topic
                OutlinedTextField(
                    value = storyTopic,
                    onValueChange = { storyTopic = it },
                    label = { Text(if (isKn) "ಕಥೆ ಅಥವಾ ವಿಷಯ" else "Story Topic") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("prompt_story_topic_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = characterName,
                        onValueChange = { characterName = it },
                        label = { Text("Character Name") },
                        modifier = Modifier.weight(1f).testTag("prompt_character_name_input")
                    )
                    OutlinedTextField(
                        value = artStyle,
                        onValueChange = { artStyle = it },
                        label = { Text("Art / Visual Style") },
                        modifier = Modifier.weight(1f).testTag("prompt_art_style_input")
                    )
                }

                OutlinedTextField(
                    value = characterAppearance,
                    onValueChange = { characterAppearance = it },
                    label = { Text("Character Appearance & Costume") },
                    modifier = Modifier.fillMaxWidth().testTag("prompt_character_appearance_input")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Environment & Location") },
                    modifier = Modifier.fillMaxWidth().testTag("prompt_location_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cameraMovement,
                        onValueChange = { cameraMovement = it },
                        label = { Text("Camera Motion") },
                        modifier = Modifier.weight(1.2f).testTag("prompt_camera_movement_input")
                    )
                    OutlinedTextField(
                        value = aspectRatio,
                        onValueChange = { aspectRatio = it },
                        label = { Text("Ratio") },
                        modifier = Modifier.weight(0.8f).testTag("prompt_aspect_ratio_input")
                    )
                }

                // Action Buttons: Generate | Improve | Translate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            generatePromptForSelectedProvider()
                            Toast.makeText(context, "Optimized prompt generated for ${selectedProvider.displayName}!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).height(46.dp).testTag("generate_optimized_prompt_btn")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isKn) "ಪ್ರಾಂಪ್ಟ್ ರಚಿಸಿ" else "Generate Prompt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            storyTopic = "$storyTopic, dramatic cinematic lighting, volumetric atmosphere, 8k resolution"
                            generatePromptForSelectedProvider()
                            Toast.makeText(context, "Added cinematic descriptors!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("improve_prompt_btn")
                    ) {
                        Text(if (isKn) "ಉತ್ತಮಗೊಳಿಸಿ" else "Improve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val nextLang = if (isKn) "en" else "kn"
                            val pkg = AiProviderManager.buildOptimizedPrompt(selectedProvider, currentParams().copy(language = nextLang))
                            editablePromptText = pkg.mainPrompt
                            Toast.makeText(context, if (isKn) "Translated to English" else "ಕನ್ನಡಕ್ಕೆ ಭಾಷಾಂತರಿಸಲಾಗಿದೆ", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("translate_prompt_btn")
                    ) {
                        Text(if (isKn) "English" else "ಕನ್ನಡ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Editable Prompt Output Area (Allows Direct User Editing, Copying, and Saving)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color(0xFF333333)),
            modifier = Modifier.fillMaxWidth().testTag("prompt_studio_output_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header with Model Tag & Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = StudioCrimson) {
                            Text(
                                text = selectedProvider.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeModelName.ifBlank { "Optimized Syntax" },
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Word and character count indicator
                    val wordCount = editablePromptText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    Text(
                        text = "$wordCount words • ${editablePromptText.length} chars",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                }

                HorizontalDivider(color = Color(0xFF333333))

                Text(
                    text = if (isKn) "ಸಂಪಾದಿಸಬಹುದಾದ ಪ್ರಾಂಪ್ಟ್ (EDITABLE PROMPT):" else "EDITABLE PROMPT CONTENT:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )

                // Editable Text Field for Prompt
                OutlinedTextField(
                    value = editablePromptText,
                    onValueChange = { editablePromptText = it },
                    label = { Text("Prompt Editor", color = Color(0xFF94A3B8)) },
                    minLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFE2E8F0),
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("prompt_editable_content_input")
                )

                // Negative Prompt if present
                if (negativePromptText.isNotBlank()) {
                    Text(
                        text = "NEGATIVE PROMPT:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    OutlinedTextField(
                        value = negativePromptText,
                        onValueChange = { negativePromptText = it },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF94A3B8),
                            unfocusedTextColor = Color(0xFF94A3B8),
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("prompt_negative_input")
                    )
                }

                // Output Actions: Copy to Clipboard | Save to Library | Generate with API
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy to Clipboard Button
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(editablePromptText))
                            Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("copy_prompt_to_clipboard_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isKn) "ನಕಲಿಸಿ" else "Copy Prompt", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }

                    // Save Prompt to Library Button
                    Button(
                        onClick = {
                            val item = AiPromptItemEntity(
                                title = storyTopic.take(35),
                                provider = selectedProvider.displayName,
                                category = "Video / Animation",
                                mainPrompt = editablePromptText,
                                negativePrompt = negativePromptText,
                                cameraMovement = cameraMovement,
                                visualStyle = artStyle
                            )
                            onSavePrompt(item)
                            Toast.makeText(context, "Prompt saved to Library!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("save_prompt_to_library_btn")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isKn) "ಉಳಿಸಿ" else "Save Prompt", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }

                // Generate via API Button
                Button(
                    onClick = {
                        if (isConnected) {
                            coroutineScope.launch {
                                val service = AIProviderManager.instance.getService(selectedProvider)
                                val key = AiProviderManager.getApiKeyForProvider(selectedProvider, settings)
                                if (service != null && service.supportedCapabilities.contains(AICapability.VIDEO_GENERATION)) {
                                    val jobResult = service.generateVideo(editablePromptText, null, "default", videoDurationSec, key)
                                    if (jobResult.isSuccess) {
                                        Toast.makeText(context, "Dispatched to ${selectedProvider.displayName} API! Job ID: ${jobResult.getOrNull()?.jobId}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Generation error: ${jobResult.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Dispatched prompt to ${selectedProvider.displayName} API!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            clipboardManager.setText(AnnotatedString(editablePromptText))
                            Toast.makeText(context, "${selectedProvider.displayName} key not set. Prompt copied to clipboard! Opening settings...", Toast.LENGTH_LONG).show()
                            onNavigateToSettings()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isConnected) Color(0xFF2EA44F) else StudioCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_via_api_btn")
                ) {
                    Icon(if (isConnected) Icons.Default.CloudSync else Icons.Default.Key, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "Generate Using Connected ${selectedProvider.displayName} API" else "Copy Prompt & Configure API Key",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
