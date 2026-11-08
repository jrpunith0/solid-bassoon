package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.github.GitHubTokenValidationResult
import com.example.data.github.TokenWarningType

@Composable
fun GitHubPATWarningBanner(
    validation: GitHubTokenValidationResult?,
    onOpenManageToken: () -> Unit,
    onRevokeToken: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (validation == null || validation.warningType == TokenWarningType.NONE) {
        return
    }

    var isDismissed by remember(validation) { mutableStateOf(false) }
    if (isDismissed) return

    val context = LocalContext.current

    val isError = validation.warningType == TokenWarningType.EXPIRED || validation.warningType == TokenWarningType.UNAUTHORIZED
    val bannerColor = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFF3CD)
    val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF856404)
    val borderColor = if (isError) MaterialTheme.colorScheme.error else Color(0xFFFFEEBA)

    val title = when (validation.warningType) {
        TokenWarningType.NEARING_EXPIRATION -> "⚠️ GitHub PAT Nearing Expiration"
        TokenWarningType.LACKS_PERMISSIONS -> "⚠️ GitHub PAT Missing Required Scopes"
        TokenWarningType.EXPIRED -> "⛔ GitHub PAT Has Expired"
        TokenWarningType.UNAUTHORIZED -> "⛔ GitHub PAT Unauthorized / Revoked"
        TokenWarningType.INVALID_FORMAT -> "⚠️ Invalid GitHub PAT Format"
        TokenWarningType.NONE -> ""
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bannerColor,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("github_pat_warning_banner")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Error else Icons.Default.Warning,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                }

                IconButton(
                    onClick = { isDismissed = true },
                    modifier = Modifier.size(24.dp).testTag("github_pat_warning_dismiss_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss Banner", tint = contentColor, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = validation.warningMessage,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = contentColor
            )

            // Expiration and scopes breakdown
            if (validation.expirationDate != null || validation.scopes.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (validation.expirationDate != null) {
                            Text(
                                text = "• Expiry Date: ${validation.expirationDate}" +
                                        (if (validation.daysUntilExpiration != null) " (${validation.daysUntilExpiration} days remaining)" else ""),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor
                            )
                        }
                        if (validation.scopes.isNotEmpty()) {
                            Text(
                                text = "• Granted Scopes: [${validation.scopes.joinToString(", ")}]",
                                fontSize = 11.sp,
                                color = contentColor
                            )
                        }
                        if (!validation.hasRepoScope && !validation.hasPublicRepoScope) {
                            Text(
                                text = "• Required Scopes: 'repo' (Full control) or 'public_repo'",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isError) MaterialTheme.colorScheme.error else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // Quick Actions: Renew on GitHub, Manage in App, Revoke
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val url = "https://github.com/settings/tokens/new?scopes=repo&description=CreatorStudioSync"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isError) MaterialTheme.colorScheme.error else Color(0xFF24292E)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("github_pat_renew_button")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isError) "Generate New PAT" else "Renew on GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onOpenManageToken,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("github_pat_manage_button")
                ) {
                    Text("Manage PAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (onRevokeToken != null) {
                    OutlinedButton(
                        onClick = onRevokeToken,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("github_pat_revoke_warning_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revoke", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
