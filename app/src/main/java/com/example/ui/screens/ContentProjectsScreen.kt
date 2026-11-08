package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.data.ContentProjectEntity
import com.example.i18n.I18n
import com.example.ui.components.PlatformBadge
import com.example.ui.components.ProjectStatusBadge
import com.example.ui.theme.*

@Composable
fun ContentProjectsScreen(
    currentLanguage: String,
    projects: List<ContentProjectEntity>,
    onAddProject: (ContentProjectEntity) -> Unit,
    onUpdateProject: (ContentProjectEntity) -> Unit,
    onDeleteProject: (ContentProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKn = currentLanguage == I18n.LANG_KN

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProjectForDetail by remember { mutableStateOf<ContentProjectEntity?>(null) }

    val filteredProjects = projects.filter { p ->
        val matchesSearch = searchQuery.isBlank() ||
                p.title.contains(searchQuery, ignoreCase = true) ||
                p.topic.contains(searchQuery, ignoreCase = true) ||
                p.platform.contains(searchQuery, ignoreCase = true)

        val matchesStatus = selectedStatusFilter == "All" ||
                p.status.equals(selectedStatusFilter, ignoreCase = true)

        matchesSearch && matchesStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = I18n.t("nav_projects", currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isKn) "ನಿಮ್ಮ ಎಲ್ಲಾ ಕಂಟೆಂಟ್ ಪ್ರಾಜೆಕ್ಟ್‌ಗಳು ಒಂದೇ ಕಡೆ" else "Manage complete content pipelines from Idea to Published",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_project_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isKn) "+ ಪ್ರಾಜೆಕ್ಟ್" else "+ Project", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isKn) "ಪ್ರಾಜೆಕ್ಟ್ ಹುಡುಕಿ..." else "Search projects by title or topic...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips for Project statuses: Idea, Script Ready, Production, Thumbnail Ready, Ready to Publish, Published
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(listOf("All", "Idea", "Script Ready", "Production", "Thumbnail Ready", "Ready to Publish", "Published")) { status ->
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = status },
                    label = { Text(status, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Projects list
        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isKn) "ಯಾವುದೇ ಪ್ರಾಜೆಕ್ಟ್‌ಗಳು ಕಂಡುಬಂದಿಲ್ಲ" else "No content projects match criteria",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProjectForDetail = project }
                            .testTag("project_card_${project.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PlatformBadge(platform = project.platform)
                                ProjectStatusBadge(status = project.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = project.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${project.format} • ${project.topic}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duration: ${project.duration}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row {
                                    // Duplicate Project
                                    IconButton(
                                        onClick = {
                                            onAddProject(project.copy(id = 0, title = "${project.title} (Copy)"))
                                            Toast.makeText(context, "Project duplicated!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                                    }

                                    // Delete Project
                                    IconButton(
                                        onClick = { onDeleteProject(project) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Project Dialog
    if (showAddDialog) {
        ProjectFormDialog(
            initialProject = null,
            currentLanguage = currentLanguage,
            onSave = {
                onAddProject(it)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Detail/Edit Dialog
    selectedProjectForDetail?.let { project ->
        ProjectFormDialog(
            initialProject = project,
            currentLanguage = currentLanguage,
            onSave = {
                onUpdateProject(it)
                selectedProjectForDetail = null
            },
            onDismiss = { selectedProjectForDetail = null }
        )
    }
}

@Composable
private fun ProjectFormDialog(
    initialProject: ContentProjectEntity?,
    currentLanguage: String,
    onSave: (ContentProjectEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialProject?.title ?: "") }
    var topic by remember { mutableStateOf(initialProject?.topic ?: "") }
    var platform by remember { mutableStateOf(initialProject?.platform ?: "YouTube") }
    var format by remember { mutableStateOf(initialProject?.format ?: "Video") }
    var status by remember { mutableStateOf(initialProject?.status ?: "Idea") }
    var duration by remember { mutableStateOf(initialProject?.duration ?: "60s") }
    var scriptText by remember { mutableStateOf(initialProject?.scriptText ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (initialProject == null) "New Content Project" else "Edit Project",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic / Core Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Status
                Text(text = "Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Idea", "Script Ready", "Production", "Thumbnail Ready", "Ready to Publish", "Published")) { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = scriptText,
                    onValueChange = { scriptText = it },
                    label = { Text("Notes / Script Outline") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    ContentProjectEntity(
                                        id = initialProject?.id ?: 0,
                                        title = title,
                                        topic = topic,
                                        platform = platform,
                                        format = format,
                                        status = status,
                                        duration = duration,
                                        scriptText = scriptText
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson)
                    ) {
                        Text("Save Project")
                    }
                }
            }
        }
    }
}
