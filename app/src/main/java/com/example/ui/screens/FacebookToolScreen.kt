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
fun FacebookToolScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    onPublishSuccess: (CalendarTaskEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var topic by remember { mutableStateOf("Why Every Creator Needs a Second Source of Income") }
    var postType by remember { mutableStateOf("Facebook Reel") }
    var showUploadDialog by remember { mutableStateOf(false) }

    val postText = remember(topic, postType) {
        ContentGenerator.generateCaptions(topic, "Facebook", currentLanguage)
    }

    val hashtags = remember(topic) {
        ContentGenerator.generateHashtags(topic, "Facebook").take(10).joinToString(" ")
    }

    fun copyText(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label copied!", Toast.LENGTH_SHORT).show()
    }

    fun openFacebook() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.facebookUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Facebook: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        text = "Facebook Creator Tool",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = FacebookBlue
                    )
                    Text(
                        text = if (isKn) "ರೀಲ್ಸ್, ವೀಡಿಯೊ ಪೋಸ್ಟ್‌ಗಳು ಮತ್ತು ಸಮುದಾಯ ಅಪ್‌ಡೇಟ್ಸ್‌" else "Reels, Video Posts, Image Posts & Discussion Updates",
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
                        modifier = Modifier.testTag("fb_tool_upload_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { openFacebook() },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("fb_tool_open_button")
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Topic and Format Selection
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
                        label = { Text("Facebook Post Topic") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Facebook Reel", "Video Post", "Image Post", "Text Story").forEach { type ->
                            FilterChip(
                                selected = postType == type,
                                onClick = { postType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Post Text Output
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
                        Text(text = "ENGAGEMENT-FOCUSED POST", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyText(postText, "Post Text") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = FacebookBlue)
                        }
                    }

                    Text(text = postText, fontSize = 12.sp, lineHeight = 17.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { copyText(postText, "Post") },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("COPY POST")
                        }
                        OutlinedButton(
                            onClick = { copyText(hashtags, "Hashtags") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("COPY HASHTAGS")
                        }
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadPublishDialog(
            initialTitle = topic,
            initialDescription = "$postText\n\n$hashtags",
            initialPlatform = "Facebook",
            settings = settings,
            onPublishSuccess = { task ->
                onPublishSuccess(task)
                showUploadDialog = false
            },
            onDismiss = { showUploadDialog = false }
        )
    }
}
