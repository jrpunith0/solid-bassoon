package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarTaskEntity
import com.example.data.CreatorSettingsEntity
import com.example.generator.ContentGenerator
import com.example.i18n.I18n
import com.example.ui.components.UploadPublishDialog
import com.example.ui.theme.*

@Composable
fun InstagramToolScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    onPublishSuccess: (CalendarTaskEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var topic by remember { mutableStateOf("Morning Routine of a Full-Time Creator") }
    var contentType by remember { mutableStateOf("Reel") }
    var showUploadDialog by remember { mutableStateOf(false) }

    val caption = remember(topic, contentType) {
        ContentGenerator.generateCaptions(topic, "Instagram", currentLanguage)
    }

    val hashtags = remember(topic) {
        ContentGenerator.generateHashtags(topic, "Instagram").joinToString(" ")
    }

    val hook = "Stop doing this in your morning routine if you want maximum creator energy 🔥"
    val coverText = "CREATOR MORNING ROUTINE ☀️"

    fun copyText(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun openInstagram() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.instagramUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Instagram: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Instagram Studio Tool",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = InstagramGradientPink
                    )
                    Text(
                        text = if (isKn) "ರೀಲ್ಸ್, ಪೋಸ್ಟ್‌ಗಳು, ಕ್ಯಾಪ್ಷನ್‌ಗಳು & ಸ್ಟೋರೀಸ್" else "Reels, Feed Posts, Captions & Story ideas",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showUploadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("ig_tool_upload_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { openInstagram() },
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramGradientPink),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("ig_tool_open_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Instagram Post / Reel Topic") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Reel", "Carousel Post", "Single Photo", "Story").forEach { type ->
                            FilterChip(
                                selected = contentType == type,
                                onClick = { contentType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Hook & Cover Text
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "🔥 3-SECOND VIRAL HOOK:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InstagramGradientPink)
                    Text(text = hook, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "🖼 COVER TEXT OVERLAY:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioPurple)
                    Text(text = coverText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Formatted Caption Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "OPTIMIZED CAPTION", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyText(caption, "Caption") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Caption", tint = InstagramGradientPink)
                        }
                    }
                    Text(text = caption, fontSize = 12.sp, lineHeight = 17.sp)

                    Button(
                        onClick = { copyText(caption, "Caption") },
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramGradientPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPY CAPTION & OPEN INSTAGRAM")
                    }
                }
            }
        }

        // Hashtags Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "30 TOP INSTAGRAM HASHTAGS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyText(hashtags, "Hashtags") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hashtags")
                        }
                    }
                    Text(text = hashtags, fontSize = 12.sp, color = InstagramGradientPink)
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadPublishDialog(
            initialTitle = topic,
            initialDescription = "$caption\n\n$hashtags",
            initialPlatform = "Instagram",
            settings = settings,
            onPublishSuccess = { task ->
                onPublishSuccess(task)
                showUploadDialog = false
            },
            onDismiss = { showUploadDialog = false }
        )
    }
}
