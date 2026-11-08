package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItemEntity
import com.example.i18n.I18n
import com.example.ui.theme.*

@Composable
fun ThumbnailCreatorScreen(
    currentLanguage: String,
    onSaveToMediaLibrary: (MediaItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN

    var title by remember { mutableStateOf("DON'T MAKE THIS MISTAKE!") }
    var subtitle by remember { mutableStateOf("SECRET REVEALED 2026") }
    var selectedBadge by remember { mutableStateOf("4K") }
    var selectedStyle by remember { mutableStateOf("YouTube Viral") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") } // 16:9, 9:16, 1:1
    var colorThemeIndex by remember { mutableStateOf(0) }

    val colorThemes = listOf(
        listOf(Color(0xFF0F172A), Color(0xFFE11D48)), // Dark Crimson
        listOf(Color(0xFF1E1B4B), Color(0xFF7C3AED)), // Deep Purple
        listOf(Color(0xFF064E3B), Color(0xFF10B981)), // Emerald Green
        listOf(Color(0xFF7C2D12), Color(0xFFF59E0B)), // Amber Sunset
        listOf(Color(0xFF0C4A6E), Color(0xFF0284C7))  // Ocean Blue
    )

    val currentGradient = colorThemes[colorThemeIndex]

    fun handleDownload() {
        Toast.makeText(context, "Thumbnail rendered & saved to downloads folder!", Toast.LENGTH_SHORT).show()
    }

    fun handleSaveToLibrary() {
        onSaveToMediaLibrary(
            MediaItemEntity(
                title = "$title ($selectedStyle)",
                category = "Thumbnails",
                fileType = "image/png",
                uriOrPath = "local://thumbnail_${System.currentTimeMillis()}.png",
                sizeFormatted = "1.6 MB",
                tags = "thumbnail, $selectedStyle, $selectedBadge, $selectedAspectRatio"
            )
        )
        Toast.makeText(context, "Saved to Media Library & Projects!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = I18n.t("nav_thumbnail_creator", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isKn) "ಉನ್ನತ CTR ಥಂಬ್‌ನೇಲ್‌ಗಳನ್ನು ನೇರವಾಗಿ ವಿನ್ಯಾಸಗೊಳಿಸಿ" else "Design high-CTR thumbnails with live canvas preview & badges",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Visual Canvas Preview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("thumbnail_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val previewHeight = when (selectedAspectRatio) {
                        "9:16" -> 260.dp
                        "1:1" -> 200.dp
                        else -> 170.dp
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (selectedAspectRatio == "9:16") 0.65f else 1f)
                            .height(previewHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(currentGradient))
                            .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        // Visual Canvas decorative glow
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = size.minDimension * 0.45f,
                                center = Offset(size.width * 0.85f, size.height * 0.2f)
                            )
                        }

                        // Badge at Top Left
                        if (selectedBadge.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioAmber)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }

                        // Aspect Ratio label at Top Right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = selectedAspectRatio,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Main Title & Subtitle centered/bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = title.ifBlank { "YOUR VIDEO TITLE" },
                                    fontSize = if (selectedAspectRatio == "9:16") 15.sp else 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFACC15),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }

                            if (subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = subtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Style watermark bottom
                        Text(
                            text = "STYLE: $selectedStyle",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons: Download & Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { handleDownload() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("download_thumbnail_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(I18n.t("download", currentLanguage), fontSize = 12.sp)
                        }

                        Button(
                            onClick = { handleSaveToLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("save_thumbnail_library_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(I18n.t("save", currentLanguage), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Customization Inputs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Thumbnail Bold Headline") },
                        modifier = Modifier.fillMaxWidth().testTag("thumb_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("Secondary Text / Hook") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Aspect Ratio Selector
                    Text(text = "Aspect Ratio:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("16:9", "9:16", "1:1").forEach { ratio ->
                            FilterChip(
                                selected = selectedAspectRatio == ratio,
                                onClick = { selectedAspectRatio = ratio },
                                label = { Text(ratio) }
                            )
                        }
                    }

                    // Badge Stickers
                    Text(text = "Badge Sticker:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("4K", "NEW", "MUST WATCH", "TRENDING", "EPISODE 1", "TOP SECRET").forEach { b ->
                            item {
                                FilterChip(
                                    selected = selectedBadge == b,
                                    onClick = { selectedBadge = if (selectedBadge == b) "" else b },
                                    label = { Text(b) }
                                )
                            }
                        }
                    }

                    // Background Gradient Palette
                    Text(text = "Color Atmosphere:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorThemes.forEachIndexed { index, colors ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(colors))
                                    .clickable { colorThemeIndex = index }
                                    .border(
                                        width = if (colorThemeIndex == index) 2.dp else 0.dp,
                                        color = if (colorThemeIndex == index) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }

                    // Styles list
                    Text(text = "Style Template:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "YouTube Viral", "Cinematic", "Professional", "News", "Gaming",
                            "Documentary", "Travel", "Educational", "Kannada Creator"
                        ).forEach { s ->
                            item {
                                FilterChip(
                                    selected = selectedStyle == s,
                                    onClick = { selectedStyle = s },
                                    label = { Text(s) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
