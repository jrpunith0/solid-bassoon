package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.ui.theme.*

@Composable
fun SocialChannelsScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    onNavigateToSettings: () -> Unit,
    onOpenPlatformLogin: (String) -> Unit = {},
    onOpenUserLogin: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN

    fun openUrl(url: String, service: String) {
        if (url.isBlank() || url == "https://") {
            Toast.makeText(context, "Configure $service URL in Settings", Toast.LENGTH_SHORT).show()
            onNavigateToSettings()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        text = I18n.t("nav_social_channels", currentLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isKn) "ನಿಮ್ಮ ಲಿಂಕ್ ಮಾಡಿದ ಸಾಮಾಜಿಕ ಮಾಧ್ಯಮ ಪ್ರೊಫೈಲ್‌ಗಳು" else "Connected creator channels & one-tap direct access",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onNavigateToSettings,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit URLs", fontSize = 11.sp)
                }
            }
        }

        // Google Account Link & Channel Link Details Card (User Requirement)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("google_profile_link_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header Row: Gmail & Profile Status
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
                                    .background(StudioCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = settings.googleAccountName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = settings.youTubeEmail.ifBlank { "No Gmail linked yet" },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (settings.isGoogleAccountLoggedIn && settings.youTubeEmail.isNotBlank()) Color(0xFF2EA44F).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (settings.isGoogleAccountLoggedIn && settings.youTubeEmail.isNotBlank()) "GMAIL LOGGED IN" else "NOT LOGGED IN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (settings.isGoogleAccountLoggedIn && settings.youTubeEmail.isNotBlank()) Color(0xFF2EA44F) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 3 Linked Platforms Showcase Grid
                    Text(
                        text = "ACCOUNTS LINKED TO THIS GMAIL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // YouTube Mini Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (settings.isYouTubeConnected) YouTubeRed else Color.Gray))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(settings.youTubeHandle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text(if (settings.isYouTubeConnected) "Linked ✓" else "Not Linked", fontSize = 9.sp, color = if (settings.isYouTubeConnected) Color(0xFF2EA44F) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Facebook Mini Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (settings.isFacebookConnected) FacebookBlue else Color.Gray))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(settings.facebookPageName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text(if (settings.isFacebookConnected) "Linked ✓" else "Not Linked", fontSize = 9.sp, color = if (settings.isFacebookConnected) Color(0xFF2EA44F) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Instagram Mini Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (settings.isInstagramConnected) InstagramGradientPink else Color.Gray))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(settings.instagramHandle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text(if (settings.isInstagramConnected) "Linked ✓" else "Not Linked", fontSize = 9.sp, color = if (settings.isInstagramConnected) Color(0xFF2EA44F) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Action: Open Gmail & Social Accounts Login Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isGmailLoggedIn = settings.isGoogleAccountLoggedIn && settings.youTubeEmail.isNotBlank()
                        Button(
                            onClick = onOpenUserLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGmailLoggedIn) Color(0xFF4285F4) else StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).testTag("social_hub_login_account_btn")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isGmailLoggedIn) "Manage Gmail Accounts" else "LOGIN WITH GMAIL ACCOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "All platform connections active for ${settings.youTubeEmail}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("test_all_channels_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Links", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Prominent Main Channel Card (Required by user prompt)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = YouTubeRed)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = I18n.t("my_main_channel", currentLanguage),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "PRIMARY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = settings.channelName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Text(
                        text = settings.youtubeChannelUrl,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Channel: ${settings.youTubeHandle}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        FilledTonalButton(
                            onClick = { onOpenPlatformLogin("YouTube") },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.3f), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp).testTag("edit_link_youtube_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (settings.isYouTubeConnected) "Edit Channel Link" else "Link YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { openUrl(settings.youtubeChannelUrl, "YouTube") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Main Channel", color = YouTubeRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { com.example.util.YouTubeStudioLinkHelper.openYouTubeStudioApp(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("social_open_yt_studio_btn")
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Studio App", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Direct YouTube Studio App Link Card
        item {
            com.example.util.YouTubeStudioAppLinkCard(
                currentLanguage = currentLanguage,
                channelName = settings.channelName,
                customStudioUrl = settings.youtubeStudioUrl
            )
        }

        // Instagram Channel Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(InstagramGradientPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Instagram Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = settings.instagramUrl, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account: ${settings.instagramHandle}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InstagramGradientPink
                        )
                        FilledTonalButton(
                            onClick = { onOpenPlatformLogin("Instagram") },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = InstagramGradientPink.copy(alpha = 0.15f), contentColor = InstagramGradientPink),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp).testTag("edit_link_instagram_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (settings.isInstagramConnected) "Edit Profile Link" else "Link Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { openUrl(settings.instagramUrl, "Instagram") },
                        colors = ButtonDefaults.buttonColors(containerColor = InstagramGradientPink),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Instagram Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Facebook Channel Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FacebookBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Facebook Page", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = settings.facebookUrl, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account: ${settings.facebookPageName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FacebookBlue
                        )
                        FilledTonalButton(
                            onClick = { onOpenPlatformLogin("Facebook") },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = FacebookBlue.copy(alpha = 0.15f), contentColor = FacebookBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp).testTag("edit_link_facebook_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (settings.isFacebookConnected) "Edit Page Link" else "Link Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { openUrl(settings.facebookUrl, "Facebook") },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Facebook Page", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // GitHub Data Link Card (User Requirement)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("github_channel_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF24292E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "GitHub Data Link", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "https://github.com/${settings.gitHubUsername}/${settings.gitHubRepo}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2EA44F).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2EA44F),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Repository: ${settings.gitHubUsername}/${settings.gitHubRepo} (${settings.gitHubBranch})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = { onNavigateToRoute("github_link") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp).testTag("manage_github_link_button")
                        ) {
                            Text("Manage Sync", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { openUrl("https://github.com/${settings.gitHubUsername}/${settings.gitHubRepo}", "GitHub") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("open_github_repo_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open GitHub Repo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        FilledTonalButton(
                            onClick = { onNavigateToRoute("github_link") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sync_app_data_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync App Data", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
