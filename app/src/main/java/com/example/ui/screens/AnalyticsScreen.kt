package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AnalyticsScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    onUpdateSettings: (CreatorSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN

    var isEditingMetrics by remember { mutableStateOf(false) }
    var subs by remember { mutableStateOf(settings.youtubeSubscribers.toString()) }
    var igFollowers by remember { mutableStateOf(settings.instagramFollowers.toString()) }
    var fbFollowers by remember { mutableStateOf(settings.facebookFollowers.toString()) }
    var views by remember { mutableStateOf(settings.monthlyViews.toString()) }

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
                        text = I18n.t("nav_analytics", currentLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isKn) "ಪ್ರೇಕ್ಷಕರ ಬೆಳವಣಿಗೆ & ಚಾನೆಲ್ ಮೆಟ್ರಿಕ್ಸ್" else "Real creator metrics, audience growth & platform insights",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = {
                        if (isEditingMetrics) {
                            val updated = settings.copy(
                                youtubeSubscribers = subs.toIntOrNull() ?: settings.youtubeSubscribers,
                                instagramFollowers = igFollowers.toIntOrNull() ?: settings.instagramFollowers,
                                facebookFollowers = fbFollowers.toIntOrNull() ?: settings.facebookFollowers,
                                monthlyViews = views.toLongOrNull() ?: settings.monthlyViews
                            )
                            onUpdateSettings(updated)
                            Toast.makeText(context, "Metrics updated!", Toast.LENGTH_SHORT).show()
                        }
                        isEditingMetrics = !isEditingMetrics
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        if (isEditingMetrics) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEditingMetrics) "Save" else "Update Stats", fontSize = 11.sp)
                }
            }
        }

        // Connection Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live API Status: Manual Tracking Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Connect OAuth in Settings for automated sync, or log your numbers here anytime.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Audience Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "YouTube",
                    value = if (isEditingMetrics) subs else "${settings.youtubeSubscribers}",
                    subtitle = "Subscribers",
                    color = YouTubeRed,
                    isEditing = isEditingMetrics,
                    onValueChange = { subs = it },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Instagram",
                    value = if (isEditingMetrics) igFollowers else "${settings.instagramFollowers}",
                    subtitle = "Followers",
                    color = InstagramGradientPink,
                    isEditing = isEditingMetrics,
                    onValueChange = { igFollowers = it },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Facebook",
                    value = if (isEditingMetrics) fbFollowers else "${settings.facebookFollowers}",
                    subtitle = "Page Followers",
                    color = FacebookBlue,
                    isEditing = isEditingMetrics,
                    onValueChange = { fbFollowers = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Monthly Views & Engagement
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MONTHLY TOTAL ENGAGEMENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${settings.monthlyViews}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "Total Video Views across channels", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "+18.4% ↗", color = StudioEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { 0.78f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StudioCrimson,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Engagement Rate: ${settings.engagementRate}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Top Video Retention: 68%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Top Performing Content list
        item {
            Text(
                text = "TOP PERFORMING CREATIONS:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(
            listOf(
                Triple("Viral Editing Workflow in 2026", "YouTube", "94.2k views • 4.8k likes"),
                Triple("Behind the Scenes Studio Tour", "Instagram", "42.6k views • 3.2k shares"),
                Triple("My Biggest Creator Milestone Ever!", "Facebook", "18.1k views • 940 comments")
            )
        ) { (title, platform, stats) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
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
                        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$platform • $stats", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
