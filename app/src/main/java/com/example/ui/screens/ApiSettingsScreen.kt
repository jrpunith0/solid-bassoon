package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiProvider
import com.example.ai.AiProviderManager
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * CENTRAL API SETTINGS DASHBOARD (MODULE 14)
 * Securely manages credentials and tests connections for:
 * - Google Gemini API
 * - OpenAI / ChatGPT API
 * - Runway API
 * - Kling AI API
 * - ElevenLabs API
 * - Custom REST AI Provider
 * - FFmpeg Video Rendering Engine
 */
@Composable
fun ApiSettingsScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    onSaveSettings: (CreatorSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isKn = currentLanguage == I18n.LANG_KN

    var geminiKey by remember { mutableStateOf(settings.geminiApiKey) }
    var openaiKey by remember { mutableStateOf(settings.openaiApiKey) }
    var runwayKey by remember { mutableStateOf(settings.runwayApiKey) }
    var klingKey by remember { mutableStateOf(settings.klingApiKey) }
    var elevenLabsKey by remember { mutableStateOf(settings.elevenLabsApiKey) }
    var customEndpoint by remember { mutableStateOf(settings.customAiEndpoint) }
    var customKey by remember { mutableStateOf(settings.customAiApiKey) }

    // Obscure password toggles
    var showGeminiKey by remember { mutableStateOf(false) }
    var showOpenaiKey by remember { mutableStateOf(false) }
    var showRunwayKey by remember { mutableStateOf(false) }
    var showKlingKey by remember { mutableStateOf(false) }
    var showElevenLabsKey by remember { mutableStateOf(false) }

    // Test results
    val testStatuses = remember { mutableStateMapOf<String, Pair<Boolean, String>>() }
    val isTestingMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("api_settings_header_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isKn) "API & ಪ್ರೊವೈಡರ್ ಸೆಟ್ಟಿಂಗ್ಸ್" else "API & Provider Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isKn) "Gemini, OpenAI, Runway, Kling, ElevenLabs & FFmpeg" else "Centralized Multi-Provider API Configuration",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        val updated = settings.copy(
                            geminiApiKey = geminiKey.trim(),
                            openaiApiKey = openaiKey.trim(),
                            runwayApiKey = runwayKey.trim(),
                            klingApiKey = klingKey.trim(),
                            elevenLabsApiKey = elevenLabsKey.trim(),
                            customAiEndpoint = customEndpoint.trim(),
                            customAiApiKey = customKey.trim()
                        )
                        onSaveSettings(updated)
                        Toast.makeText(context, "All API keys saved securely!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_all_api_keys_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isKn) "ಉಳಿಸಿ" else "Save All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 1. Google Gemini API Card
        ApiProviderConfigCard(
            title = "Google Gemini API (Google AI Studio)",
            badge = "Gemini 2.5 + Imagen 3 + Veo 2.0",
            badgeColor = Color(0xFF4285F4),
            apiKey = geminiKey,
            onApiKeyChange = { geminiKey = it },
            showKey = showGeminiKey,
            onToggleShowKey = { showGeminiKey = !showGeminiKey },
            docUrl = "https://aistudio.google.com/app/apikey",
            testResult = testStatuses["gemini"],
            isTesting = isTestingMap["gemini"] == true,
            onTestConnection = {
                coroutineScope.launch {
                    isTestingMap["gemini"] = true
                    val result = AiProviderManager.testProviderConnection(AiProvider.GEMINI, geminiKey)
                    testStatuses["gemini"] = result
                    isTestingMap["gemini"] = false
                }
            },
            testTagPrefix = "gemini"
        )

        // 2. OpenAI / ChatGPT API Card
        ApiProviderConfigCard(
            title = "OpenAI API (ChatGPT & DALL-E 3)",
            badge = "GPT-4o + DALL-E 3 + Whisper",
            badgeColor = Color(0xFF10A37F),
            apiKey = openaiKey,
            onApiKeyChange = { openaiKey = it },
            showKey = showOpenaiKey,
            onToggleShowKey = { showOpenaiKey = !showOpenaiKey },
            docUrl = "https://platform.openai.com/api-keys",
            testResult = testStatuses["openai"],
            isTesting = isTestingMap["openai"] == true,
            onTestConnection = {
                coroutineScope.launch {
                    isTestingMap["openai"] = true
                    val result = AiProviderManager.testProviderConnection(AiProvider.OPENAI, openaiKey)
                    testStatuses["openai"] = result
                    isTestingMap["openai"] = false
                }
            },
            testTagPrefix = "openai"
        )

        // 3. Runway API Card
        ApiProviderConfigCard(
            title = "Runway Gen-3 API",
            badge = "Gen-3 Alpha Turbo (Video)",
            badgeColor = Color(0xFFEC4899),
            apiKey = runwayKey,
            onApiKeyChange = { runwayKey = it },
            showKey = showRunwayKey,
            onToggleShowKey = { showRunwayKey = !showRunwayKey },
            docUrl = "https://runwayml.com/api",
            testResult = testStatuses["runway"],
            isTesting = isTestingMap["runway"] == true,
            onTestConnection = {
                coroutineScope.launch {
                    isTestingMap["runway"] = true
                    val result = AiProviderManager.testProviderConnection(AiProvider.RUNWAY, runwayKey)
                    testStatuses["runway"] = result
                    isTestingMap["runway"] = false
                }
            },
            testTagPrefix = "runway"
        )

        // 4. Kling AI Card
        ApiProviderConfigCard(
            title = "Kling AI Video API",
            badge = "Kling 2.0 (Character & Cartoon)",
            badgeColor = Color(0xFF8B5CF6),
            apiKey = klingKey,
            onApiKeyChange = { klingKey = it },
            showKey = showKlingKey,
            onToggleShowKey = { showKlingKey = !showKlingKey },
            docUrl = "https://klingai.com",
            testResult = testStatuses["kling"],
            isTesting = isTestingMap["kling"] == true,
            onTestConnection = {
                coroutineScope.launch {
                    isTestingMap["kling"] = true
                    val result = AiProviderManager.testProviderConnection(AiProvider.KLING, klingKey)
                    testStatuses["kling"] = result
                    isTestingMap["kling"] = false
                }
            },
            testTagPrefix = "kling"
        )

        // 5. ElevenLabs Voice API Card
        ApiProviderConfigCard(
            title = "ElevenLabs Voice API",
            badge = "Kannada & English Multilingual TTS",
            badgeColor = Color(0xFFF59E0B),
            apiKey = elevenLabsKey,
            onApiKeyChange = { elevenLabsKey = it },
            showKey = showElevenLabsKey,
            onToggleShowKey = { showElevenLabsKey = !showElevenLabsKey },
            docUrl = "https://elevenlabs.io",
            testResult = testStatuses["elevenlabs"],
            isTesting = isTestingMap["elevenlabs"] == true,
            onTestConnection = {
                coroutineScope.launch {
                    isTestingMap["elevenlabs"] = true
                    val result = AiProviderManager.testProviderConnection(AiProvider.ELEVEN_LABS, elevenLabsKey)
                    testStatuses["elevenlabs"] = result
                    isTestingMap["elevenlabs"] = false
                }
            },
            testTagPrefix = "elevenlabs"
        )

        // 6. FFmpeg Video Rendering Engine Info
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth().testTag("ffmpeg_info_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FFmpeg Video Rendering Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF2EA44F).copy(alpha = 0.15f)) {
                        Text("BUILT-IN HARDWARE ACCELERATED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2EA44F), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(
                    text = "FFmpeg binary is compiled and configured to transcode multi-track timelines into 1080p and 4K MP4 files with H.264/AAC audio.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ApiProviderConfigCard(
    title: String,
    badge: String,
    badgeColor: Color,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    showKey: Boolean,
    onToggleShowKey: () -> Unit,
    docUrl: String,
    testResult: Pair<Boolean, String>?,
    isTesting: Boolean,
    onTestConnection: () -> Unit,
    testTagPrefix: String
) {
    val context = LocalContext.current
    val hasKey = apiKey.isNotBlank() && apiKey.length >= 8

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().testTag("${testTagPrefix}_config_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(shape = RoundedCornerShape(4.dp), color = badgeColor.copy(alpha = 0.15f)) {
                        Text(badge, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (hasKey) Color(0xFF2EA44F).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (hasKey) "CONFIGURED" else "NOT SET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasKey) Color(0xFF2EA44F) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Secret Key") },
                placeholder = { Text("Paste key here...") },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onToggleShowKey) {
                        Icon(
                            imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("${testTagPrefix}_key_input")
            )

            // Test and Docs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(docUrl))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Get API Key", fontSize = 11.sp)
                }

                Button(
                    onClick = onTestConnection,
                    enabled = !isTesting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("${testTagPrefix}_test_btn")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Test Connection", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Test Result Banner
            testResult?.let { (success, message) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (success) Color(0xFF2EA44F).copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (success) Color(0xFF2EA44F) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            fontSize = 11.sp,
                            color = if (success) Color(0xFF2EA44F) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
