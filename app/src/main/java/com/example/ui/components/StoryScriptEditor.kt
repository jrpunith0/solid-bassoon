package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.generator.AssetStatus
import com.example.generator.PromptSuggestionEngine
import com.example.generator.StoryAssetStorageService
import com.example.generator.StorySceneData
import com.example.generator.StoryScriptData
import com.example.i18n.I18n
import com.example.ui.theme.*
import java.io.File

/**
 * StoryScriptEditor:
 * Jetpack Compose UI component that displays the generated story in editable text fields,
 * allowing users to freely modify the title, scenes, narration, dialogue, visual prompts,
 * and duration before proceeding to image generation.
 *
 * Includes:
 * 1. Automatic Image Prompt Suggestion based on narration text.
 * 2. Visual Progress Indicators for each scene (Pending, Generating, Complete).
 * 3. Drag-and-drop / Move Up-Down scene reordering component.
 * 4. 'Regenerate All' batch action button.
 * 5. Room Database local auto-save status indicator.
 */
@Composable
fun StoryScriptEditor(
    script: StoryScriptData,
    onScriptUpdated: (StoryScriptData) -> Unit,
    onProceedToImageGeneration: (StoryScriptData) -> Unit,
    onRegenerateAll: ((StoryScriptData) -> Unit)? = null,
    onPreviewAudio: ((sceneIndex: Int, voiceover: String) -> Unit)? = null,
    onResetToOriginal: (() -> Unit)? = null,
    autoSaveStatusText: String = "Saved",
    isGeneratingAssets: Boolean = false,
    isKn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Local mutable state copies for fluent editing
    var storyTitle by remember(script.id) { mutableStateOf(script.title) }
    var characterPrompt by remember(script.id) { mutableStateOf(script.characterAppearancePrompt) }
    var introText by remember(script.id) { mutableStateOf(script.introduction) }
    var closingText by remember(script.id) { mutableStateOf(script.closingScene) }

    // Editable scenes list
    val scenesList = remember(script.id, script.scenes) {
        mutableStateListOf<StorySceneData>().apply {
            addAll(script.scenes.map { it.copy() })
        }
    }

    var expandedSceneIndex by remember { mutableIntStateOf(0) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showReorderDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // Helper to push updates back
    fun notifyScriptChange(approved: Boolean = script.isApproved) {
        val reindexed = scenesList.mapIndexed { i, s ->
            if (s.sceneNumber != i + 1) s.copy(sceneNumber = i + 1) else s
        }
        val updated = script.copy(
            title = storyTitle.trim().ifBlank { script.title },
            characterAppearancePrompt = characterPrompt.trim(),
            introduction = introText.trim(),
            closingScene = closingText.trim(),
            scenes = reindexed,
            isApproved = approved
        )
        hasUnsavedChanges = false
        onScriptUpdated(updated)
    }

    fun reindexScenesList() {
        val reindexed = scenesList.mapIndexed { i, s ->
            if (s.sceneNumber != i + 1) s.copy(sceneNumber = i + 1) else s
        }
        scenesList.clear()
        scenesList.addAll(reindexed)
    }

    // Auto-save debounced trigger
    LaunchedEffect(storyTitle, characterPrompt, introText, closingText, scenesList.toList()) {
        if (hasUnsavedChanges) {
            notifyScriptChange(approved = script.isApproved)
        }
    }

    // Calculate asset generation metrics
    val totalScenes = scenesList.size
    val readyImages = scenesList.count { it.imageStatus == AssetStatus.READY }
    val readyAudios = scenesList.count { it.audioStatus == AssetStatus.READY }
    val totalAssets = totalScenes * 2
    val completedAssets = readyImages + readyAudios
    val assetProgressFraction = if (totalAssets > 0) completedAssets.toFloat() / totalAssets.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, StudioIndigo.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("story_script_editor")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header with Title & Auto-Save Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StudioIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Script Editor",
                            tint = StudioIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isKn) "ಕಥೆಯ ಸ್ಕ್ರಿಪ್ಟ್ ಎಡಿಟರ್ (Script Editor)" else "Story Script & Scene Editor",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isKn) "ಚಿತ್ರಗಳನ್ನು ರಚಿಸುವ ಮೊದಲು ಶೀರ್ಷಿಕೆ ಮತ್ತು ನಿರೂಪಣೆಯನ್ನು ತಿದ್ದಿ"
                            else "Modify title, narration & visual prompts before image creation",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Auto-Save to Room Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = autoSaveStatusText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioEmerald
                        )
                    }
                }
            }

            // 2. Metadata Chips & Overall Asset Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("📐 ${script.format.label}", fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("⏱️ ${script.targetDurationSec}s", fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("🎬 ${scenesList.size} ${if (isKn) "ದೃಶ್ಯಗಳು" else "Scenes"}", fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = StudioIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("🌐 ${if (script.language == I18n.LANG_KN) "ಕನ್ನಡ (KN)" else "English (EN)"}", fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }

            // Overall Asset Generation Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isKn) "ಚಿತ್ರ & ಆಡಿಯೋ ಪ್ರಗತಿ (Asset Progress):" else "Asset Generation Progress:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedAssets / $totalAssets Ready (${(assetProgressFraction * 100).toInt()}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedAssets == totalAssets) StudioEmerald else StudioAmber
                    )
                }
                LinearProgressIndicator(
                    progress = { assetProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (completedAssets == totalAssets) StudioEmerald else StudioIndigo,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // 3. Editable Story Title Field
            OutlinedTextField(
                value = storyTitle,
                onValueChange = {
                    storyTitle = it
                    hasUnsavedChanges = true
                },
                label = { Text(if (isKn) "ಕಥೆಯ ಶೀರ್ಷಿಕೆ (Story Title)" else "Story Title", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = StudioIndigo, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (storyTitle.isNotBlank()) {
                        IconButton(
                            onClick = {
                                storyTitle = ""
                                hasUnsavedChanges = true
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Title", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("script_title_input"),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            // 4. Character & Style Prompt (Hero visual identity)
            OutlinedTextField(
                value = characterPrompt,
                onValueChange = {
                    characterPrompt = it
                    hasUnsavedChanges = true
                },
                label = {
                    Text(
                        if (isKn) "ಪಾತ್ರ & ದೃಶ್ಯ ಶೈಲಿ ಪ್ರಾಂಪ್ಟ್ (Character & Art Style)"
                        else "Character & Art Style Prompt",
                        fontSize = 11.sp
                    )
                },
                placeholder = {
                    Text("e.g. Young determined astronaut, neon rim lighting, 8k cinematic photorealistic", fontSize = 10.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Face, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(18.dp))
                },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("character_prompt_input"),
                shape = RoundedCornerShape(10.dp)
            )

            // Quick Prompt Style Chips & "Auto-Suggest All Prompts" Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val styles = listOf(
                        "Cinematic 8K",
                        "Hyperrealistic",
                        "Anime Aesthetic",
                        "Volumetric Lighting",
                        "Golden Hour Sunset"
                    )
                    styles.forEach { style ->
                        AssistChip(
                            onClick = {
                                if (!characterPrompt.contains(style, ignoreCase = true)) {
                                    characterPrompt = if (characterPrompt.isBlank()) style else "$characterPrompt, $style"
                                    hasUnsavedChanges = true
                                }
                            },
                            label = { Text("+ $style", fontSize = 9.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Global Button: Suggest Prompts for All Scenes based on narration
                FilledTonalButton(
                    onClick = {
                        scenesList.forEachIndexed { i, scene ->
                            val suggested = PromptSuggestionEngine.suggestImagePrompt(
                                narration = scene.voiceover,
                                sceneTitle = scene.title,
                                genre = script.genre,
                                characterPrompt = characterPrompt,
                                language = script.language
                            )
                            scenesList[i] = scene.copy(visualPrompt = suggested)
                        }
                        hasUnsavedChanges = true
                        notifyScriptChange(script.isApproved)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("suggest_all_prompts_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isKn) "ಎಲ್ಲಾ ಪ್ರಾಂಪ್ಟ್ ಸಲಹೆ" else "Auto-Suggest All", fontSize = 9.sp)
                }
            }

            // 5. Scenes Section Header with Reorder and Add Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isKn) "ದೃಶ್ಯಗಳ ವಿವರ ಮತ್ತು ನಿರೂಪಣೆ (${scenesList.size} Scenes)"
                    else "Scene Breakdown & Narration (${scenesList.size} Scenes)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Reorder Scenes Modal Trigger
                    OutlinedButton(
                        onClick = { showReorderDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("reorder_scenes_dialog_button")
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isKn) "ದೃಶ್ಯಗಳ ಕ್ರಮ (Reorder)" else "Reorder", fontSize = 10.sp)
                    }

                    // Add Scene Button
                    OutlinedButton(
                        onClick = {
                            val newNumber = scenesList.size + 1
                            val newScene = StorySceneData(
                                sceneNumber = newNumber,
                                title = if (isKn) "ದೃಶ್ಯ $newNumber: ಹೊಸ ತಿರುವು" else "Scene $newNumber: New Development",
                                characterDescription = characterPrompt.take(60),
                                dialogue = if (isKn) "\"ಮುಂದಿನ ಹೆಜ್ಜೆ ಅತ್ಯಂತ ಮುಖ್ಯ!\"" else "\"Every second counts now!\"",
                                voiceover = if (isKn) "ಕಥೆಯ ಈ ಭಾಗದಲ್ಲಿ ಮತ್ತೊಂದು ಹೊಸ ಸವಾಲು ಎದುರಾಯಿತು." else "At this crucial juncture, another turning point revealed itself.",
                                visualPrompt = PromptSuggestionEngine.suggestImagePrompt(
                                    narration = "At this crucial juncture, another turning point revealed itself.",
                                    genre = script.genre,
                                    characterPrompt = characterPrompt,
                                    language = script.language
                                ),
                                durationSec = 5,
                                imageStatus = AssetStatus.PENDING,
                                audioStatus = AssetStatus.PENDING
                            )
                            scenesList.add(newScene)
                            expandedSceneIndex = scenesList.size - 1
                            hasUnsavedChanges = true
                            notifyScriptChange(script.isApproved)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_scene_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isKn) "+ ಹೊಸ ದೃಶ್ಯ" else "+ Add", fontSize = 10.sp)
                    }
                }
            }

            // 6. Scene-by-Scene Cards with Visual Progress Indicators & Move Controls
            scenesList.forEachIndexed { idx, scene ->
                val isExpanded = expandedSceneIndex == idx

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isExpanded) StudioIndigo.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scene_editor_card_$idx")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Scene Header with Drag Handle, Move Controls & Progress Indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedSceneIndex = if (isExpanded) -1 else idx
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Drag handle icon
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = StudioIndigo
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = scene.title.ifBlank { "Scene ${idx + 1}" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.widthIn(max = 130.dp)
                                )
                            }

                            // Visual Progress Badges (Image & Audio)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Image Status Badge
                                SceneAssetStatusBadge(
                                    label = "IMG",
                                    status = scene.imageStatus,
                                    isAudio = false
                                )

                                // Audio Status Badge
                                SceneAssetStatusBadge(
                                    label = "AUD",
                                    status = scene.audioStatus,
                                    isAudio = true
                                )

                                // Duration Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${scene.durationSec}s",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                // Expand / Collapse Icon
                                IconButton(
                                    onClick = { expandedSceneIndex = if (isExpanded) -1 else idx },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Scene Details",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Collapsed Preview Summary
                        if (!isExpanded) {
                            Text(
                                text = "🎙 \"${scene.voiceover.take(65)}${if (scene.voiceover.length > 65) "..." else ""}\"",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Expanded Full Editable Fields
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Reorder Quick Actions Bar for this Scene
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (isKn) "ಸ್ಥಳ: ${idx + 1} / ${scenesList.size}" else "Position: ${idx + 1} of ${scenesList.size}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Move Up Button
                                        IconButton(
                                            onClick = {
                                                if (idx > 0) {
                                                    val item = scenesList.removeAt(idx)
                                                    scenesList.add(idx - 1, item)
                                                    reindexScenesList()
                                                    expandedSceneIndex = idx - 1
                                                    hasUnsavedChanges = true
                                                    notifyScriptChange(script.isApproved)
                                                }
                                            },
                                            enabled = idx > 0,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("move_up_button_$idx")
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = "Move Scene Up",
                                                tint = if (idx > 0) StudioIndigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Move Down Button
                                        IconButton(
                                            onClick = {
                                                if (idx < scenesList.size - 1) {
                                                    val item = scenesList.removeAt(idx)
                                                    scenesList.add(idx + 1, item)
                                                    reindexScenesList()
                                                    expandedSceneIndex = idx + 1
                                                    hasUnsavedChanges = true
                                                    notifyScriptChange(script.isApproved)
                                                }
                                            },
                                            enabled = idx < scenesList.size - 1,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("move_down_button_$idx")
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = "Move Scene Down",
                                                tint = if (idx < scenesList.size - 1) StudioIndigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                // Editable Scene Title
                                OutlinedTextField(
                                    value = scene.title,
                                    onValueChange = {
                                        scene.title = it
                                        hasUnsavedChanges = true
                                    },
                                    label = { Text(if (isKn) "ದೃಶ್ಯ ಶೀರ್ಷಿಕೆ (Scene Title)" else "Scene Title", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_scene_title_$idx"),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                // Editable Narration (Voiceover) Text Field
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    OutlinedTextField(
                                        value = scene.voiceover,
                                        onValueChange = {
                                            scene.voiceover = it
                                            hasUnsavedChanges = true
                                        },
                                        label = {
                                            Text(
                                                if (isKn) "ಧ್ವನಿ ನಿರೂಪಣೆ (Narration / Voiceover Text)"
                                                else "Narration / Voiceover Text",
                                                fontSize = 10.sp
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                if (isKn) "ನಿರೂಪಣೆಯ ಪೂರ್ಣ ವಾಕ್ಯಗಳನ್ನು ಇಲ್ಲಿ ಬರೆಯಿರಿ..."
                                                else "Enter spoken narration text for TTS...",
                                                fontSize = 10.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Mic, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(16.dp))
                                        },
                                        minLines = 3,
                                        maxLines = 5,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("edit_scene_narration_$idx"),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${scene.voiceover.length} chars • ~${(scene.voiceover.split("\\s+".toRegex()).size * 0.4).toInt().coerceAtLeast(2)}s spoken",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Play preview TTS button
                                        if (onPreviewAudio != null && scene.voiceover.isNotBlank()) {
                                            TextButton(
                                                onClick = { onPreviewAudio(idx, scene.voiceover) },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(if (isKn) "ಧ್ವನಿ ಆಲಿಸಿ" else "Preview TTS", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }

                                // Editable Dialogue Field (Captions/Subtitles)
                                OutlinedTextField(
                                    value = scene.dialogue,
                                    onValueChange = {
                                        scene.dialogue = it
                                        hasUnsavedChanges = true
                                    },
                                    label = { Text(if (isKn) "ಸಂಭಾಷಣೆ / ಸಬ್‌ಟೈಟಲ್ (Dialogue / Caption)" else "Dialogue / On-Screen Caption", fontSize = 10.sp) },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Subtitles, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(16.dp))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_scene_dialogue_$idx"),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                // Editable Visual Prompt with "Auto-Suggest Prompt" Button
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isKn) "ಚಿತ್ರ ರಚನೆಯ ಪ್ರಾಂಪ್ಟ್ (AI Image Prompt):" else "AI Image Prompt (Imagen 3):",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // Suggest Prompt for this scene based on narration
                                        TextButton(
                                            onClick = {
                                                val suggested = PromptSuggestionEngine.suggestImagePrompt(
                                                    narration = scene.voiceover,
                                                    sceneTitle = scene.title,
                                                    genre = script.genre,
                                                    characterPrompt = characterPrompt,
                                                    language = script.language
                                                )
                                                scene.visualPrompt = suggested
                                                hasUnsavedChanges = true
                                                notifyScriptChange(script.isApproved)
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .height(26.dp)
                                                .testTag("suggest_prompt_button_$idx")
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(if (isKn) "ನಿರೂಪಣೆಯಿಂದ ಸೂಚಿಸಿ" else "Suggest from Narration", fontSize = 9.sp, color = StudioAmber)
                                        }
                                    }

                                    OutlinedTextField(
                                        value = scene.visualPrompt,
                                        onValueChange = {
                                            scene.visualPrompt = it
                                            hasUnsavedChanges = true
                                        },
                                        placeholder = {
                                            Text("Cinematic camera angle, dramatic atmosphere, 8k resolution...", fontSize = 10.sp)
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = StudioPurple, modifier = Modifier.size(16.dp))
                                        },
                                        minLines = 2,
                                        maxLines = 4,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("edit_scene_prompt_$idx"),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                // Duration Adjustment Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isKn) "ದೃಶ್ಯದ ಅವಧಿ (Duration):" else "Scene Duration:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(3, 5, 8, 10).forEach { dur ->
                                            FilterChip(
                                                selected = scene.durationSec == dur,
                                                onClick = {
                                                    scene.durationSec = dur
                                                    hasUnsavedChanges = true
                                                },
                                                label = { Text("${dur}s", fontSize = 9.sp) },
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }
                                }

                                // Scene Card Actions (Delete Scene)
                                if (scenesList.size > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                scenesList.removeAt(idx)
                                                reindexScenesList()
                                                expandedSceneIndex = (idx - 1).coerceAtLeast(0)
                                                hasUnsavedChanges = true
                                                notifyScriptChange(script.isApproved)
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = StudioCrimson),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(if (isKn) "ತೆಗೆದುಹಾಕಿ" else "Remove Scene", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Bottom Actions: Save, Regenerate All, and Approve CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save Script Draft Button
                    OutlinedButton(
                        onClick = {
                            notifyScriptChange(approved = script.isApproved)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("save_script_changes_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKn) "ಬದಲಾವಣೆ ಉಳಿಸಿ" else "Save Draft",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Regenerate All Button (Triggers sequence of batch API calls)
                    if (onRegenerateAll != null) {
                        Button(
                            onClick = {
                                notifyScriptChange(approved = true)
                                val currentScript = script.copy(
                                    title = storyTitle.trim().ifBlank { script.title },
                                    characterAppearancePrompt = characterPrompt.trim(),
                                    introduction = introText.trim(),
                                    closingScene = closingText.trim(),
                                    scenes = scenesList.toList()
                                )
                                onRegenerateAll(currentScript)
                            },
                            enabled = !isGeneratingAssets,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("regenerate_all_assets_button")
                        ) {
                            if (isGeneratingAssets) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isKn) "ರಚಿಸಲಾಗುತ್ತಿದೆ..." else "Regenerating...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKn) "ಮರು-ರಚಿಸಿ (Regenerate All)" else "Regenerate All",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Reset Button
                    if (onResetToOriginal != null) {
                        IconButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset Script",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Primary CTA: Approve & Proceed to Image Generation
                Button(
                    onClick = {
                        notifyScriptChange(approved = true)
                        val approvedScript = script.copy(
                            title = storyTitle.trim().ifBlank { script.title },
                            characterAppearancePrompt = characterPrompt.trim(),
                            introduction = introText.trim(),
                            closingScene = closingText.trim(),
                            scenes = scenesList.toList(),
                            isApproved = true
                        )
                        onProceedToImageGeneration(approvedScript)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("proceed_to_image_generation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isKn) "ಅನುಮೋದಿಸಿ & ಚಿತ್ರಗಳನ್ನು ರಚಿಸಿ (Proceed to Images) ➔"
                        else "Approve & Proceed to Image Generation ➔",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Interactive Scene Reordering Dialog
    if (showReorderDialog) {
        Dialog(onDismissRequest = { showReorderDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("scene_reorder_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapVert, contentDescription = null, tint = StudioIndigo)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKn) "ದೃಶ್ಯಗಳ ಕ್ರಮ ಬದಲಾವಣೆ" else "Reorder Story Scenes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showReorderDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }

                    Text(
                        text = if (isKn) "ಚಿತ್ರಗಳನ್ನು ರಚಿಸುವ ಮೊದಲು ದೃಶ್ಯಗಳನ್ನು ಮೇಲಕ್ಕೆ ಅಥವಾ ಕೆಳಕ್ಕೆ ಸರಿಸಿ."
                        else "Use arrows to re-sequence scenes in your narrative before image generation.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Reorder List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scenesList.forEachIndexed { i, s ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StudioIndigo
                                        ) {
                                            Text(
                                                text = "${i + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = s.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = {
                                                if (i > 0) {
                                                    val item = scenesList.removeAt(i)
                                                    scenesList.add(i - 1, item)
                                                    reindexScenesList()
                                                    hasUnsavedChanges = true
                                                    notifyScriptChange(script.isApproved)
                                                }
                                            },
                                            enabled = i > 0,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = "Move Up",
                                                tint = if (i > 0) StudioIndigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (i < scenesList.size - 1) {
                                                    val item = scenesList.removeAt(i)
                                                    scenesList.add(i + 1, item)
                                                    reindexScenesList()
                                                    hasUnsavedChanges = true
                                                    notifyScriptChange(script.isApproved)
                                                }
                                            },
                                            enabled = i < scenesList.size - 1,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = "Move Down",
                                                tint = if (i < scenesList.size - 1) StudioIndigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showReorderDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(if (isKn) "ಪೂರ್ಣಗೊಳಿಸಿ (Done)" else "Done Reordering", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetConfirmDialog && onResetToOriginal != null) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ಮರುಹೊಂದಿಸುವುದೇ?" else "Reset Story Script?")
            },
            text = {
                Text(
                    if (isKn) "ನಿಮ್ಮ ತಿದ್ದುಪಡಿಗಳನ್ನು ರದ್ದುಗೊಳಿಸಿ ಮೂಲ AI ಕಥೆಯನ್ನು ಮರುಸ್ಥಾಪಿಸಲಾಗುತ್ತದೆ."
                    else "This will revert your edits back to the original AI generated script."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetToOriginal()
                    }
                ) {
                    Text(if (isKn) "ಹೌದು, ಮರುಹೊಂದಿಸಿ" else "Yes, Reset", color = StudioCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(if (isKn) "ರದ್ದು" else "Cancel")
                }
            }
        )
    }
}

/**
 * Visual Progress Indicator Badge for scene assets (Pending, Generating, Complete).
 */
@Composable
fun SceneAssetStatusBadge(
    label: String,
    status: AssetStatus,
    isAudio: Boolean,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeBorder, badgeText, badgeIcon) = when (status) {
        AssetStatus.PENDING -> {
            Quad4(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Default.HourglassEmpty
            )
        }
        AssetStatus.GENERATING -> {
            Quad4(
                StudioAmber.copy(alpha = 0.2f),
                StudioAmber.copy(alpha = 0.6f),
                StudioAmber,
                Icons.Default.Sync
            )
        }
        AssetStatus.READY -> {
            Quad4(
                StudioEmerald.copy(alpha = 0.2f),
                StudioEmerald.copy(alpha = 0.6f),
                StudioEmerald,
                if (isAudio) Icons.Default.VolumeUp else Icons.Default.CheckCircle
            )
        }
        AssetStatus.FAILED -> {
            Quad4(
                StudioCrimson.copy(alpha = 0.2f),
                StudioCrimson.copy(alpha = 0.6f),
                StudioCrimson,
                Icons.Default.ErrorOutline
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeBorder),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (status == AssetStatus.GENERATING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(8.dp),
                    color = badgeText,
                    strokeWidth = 1.5.dp
                )
            } else {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = badgeText,
                    modifier = Modifier.size(9.dp)
                )
            }
            Text(
                text = "$label: ${status.name.take(4)}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = badgeText
            )
        }
    }
}

private data class Quad4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
