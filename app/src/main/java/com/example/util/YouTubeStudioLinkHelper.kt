package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.YouTubeRed

object YouTubeStudioLinkHelper {
    const val YOUTUBE_STUDIO_PACKAGE = "com.google.android.apps.youtube.creator"
    const val YOUTUBE_STUDIO_WEB_BASE = "https://studio.youtube.com"
    const val YOUTUBE_STUDIO_PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.google.android.apps.youtube.creator"
    const val YOUTUBE_STUDIO_MARKET_URI =
        "market://details?id=com.google.android.apps.youtube.creator"

    /**
     * Checks if the official YouTube Studio app is installed on the device.
     */
    fun isYouTubeStudioInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(YOUTUBE_STUDIO_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Opens YouTube Studio app directly if available, with deep-link support to a specific video ID or channel,
     * falling back to the browser or Google Play Store.
     */
    fun openYouTubeStudioApp(
        context: Context,
        videoId: String? = null,
        customUrl: String? = null
    ) {
        val targetUrl = when {
            !videoId.isNullOrBlank() -> "https://studio.youtube.com/video/$videoId/edit"
            !customUrl.isNullOrBlank() -> customUrl
            else -> YOUTUBE_STUDIO_WEB_BASE
        }

        val packageManager = context.packageManager
        val isInstalled = isYouTubeStudioInstalled(context)

        if (isInstalled) {
            // 1. Try launching with explicit package and URI
            try {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                    setPackage(YOUTUBE_STUDIO_PACKAGE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(appIntent)
                Toast.makeText(context, "Opening in YouTube Studio App...", Toast.LENGTH_SHORT).show()
                return
            } catch (_: Exception) {
                // Fall back to main launcher intent of the app
            }

            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(YOUTUBE_STUDIO_PACKAGE)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Opening YouTube Studio App...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (_: Exception) {
                // Fall through to browser
            }
        }

        // 2. Not installed or failed to launch directly: open web URL with fallback
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            Toast.makeText(context, "Opening YouTube Studio Web...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            openYouTubeStudioPlayStore(context)
        }
    }

    /**
     * Opens the Google Play Store listing for the official YouTube Studio app.
     */
    fun openYouTubeStudioPlayStore(context: Context) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_STUDIO_MARKET_URI)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_STUDIO_PLAY_STORE_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}

@Composable
fun YouTubeStudioAppLinkCard(
    currentLanguage: String = "en",
    channelName: String = "Creator Channel",
    customStudioUrl: String = YouTubeStudioLinkHelper.YOUTUBE_STUDIO_WEB_BASE,
    videoId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == "kn"
    val isInstalled = remember { YouTubeStudioLinkHelper.isYouTubeStudioInstalled(context) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("youtube_studio_app_link_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1010)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "YouTube Studio",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "YouTube Studio App",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isInstalled) Color(0xFF2EA44F) else StudioCrimson
                            ) {
                                Text(
                                    text = if (isInstalled) "INSTALLED" else "LINK AVAILABLE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isKn) "ಅಧಿಕೃತ YouTube Studio ಮೊಬೈಲ್ ಅಪ್ಲಿಕೇಶನ್ ಲಿಂಕ್"
                            else "Official creator mobile app & web dashboard",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Text(
                text = if (isKn)
                    "ನಿಮ್ಮ ಚಾನೆಲ್ ಅಂಕಿಅಂಶಗಳು, ವೀಡಿಯೊ ಮಾನಿಟರಿಂಗ್, ಕಾಮೆಂಟ್‌ಗಳು ಮತ್ತು ಅಪ್‌ಲೋಡ್‌ಗಳನ್ನು ನೇರವಾಗಿ YouTube Studio ಆಪ್‌ನಲ್ಲಿ ತೆರೆಯಿರಿ."
                else
                    "Manage $channelName analytics, view video performance, reply to comments, and configure channel settings directly in YouTube Studio.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        YouTubeStudioLinkHelper.openYouTubeStudioApp(
                            context = context,
                            videoId = videoId,
                            customUrl = customStudioUrl
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_open_youtube_studio_app")
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isKn) "ಆಪ್‌ನಲ್ಲಿ ತೆರೆಯಿರಿ" else "OPEN STUDIO APP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                FilledTonalButton(
                    onClick = {
                        YouTubeStudioLinkHelper.openYouTubeStudioPlayStore(context)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_get_studio_playstore")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shop,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isKn) "Play Store" else "PLAY STORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
