package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.VideoMetadataEntity
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.YouTubeRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDraftsManagerDialog(
    videoDrafts: List<VideoMetadataEntity>,
    currentTopic: String,
    currentFormat: String,
    onSaveCurrentAsDraft: (VideoMetadataEntity) -> Unit,
    onLoadDraft: (VideoMetadataEntity) -> Unit,
    onUpdateDraftStatus: (Long, String) -> Unit,
    onDeleteDraft: (VideoMetadataEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var isCreatingNewDraft by remember { mutableStateOf(false) }

    // New Draft form fields
    var draftTitle by remember { mutableStateOf(currentTopic.ifBlank { "New Video Project" }) }
    var draftDescription by remember { mutableStateOf("AI-assisted video production with multi-scene storyboard.") }
    var draftTopic by remember { mutableStateOf(currentTopic) }
    var draftFormat by remember { mutableStateOf(currentFormat) }
    var draftResolution by remember { mutableStateOf("1080p") }
    var draftFps by remember { mutableIntStateOf(60) }
    var draftPlatform by remember { mutableStateOf(if (currentFormat.contains("9:16")) "Instagram" else "YouTube") }
    var draftStatus by remember { mutableStateOf("Draft") }
    var draftNotes by remember { mutableStateOf("") }

    val filterOptions = listOf("All", "Draft", "Editing", "Rendering", "Ready", "Published")

    val filteredList = remember(videoDrafts, selectedFilter) {
        if (selectedFilter == "All") videoDrafts
        else videoDrafts.filter { it.draftStatus.equals(selectedFilter, ignoreCase = true) }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp)
                .testTag("video_drafts_manager_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = StudioCrimson.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = StudioCrimson,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Video Metadata & Drafts",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Room Database (${videoDrafts.size} stored videos)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_drafts_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle between List and New Draft Form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { isCreatingNewDraft = false },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (!isCreatingNewDraft) StudioCrimson.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isCreatingNewDraft) StudioCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_view_drafts")
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Drafts & Metadata", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { isCreatingNewDraft = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCreatingNewDraft) StudioCrimson else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isCreatingNewDraft) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_save_current_draft")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save New Draft", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isCreatingNewDraft) {
                    // Create / Save Draft Form
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = draftTitle,
                                onValueChange = { draftTitle = it },
                                label = { Text("Video Title") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_draft_title")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = draftTopic,
                                onValueChange = { draftTopic = it },
                                label = { Text("Topic / Niche") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_draft_topic")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = draftDescription,
                                onValueChange = { draftDescription = it },
                                label = { Text("Description & Notes") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Format selection
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Format", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = draftFormat.contains("16:9"),
                                            onClick = { draftFormat = "16:9 Long-Form" },
                                            label = { Text("16:9", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = draftFormat.contains("9:16"),
                                            onClick = { draftFormat = "9:16 Shorts" },
                                            label = { Text("9:16", fontSize = 11.sp) }
                                        )
                                    }
                                }

                                // Resolution
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Resolution", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("1080p", "4K").forEach { res ->
                                            FilterChip(
                                                selected = draftResolution == res,
                                                onClick = { draftResolution = res },
                                                label = { Text(res, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Platform selection
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Target Platform", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("YouTube", "Instagram", "Facebook").forEach { plat ->
                                            FilterChip(
                                                selected = draftPlatform == plat,
                                                onClick = { draftPlatform = plat },
                                                label = { Text(plat, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }

                                // Status selection
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Draft Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Draft", "Editing", "Ready").forEach { st ->
                                            FilterChip(
                                                selected = draftStatus == st,
                                                onClick = { draftStatus = st },
                                                label = { Text(st, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = draftNotes,
                                onValueChange = { draftNotes = it },
                                label = { Text("Production Checklist / Todo") },
                                placeholder = { Text("e.g., Color grading needed, check audio levels") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val newEntity = VideoMetadataEntity(
                                        title = draftTitle.ifBlank { "Untitled Project" },
                                        description = draftDescription,
                                        topic = draftTopic,
                                        format = draftFormat,
                                        resolution = draftResolution,
                                        fps = draftFps,
                                        durationSec = if (draftFormat.contains("9:16")) 45 else 600,
                                        platform = draftPlatform,
                                        draftStatus = draftStatus,
                                        isDraft = draftStatus != "Published",
                                        completionProgress = when (draftStatus) {
                                            "Ready" -> 1.0f
                                            "Editing" -> 0.5f
                                            else -> 0.25f
                                        },
                                        notes = draftNotes,
                                        aspectRatio = if (draftFormat.contains("9:16")) "9:16" else "16:9"
                                    )
                                    onSaveCurrentAsDraft(newEntity)
                                    isCreatingNewDraft = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_save_draft_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save to Room Database", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterOptions) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = filter,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (selectedFilter == filter) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedFilter == "All") "No video drafts stored yet" else "No drafts with status '$selectedFilter'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FilledTonalButton(
                                    onClick = { isCreatingNewDraft = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create First Draft")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredList, key = { it.id }) { video ->
                                VideoDraftCard(
                                    video = video,
                                    dateFormatted = dateFormatter.format(Date(video.updatedAt)),
                                    onLoad = {
                                        onLoadDraft(video)
                                        onDismiss()
                                    },
                                    onStatusChange = { newStatus ->
                                        onUpdateDraftStatus(video.id, newStatus)
                                    },
                                    onDelete = { onDeleteDraft(video) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoDraftCard(
    video: VideoMetadataEntity,
    dateFormatted: String,
    onLoad: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    val statusColor = when (video.draftStatus.lowercase()) {
        "ready", "published" -> Color(0xFF4CAF50)
        "editing", "rendering" -> Color(0xFFFF9800)
        else -> StudioCrimson
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_draft_item_${video.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Title, Platform, Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Chip with click to change
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { showStatusMenu = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = video.draftStatus.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = statusColor
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        listOf("Draft", "Editing", "Rendering", "Ready", "Published", "Archived").forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    onStatusChange(st)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Metadata tags & details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Platform tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = video.platform,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Format & resolution
                Text(
                    text = "${video.format} • ${video.resolution} ${video.fps}fps",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Duration
                val min = video.durationSec / 60
                val sec = video.durationSec % 60
                Text(
                    text = if (min > 0) "${min}m ${sec}s" else "${sec}s",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (video.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { video.completionProgress.coerceIn(0f, 1f) },
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(video.completionProgress * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // Row 4: Updated timestamp & Action buttons
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalButton(
                        onClick = onLoad,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("load_draft_button_${video.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_draft_button_${video.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
