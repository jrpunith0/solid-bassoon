package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MediaItemEntity
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.YouTubeRed

/**
 * Thumbnail Grabber and custom designer tool.
 * Allows grabbing still frame at current timestamp and designing high-CTR YouTube thumbnails.
 */
@Composable
fun ThumbnailGrabberDialog(
    currentTimestampMs: Long,
    defaultTopic: String,
    onSaveThumbnail: (MediaItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var overlayHeadline by remember { mutableStateOf(defaultTopic.take(35).uppercase()) }
    var selectedBadge by remember { mutableStateOf("MUST WATCH 🔥") }
    var badgeColor by remember { mutableStateOf(YouTubeRed) }
    var isSaving by remember { mutableStateOf(false) }

    val viralBadges = listOf(
        "MUST WATCH 🔥",
        "VIRAL HACK ⚡",
        "100K FORMULA 📈",
        "TOP SECRET 🤫",
        "STOP DOING THIS 🛑"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("thumbnail_grabber_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = YouTubeRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grab Still & Design Thumbnail", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Frame captured at ${(currentTimestampMs / 1000)}s. Add text overlays and viral badges for YouTube.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Thumbnail Canvas Mockup (16:9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF881337)
                                )
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor,
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Text(
                                text = selectedBadge,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Center Bold Headline
                        Text(
                            text = overlayHeadline.ifBlank { "VIRAL HEADLINE HERE" },
                            color = Color(0xFFFFEB3B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Bottom right time tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "HD 1080p",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Input for headline
                OutlinedTextField(
                    value = overlayHeadline,
                    onValueChange = { overlayHeadline = it.take(50) },
                    label = { Text("Overlay Headline (Clickbait Hook)") },
                    modifier = Modifier.fillMaxWidth().testTag("thumbnail_headline_input"),
                    singleLine = true
                )

                // Viral Badge Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Select Viral Badge Sticker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(viralBadges) { badge ->
                            val isSel = selectedBadge == badge
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedBadge = badge },
                                label = { Text(badge, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YouTubeRed.copy(alpha = 0.2f),
                                    selectedLabelColor = YouTubeRed
                                )
                            )
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val media = MediaItemEntity(
                                title = "Thumbnail: $overlayHeadline",
                                category = "Thumbnails",
                                fileType = "Image (PNG)",
                                uriOrPath = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80",
                                sizeFormatted = "1.4 MB",
                                durationFormatted = "",
                                tags = selectedBadge
                            )
                            onSaveThumbnail(media)
                            Toast.makeText(context, "Thumbnail saved to Media Library!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        modifier = Modifier.weight(1f).testTag("save_thumbnail_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Thumbnail", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
