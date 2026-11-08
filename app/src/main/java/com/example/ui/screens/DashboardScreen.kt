package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AutoVideoJobEntity
import com.example.data.CalendarTaskEntity
import com.example.data.ContentProjectEntity
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.data.CachedGitHubRepoEntity
import com.example.data.github.GitHubTokenValidationResult
import com.example.ui.components.GitHubPATWarningBanner
import com.example.ui.components.GitHubRepoDetailDialog
import com.example.ui.components.OfficialLinksSection
import com.example.ui.components.PlatformBadge
import com.example.ui.components.ProjectStatusBadge
import com.example.util.YouTubeStudioLinkHelper
import com.example.ui.components.QuickActionsBar
import com.example.ui.components.TaskStatusBadge
import com.example.ui.components.VideoPlayerView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import java.util.Locale
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    projects: List<ContentProjectEntity>,
    calendarTasks: List<CalendarTaskEntity>,
    autoVideoJobs: List<AutoVideoJobEntity> = emptyList(),
    cachedGitHubRepos: List<CachedGitHubRepoEntity> = emptyList(),
    gitHubTokenWarning: GitHubTokenValidationResult? = null,
    isSyncingGitHubRepos: Boolean = false,
    onSyncGitHubRepos: () -> Unit = {},
    onRevokeGitHubPAT: () -> Unit = {},
    onSelectActiveRepo: ((String, String) -> Unit)? = null,
    isBatchReprocessing: Boolean = false,
    onReprocessLast100: () -> Unit = {},
    onReprocessAll: () -> Unit = {},
    onConnectPlatform: (String) -> Unit = {},
    onOpenUserLogin: () -> Unit = {},
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN
    var previewVideoJob by remember { mutableStateOf<AutoVideoJobEntity?>(null) }
    var selectedRepoForDetail by remember { mutableStateOf<CachedGitHubRepoEntity?>(null) }

    fun openBrowserUrl(url: String, service: String) {
        val trimmed = url.trim()
        if (trimmed.isBlank() || trimmed == "https://") {
            Toast.makeText(context, "Configure $service URL in Settings", Toast.LENGTH_SHORT).show()
            onNavigate("settings")
            return
        }
        val formatted = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else trimmed
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formatted))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF831843)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = StudioAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKn) "ಕ್ರಿಯೇಟರ್ ಕಂಟ್ರೋಲ್ ಸೆಂಟರ್" else "ALL-IN-ONE CREATOR STUDIO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isKn) "ನಿಮ್ಮ ಕಂಟೆಂಟ್. ಒಂದೇ ಸ್ಟುಡಿಯೋ." else "YOUR CONTENT. ONE CREATOR STUDIO.",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isKn) "YouTube, Instagram ಮತ್ತು Facebook ವಿಷಯಗಳನ್ನು ಒಂದೇ ಸ್ಥಳದಿಂದ ನಿರ್ವಹಿಸಿ."
                            else "Create, organize, and manage YouTube, Instagram, and Facebook content from one central place.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigate("create_video") },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_create_video_button")
                            ) {
                                Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isKn) "ವೀಡಿಯೊ" else "CREATE VIDEO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { onNavigate("shorts_reels") },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_create_short_button")
                            ) {
                                Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isKn) "ರೀಲ್ಸ್" else "CREATE SHORT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { openBrowserUrl(settings.youtubeChannelUrl, "YouTube") },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("hero_my_channel_button")
                            ) {
                                Text(
                                    text = if (isKn) "ಚಾನೆಲ್" else "CHANNEL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { YouTubeStudioLinkHelper.openYouTubeStudioApp(context) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = YouTubeRed.copy(alpha = 0.85f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("dashboard_open_yt_studio_app_button")
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "STUDIO APP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigate("manga_studio") },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_manga_studio_button")
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKn) "📖 ಮಂಗಾ ಸ್ಟುಡಿಯೋ AI" else "📖 MANGA STORY STUDIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { onNavigate("auto_generator") },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_auto_video_button")
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isKn) "⚡ ಆಟೋ ವೀಡಿಯೊ" else "⚡ 10-MIN AUTO VIDEO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Story-to-Video AI Engine Primary Launch Banner
                        Button(
                            onClick = { onNavigate("story_to_video") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hero_story_to_video_button")
                        ) {
                            Icon(Icons.Default.AutoAwesomeMotion, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKn) "✨ ಕಥೆ-ಟು-ವೀಡಿಯೊ AI ಜನರೇಟರ್ (Story-to-Video Pipeline)" else "✨ STORY-TO-VIDEO AI ENGINE (COMPLETE PIPELINE)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Studio Launchers Row: AI Prompt Studio, Cartoon Studio, Publishing Centre
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onNavigate("ai_prompt_studio") },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f).testTag("hero_ai_prompt_studio_button")
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isKn) "AI ಪ್ರಾಂಪ್ಟ್" else "AI Prompt Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigate("cartoon_studio") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f).testTag("hero_cartoon_studio_button")
                            ) {
                                Icon(Icons.Default.Animation, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isKn) "ಕಾರ್ಟೂನ್" else "Cartoon Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigate("publishing_centre") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f).testTag("hero_publishing_centre_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isKn) "ಪ್ರಕಾಶನ" else "Publish Centre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cross-Platform Ecosystem Hub Launcher (Website + Android + iOS)
                        Button(
                            onClick = { onNavigate("cross_platform_hub") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("hero_cross_platform_hub_button")
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp), tint = StudioAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isKn) "💻 ಕ್ರಾಸ್-ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಕೇಂದ್ರ (ವೆಬ್ • ಆಂಡ್ರಾಯ್ಡ್ • iOS)" else "💻 CROSS-PLATFORM HUB (WEB • ANDROID • IOS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Official Creator Links & APIs Bar (YouTube Studio, Meta Business, Instagram, AI Studio, Imagen, TTS, Video Gen, FFmpeg)
        item {
            OfficialLinksSection(
                isKn = isKn,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. YouTube, Facebook & Instagram Main Details Section
        item {
            SocialPlatformsMainDetailsSection(
                settings = settings,
                currentLanguage = currentLanguage,
                autoVideoJobs = autoVideoJobs,
                projects = projects,
                onOpenUrl = { url, svc -> openBrowserUrl(url, svc) },
                onNavigate = onNavigate,
                onConnectPlatform = onConnectPlatform,
                onOpenUserLogin = onOpenUserLogin
            )
        }

        // 3. 10-Minute Auto Video Generator & Direct Upload Spotlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate("auto_generator") }
                    .testTag("dashboard_10min_auto_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isKn) "10-ನಿಮಿಷದ ಆಟೋ ವೀಡಿಯೊ ಜನರೇಟರ್" else "10-MIN AUTO VIDEO GENERATOR",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StudioAmber)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("AUTO UPLOAD", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                }
                            }
                            Text(
                                text = if (isKn) "10 ದೃಶ್ಯಗಳು, ಟೈಮ್‌ಸ್ಟ್ಯಾಂಪ್‌ಗಳು, B-Roll ಮತ್ತು YouTube/IG/FB ಗೆ ನೇರ ಅಪ್‌ಲೋಡ್"
                                else "Automated scheduler, Gemini script engine, 3 visual prompts & direct upload to YouTube/FB/IG",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { onNavigate("auto_generator") }) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Open", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 4. Quick Actions Bar (Mandated in user request for one-click access to Script, Caption, Hashtags, and GitHub Sync)
        item {
            QuickActionsBar(
                currentLanguage = currentLanguage,
                onOpenScriptGenerator = { onNavigate("script_gen") },
                onOpenCaptionGenerator = { onNavigate("instagram") },
                onOpenHashtagGenerator = { onNavigate("ai_tools") },
                onOpenGitHubSync = { onNavigate("github_link") }
            )
        }

        // GitHub PAT Warning Banner (Prompt 6)
        if (gitHubTokenWarning != null) {
            item {
                GitHubPATWarningBanner(
                    validation = gitHubTokenWarning,
                    onOpenManageToken = { onNavigate("github_link") },
                    onRevokeToken = onRevokeGitHubPAT
                )
            }
        }

        // GitHub Data Link Card on Dashboard
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("github_link") }
                    .testTag("dashboard_github_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF24292E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isKn) "ಗಿಟ್‌ಹಬ್ ಡೇಟಾ ಲಿಂಕ್" else "GITHUB DATA LINK",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2EA44F).copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("SYNC READY", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF2EA44F))
                                }
                            }
                            Text(
                                text = "Repo: ${settings.gitHubUsername}/${settings.gitHubRepo} (${settings.gitHubBranch}) • Last synced: ${settings.gitHubLastSync}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { onNavigate("github_link") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("dashboard_open_github_sync")
                    ) {
                        Text(if (isKn) "ಸಿಂಕ್ ಮಾಡಿ" else "Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // GitHub Repository Metadata Sync List (Prompt 7 & 8)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_github_repos_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKn) "ಸಿಂಕ್ ಮಾಡಲಾದ GitHub ರೆಪೊಸಿಟರಿಗಳು" else "SYNCED GITHUB REPOSITORIES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Repository metadata fetched via PAT (${cachedGitHubRepos.size} repos cached)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onSyncGitHubRepos,
                            enabled = !isSyncingGitHubRepos,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EA44F)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("dashboard_sync_repos_btn")
                        ) {
                            if (isSyncingGitHubRepos) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (cachedGitHubRepos.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No repositories cached yet. Tap 'Sync' or connect PAT in GitHub Data Link.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            cachedGitHubRepos.take(5).forEach { repo ->
                                val langColor = when (repo.language?.lowercase()) {
                                    "kotlin" -> Color(0xFF7F52FF)
                                    "typescript" -> Color(0xFF3178C6)
                                    "python" -> Color(0xFF3572A5)
                                    "javascript" -> Color(0xFFF7DF1E)
                                    "go" -> Color(0xFF00ADD8)
                                    "rust" -> Color(0xFFDEA584)
                                    else -> Color(0xFF6E7681)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRepoForDetail = repo }
                                        .testTag("dashboard_repo_item_${repo.name}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = repo.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (repo.name.equals(settings.gitHubRepo, ignoreCase = true)) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = StudioCrimson.copy(alpha = 0.15f)
                                                    ) {
                                                        Text("TARGET", fontSize = 8.sp, fontWeight = FontWeight.Black, color = StudioCrimson, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(langColor))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = repo.language ?: "Code", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text(text = "⭐ ${repo.stargazersCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "🍴 ${repo.forksCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Details",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "View Details",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Live DB Metrics Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    label = if (isKn) "ಒಟ್ಟು ಪ್ರಾಜೆಕ್ಟ್‌ಗಳು" else "Total Projects",
                    value = "${projects.size}",
                    color = StudioCrimson,
                    icon = Icons.Default.Folder,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    label = if (isKn) "ನಿಗದಿತ ಕಂಟೆಂಟ್" else "Scheduled Posts",
                    value = "${calendarTasks.count { it.status.equals("Scheduled", ignoreCase = true) }}",
                    color = StudioAmber,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    label = if (isKn) "ಪ್ರಕಟಿಸಲಾಗಿದೆ" else "Published",
                    value = "${calendarTasks.count { it.status.equals("Published", ignoreCase = true) }}",
                    color = StudioEmerald,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Dashboard Studio Tools (12 Dedicated Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isKn) "೧೨ ಪ್ರಮುಖ ಸ್ಟುಡಿಯೋ ಮಾಡ್ಯೂಲ್‌ಗಳು (12 Core Studios)" else "12 CORE STUDIO MODULES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioCrimson.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "12 DEDICATED MODULES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = StudioCrimson,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 12 Dedicated Cards
        item {
            val core12Cards = listOf(
                StudioTool("manga_studio", if (isKn) "1. 📖 ಮಂಗಾ ಸ್ಟೋರಿ ಕ್ರಿಯೇಟರ್" else "1. 📖 Manga Story Creator", if (isKn) "ಕಥೆ, ಪಾತ್ರಗಳು, ಸಂಭಾಷಣೆ, ಅಧ್ಯಾಯ & ಪ್ಯಾನಲ್‌ಗಳು" else "Original manga stories, chapters, panels & export", StudioCrimson, Icons.Default.AutoStories),
                StudioTool("youtube", if (isKn) "2. 🔴 ಯೂಟ್ಯೂಬ್ ಸ್ಟುಡಿಯೋ" else "2. 🔴 YouTube Studio", if (isKn) "ದೀರ್ಘ ಸ್ಕ್ರಿಪ್ಟ್‌ಗಳು, SEO, ಥಂಬ್‌ನೇಲ್ & ಚಾನೆಲ್ ಅಪ್‌ಲೋಡ್" else "Long videos, scripts, SEO, thumbnails & upload", YouTubeRed, Icons.Default.SmartDisplay),
                StudioTool("facebook", if (isKn) "3. 🔵 ಫೇಸ್‌ಬುಕ್ ಸ್ಟುಡಿಯೋ" else "3. 🔵 Facebook Studio", if (isKn) "ಪೋಸ್ಟ್‌ಗಳು, ವೀಡಿಯೊ, ಕ್ಯಾಪ್ಶನ್, ಹ್ಯಾಶ್‌ಟ್ಯಾಗ್ & ಪ್ರಕಟಣೆ" else "Posts, videos, AI captions, hashtags & publishing", Color(0xFF1877F2), Icons.Default.ThumbUp),
                StudioTool("instagram", if (isKn) "4. 🟣 ಇನ್‌ಸ್ಟಾಗ್ರಾಮ್ ಸ್ಟುಡಿಯೋ" else "4. 🟣 Instagram Studio", if (isKn) "ಕ್ಯಾರೋಸೆಲ್‌ಗಳು, ಪೋಸ್ಟ್‌ಗಳು, ಸ್ಟೋರಿ & ಕ್ಯಾಪ್ಶನ್‌ಗಳು" else "Posts, carousels, stories, captions & hashtags", Color(0xFFE1306C), Icons.Default.CameraAlt),
                StudioTool("shorts_reels", if (isKn) "5. ⚡ ಯೂಟ್ಯೂಬ್ ಶಾರ್ಟ್ಸ್" else "5. ⚡ YouTube Shorts", if (isKn) "15-60ಸೆ ಲಂಬ ಸ್ಕ್ರಿಪ್ಟ್‌ಗಳು, 1-3ಸೆ ವೈರಲ್ ಹುಕ್‌ಗಳು & 9:16" else "15-60s vertical scripts, 1-3s hooks & 9:16 export", StudioCyan, Icons.Default.Smartphone),
                StudioTool("shorts_reels", if (isKn) "6. 📱 ಇನ್‌ಸ್ಟಾಗ್ರಾಮ್ ರೀಲ್ಸ್" else "6. 📱 Instagram Reels", if (isKn) "ಟ್ರೆಂಡಿಂಗ್ ಕಾನ್ಸೆಪ್ಟ್, ವೇಗದ ಪೇಸಿಂಗ್, ಸಂಗೀತ & 9:16" else "Trending reels, short scripts, audio & 9:16 video", Color(0xFFC13584), Icons.Default.VideoLibrary),
                StudioTool("story_to_video", if (isKn) "7. 🎬 AI ವಿಡಿಯೊ ಜನರೇಟರ್" else "7. 🎬 AI Video Generator", if (isKn) "ಕಥೆಯಿಂದ ಸಂಪೂರ್ಣ ವೀಡಿಯೊ ಪೈಪ್‌ಲೈನ್, ಇಮೇಜ್ & MP4" else "Story-to-video pipeline, scene images & MP4 export", StudioEmerald, Icons.Default.AutoAwesomeMotion),
                StudioTool("thumbnail_creator", if (isKn) "8. 🖼️ ಇಮೇಜ್ ಜನರೇಟರ್" else "8. 🖼️ Image Generator", if (isKn) "ವಿಶುವಲ್ ಕ್ಯಾನ್ವಾಸ್, ಥಂಬ್‌ನೇಲ್‌ಗಳು & ಪಾತ್ರ ಚಿತ್ರಗಳು" else "Visual canvas, thumbnails & character art", StudioAmber, Icons.Default.Image),
                StudioTool("story_to_video", if (isKn) "9. 🎙️ ವಾಯ್ಸ್ ಜನರೇಟರ್" else "9. 🎙️ Voice Generator", if (isKn) "ಕನ್ನಡ ಮತ್ತು ಇಂಗ್ಲಿಷ್ TTS, ಧ್ವನಿ ಎಡಿಟರ್ & ಆಡಿಯೋ" else "Kannada & English TTS, voiceover audio track", StudioIndigo, Icons.Default.RecordVoiceOver),
                StudioTool("video_editor", if (isKn) "10. ✂️ ವೀಡಿಯೊ ಎಡಿಟರ್" else "10. ✂️ Video Editor", if (isKn) "ಟೈಮ್‌ಲೈನ್, ಸಬ್‌ಟೈಟಲ್, ಟ್ರಿಮ್ & ಲೈವ್ ಪ್ರಿವ್ಯೂ" else "Multi-track timeline, trim, split, effects & render", StudioPurple, Icons.Default.MovieFilter),
                StudioTool("projects", if (isKn) "11. 📁 ಮೈ ಪ್ರಾಜೆಕ್ಟ್ಸ್" else "11. 📁 My Projects", if (isKn) "ಉಳಿಸಿದ ವಿಷಯಗಳು, SQLite ಕರಡುಗಳು & ರಫ್ತು" else "Saved content, Room database drafts & multi-export", Color(0xFF14B8A6), Icons.Default.FolderSpecial),
                StudioTool("settings", if (isKn) "12. ⚙️ API ಸೆಟ್ಟಿಂಗ್ಸ್" else "12. ⚙️ API Settings", if (isKn) "ಜೆಮಿನಿ ಕೀಲಿ, FFmpeg ಪಿಂಗ್ ಟೆಸ್ಟ್ & ಭಾಷೆ" else "Gemini API key, FFmpeg ping-test & channels", Color(0xFF64748B), Icons.Default.Settings)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in core12Cards.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (tool in row) {
                            StudioToolCard(
                                tool = tool,
                                onClick = { onNavigate(tool.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Additional Studio Utilities
        item {
            Text(
                text = if (isKn) "ಹೆಚ್ಚುವರಿ ಪರಿಕರಗಳು (Additional Utilities)" else "ADDITIONAL UTILITIES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            val extraTools = listOf(
                StudioTool("auto_generator", if (isKn) "⚡ ಆಟೋ ವೀಡಿಯೊ" else "⚡ 10-Min Auto Video", "Automated scheduler & auto-render", StudioCrimson, Icons.Default.Bolt),
                StudioTool("script_gen", I18n.t("card_script", currentLanguage), "Kannada & English viral script generator", StudioCyan, Icons.Default.Create),
                StudioTool("ai_tools", I18n.t("card_hashtags", currentLanguage), "Trending & high-reach tags generator", Color(0xFF10B981), Icons.Default.Tag),
                StudioTool("calendar", I18n.t("card_calendar", currentLanguage), "Status badges, search, CSV export", Color(0xFF3B82F6), Icons.Default.CalendarMonth),
                StudioTool("media", I18n.t("card_media", currentLanguage), "Organize clips, audio, covers, scripts", Color(0xFF8B5CF6), Icons.Default.PhotoLibrary),
                StudioTool("analytics", I18n.t("card_analytics", currentLanguage), "Views, followers, performance track", Color(0xFFF97316), Icons.Default.BarChart),
                StudioTool("social_channels", I18n.t("card_channels", currentLanguage), "Social profile links & handle tracker", Color(0xFF06B6D4), Icons.Default.Share),
                StudioTool("github_link", if (isKn) "🐙 ಗಿಟ್‌ಹಬ್ ಡೇಟಾ ಲಿಂಕ್" else "🐙 GitHub Data Link", "Sync app projects, scripts & calendar to Git", Color(0xFF24292E), Icons.Default.Code)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in extraTools.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (tool in row) {
                            StudioToolCard(
                                tool = tool,
                                onClick = { onNavigate(tool.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 6. Upcoming Calendar Items with Visual Status Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isKn) "ಮುಂಬರುವ ಕಂಟೆಂಟ್ (Upcoming Schedule)" else "UPCOMING CONTENT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onNavigate("calendar") }) {
                    Text(text = if (isKn) "ಕ್ಯಾಲೆಂಡರ್ ತೆರೆಯಿರಿ" else "View All Calendar", fontSize = 12.sp)
                }
            }
        }

        items(calendarTasks.take(4)) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("calendar") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PlatformBadge(platform = task.platform)
                            Text(
                                text = "${task.scheduledDate} • ${task.scheduledTime}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TaskStatusBadge(status = task.status)
                }
            }
        }

        // 7. Recent Projects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isKn) "ಇತ್ತೀಚಿನ ಪ್ರಾಜೆಕ್ಟ್‌ಗಳು (Recent Projects)" else "RECENT PROJECTS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onNavigate("projects") }) {
                    Text(text = if (isKn) "ಎಲ್ಲವೂ ನೋಡಿ" else "View Projects", fontSize = 12.sp)
                }
            }
        }

        items(projects.take(3)) { project ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("projects") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${project.platform} • ${project.format} • ${project.topic}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ProjectStatusBadge(status = project.status)
                }
            }
        }

        // 8. Previously Generated Videos & Past Content Feed (MongoDB & AI Generator Tracking)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(StudioCrimson)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isKn) "ಹಿಂದೆ ರಚಿಸಲಾದ ವೀಡಿಯೊಗಳು (Past Generated Content)"
                        else "PREVIOUSLY GENERATED VIDEOS (${autoVideoJobs.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onReprocessLast100,
                        enabled = !isBatchReprocessing,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("dashboard_reprocess_last_100_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isBatchReprocessing) "Processing..." else "Reprocess 100",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = onReprocessAll,
                        enabled = !isBatchReprocessing,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("dashboard_reprocess_all_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "All",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { onNavigate("auto_generator") }) {
                        Text(text = if (isKn) "ಎಲ್ಲಾ ವೀಡಿಯೊಗಳು" else "View All", fontSize = 11.sp)
                    }
                }
            }
        }

        if (autoVideoJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("empty_past_content_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MovieFilter, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isKn) "ಇನ್ನೂ ಯಾವುದೇ ವೀಡಿಯೊ ರಚಿಸಲಾಗಿಲ್ಲ" else "No Videos Generated Yet",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isKn) "ವೀಡಿಯೊ ರಚಿಸಲು 'ವೀಡಿಯೊ ಎಡಿಟರ್' ಅಥವಾ '10-ನಿಮಿಷದ ಆಟೋ ವೀಡಿಯೊ' ಬಳಸಿ"
                            else "Click 'Video Editor & AI' or '10-Min Auto Video' above to generate your first video.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(autoVideoJobs.take(5)) { job ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { previewVideoJob = job }
                    .testTag("dashboard_past_video_${job.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = job.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${job.platform} • ${job.format} • ${job.durationSec}s",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (job.description.isNotBlank()) {
                            Text(
                                text = job.description.take(80) + "...",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (job.status == "Uploaded") Color(0xFF2EA44F).copy(alpha = 0.15f) else StudioCrimson.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = job.status.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (job.status == "Uploaded") Color(0xFF2EA44F) else StudioCrimson,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        if (job.platform.equals("YouTube", ignoreCase = true)) {
                            IconButton(
                                onClick = { YouTubeStudioLinkHelper.openYouTubeStudioApp(context, job.uploadId) },
                                modifier = Modifier.size(32.dp).testTag("dashboard_job_yt_studio_${job.id}")
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = "Studio App", tint = YouTubeRed, modifier = Modifier.size(18.dp))
                            }
                        }

                        IconButton(
                            onClick = { previewVideoJob = job },
                            modifier = Modifier.size(32.dp).testTag("preview_job_${job.id}")
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Preview", tint = StudioCrimson, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal to preview previously generated video with VideoPlayerView
    if (previewVideoJob != null) {
        val job = previewVideoJob!!
        Dialog(onDismissRequest = { previewVideoJob = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().testTag("dashboard_video_preview_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "${job.platform} • ${job.format} • Status: ${job.status}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { previewVideoJob = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    VideoPlayerView(
                        videoUri = if (job.uploadUrl.startsWith("http") && job.uploadUrl.endsWith(".mp4")) job.uploadUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        title = job.title,
                        aspectRatio = if (job.format.contains("9:16")) 9f / 16f else 16f / 9f,
                        autoPlay = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (job.scriptText.isNotBlank()) {
                        Text(
                            text = "Generated AI Script:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = job.scriptText.take(180) + if (job.scriptText.length > 180) "..." else "",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (job.uploadUrl.isNotBlank()) {
                        Button(
                            onClick = { openBrowserUrl(job.uploadUrl, job.platform) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open on ${job.platform}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (selectedRepoForDetail != null) {
        GitHubRepoDetailDialog(
            repo = selectedRepoForDetail,
            onDismiss = { selectedRepoForDetail = null },
            onSetAsActiveRepo = onSelectActiveRepo
        )
    }
}

data class StudioTool(
    val id: String,
    val title: String,
    val desc: String,
    val accent: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun StudioToolCard(
    tool: StudioTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tool.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tool.desc,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun PlatformOverviewCard(
    title: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statText: String,
    buttonText: String,
    onOpen: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun formatNumber(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

private fun formatNumber(count: Int): String = formatNumber(count.toLong())

@Composable
fun SocialPlatformsMainDetailsSection(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    autoVideoJobs: List<AutoVideoJobEntity>,
    projects: List<ContentProjectEntity>,
    onOpenUrl: (String, String) -> Unit,
    onNavigate: (String) -> Unit,
    onConnectPlatform: (String) -> Unit,
    onOpenUserLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN
    var selectedTab by remember { mutableStateOf("all") }

    val ytJobsCount = autoVideoJobs.count { it.platform.equals("YouTube", true) || it.platform.equals("All", true) }
    val fbJobsCount = autoVideoJobs.count { it.platform.equals("Facebook", true) || it.platform.equals("All", true) }
    val igJobsCount = autoVideoJobs.count { it.platform.equals("Instagram", true) || it.platform.equals("All", true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = StudioCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isKn) "ಮುಖ್ಯ ಚಾನೆಲ್‌ಗಳು & ಪುಟಗಳ ವಿವರಗಳು" else "YOUTUBE, FACEBOOK & INSTAGRAM DETAILS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = if (isKn) "ಲೈವ್ ಚಾನೆಲ್ ಅಂಕಿಅಂಶಗಳು, ಲಾಗಿನ್ ಖಾತೆಗಳು & ನೇರ ಸ್ಟುಡಿಯೋ ನಿಯಂತ್ರಣಗಳು"
                    else "Live channel status, account handles, metrics & direct studio tools",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = { onNavigate("social_channels") },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isKn) "ಎಲ್ಲಾ ಚಾನೆಲ್‌ಗಳು" else "Social Hub",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Gmail Account & 3 Linked Social Channels Showcase Card
        val isGmailLinked = settings.isGoogleAccountLoggedIn && settings.youTubeEmail.isNotBlank() && settings.youTubeEmail.contains("@")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_gmail_link_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isGmailLinked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isGmailLinked) Color(0xFF4285F4).copy(alpha = 0.6f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isGmailLinked) Color(0xFF4285F4).copy(alpha = 0.15f) else StudioCrimson.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGmailLinked) Icons.Default.Email else Icons.Default.AccountCircle,
                                contentDescription = "Gmail Account",
                                tint = if (isGmailLinked) Color(0xFF4285F4) else StudioCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isGmailLinked) "GMAIL ACCOUNT LINKED" else "GOOGLE GMAIL: NOT LOGGED IN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isGmailLinked) Color(0xFF4285F4) else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isGmailLinked) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isGmailLinked) "LOGGED IN" else "LOGIN REQUIRED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGmailLinked) Color(0xFF059669) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Text(
                                text = if (isGmailLinked) settings.youTubeEmail else "Login with Gmail to connect YouTube, Facebook & Instagram",
                                fontSize = 12.sp,
                                fontWeight = if (isGmailLinked) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onOpenUserLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isGmailLinked) Color(0xFF4285F4) else StudioCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_link_gmail_btn"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isGmailLinked) Icons.Default.Sync else Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGmailLinked) "Manage" else "LOGIN ADD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 3 Linked Channels Summary Row
                Text(
                    text = "ACCOUNTS LINKED TO THIS GMAIL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // YouTube
                    LinkedChannelMiniChip(
                        platformName = "YouTube",
                        isConnected = settings.isYouTubeConnected,
                        identifier = if (settings.youTubeHandle.isNotBlank()) settings.youTubeHandle else if (settings.channelName.isNotBlank()) settings.channelName else "YouTube Studio",
                        color = YouTubeRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("youtube") }
                    )

                    // Facebook
                    LinkedChannelMiniChip(
                        platformName = "Facebook",
                        isConnected = settings.isFacebookConnected,
                        identifier = if (settings.isFacebookConnected && settings.channelName.isNotBlank()) settings.channelName else "Page & Reels",
                        color = FacebookBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("facebook") }
                    )

                    // Instagram
                    LinkedChannelMiniChip(
                        platformName = "Instagram",
                        isConnected = settings.isInstagramConnected,
                        identifier = if (settings.isInstagramConnected && settings.channelName.isNotBlank()) "@${settings.channelName.lowercase().replace(" ", "")}" else "Reels & Feed",
                        color = InstagramGradientPink,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("instagram") }
                    )
                }
            }
        }

        // Platform Filter Tabs (All Channels, YouTube, Facebook, Instagram)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == "all",
                onClick = { selectedTab = "all" },
                label = { Text("All Platforms (3)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            )

            FilterChip(
                selected = selectedTab == "youtube",
                onClick = { selectedTab = "youtube" },
                label = { Text("YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed)
                    )
                }
            )

            FilterChip(
                selected = selectedTab == "facebook",
                onClick = { selectedTab = "facebook" },
                label = { Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue)
                    )
                }
            )

            FilterChip(
                selected = selectedTab == "instagram",
                onClick = { selectedTab = "instagram" },
                label = { Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(InstagramGradientPink)
                    )
                }
            )
        }

        // YouTube Details Card
        if (selectedTab == "all" || selectedTab == "youtube") {
            YouTubePlatformDetailCard(
                settings = settings,
                isKn = isKn,
                uploadedCount = ytJobsCount,
                onOpenUrl = { onOpenUrl(settings.youtubeChannelUrl, "YouTube") },
                onOpenStudioApp = { YouTubeStudioLinkHelper.openYouTubeStudioApp(context) },
                onNavigate = onNavigate,
                onConnectPlatform = { onConnectPlatform("YouTube") }
            )
        }

        // Facebook Details Card
        if (selectedTab == "all" || selectedTab == "facebook") {
            FacebookPlatformDetailCard(
                settings = settings,
                isKn = isKn,
                uploadedCount = fbJobsCount,
                onOpenUrl = { onOpenUrl(settings.facebookUrl, "Facebook") },
                onNavigate = onNavigate,
                onConnectPlatform = { onConnectPlatform("Facebook") }
            )
        }

        // Instagram Details Card
        if (selectedTab == "all" || selectedTab == "instagram") {
            InstagramPlatformDetailCard(
                settings = settings,
                isKn = isKn,
                uploadedCount = igJobsCount,
                onOpenUrl = { onOpenUrl(settings.instagramUrl, "Instagram") },
                onNavigate = onNavigate,
                onConnectPlatform = { onConnectPlatform("Instagram") }
            )
        }

        // Cross-Platform Combined Audience Card
        CombinedAudienceCard(
            settings = settings,
            totalJobs = autoVideoJobs.size,
            isKn = isKn,
            onOpenStudioApp = { YouTubeStudioLinkHelper.openYouTubeStudioApp(context) },
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun YouTubePlatformDetailCard(
    settings: CreatorSettingsEntity,
    isKn: Boolean,
    uploadedCount: Int,
    onOpenUrl: () -> Unit,
    onOpenStudioApp: () -> Unit,
    onNavigate: (String) -> Unit,
    onConnectPlatform: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("platform_card_youtube"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon, Title & Connection Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "YouTube Main Channel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = YouTubeRed,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "${settings.channelName} • ${settings.youTubeHandle}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (settings.isYouTubeConnected) Color(0xFF2EA44F).copy(alpha = 0.15f) else StudioAmber.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (settings.isYouTubeConnected) Color(0xFF2EA44F) else StudioAmber)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isYouTubeConnected) "CONNECTED" else "NOT CONNECTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (settings.isYouTubeConnected) Color(0xFF2EA44F) else StudioAmber
                        )
                    }
                }
            }

            // Google Account & Channel Details Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Account: ${settings.googleAccountName} (${settings.youTubeEmail})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Channel URL: ${settings.youtubeChannelUrl}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "ID: ${settings.googleAccountId.take(6)}...",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Metrics Grid (3 stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformMetricBox(
                    title = "Subscribers",
                    value = formatNumber(settings.youtubeSubscribers),
                    color = YouTubeRed,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                PlatformMetricBox(
                    title = "Monthly Views",
                    value = formatNumber(settings.monthlyViews),
                    color = StudioAmber,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
                PlatformMetricBox(
                    title = "Published Content",
                    value = "$uploadedCount Jobs",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.VideoLibrary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenStudioApp,
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("dashboard_open_yt_studio_app_btn")
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("STUDIO APP", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                FilledTonalButton(
                    onClick = onOpenUrl,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Channel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate("youtube") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("YouTube Tool", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigate("create_video") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create Video", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onConnectPlatform,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Text(if (settings.isYouTubeConnected) "Manage Login" else "Connect Account", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FacebookPlatformDetailCard(
    settings: CreatorSettingsEntity,
    isKn: Boolean,
    uploadedCount: Int,
    onOpenUrl: () -> Unit,
    onNavigate: (String) -> Unit,
    onConnectPlatform: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("platform_card_facebook"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon, Title & Connection Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FacebookBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Facebook Main Page",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = FacebookBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "${settings.facebookPageName} • Page ID: 849201948",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (settings.isFacebookConnected) Color(0xFF2EA44F).copy(alpha = 0.15f) else StudioAmber.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (settings.isFacebookConnected) Color(0xFF2EA44F) else StudioAmber)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isFacebookConnected) "CONNECTED" else "NOT CONNECTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (settings.isFacebookConnected) Color(0xFF2EA44F) else StudioAmber
                        )
                    }
                }
            }

            // Page Details Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Page: ${settings.facebookPageName} (Official Creator Community)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "URL: ${settings.facebookUrl}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Verified Hub",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = FacebookBlue
                    )
                }
            }

            // Metrics Grid (3 stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformMetricBox(
                    title = "Page Followers",
                    value = formatNumber(settings.facebookFollowers),
                    color = FacebookBlue,
                    icon = Icons.Default.ThumbUp,
                    modifier = Modifier.weight(1f)
                )
                PlatformMetricBox(
                    title = "Monthly Reach",
                    value = "45.2K",
                    color = StudioAmber,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                PlatformMetricBox(
                    title = "Published Content",
                    value = "$uploadedCount Posts",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.DynamicFeed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenUrl,
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OPEN PAGE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                FilledTonalButton(
                    onClick = { onNavigate("facebook") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Facebook Tool", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate("shorts_reels") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create FB Reel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onConnectPlatform,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Text(if (settings.isFacebookConnected) "Manage Page" else "Connect Page", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InstagramPlatformDetailCard(
    settings: CreatorSettingsEntity,
    isKn: Boolean,
    uploadedCount: Int,
    onOpenUrl: () -> Unit,
    onNavigate: (String) -> Unit,
    onConnectPlatform: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("platform_card_instagram"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, InstagramGradientPink.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon, Title & Connection Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(InstagramGradientPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Instagram Main Profile",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = InstagramGradientPink,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "${settings.instagramHandle} • Creator Profile",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (settings.isInstagramConnected) Color(0xFF2EA44F).copy(alpha = 0.15f) else StudioAmber.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (settings.isInstagramConnected) Color(0xFF2EA44F) else StudioAmber)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isInstagramConnected) "CONNECTED" else "NOT CONNECTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (settings.isInstagramConnected) Color(0xFF2EA44F) else StudioAmber
                        )
                    }
                }
            }

            // Profile Details Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Handle: ${settings.instagramHandle} (Reels & Feed)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "URL: ${settings.instagramUrl}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Official Profile",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = InstagramGradientPink
                    )
                }
            }

            // Metrics Grid (3 stats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformMetricBox(
                    title = "Followers",
                    value = formatNumber(settings.instagramFollowers),
                    color = InstagramGradientPink,
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                PlatformMetricBox(
                    title = "Reels Views",
                    value = "92.8K",
                    color = StudioAmber,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
                PlatformMetricBox(
                    title = "Published Reels",
                    value = "$uploadedCount Reels",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Smartphone,
                    modifier = Modifier.weight(1f)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onOpenUrl,
                    colors = ButtonDefaults.buttonColors(containerColor = InstagramGradientPink),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OPEN INSTAGRAM", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                FilledTonalButton(
                    onClick = { onNavigate("instagram") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Instagram Tool", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate("shorts_reels") },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create Reel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onConnectPlatform,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Text(if (settings.isInstagramConnected) "Manage Profile" else "Connect Account", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PlatformMetricBox(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CombinedAudienceCard(
    settings: CreatorSettingsEntity,
    totalJobs: Int,
    isKn: Boolean,
    onOpenStudioApp: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAudience = settings.youtubeSubscribers + settings.facebookFollowers + settings.instagramFollowers
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_combined_audience_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isKn) "ಒಟ್ಟು ಸಾಮಾಜಿಕ ಪ್ರೇಕ್ಷಕರು" else "TOTAL MULTI-PLATFORM AUDIENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatNumber(totalAudience)} Audience",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Across YouTube, Facebook & Instagram • ${settings.engagementRate}% Engagement",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onOpenStudioApp,
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("STUDIO APP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LinkedChannelMiniChip(
    platformName: String,
    isConnected: Boolean,
    identifier: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isConnected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isConnected) color.copy(alpha = 0.4f) else Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = platformName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isConnected) color else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF10B981) else Color(0xFF9CA3AF))
                )
            }
            Text(
                text = identifier,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = if (isConnected) "Connected" else "Not Linked",
                fontSize = 8.sp,
                color = if (isConnected) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

