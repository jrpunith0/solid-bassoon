package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.I18n
import com.example.ui.theme.*

@Composable
fun QuickActionsBar(
    currentLanguage: String,
    onOpenScriptGenerator: () -> Unit,
    onOpenCaptionGenerator: () -> Unit,
    onOpenHashtagGenerator: () -> Unit,
    onOpenGitHubSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_actions_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioCrimson)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = I18n.t("quick_actions", currentLanguage).uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = if (currentLanguage == I18n.LANG_KN) "1-ಟ್ಯಾಪ್ ಪ್ರವೇಶ" else "1-Click Access",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Action 1: Script Generator
                QuickActionButton(
                    icon = Icons.Default.Create,
                    label = I18n.t("quick_script", currentLanguage),
                    gradient = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                    testTag = "quick_action_script",
                    onClick = onOpenScriptGenerator,
                    modifier = Modifier.weight(1f)
                )

                // Quick Action 2: Caption Generator
                QuickActionButton(
                    icon = Icons.Default.EditNote,
                    label = I18n.t("quick_caption", currentLanguage),
                    gradient = listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
                    testTag = "quick_action_caption",
                    onClick = onOpenCaptionGenerator,
                    modifier = Modifier.weight(1f)
                )

                // Quick Action 3: Hashtag Generator
                QuickActionButton(
                    icon = Icons.Default.Tag,
                    label = I18n.t("quick_hashtags", currentLanguage),
                    gradient = listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
                    testTag = "quick_action_hashtags",
                    onClick = onOpenHashtagGenerator,
                    modifier = Modifier.weight(1f)
                )

                // Quick Action 4: GitHub Sync
                QuickActionButton(
                    icon = Icons.Default.Code,
                    label = if (currentLanguage == I18n.LANG_KN) "ಗಿಟ್‌ಹಬ್ ಸಿಂಕ್" else "GitHub Sync",
                    gradient = listOf(Color(0xFF24292E), Color(0xFF1E293B)),
                    testTag = "quick_action_github",
                    onClick = onOpenGitHubSync,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    gradient: List<Color>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}
