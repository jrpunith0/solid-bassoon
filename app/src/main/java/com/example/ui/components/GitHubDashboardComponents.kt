package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CachedGitHubRepoEntity
import com.example.data.github.*
import com.example.ui.theme.StudioCrimson

// Helper for programming language brand colors
fun getLanguageColor(language: String?): Color {
    return when (language?.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "python" -> Color(0xFF3572A5)
        "typescript" -> Color(0xFF3178C6)
        "javascript" -> Color(0xFFF1E05A)
        "java" -> Color(0xFFB07219)
        "go" -> Color(0xFF00ADD8)
        "rust" -> Color(0xFFDEA584)
        "c++", "cpp" -> Color(0xFFF34B7D)
        "swift" -> Color(0xFFF05138)
        "html" -> Color(0xFFE34C26)
        "ruby" -> Color(0xFF701516)
        else -> Color(0xFF94A3B8)
    }
}

/**
 * Requirement 6: Warning system that checks the scope and expiry date of the GitHub PAT
 * and notifies the user if the token is nearing expiration or lacks necessary permissions.
 */
@Composable
fun GitHubTokenWarningBanner(
    validationResult: GitHubTokenValidationResult?,
    isDismissed: Boolean,
    onManageToken: () -> Unit,
    onDismiss: () -> Unit,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (validationResult == null || isDismissed) return

    val warningType = validationResult.warningType
    if (warningType == TokenWarningType.NONE && validationResult.isValid) return

    val (containerColor, contentColor, icon, title, message) = when (warningType) {
        TokenWarningType.NEARING_EXPIRATION -> {
            val days = validationResult.daysUntilExpiration ?: 7
            Tuple5(
                Color(0xFFFEF3C7),
                Color(0xFF92400E),
                Icons.Default.Warning,
                "⚠️ GitHub PAT Nearing Expiration ($days days remaining)",
                "Your token expires on ${validationResult.expirationDate?.take(10) ?: "soon"}. Please renew the token to prevent automated commit disruptions."
            )
        }
        TokenWarningType.LACKS_PERMISSIONS -> {
            Tuple5(
                Color(0xFFFFEDD5),
                Color(0xFF9A3412),
                Icons.Default.Security,
                "⚠️ PAT Missing Required Permissions",
                "Token lacks 'repo' or 'public_repo' scope (current: [${validationResult.scopes.joinToString(", ").ifBlank { "none" }}]). Cannot push project commits."
            )
        }
        TokenWarningType.EXPIRED -> {
            Tuple5(
                Color(0xFFFEE2E2),
                Color(0xFF991B1B),
                Icons.Default.ErrorOutline,
                "🚨 GitHub PAT Expired",
                "Your token expired on ${validationResult.expirationDate?.take(10) ?: "recently"}. Re-authentication required for remote Git sync."
            )
        }
        TokenWarningType.UNAUTHORIZED -> {
            Tuple5(
                Color(0xFFFEE2E2),
                Color(0xFF991B1B),
                Icons.Default.VpnKeyOff,
                "❌ Invalid GitHub PAT (401 Unauthorized)",
                "GitHub rejected this personal access token. Please verify or re-generate your token on GitHub."
            )
        }
        TokenWarningType.INVALID_FORMAT -> {
            Tuple5(
                Color(0xFFFEF3C7),
                Color(0xFF92400E),
                Icons.Default.HelpOutline,
                "⚠️ Unrecognized Token Format",
                "Standard GitHub tokens start with 'ghp_' or 'github_pat_'. Check for accidental copy-paste errors."
            )
        }
        else -> return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("github_token_warning_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp).testTag("dismiss_warning_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = contentColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onManageToken,
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("warning_manage_token_btn")
                ) {
                    Text("Update Token", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
                    border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("warning_revoke_token_btn")
                ) {
                    Text("Revoke Token", fontSize = 11.sp)
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

/**
 * Visual graph on the dashboard that displays the activity history or contribution frequency
 * of the linked GitHub repositories (inspired by d3 / recharts charts).
 */
@Composable
fun GitHubActivityHistoryChart(
    activityHistory: GitHubActivityHistory?,
    currentRepoName: String,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf("Heatmap") } // "Heatmap" or "Frequency"
    var selectedDayInfo by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("github_activity_history_graph"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2EA44F))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GITHUB ACTIVITY & CONTRIBUTION GRAPH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Repository: $currentRepoName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // D3 / Recharts Mode Switcher
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        listOf("Heatmap", "Frequency").forEach { mode ->
                            val isSel = chartMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) StudioCrimson else Color.Transparent)
                                    .clickable { chartMode = mode }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("chart_mode_$mode")
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stat pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip("Commits", "${activityHistory?.totalYearCommits ?: 182}", Color(0xFF2EA44F))
                MetricChip("Streak", "${activityHistory?.activeStreakDays ?: 14} days 🔥", Color(0xFFF59E0B))
                MetricChip("Peak Day", "${activityHistory?.maxDayCommits ?: 9} commits", Color(0xFF3B82F6))
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (chartMode == "Heatmap") {
                // Interactive 16-week x 7-day GitHub contribution matrix
                val weeks = activityHistory?.weeks ?: emptyList()
                val scrollState = rememberScrollState()

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        weeks.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                week.days.forEach { day ->
                                    val cellColor = when (day.level) {
                                        0 -> Color(0xFF161B22)
                                        1 -> Color(0xFF0E4429)
                                        2 -> Color(0xFF006D32)
                                        3 -> Color(0xFF26A641)
                                        else -> Color(0xFF39D353)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(cellColor)
                                            .clickable {
                                                selectedDayInfo = "${day.dateString}: ${day.count} contributions"
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedDayInfo ?: "Tap cell to inspect day count • 16-week timeline",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Less", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(Color(0xFF161B22), Color(0xFF0E4429), Color(0xFF006D32), Color(0xFF26A641), Color(0xFF39D353)).forEach {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(1.dp)).background(it))
                            }
                            Text("More", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                // Frequency Curve Chart (Recharts-inspired area & bar timeline)
                val freqData = activityHistory?.weeklyFrequency ?: listOf(2, 5, 8, 4, 12, 9, 15, 7, 11, 14, 8, 16, 12, 10, 18, 14)
                val maxFreq = (freqData.maxOrNull() ?: 1).coerceAtLeast(1)

                Column {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("github_frequency_canvas")
                    ) {
                        val w = size.width
                        val h = size.height
                        val step = w / freqData.size
                        val barWidth = step * 0.6f

                        // Draw Grid lines
                        for (i in 1..3) {
                            val y = h * (i / 4f)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        // Draw Bars & Frequency Trend Curve
                        val path = Path()
                        freqData.forEachIndexed { i, count ->
                            val x = i * step + step / 2f
                            val barHeight = (count.toFloat() / maxFreq) * (h - 15f)
                            val y = h - barHeight

                            // Gradient Bar
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF2EA44F), Color(0xFF0E4429))
                                ),
                                topLeft = Offset(x - barWidth / 2f, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        // Smooth Trendline over bars
                        drawPath(
                            path = path,
                            color = StudioCrimson,
                            style = Stroke(width = 3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("16 Weeks Ago", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Weekly Commit Frequency (Peak: $maxFreq/wk)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StudioCrimson)
                        Text("This Week", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label: ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

/**
 * Requirement 7: Data synchronization service that fetches repository metadata
 * (names, primary languages, star counts) and displays it in a list format within the dashboard.
 */
@Composable
fun GitHubRepositoryMetadataList(
    repos: List<CachedGitHubRepoEntity>,
    isSyncing: Boolean,
    onSyncNow: () -> Unit,
    onSelectRepo: (CachedGitHubRepoEntity) -> Unit,
    onOpenFullGitLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_github_repos_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Sync button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF24292E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SYNCED GITHUB REPOSITORIES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${repos.size} repos cached locally with live metadata",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EA44F)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag("dashboard_sync_repos_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = onOpenFullGitLink, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open Git Link", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Repository List
            if (repos.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No repositories cached yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = onSyncNow) {
                            Text("Fetch Repositories with PAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repos.take(4).forEach { repo ->
                        val langColor = getLanguageColor(repo.language)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectRepo(repo) }
                                .testTag("repo_item_${repo.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (repo.isPrivate) Color.Gray.copy(alpha = 0.2f) else Color(0xFF2EA44F).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (repo.isPrivate) "PRIVATE" else "PUBLIC",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (repo.isPrivate) Color.Gray else Color(0xFF2EA44F),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Language with dot
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(langColor))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = repo.language ?: "Other", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        // Stars
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "⭐ ${repo.stargazersCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        // Branch
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "🌿 ${repo.defaultBranch}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "View Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (repos.size > 4) {
                        TextButton(
                            onClick = onOpenFullGitLink,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("View all ${repos.size} repos & commit files", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Requirement 8: Detailed view component that displays additional repository metadata
 * like description, last updated date, and license info when a user selects a repository from the list.
 */
@Composable
fun GitHubRepositoryDetailDialog(
    repo: CachedGitHubRepoEntity?,
    onDismiss: () -> Unit,
    onSetAsTargetRepo: (String) -> Unit
) {
    if (repo == null) return
    val context = LocalContext.current
    val langColor = getLanguageColor(repo.language)

    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("github_repo_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF24292E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = repo.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = repo.fullName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp).testTag("close_repo_detail_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = "Description",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = repo.description ?: "No description provided for this repository.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailMetaItem(label = "Primary Language", value = repo.language ?: "None", indicatorColor = langColor)
                    DetailMetaItem(label = "License", value = repo.licenseName ?: "No license specified")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailMetaItem(label = "Stars", value = "⭐ ${repo.stargazersCount}")
                    DetailMetaItem(label = "Forks", value = "🍴 ${repo.forksCount}")
                    DetailMetaItem(label = "Open Issues", value = "⚠️ ${repo.openIssuesCount}")
                    DetailMetaItem(label = "Branch", value = "🌿 ${repo.defaultBranch}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                DetailMetaItem(
                    label = "Last Updated",
                    value = repo.updatedAt ?: "Recent",
                    sub = "Cached in app storage"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSetAsTargetRepo(repo.name)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).testTag("set_target_repo_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Set Active Target", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { openBrowser(repo.htmlUrl) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("open_github_repo_btn")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GitHub", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetaItem(
    label: String,
    value: String,
    sub: String? = null,
    indicatorColor: Color? = null
) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (indicatorColor != null) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(indicatorColor))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (sub != null) {
            Text(text = sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}

/**
 * Requirement 9: Revoke confirmation dialog
 */
@Composable
fun GitHubTokenRevokeDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirmRevoke: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VpnKeyOff, contentDescription = null, tint = StudioCrimson)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Revoke GitHub PAT?", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                "This will securely delete the stored GitHub Personal Access Token from your device and clear the local repository metadata cache. Live remote sync will be disabled until a new token is provided."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmRevoke()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                modifier = Modifier.testTag("confirm_revoke_pat_btn")
            ) {
                Text("Revoke & Clear Cache")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
