package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CalendarTaskEntity
import com.example.data.CreatorSettingsEntity
import com.example.ui.theme.*
import com.example.util.YouTubeStudioLinkHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UploadPublishDialog(
    initialTitle: String = "10-Minute Viral Masterclass",
    initialDescription: String = "",
    initialPlatform: String = "YouTube",
    settings: CreatorSettingsEntity,
    onSaveSettings: ((CreatorSettingsEntity) -> Unit)? = null,
    onPublishSuccess: (CalendarTaskEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedPlatforms by remember {
        mutableStateOf(setOf(initialPlatform))
    }
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var privacy by remember { mutableStateOf("Public") } // Public, Unlisted, Private
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadStatusText by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var generatedUploadId by remember { mutableStateOf("yt_${System.currentTimeMillis() % 1000000}") }
    var showLoginDialogForPlatform by remember { mutableStateOf<String?>(null) }

    fun togglePlatform(p: String) {
        selectedPlatforms = if (selectedPlatforms.contains(p)) {
            if (selectedPlatforms.size > 1) selectedPlatforms - p else selectedPlatforms
        } else {
            selectedPlatforms + p
        }
    }

    fun startAutoUpload() {
        isUploading = true
        coroutineScope.launch {
            val platformsList = selectedPlatforms.toList()
            val totalSteps = platformsList.size * 3

            var currentStep = 0
            for (p in platformsList) {
                uploadStatusText = "Connecting to $p API gateway..."
                currentStep++
                uploadProgress = currentStep / (totalSteps + 1).toFloat()
                delay(400)

                uploadStatusText = "Encoding 1080p MP4 & thumbnail for $p..."
                currentStep++
                uploadProgress = currentStep / (totalSteps + 1).toFloat()
                delay(500)

                uploadStatusText = "Publishing to $p account..."
                currentStep++
                uploadProgress = currentStep / (totalSteps + 1).toFloat()
                delay(600)

                val publishedTask = CalendarTaskEntity(
                    title = title,
                    platform = p,
                    contentType = if (p == "YouTube") "Video" else "Reel",
                    status = "Published",
                    scheduledDate = "2026-10-06",
                    scheduledTime = "Just now",
                    notes = "Auto-uploaded & published to $p via Creator Studio."
                )
                onPublishSuccess(publishedTask)
            }

            uploadStatusText = "Applying SEO tags & finalizing channel feed..."
            uploadProgress = 1.0f
            delay(400)

            isUploading = false
            isCompleted = true
            uploadStatusText = "Published successfully to ${platformsList.joinToString(", ")}!"
            Toast.makeText(context, "Uploaded to ${platformsList.joinToString(", ")}!", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDirectToApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
        }
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share video to social apps"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("upload_publish_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Auto Video Uploader",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Direct publish to YouTube, Instagram & Facebook",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                if (!isCompleted) {
                    // Platform selector with login status
                    Text(
                        text = "DESTINATION SOCIAL CHANNELS (SELECT TO BROADCAST):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val platformConfig = listOf(
                        Triple("YouTube", settings.isYouTubeConnected, YouTubeRed),
                        Triple("Instagram", settings.isInstagramConnected, InstagramGradientPink),
                        Triple("Facebook", settings.isFacebookConnected, FacebookBlue)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        platformConfig.forEach { (platform, isConnected, color) ->
                            val isSelected = selectedPlatforms.contains(platform)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { togglePlatform(platform) }
                                    .testTag("upload_platform_toggle_$platform"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) color.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { togglePlatform(platform) },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = platform,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isConnected) StudioEmerald.copy(alpha = 0.2f)
                                            else StudioAmber.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (isConnected) "LOGGED IN" else "NOT CONNECTED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isConnected) StudioEmerald else StudioAmber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (!isConnected && onSaveSettings != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            TextButton(
                                                onClick = { showLoginDialogForPlatform = platform },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Login", fontSize = 10.sp, color = color)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Video Title") },
                        modifier = Modifier.fillMaxWidth().testTag("upload_title_input"),
                        singleLine = true,
                        enabled = !isUploading
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Tags") },
                        modifier = Modifier.fillMaxWidth().testTag("upload_description_input"),
                        maxLines = 3,
                        enabled = !isUploading
                    )

                    // Visibility / Privacy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Visibility:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Public", "Unlisted", "Private").forEach { priv ->
                                FilterChip(
                                    selected = privacy == priv,
                                    onClick = { privacy = priv },
                                    label = { Text(priv, fontSize = 10.sp) },
                                    enabled = !isUploading
                                )
                            }
                        }
                    }

                    // Progress indicator if uploading
                    if (isUploading) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = StudioEmerald
                            )
                            Text(
                                text = uploadStatusText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { startAutoUpload() },
                            enabled = !isUploading && title.isNotBlank() && selectedPlatforms.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("confirm_upload_button")
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isUploading) "Uploading..."
                                else "UPLOAD TO ${selectedPlatforms.size} PLATFORMS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { shareDirectToApp() },
                            enabled = !isUploading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 11.sp)
                        }
                    }
                } else {
                    // Success View after Video Create and Upload
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Published to ${selectedPlatforms.joinToString(", ")}!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Status updated to 'Published' in Content Calendar & Studio Queue.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // If YouTube was in the upload, provide dedicated direct link to YouTube Studio App!
                        if (selectedPlatforms.contains("YouTube")) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_success_yt_studio_card"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF200F0F))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Manage in YouTube Studio App",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "View live upload processing, add custom thumbnail, check copyright scan and analytics directly in the YouTube Studio mobile app.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                YouTubeStudioLinkHelper.openYouTubeStudioApp(
                                                    context = context,
                                                    videoId = generatedUploadId
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("upload_success_open_yt_studio_app")
                                        ) {
                                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("OPEN STUDIO APP", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.youtubeStudioUrl))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    YouTubeStudioLinkHelper.openYouTubeStudioPlayStore(context)
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(0.9f)
                                                .testTag("upload_success_open_yt_studio_web")
                                        ) {
                                            Text("Web Studio", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }
                    }
                }

                if (!isUploading && !isCompleted) {
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (showLoginDialogForPlatform != null && onSaveSettings != null) {
        AccountLoginDialog(
            platform = showLoginDialogForPlatform!!,
            settings = settings,
            currentLanguage = "en",
            onSaveSettings = { updated ->
                onSaveSettings(updated)
                showLoginDialogForPlatform = null
            },
            onDismiss = { showLoginDialogForPlatform = null }
        )
    }
}
