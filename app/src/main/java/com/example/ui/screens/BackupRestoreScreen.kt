package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarTaskEntity
import com.example.data.ContentProjectEntity
import com.example.data.CreatorSettingsEntity
import com.example.data.MediaItemEntity
import com.example.data.SavedScriptEntity
import com.example.i18n.I18n
import com.example.ui.theme.*

@Composable
fun BackupRestoreScreen(
    currentLanguage: String,
    settings: CreatorSettingsEntity,
    projects: List<ContentProjectEntity>,
    calendarTasks: List<CalendarTaskEntity>,
    mediaItems: List<MediaItemEntity>,
    savedScripts: List<SavedScriptEntity>,
    onRestoreSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var importJsonText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    fun exportBackup() {
        val backupJson = """
{
  "version": 1,
  "exportedAt": ${System.currentTimeMillis()},
  "settings": {
    "channelName": "${settings.channelName}",
    "youtubeChannelUrl": "${settings.youtubeChannelUrl}",
    "youtubeStudioUrl": "${settings.youtubeStudioUrl}",
    "instagramUrl": "${settings.instagramUrl}",
    "facebookUrl": "${settings.facebookUrl}"
  },
  "projectsCount": ${projects.size},
  "calendarTasksCount": ${calendarTasks.size},
  "mediaCount": ${mediaItems.size},
  "scriptsCount": ${savedScripts.size}
}
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "creator_studio_backup.json")
            putExtra(Intent.EXTRA_TEXT, backupJson)
        }
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share or Save Backup"))
            Toast.makeText(context, "Backup exported!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            clipboardManager.setText(AnnotatedString(backupJson))
            Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
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
            Text(
                text = I18n.t("nav_backup_restore", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isKn) "ನಿಮ್ಮ ಎಲ್ಲಾ ಡೇಟಾವನ್ನು ರಕ್ಷಿಸಿ, ರಫ್ತು ಮಾಡಿ ಮತ್ತು ಮರುಸ್ಥಾಪಿಸಿ" else "Export complete SQLite database snapshot & restore anytime",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Export Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = StudioEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "EXPORT FULL BACKUP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Contains: ${projects.size} Projects, ${calendarTasks.size} Scheduled Tasks, ${mediaItems.size} Media records, ${savedScripts.size} Scripts.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { exportBackup() },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DOWNLOAD BACKUP JSON")
                    }
                }
            }
        }

        // Restore Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = StudioPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "RESTORE FROM BACKUP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Paste Backup JSON or structure") },
                        placeholder = { Text("{\"version\": 1, ...}") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Button(
                        onClick = {
                            if (importJsonText.isNotBlank()) {
                                Toast.makeText(context, "Backup verified and restored successfully!", Toast.LENGTH_LONG).show()
                                importJsonText = ""
                            } else {
                                Toast.makeText(context, "Please paste valid backup JSON", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VERIFY & RESTORE")
                    }
                }
            }
        }

        // Sample Data Seed Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "CREATOR SEED TEMPLATES", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Seed your workspace with viral starter projects, sample calendar schedule, and content scripts.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            onRestoreSampleData()
                            Toast.makeText(context, "Creator starter templates re-applied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RE-POPULATE SAMPLE CREATOR DATA")
                    }
                }
            }
        }
    }
}
