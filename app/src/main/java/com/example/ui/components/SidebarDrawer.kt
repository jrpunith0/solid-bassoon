package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.I18n
import com.example.ui.theme.*

data class NavItem(
    val id: String,
    val titleKey: String,
    val icon: ImageVector,
    val badge: String? = null
)

val AllNavItems = listOf(
    NavItem("dashboard", "nav_dashboard", Icons.Default.Dashboard),
    NavItem("cross_platform_hub", "nav_cross_platform_hub", Icons.Default.Devices, "ALL 3"),
    NavItem("ai_prompt_studio", "nav_ai_prompt_studio", Icons.Default.Psychology, "MULTI-AI"),
    NavItem("story_to_video", "nav_story_to_video", Icons.Default.AutoAwesomeMotion, "AI PIPELINE"),
    NavItem("cartoon_studio", "nav_cartoon_studio", Icons.Default.Animation, "CARTOON"),
    NavItem("auto_generator", "nav_auto_generator", Icons.Default.Bolt, "10-MIN"),
    NavItem("create_video", "nav_create_video", Icons.Default.MovieCreation),
    NavItem("shorts_reels", "nav_shorts_reels", Icons.Default.Smartphone),
    NavItem("script_gen", "nav_script_gen", Icons.Default.Create),
    NavItem("thumbnail_creator", "nav_thumbnail_creator", Icons.Default.Image),
    NavItem("publishing_centre", "nav_publishing_centre", Icons.Default.CloudUpload, "PUBLISH"),
    NavItem("youtube", "nav_youtube", Icons.Default.PlayCircleFilled),
    NavItem("instagram", "nav_instagram", Icons.Default.CameraAlt),
    NavItem("facebook", "nav_facebook", Icons.Default.ThumbUp),
    NavItem("projects", "nav_projects", Icons.Default.FolderSpecial),
    NavItem("calendar", "nav_calendar", Icons.Default.CalendarMonth),
    NavItem("media", "nav_media", Icons.Default.PhotoLibrary),
    NavItem("analytics", "nav_analytics", Icons.Default.BarChart),
    NavItem("ai_tools", "nav_ai_tools", Icons.Default.AutoAwesome),
    NavItem("social_channels", "nav_social_channels", Icons.Default.Share),
    NavItem("video_editor", "nav_video_editor", Icons.Default.MovieFilter, "AI + PLAYER"),
    NavItem("opencreator_hub", "nav_opencreator_hub", Icons.Default.Hub, "28 PRESETS"),
    NavItem("manga_studio", "nav_manga_studio", Icons.Default.AutoStories, "MANGA + AI"),
    NavItem("github_link", "nav_github_link", Icons.Default.Code, "GIT SYNC"),
    NavItem("api_settings", "nav_api_settings", Icons.Default.Key, "API KEYS"),
    NavItem("backup_restore", "nav_backup_restore", Icons.Default.Backup),
    NavItem("settings", "nav_settings", Icons.Default.Settings)
)

@Composable
fun SidebarDrawerContent(
    currentRoute: String,
    currentLanguage: String,
    onSelectRoute: (String) -> Unit,
    onOpenUserLogin: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    creatorName: String = "Pro Creator",
    creatorEmail: String = "creator@gmail.com",
    isPermanent: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isPermanent) {
        Surface(
            modifier = modifier.width(260.dp).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            SidebarInnerContent(
                currentRoute = currentRoute,
                currentLanguage = currentLanguage,
                onSelectRoute = onSelectRoute,
                onOpenUserLogin = onOpenUserLogin,
                onCloseDrawer = onCloseDrawer,
                creatorName = creatorName,
                creatorEmail = creatorEmail,
                isPermanent = true
            )
        }
    } else {
        ModalDrawerSheet(
            modifier = modifier.width(300.dp),
            drawerContainerColor = MaterialTheme.colorScheme.surface
        ) {
            SidebarInnerContent(
                currentRoute = currentRoute,
                currentLanguage = currentLanguage,
                onSelectRoute = onSelectRoute,
                onOpenUserLogin = onOpenUserLogin,
                onCloseDrawer = onCloseDrawer,
                creatorName = creatorName,
                creatorEmail = creatorEmail,
                isPermanent = false
            )
        }
    }
}

@Composable
private fun SidebarInnerContent(
    currentRoute: String,
    currentLanguage: String,
    onSelectRoute: (String) -> Unit,
    onOpenUserLogin: () -> Unit,
    onCloseDrawer: () -> Unit,
    creatorName: String,
    creatorEmail: String,
    isPermanent: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
            // Header with Account Profile Chip & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CREATOR STUDIO",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = creatorName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCrimson
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenUserLogin,
                        modifier = Modifier.size(32.dp).testTag("drawer_profile_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Login",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!isPermanent) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier.size(32.dp).testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Nav List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(AllNavItems) { item ->
                    val isSelected = currentRoute == item.id
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable { onSelectRoute(item.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("nav_${item.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = I18n.t(item.titleKey, currentLanguage),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.badge != null) {
                            Text(
                                text = item.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioCrimson
                            )
                        }
                    }
                }
            }

            // Footer
            Text(
                text = "v2.0 • Offline Ready Room DB",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
