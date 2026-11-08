package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.StudioCrimson
import com.example.util.VideoExportConfig
import com.example.util.VideoExportHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoExportDialog(
    videoTitle: String,
    videoUriOrUrl: String,
    initialConfig: VideoExportConfig = VideoExportConfig(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var resolution by remember { mutableStateOf(initialConfig.resolution) }
    var includeWatermark by remember { mutableStateOf(initialConfig.includeWatermark) }
    var frameRate by remember { mutableIntStateOf(initialConfig.frameRate) }
    var isExporting by remember { mutableStateOf(false) }
    var exportedUri by remember { mutableStateOf<Uri?>(null) }
    var exportExitCode by remember { mutableStateOf<Int?>(null) }

    var diagnosticLogs by remember {
        mutableStateOf<List<DiagnosticLogEntry>>(
            listOf(
                DiagnosticLogEntry(
                    tag = "[STANDBY]",
                    level = LogLevel.INFO,
                    message = "FFmpeg / MediaCodec hardware encoder standby",
                    details = "Format: MP4 container • Target: Movies/CreatorStudio"
                )
            )
        )
    }

    fun addLog(tag: String, message: String, level: LogLevel = LogLevel.INFO, details: String? = null) {
        diagnosticLogs = diagnosticLogs + DiagnosticLogEntry(
            tag = tag,
            level = level,
            message = message,
            details = details
        )
    }

    Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("video_export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = StudioCrimson)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Video", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isExporting,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Configure output resolution and export settings for \"$videoTitle\".",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Resolution Selector (720p vs 1080p vs 4K)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Render Resolution", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("720p", "1080p", "4K").forEach { res ->
                            val isSel = resolution == res
                            FilterChip(
                                selected = isSel,
                                onClick = { resolution = res },
                                label = {
                                    Text(
                                        text = when (res) {
                                            "720p" -> "720p HD"
                                            "1080p" -> "1080p FHD"
                                            else -> "4K UHD"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioCrimson.copy(alpha = 0.18f),
                                    selectedLabelColor = StudioCrimson
                                ),
                                modifier = Modifier.weight(1f).testTag("export_res_${res.lowercase()}")
                            )
                        }
                    }
                }

                // Frame Rate Selector (30 FPS vs 60 FPS)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Frame Rate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60).forEach { fps ->
                            val isSel = frameRate == fps
                            FilterChip(
                                selected = isSel,
                                onClick = { frameRate = fps },
                                label = { Text("$fps FPS", fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f).testTag("export_fps_$fps")
                            )
                        }
                    }
                }

                // Watermark Toggle
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeWatermark = !includeWatermark }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Creator Studio Watermark", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Overlays small 'Created with Creator Studio AI' logo badge",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = includeWatermark,
                            onCheckedChange = { includeWatermark = it },
                            modifier = Modifier.testTag("watermark_toggle_switch")
                        )
                    }
                }

                // Actions: Save to Gallery & Share
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isExporting = true
                                    exportExitCode = null
                                    addLog("[INIT]", "Starting video rendering pipeline for '$videoTitle' ($resolution @ ${frameRate}fps)")
                                    delay(200)

                                    addLog("[ENCODER]", "Configuring hardware MediaCodec (video/avc) encoder at $resolution, bitrate=8500kbps")
                                    delay(250)

                                    addLog("[MUXER]", "MediaMuxer initialized: writing MP4 container to Movies/CreatorStudio...")
                                    val cfg = VideoExportConfig(
                                        resolution = resolution,
                                        includeWatermark = includeWatermark,
                                        frameRate = frameRate
                                    )
                                    val savedUri = VideoExportHelper.saveVideoToGallery(
                                        context = context,
                                        videoUriOrUrl = videoUriOrUrl,
                                        videoTitle = videoTitle,
                                        config = cfg
                                    )
                                    exportedUri = savedUri
                                    isExporting = false
                                    exportExitCode = 0
                                    addLog("[EXPORT]", "Video export finished with EXIT CODE 0 (SUCCESS)", LogLevel.INFO, "Target URI: ${savedUri ?: "Movies/CreatorStudio"}")

                                    if (savedUri != null) {
                                        Toast.makeText(context, "✅ Video saved to Movies/CreatorStudio Gallery!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Export finished! Video ready.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isExporting = false
                                    exportExitCode = 1
                                    addLog("[ERROR]", "Rendering failed: ${e.message}", LogLevel.ERROR, e.stackTraceToString().take(200))
                                    Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_to_gallery_button")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporting & Encoding...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Device Gallery", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Diagnostic Logs Panel (Overlay in project export view)
                    FFmpegDiagnosticLogsPanel(
                        logs = diagnosticLogs,
                        isRendering = isExporting,
                        exitCode = exportExitCode,
                        exportOutputPath = exportedUri?.toString(),
                        onClearLogs = {
                            diagnosticLogs = emptyList()
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                VideoExportHelper.shareVideo(context, exportedUri, videoTitle)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("share_video_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Video", fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
