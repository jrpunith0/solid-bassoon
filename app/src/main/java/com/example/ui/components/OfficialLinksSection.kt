package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.YouTubeRed

data class OfficialPortalLink(
    val id: String,
    val titleEn: String,
    val titleKn: String,
    val subtitleEn: String,
    val subtitleKn: String,
    val url: String,
    val icon: ImageVector,
    val accentColor: Color
)

object OfficialLinksData {
    val LINKS = listOf(
        OfficialPortalLink(
            id = "youtube_studio",
            titleEn = "YouTube Studio",
            titleKn = "ಯೂಟ್ಯೂಬ್ ಸ್ಟುಡಿಯೋ",
            subtitleEn = "Official Channel Portal",
            subtitleKn = "ಅಧಿಕೃತ ಚಾನೆಲ್ ಪೋರ್ಟಲ್",
            url = "https://studio.youtube.com",
            icon = Icons.Default.SmartDisplay,
            accentColor = YouTubeRed
        ),
        OfficialPortalLink(
            id = "meta_business",
            titleEn = "Meta Business Suite",
            titleKn = "ಮೆಟಾ ಬಿಸಿನೆಸ್ ಸೂಟ್",
            subtitleEn = "Facebook & Instagram Mgmt",
            subtitleKn = "ಫೇಸ್‌ಬುಕ್ ಮತ್ತು ಇನ್‌ಸ್ಟಾಗ್ರಾಮ್ ನಿರ್ವಹಣೆ",
            url = "https://business.facebook.com",
            icon = Icons.Default.Business,
            accentColor = Color(0xFF1877F2)
        ),
        OfficialPortalLink(
            id = "instagram",
            titleEn = "Instagram Portal",
            titleKn = "ಇನ್‌ಸ್ಟಾಗ್ರಾಮ್ ಪೋರ್ಟಲ್",
            subtitleEn = "Direct Web & Feed",
            subtitleKn = "ನೇರ ವೆಬ್ ಮತ್ತು ಫೀಡ್",
            url = "https://www.instagram.com",
            icon = Icons.Default.CameraAlt,
            accentColor = Color(0xFFE1306C)
        ),
        OfficialPortalLink(
            id = "google_ai_studio",
            titleEn = "Google AI Studio",
            titleKn = "ಗೂಗಲ್ AI ಸ್ಟುಡಿಯೋ",
            subtitleEn = "Get Free Gemini API Key",
            subtitleKn = "ಉಚಿತ ಜೆಮಿನಿ API ಕೀಲಿ ಪಡೆಯಿರಿ",
            url = "https://aistudio.google.com/app/apikey",
            icon = Icons.Default.Key,
            accentColor = StudioAmber
        ),
        OfficialPortalLink(
            id = "imagen_api",
            titleEn = "AI Image Generation API",
            titleKn = "AI ಚಿತ್ರ ಜನರೇಟರ್ API",
            subtitleEn = "Imagen 3 Official Docs",
            subtitleKn = "ಇಮೇಜನ್ 3 ಅಧಿಕೃತ ಡಾಕ್ಯುಮೆಂಟ್ಸ್",
            url = "https://ai.google.dev/gemini-api/docs/imagen",
            icon = Icons.Default.Image,
            accentColor = StudioPurple
        ),
        OfficialPortalLink(
            id = "gemini_tts_api",
            titleEn = "AI Voice / TTS API",
            titleKn = "AI ಧ್ವನಿ / TTS API",
            subtitleEn = "Gemini & Cloud TTS Docs",
            subtitleKn = "ಜೆಮಿನಿ ಧ್ವನಿ ಸಿಂಥೆಸಿಸ್ ಮಾರ್ಗದರ್ಶಿ",
            url = "https://ai.google.dev/gemini-api/docs/speech-generation",
            icon = Icons.Default.RecordVoiceOver,
            accentColor = StudioEmerald
        ),
        OfficialPortalLink(
            id = "gemini_video_api",
            titleEn = "AI Video Generation API",
            titleKn = "AI ವೀಡಿಯೊ ಜನರೇಟರ್ API",
            subtitleEn = "Gemini Video Pipeline",
            subtitleKn = "ಜೆಮಿನಿ ವೀಡಿಯೊ ಮಾರ್ಗದರ್ಶಿ",
            url = "https://ai.google.dev/gemini-api/docs/video-generation",
            icon = Icons.Default.Videocam,
            accentColor = StudioCyan
        ),
        OfficialPortalLink(
            id = "ffmpeg_download",
            titleEn = "FFmpeg Download",
            titleKn = "FFmpeg ಡೌನ್‌ಲೋಡ್",
            subtitleEn = "Hardware MP4 Render Engine",
            subtitleKn = "MP4 ವೀಡಿಯೊ ಎಕ್ಸ್‌ಪೋರ್ಟ್ ಎಂಜಿನ್",
            url = "https://ffmpeg.org/download.html",
            icon = Icons.Default.Download,
            accentColor = StudioIndigo
        )
    )
}

@Composable
fun OfficialLinksSection(
    isKn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun openUrl(url: String, name: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open $name: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("official_links_section_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = StudioAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isKn) "ಅಧಿಕೃತ ಲಿಂಕ್‌ಗಳು ಮತ್ತು ಪೋರ್ಟಲ್‌ಗಳು" else "OFFICIAL CREATOR LINKS & APIS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isKn) "8 ಪೋರ್ಟಲ್‌ಗಳು" else "8 PORTALS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(OfficialLinksData.LINKS) { item ->
                    OfficialLinkChipCard(
                        link = item,
                        isKn = isKn,
                        onClick = { openUrl(item.url, item.titleEn) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OfficialLinkChipCard(
    link: OfficialPortalLink,
    isKn: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
            .testTag("official_link_${link.id}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, link.accentColor.copy(alpha = 0.35f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = link.accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = link.icon,
                            contentDescription = null,
                            tint = link.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open Link",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isKn) link.titleKn else link.titleEn,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = if (isKn) link.subtitleKn else link.subtitleEn,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp,
                maxLines = 1
            )
        }
    }
}
