package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.data.github.GitHubRepo
import com.example.data.github.GitHubRepository
import com.example.data.github.GitHubSyncResult
import com.example.data.github.GitHubUser
import com.example.data.github.GitHubTokenValidationResult
import com.example.i18n.I18n
import com.example.ui.components.GitHubPATWarningBanner
import com.example.ui.components.GitHubRepoDetailDialog
import com.example.ui.components.GitHubFileExplorerComponent
import com.example.ui.components.VideoGenerationProgressIndicator
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GitHubLinkScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    projects: List<ContentProjectEntity>,
    calendarTasks: List<CalendarTaskEntity>,
    savedScripts: List<SavedScriptEntity>,
    onSaveSettings: (CreatorSettingsEntity) -> Unit,
    onRevokeGitHubPAT: () -> Unit = {},
    gitHubTokenWarning: GitHubTokenValidationResult? = null,
    isBatchReprocessing: Boolean = false,
    batchReprocessProgress: Float = 0f,
    batchReprocessStatus: String = "",
    onReprocessAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val isKn = currentLanguage == I18n.LANG_KN
    val gitHubRepoManager = remember { GitHubRepository() }

    var usernameInput by remember { mutableStateOf(settings.gitHubUsername) }
    var repoInput by remember { mutableStateOf(settings.gitHubRepo) }
    var branchInput by remember { mutableStateOf(settings.gitHubBranch) }
    var tokenInput by remember { mutableStateOf(settings.gitHubToken) }
    var isTokenMasked by remember { mutableStateOf(true) }
    var showRevokeConfirmation by remember { mutableStateOf(false) }
    var selectedRepoForDetail by remember { mutableStateOf<CachedGitHubRepoEntity?>(null) }
    var localTokenWarning by remember { mutableStateOf(gitHubTokenWarning) }

    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("") }
    var syncResult by remember { mutableStateOf<GitHubSyncResult?>(null) }
    var userProfile by remember { mutableStateOf<GitHubUser?>(null) }
    var userRepos by remember { mutableStateOf<List<GitHubRepo>>(emptyList()) }
    var isLoadingProfile by remember { mutableStateOf(false) }

    // Initial load of profile & repos & token validation
    LaunchedEffect(settings.gitHubUsername, settings.gitHubToken) {
        isLoadingProfile = true
        if (settings.gitHubToken.isNotBlank()) {
            localTokenWarning = gitHubRepoManager.validateToken(settings.gitHubToken, settings.gitHubUsername)
        } else {
            localTokenWarning = null
        }
        gitHubRepoManager.fetchUserProfile(settings.gitHubUsername, settings.gitHubToken)
            .onSuccess { userProfile = it }
        gitHubRepoManager.fetchUserRepos(settings.gitHubUsername, settings.gitHubToken)
            .onSuccess { userRepos = it }
        isLoadingProfile = false
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSaveConfig() {
        val updated = settings.copy(
            isGitHubConnected = true,
            gitHubUsername = usernameInput.trim(),
            gitHubRepo = repoInput.trim(),
            gitHubBranch = branchInput.trim(),
            gitHubToken = tokenInput.trim()
        )
        onSaveSettings(updated)
        Toast.makeText(context, "GitHub configuration saved!", Toast.LENGTH_SHORT).show()
        scope.launch {
            isLoadingProfile = true
            gitHubRepoManager.fetchUserProfile(updated.gitHubUsername, updated.gitHubToken)
                .onSuccess { userProfile = it }
            gitHubRepoManager.fetchUserRepos(updated.gitHubUsername, updated.gitHubToken)
                .onSuccess { userRepos = it }
            isLoadingProfile = false
        }
    }

    fun handleSyncAllData() {
        scope.launch {
            isSyncing = true
            syncStatusText = "Generating creator repository package..."

            // Compile all app data into Markdown format
            val markdownBuilder = StringBuilder()
            markdownBuilder.append("# Creator Studio Backup & Data Sync\n")
            markdownBuilder.append("Auto-synced from Creator Studio Android App on ${java.util.Date()}\n\n")

            markdownBuilder.append("## Linked Creator Profile\n")
            markdownBuilder.append("- Channel: ${settings.channelName}\n")
            markdownBuilder.append("- Google Account: ${settings.googleAccountName} (${settings.youTubeEmail})\n")
            markdownBuilder.append("- YouTube Handle: ${settings.youTubeHandle}\n")
            markdownBuilder.append("- Instagram: ${settings.instagramHandle}\n\n")

            markdownBuilder.append("## Content Projects (${projects.size})\n")
            projects.forEach { p ->
                markdownBuilder.append("### ${p.title} [${p.format} - ${p.platform}]\n")
                markdownBuilder.append("- Status: ${p.status}\n")
                markdownBuilder.append("- Hook: ${p.hookText}\n")
                markdownBuilder.append("- Script: ${p.scriptText.take(200)}...\n\n")
            }

            markdownBuilder.append("## Content Calendar Tasks (${calendarTasks.size})\n")
            calendarTasks.forEach { t ->
                markdownBuilder.append("- [${t.status}] **${t.title}** (${t.platform} - ${t.scheduledDate} ${t.scheduledTime})\n")
            }

            val timestamp = System.currentTimeMillis()
            val fileName = "content_studio_sync_${timestamp}.md"
            val path = "creator-studio/$fileName"

            val result = gitHubRepoManager.syncProjectToRepo(
                owner = usernameInput.trim(),
                repo = repoInput.trim(),
                branch = branchInput.trim(),
                path = path,
                content = markdownBuilder.toString(),
                commitMessage = "feat: Auto-sync Creator Studio data ($fileName)",
                token = tokenInput.trim().ifBlank { null }
            )

            isSyncing = false
            syncResult = result

            if (result.success) {
                val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                onSaveSettings(settings.copy(gitHubLastSync = now))
                Toast.makeText(context, "App data synced to GitHub repository!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Sync notice: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("github_link_screen"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24292E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isKn) "ಗಿಟ್‌ಹಬ್ ಡೇಟಾ ಲಿಂಕ್" else "GitHub Data Link",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isKn) "ನಿಮ್ಮ ಅಪ್ಲಿಕೇಶನ್ ಯೋಜನೆಗಳು ಮತ್ತು ಸ್ಕ್ರಿಪ್ಟ್‌ಗಳನ್ನು GitHub ನೊಂದಿಗೆ ಸಿಂಕ್ ಮಾಡಿ" else "Sync video scripts, prompts & calendar data directly to your GitHub repo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (settings.isGitHubConnected) Color(0xFF2EA44F).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (settings.isGitHubConnected) "LINKED" else "UNLINKED",
                        color = if (settings.isGitHubConnected) Color(0xFF2EA44F) else MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Batch Reprocess All Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("github_batch_reprocess_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKn) "ಎಲ್ಲಾ ಕಂಟೆಂಟ್ ಮರುಸಂಸ್ಕರಣೆ (Reprocess All)" else "BATCH REPROCESS ALL",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Regenerate scripts, scene metadata & sync full catalog to GitHub",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onReprocessAll,
                            enabled = !isBatchReprocessing,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("github_reprocess_all_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBatchReprocessing) "Processing..." else "Reprocess All",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isBatchReprocessing) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { batchReprocessProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = StudioCrimson
                            )
                            Text(
                                text = batchReprocessStatus.ifBlank { "Reprocessing videos and scripts..." },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // GitHub PAT Warning Banner (Prompt 6)
        if (localTokenWarning != null) {
            item {
                GitHubPATWarningBanner(
                    validation = localTokenWarning,
                    onOpenManageToken = { },
                    onRevokeToken = { showRevokeConfirmation = true }
                )
            }
        }

        // Circular progress indicator during sync
        item {
            VideoGenerationProgressIndicator(
                isVisible = isSyncing,
                statusText = syncStatusText,
                stepText = "Pushing markdown packages & scripts to GitHub...",
                actionType = "upload",
                onCancel = { isSyncing = false }
            )
        }

        // Profile Details Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("github_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF24292E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userProfile?.name ?: settings.googleAccountName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@${userProfile?.login ?: settings.gitHubUsername}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { openUrl("https://github.com/${settings.gitHubUsername}") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("github_view_profile_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Profile", fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = userProfile?.bio ?: settings.gitHubBio,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${userProfile?.publicRepos ?: settings.gitHubPublicRepos}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Public Repos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${userProfile?.followers ?: 42}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Followers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = settings.gitHubLastSync.take(10), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Last Synced", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Featured AI YouTube Shorts Generator Card (Anil-matcha/AI-Youtube-Shorts-Generator)
        item {
            val isAnilLinked = usernameInput.equals(GitHubRepository.ANIL_MATCHA_DEFAULT_OWNER, ignoreCase = true) &&
                    repoInput.equals(GitHubRepository.ANIL_MATCHA_DEFAULT_REPO, ignoreCase = true)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = if (isAnilLinked) BorderStroke(1.5.dp, StudioCrimson) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_anil_matcha_shorts_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(StudioCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MovieFilter,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AI Video Generator",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = StudioAmber
                                    ) {
                                        Text(
                                            "SHORTS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Anil-matcha/AI-Youtube-Shorts-Generator",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isAnilLinked) Color(0xFF2EA44F).copy(alpha = 0.15f) else StudioCrimson.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isAnilLinked) "TARGET LINKED" else "AI GENERATOR",
                                color = if (isAnilLinked) Color(0xFF2EA44F) else StudioCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Automated AI YouTube Shorts Generator: Automated prompt/text-to-video pipeline with AI speech voiceovers, styled auto-captions, and viral background video rendering.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = GitHubRepository.ANIL_MATCHA_REPO_URL,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(GitHubRepository.ANIL_MATCHA_REPO_URL))
                                    Toast.makeText(context, "URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                usernameInput = GitHubRepository.ANIL_MATCHA_DEFAULT_OWNER
                                repoInput = GitHubRepository.ANIL_MATCHA_DEFAULT_REPO
                                branchInput = "main"
                                handleSaveConfig()
                                Toast.makeText(context, "Linked Anil-matcha/AI-Youtube-Shorts-Generator!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAnilLinked) Color(0xFF2EA44F) else StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1.3f).testTag("github_link_anil_shorts_button")
                        ) {
                            Icon(if (isAnilLinked) Icons.Default.Check else Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAnilLinked) "Linked as Target" else "1-Click Link Repo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { openUrl(GitHubRepository.ANIL_MATCHA_REPO_URL) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).testTag("github_open_anil_shorts_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Featured Lanshu AI Presenter Video Repository Card (User Linked Repo)
        item {
            val isLanshuLinked = usernameInput.equals(GitHubRepository.LANSHU_DEFAULT_OWNER, ignoreCase = true) &&
                    repoInput.equals(GitHubRepository.LANSHU_DEFAULT_REPO, ignoreCase = true)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("github_lanshu_featured_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                    .background(StudioCrimson.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = StudioCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Lanshu AI Presenter Video",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "cclank/lanshu-create-ai-presenter-video",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isLanshuLinked) Color(0xFF2EA44F).copy(alpha = 0.15f) else StudioCrimson.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isLanshuLinked) "ACTIVE LINK" else "FEATURED REPO",
                                color = if (isLanshuLinked) Color(0xFF2EA44F) else StudioCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Automated AI digital presenter video synthesis, avatar animation, script-to-video workflow and speech sync.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = GitHubRepository.LANSHU_REPO_URL,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(GitHubRepository.LANSHU_REPO_URL))
                                    Toast.makeText(context, "URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                usernameInput = GitHubRepository.LANSHU_DEFAULT_OWNER
                                repoInput = GitHubRepository.LANSHU_DEFAULT_REPO
                                branchInput = "main"
                                handleSaveConfig()
                                Toast.makeText(context, "Linked cclank/lanshu-create-ai-presenter-video!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isLanshuLinked) Color(0xFF2EA44F) else StudioCrimson),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1.3f).testTag("github_link_lanshu_button")
                        ) {
                            Icon(if (isLanshuLinked) Icons.Default.Check else Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isLanshuLinked) "Linked as Target" else "1-Click Link Repo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { openUrl(GitHubRepository.LANSHU_REPO_URL) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).testTag("github_open_lanshu_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("github_config_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Repository & Credentials",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { input ->
                            val parsed = GitHubRepository.parseGitHubUrl(input)
                            if (parsed != null) {
                                usernameInput = parsed.first
                                repoInput = parsed.second
                                Toast.makeText(context, "Extracted: ${parsed.first}/${parsed.second}", Toast.LENGTH_SHORT).show()
                            } else {
                                usernameInput = input
                            }
                        },
                        label = { Text("GitHub Username / Organization") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("github_username_input"),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = repoInput,
                            onValueChange = { input ->
                                val parsed = GitHubRepository.parseGitHubUrl(input)
                                if (parsed != null) {
                                    usernameInput = parsed.first
                                    repoInput = parsed.second
                                    Toast.makeText(context, "Extracted: ${parsed.first}/${parsed.second}", Toast.LENGTH_SHORT).show()
                                } else {
                                    repoInput = input
                                }
                            },
                            label = { Text("Target Repository") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("github_repo_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = branchInput,
                            onValueChange = { branchInput = it },
                            label = { Text("Branch") },
                            modifier = Modifier.width(95.dp).testTag("github_branch_input"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Personal Access Token (PAT)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = if (tokenInput.isNotBlank()) Color(0xFF2EA44F) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isTokenMasked = !isTokenMasked },
                                    modifier = Modifier.testTag("github_token_mask_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (isTokenMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isTokenMasked) "Reveal Token" else "Mask Token",
                                        tint = if (isTokenMasked) MaterialTheme.colorScheme.primary else Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (tokenInput.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            tokenInput = ""
                                            Toast.makeText(context, "Token cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("github_clear_token_btn")
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Token", modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            val clip = clipboardManager.getText()?.text
                                            if (!clip.isNullOrBlank()) {
                                                tokenInput = clip.trim()
                                                Toast.makeText(context, "Token pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty. Copy your token first.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("github_paste_token_button")
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste Token", tint = StudioCrimson)
                                    }
                                }
                            }
                        },
                        visualTransformation = if (isTokenMasked) PasswordVisualTransformation() else VisualTransformation.None,
                        supportingText = {
                            if (tokenInput.isNotBlank()) {
                                Text(
                                    text = if (isTokenMasked) "🔒 Token is securely masked when not in use" else "⚠️ Plaintext visible • Mask before sharing screen",
                                    color = if (isTokenMasked) Color(0xFF2EA44F) else Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text("Leave blank for simulation, or paste your 'ghp_...' token for live commits")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("github_token_input"),
                        singleLine = true
                    )

                    // Security Setting: Mask Token Switch
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mask PAT for Security",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Obscures characters when not actively editing",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isTokenMasked,
                                onCheckedChange = { isTokenMasked = it },
                                modifier = Modifier.testTag("github_token_mask_switch")
                            )
                        }
                    }

                    // Quick Token Actions Row (Paste, Generate Link, Quick Demo)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    tokenInput = clip.trim()
                                    Toast.makeText(context, "Token pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Clipboard is empty. Copy your GitHub PAT first.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).testTag("github_paste_clipboard_action_btn")
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                openUrl("https://github.com/settings/tokens/new?scopes=repo&description=CreatorStudioSync")
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1.3f).testTag("github_generate_token_link_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate on GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                tokenInput = "ghp_creator_studio_punithjr2_token"
                                Toast.makeText(context, "Demo token applied for testing!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).testTag("github_use_demo_token_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons Row: Save & Sync Now, Save Config, Open Repo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                handleSaveConfig()
                                handleSyncAllData()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EA44F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).testTag("github_save_and_sync_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save & Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { handleSaveConfig() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("github_save_config_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { openUrl("https://github.com/${usernameInput.trim()}/${repoInput.trim()}") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("github_open_repo_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Repo", fontSize = 12.sp)
                        }
                    }

                    if (tokenInput.isNotBlank() || settings.isGitHubConnected) {
                        OutlinedButton(
                            onClick = { showRevokeConfirmation = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("github_revoke_token_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REVOKE GITHUB PAT & CLEAR LOCAL CACHE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Sync Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().testTag("github_sync_action_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Live status chip
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (tokenInput.isNotBlank()) Color(0xFF2EA44F).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = if (tokenInput.isNotBlank()) "🟢 LIVE REMOTE COMMIT READY" else "🟡 LOCAL SIMULATION (ADD TOKEN FOR REMOTE)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tokenInput.isNotBlank()) Color(0xFF2E7D32) else Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Push App Data to GitHub",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Commits ${projects.size} projects, ${calendarTasks.size} tasks, and scripts to $repoInput ($branchInput)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { handleSyncAllData() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            modifier = Modifier.testTag("github_push_data_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now")
                            }
                        }
                    }

                    if (syncResult != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (syncResult?.success == true) "✅ ${syncResult?.message}" else "❌ ${syncResult?.message}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (syncResult?.sha != null) {
                                    Text(
                                        text = "Commit SHA: ${syncResult?.sha}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (syncResult?.fileUrl != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { openUrl(syncResult?.fileUrl!!) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("github_view_committed_file_btn")
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View File on GitHub", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Repository File Explorer Component (PAT Validation, Search Bar, Breadcrumbs, File Extension Icons)
        item {
            GitHubFileExplorerComponent(
                owner = usernameInput.trim().ifBlank { settings.gitHubUsername },
                repo = repoInput.trim().ifBlank { settings.gitHubRepo },
                branch = branchInput.trim().ifBlank { settings.gitHubBranch },
                token = tokenInput.trim().ifBlank { settings.gitHubToken },
                tokenValidation = localTokenWarning,
                repositoryManager = gitHubRepoManager,
                isKannada = isKn
            )
        }

        // Public Repositories List
        item {
            Text(
                text = "Your GitHub Repositories",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(userRepos) { repo ->
            val isCurrent = repo.name.equals(repoInput, ignoreCase = true)
            val langColor = when (repo.language?.lowercase()) {
                "kotlin" -> Color(0xFF7F52FF)
                "typescript" -> Color(0xFF3178C6)
                "python" -> Color(0xFF3572A5)
                "javascript" -> Color(0xFFF7DF1E)
                "go" -> Color(0xFF00ADD8)
                "rust" -> Color(0xFFDEA584)
                else -> Color(0xFF6E7681)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp) else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isCurrent) 1.5.dp else 0.5.dp,
                        color = if (isCurrent) StudioCrimson else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        repoInput = repo.name
                        branchInput = repo.defaultBranch
                        handleSaveConfig()
                        selectedRepoForDetail = CachedGitHubRepoEntity(
                            id = repo.id,
                            name = repo.name,
                            fullName = repo.fullName,
                            description = repo.description,
                            htmlUrl = repo.htmlUrl,
                            language = repo.language ?: "Kotlin",
                            stargazersCount = repo.stargazersCount,
                            forksCount = repo.forksCount,
                            openIssuesCount = repo.openIssuesCount,
                            defaultBranch = repo.defaultBranch,
                            updatedAt = repo.updatedAt,
                            isPrivate = repo.private,
                            licenseName = repo.license?.name,
                            licenseSpdxId = repo.license?.spdxId,
                            size = repo.size
                        )
                    }
                    .testTag("github_repo_item_${repo.name}")
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
                                text = repo.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = StudioCrimson.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = StudioCrimson,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (!repo.description.isNullOrBlank()) {
                            Text(
                                text = repo.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // Primary Language Badge (Prompt 7)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(langColor))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = repo.language ?: "Code", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = "⭐ ${repo.stargazersCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "🍴 ${repo.forksCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "🌿 ${repo.defaultBranch}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(
                        onClick = {
                            selectedRepoForDetail = CachedGitHubRepoEntity(
                                id = repo.id,
                                name = repo.name,
                                fullName = repo.fullName,
                                description = repo.description,
                                htmlUrl = repo.htmlUrl,
                                language = repo.language ?: "Kotlin",
                                stargazersCount = repo.stargazersCount,
                                forksCount = repo.forksCount,
                                openIssuesCount = repo.openIssuesCount,
                                defaultBranch = repo.defaultBranch,
                                updatedAt = repo.updatedAt,
                                isPrivate = repo.private,
                                licenseName = repo.license?.name,
                                licenseSpdxId = repo.license?.spdxId,
                                size = repo.size
                            )
                        },
                        modifier = Modifier.testTag("github_repo_view_detail_${repo.name}")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "View Repo Details", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // Detailed View Component (Prompt 8)
    if (selectedRepoForDetail != null) {
        GitHubRepoDetailDialog(
            repo = selectedRepoForDetail,
            onDismiss = { selectedRepoForDetail = null },
            onSetAsActiveRepo = { name, branch ->
                repoInput = name
                branchInput = branch
                handleSaveConfig()
            }
        )
    }

    // Revoke Token Confirmation Dialog (Prompt 9)
    if (showRevokeConfirmation) {
        AlertDialog(
            onDismissRequest = { showRevokeConfirmation = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Revoke Stored GitHub PAT?") },
            text = {
                Text("This will immediately delete your GitHub Personal Access Token from secure storage, disconnect the GitHub sync integration, and clear all local cached repository metadata.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRevokeConfirmation = false
                        tokenInput = ""
                        onRevokeGitHubPAT()
                        val updated = settings.copy(
                            gitHubToken = "",
                            isGitHubConnected = false,
                            gitHubLastSync = "Token Revoked"
                        )
                        onSaveSettings(updated)
                        localTokenWarning = null
                        Toast.makeText(context, "GitHub PAT revoked and cache cleared.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("github_confirm_revoke_dialog")
                ) {
                    Text("Revoke Token & Clear Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
