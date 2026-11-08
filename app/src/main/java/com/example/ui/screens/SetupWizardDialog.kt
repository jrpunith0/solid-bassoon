package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.ui.theme.*

@Composable
fun SetupWizardDialog(
    initialSettings: CreatorSettingsEntity,
    onSaveAndFinish: (CreatorSettingsEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var ytUrl by remember { mutableStateOf(initialSettings.youtubeChannelUrl) }
    var igUrl by remember { mutableStateOf(initialSettings.instagramUrl) }
    var fbUrl by remember { mutableStateOf(initialSettings.facebookUrl) }
    var selectedLang by remember { mutableStateOf(initialSettings.defaultLanguage) }
    var selectedTheme by remember { mutableStateOf(initialSettings.theme) }
    var channelName by remember { mutableStateOf(initialSettings.channelName) }

    val isKn = selectedLang == I18n.LANG_KN

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("setup_wizard_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioCrimson),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isKn) "ಕ್ರಿಯೇಟರ್ ಸ್ಟುಡಿಯೋಗೆ ಸ್ವಾಗತ!" else "Welcome to Creator Studio!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isKn) "ನಿಮ್ಮ ಸಾಮಾಜಿಕ ಮಾಧ್ಯಮ ಪ್ರೊಫೈಲ್ ಲಿಂಕ್‌ಗಳನ್ನು ಸೇರಿಸಿ" else "Connect your creator social links to get started",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Channel Name
                OutlinedTextField(
                    value = channelName,
                    onValueChange = { channelName = it },
                    label = { Text(if (isKn) "ಚಾನೆಲ್ ಅಥವಾ ಪ್ರೊಫೈಲ್ ಹೆಸರು" else "Channel / Brand Name") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("wizard_channel_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // YouTube Channel URL
                OutlinedTextField(
                    value = ytUrl,
                    onValueChange = { ytUrl = it },
                    label = { Text("YouTube Channel URL") },
                    leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = YouTubeRed) },
                    placeholder = { Text("https://www.youtube.com/@channel") },
                    modifier = Modifier.fillMaxWidth().testTag("wizard_youtube_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Instagram URL
                OutlinedTextField(
                    value = igUrl,
                    onValueChange = { igUrl = it },
                    label = { Text("Instagram Profile URL") },
                    leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = InstagramGradientPink) },
                    placeholder = { Text("https://www.instagram.com/username") },
                    modifier = Modifier.fillMaxWidth().testTag("wizard_instagram_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Facebook URL
                OutlinedTextField(
                    value = fbUrl,
                    onValueChange = { fbUrl = it },
                    label = { Text("Facebook Page / Profile URL") },
                    leadingIcon = { Icon(Icons.Default.ThumbUp, contentDescription = null, tint = FacebookBlue) },
                    placeholder = { Text("https://www.facebook.com/page") },
                    modifier = Modifier.fillMaxWidth().testTag("wizard_facebook_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language choice
                Text(
                    text = if (isKn) "ಡೀಫಾಲ್ಟ್ ಭಾಷೆ (Default Language):" else "Default Language:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedLang == I18n.LANG_KN,
                        onClick = { selectedLang = I18n.LANG_KN },
                        label = { Text("ಕನ್ನಡ (Kannada)") },
                        modifier = Modifier.weight(1f).testTag("wizard_lang_kn")
                    )
                    FilterChip(
                        selected = selectedLang == I18n.LANG_EN,
                        onClick = { selectedLang = I18n.LANG_EN },
                        label = { Text("English") },
                        modifier = Modifier.weight(1f).testTag("wizard_lang_en")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Theme choice
                Text(
                    text = if (isKn) "ಥೀಮ್ (Theme):" else "Theme:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedTheme == "dark",
                        onClick = { selectedTheme = "dark" },
                        label = { Text("Dark Mode 🌙") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedTheme == "light",
                        onClick = { selectedTheme = "light" },
                        label = { Text("Light Mode ☀️") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                var doNotShowAgain by remember { mutableStateOf(true) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = doNotShowAgain,
                        onCheckedChange = { doNotShowAgain = it },
                        modifier = Modifier.testTag("wizard_do_not_show_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isKn) "ಪ್ರತಿ ಬಾರಿ ತೋರಿಸಬೇಡಿ (Do not show every time)" else "Do not show every time on startup",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Button(
                    onClick = {
                        val updated = initialSettings.copy(
                            channelName = channelName,
                            youtubeChannelUrl = ytUrl,
                            instagramUrl = igUrl,
                            facebookUrl = fbUrl,
                            defaultLanguage = selectedLang,
                            theme = selectedTheme,
                            wizardCompleted = true
                        )
                        onSaveAndFinish(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("wizard_finish_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isKn) "ಸೆಟಪ್ ಪೂರ್ಣಗೊಳಿಸಿ (Finish Setup)" else "FINISH SETUP & ENTER STUDIO",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        if (doNotShowAgain) {
                            onSaveAndFinish(initialSettings.copy(wizardCompleted = true))
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("wizard_skip_button")
                ) {
                    Text(
                        text = if (isKn) "ನಂತರ ಹೊಂದಿಸಿ (Skip for now)" else "Skip for now",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
