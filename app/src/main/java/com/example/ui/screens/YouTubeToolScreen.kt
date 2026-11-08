package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.i18n.I18n
import com.example.ui.components.UploadPublishDialog
import com.example.ui.theme.*

@Composable
fun YouTubeToolScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    onPublishSuccess: (CalendarTaskEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var topic by remember { mutableStateOf("How I Grew from 0 to 100K Subscribers Fast") }
    var showUploadDialog by remember { mutableStateOf(false) }

    val titleOptions = remember(topic) {
        listOf(
            "I Tried the Viral YouTube Strategy for 30 Days (SHOCKING)",
            "How Anyone Can Hit 100K Subscribers in 2026 (Full Roadmap)",
            "The 3 YouTube Mistakes Killing Your Channel Right Now",
            "Stop Making YouTube Videos Until You Watch This!",
            "How Top Creators Actually Gain Millions of Views"
        )
    }

    val seoDescription = remember(topic) {
        """
In this video, I break down the exact strategy for $topic. 

Timestamps:
0:00 - The Harsh Truth About Growth
1:32 - Step 1: High CTR Packaging (Titles & Thumbnails)
4:15 - Step 2: First 30 Seconds Retention Blueprint
7:45 - Step 3: Audience Feedback & Algorithm Triggers
10:20 - Final Takeaway & Action Item

🔔 Subscribe to the channel for weekly creator tutorials!
        """.trimIndent()
    }

    val tags = "youtube growth, viral algorithm, creator tips, high ctr, watch time, 100k subscribers, $topic"
    val hashtags = "#youtube #creator #growthhacks #filmmaking #tutorial"

    fun copyText(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

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
                        text = "YouTube Studio Tool",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = YouTubeRed
                    )
                    Text(
                        text = if (isKn) "5 ಶೀರ್ಷಿಕೆಗಳು, SEO ವಿವರಣೆ, ಟ್ಯಾಗ್‌ಗಳು ಮತ್ತು ಆಟೋ ಚಾಪ್ಟರ್‌ಗಳು" else "5 Title variations, SEO description, tags & auto chapters",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showUploadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("yt_tool_upload_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { openUrl(settings.youtubeChannelUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("yt_tool_open_youtube")
                    ) {
                        Text("Channel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { com.example.util.YouTubeStudioLinkHelper.openYouTubeStudioApp(context) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("yt_tool_open_studio")
                    ) {
                        Text("Studio App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Official YouTube Studio App Link Card
        item {
            com.example.util.YouTubeStudioAppLinkCard(
                currentLanguage = currentLanguage,
                channelName = settings.channelName,
                customStudioUrl = settings.youtubeStudioUrl
            )
        }

        // Topic Input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Video Topic") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // 5 High CTR Title Options
        item {
            Text(
                text = "🔥 5 HIGH-CTR TITLE OPTIONS:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        itemsIndexed(titleOptions) { index, titleText ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}. $titleText",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { copyText(titleText, "Title") }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = YouTubeRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // SEO Description with auto chapters
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
                        Text(
                            text = "SEO DESCRIPTION & AUTO CHAPTERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCrimson
                        )
                        IconButton(onClick = { copyText(seoDescription, "SEO Description") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = StudioCrimson)
                        }
                    }
                    Text(
                        text = seoDescription,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Tags & Hashtags
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
                        Text(text = "TAGS & KEYWORDS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyText(tags, "Tags") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(text = tags, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "HASHTAGS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyText(hashtags, "Hashtags") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(text = hashtags, fontSize = 12.sp, color = YouTubeRed)
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadPublishDialog(
            initialTitle = titleOptions.firstOrNull() ?: topic,
            initialDescription = seoDescription,
            initialPlatform = "YouTube",
            settings = settings,
            onPublishSuccess = { task ->
                onPublishSuccess(task)
                showUploadDialog = false
            },
            onDismiss = { showUploadDialog = false }
        )
    }
}
