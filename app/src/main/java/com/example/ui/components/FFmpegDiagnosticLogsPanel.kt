package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class LogLevel(val label: String, val badgeColor: Color) {
    ALL("ALL", StudioCyan),
    INFO("INFO", StudioEmerald),
    WARN("WARN", StudioAmber),
    ERROR("ERROR", StudioCrimson)
}

data class DiagnosticLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String, // e.g. [INIT], [ENCODER], [AUDIO], [MUXER], [EXPORT], [DEBUG]
    val level: LogLevel = LogLevel.INFO,
    val message: String,
    val details: String? = null
)

/**
 * Collapsible diagnostic logs panel for FFmpeg and MediaCodec video export pipeline.
 * Features:
 * - Real-time event log streaming with auto-scroll.
 * - Log level filtering (ALL, INFO, WARN, ERROR).
 * - Exit code indicator (Exit Code 0: SUCCESS, or failure reason).
 * - One-tap 'Copy Logs' button to clipboard.
 * - Troubleshooting tips drawer for common export failures.
 * - Responsive, accessible (>=48dp touch targets), bilingual English/Kannada.
 */
@Composable
fun FFmpegDiagnosticLogsPanel(
    logs: List<DiagnosticLogEntry>,
    isRendering: Boolean = false,
    exitCode: Int? = null,
    exportOutputPath: String? = null,
    isKn: Boolean = false,
    onClearLogs: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(LogLevel.ALL) }
    var showTroubleshooting by remember { mutableStateOf(false) }
    var copyNotice by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == LogLevel.ALL) logs
        else logs.filter { it.level == selectedFilter }
    }

    // Auto-scroll to latest log entry
    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    LaunchedEffect(copyNotice) {
        if (copyNotice) {
            kotlinx.coroutines.delay(2000)
            copyNotice = false
        }
    }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ffmpeg_diagnostic_logs_panel"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Sleek terminal dark slate
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRendering) StudioIndigo.copy(alpha = 0.6f)
            else if (exitCode == 0) StudioEmerald.copy(alpha = 0.5f)
            else if (exitCode != null && exitCode != 0) StudioCrimson.copy(alpha = 0.5f)
            else Color(0xFF334155)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title, Live indicator or Exit Code, Collapse Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRendering) StudioAmber.copy(alpha = 0.15f)
                                else if (exitCode == 0) StudioEmerald.copy(alpha = 0.15f)
                                else Color(0xFF1E293B)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal Icon",
                            tint = if (isRendering) StudioAmber
                            else if (exitCode == 0) StudioEmerald
                            else StudioCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isKn) "FFmpeg ರೆಂಡರಿಂಗ್ ಲಾಗ್ಸ್ & ಡಯಾಗ್ನಾಸ್ಟಿಕ್ಸ್" else "FFmpeg / MediaCodec Logs",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Status badge
                            if (isRendering) {
                                Surface(
                                    color = StudioAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isKn) "ಪ್ರಕ್ರಿಯೆ ನಡೆಯುತ್ತಿದೆ" else "STREAMING",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioAmber
                                    )
                                }
                            } else if (exitCode != null) {
                                Surface(
                                    color = if (exitCode == 0) StudioEmerald.copy(alpha = 0.2f) else StudioCrimson.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (exitCode == 0) "EXIT 0 (SUCCESS)" else "EXIT $exitCode (ERROR)",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (exitCode == 0) StudioEmerald else StudioCrimson
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isKn) "${logs.size} ಈವೆಂಟ್‌ಗಳು ದಾಖಲಾಗಿವೆ" else "${logs.size} pipeline events logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Expand / Collapse Action Button
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_logs_panel_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Logs" else "Expand Logs",
                        tint = Color.White
                    )
                }
            }

            // Quick summary row when collapsed
            if (!isExpanded && logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val latest = logs.last()
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = latest.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = latest.level.badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Text(
                            text = latest.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Expanded Panel Contents
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Filter bar + Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Level filter chips
                        LogLevel.values().forEach { level ->
                            val isSelected = selectedFilter == level
                            val count = if (level == LogLevel.ALL) logs.size else logs.count { it.level == level }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = level },
                                label = {
                                    Text(
                                        text = "${level.label} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = level.badgeColor.copy(alpha = 0.25f),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) level.badgeColor else Color.Transparent
                                ),
                                modifier = Modifier.height(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Troubleshooting Button
                        IconButton(
                            onClick = { showTroubleshooting = !showTroubleshooting },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("logs_troubleshooting_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Troubleshooting Guide",
                                tint = if (showTroubleshooting) StudioAmber else Color(0xFF94A3B8)
                            )
                        }

                        // Copy Logs Button
                        IconButton(
                            onClick = {
                                val fullLogText = buildString {
                                    appendLine("=== CREATOR STUDIO PRO FFmpeg DIAGNOSTIC DUMP ===")
                                    appendLine("Generated: ${Date()}")
                                    appendLine("Exit Code: ${exitCode ?: "In-Progress / Unknown"}")
                                    if (exportOutputPath != null) appendLine("Output File: $exportOutputPath")
                                    appendLine("=================================================")
                                    logs.forEach { entry ->
                                        appendLine("[${timeFormatter.format(Date(entry.timestamp))}] ${entry.tag} [${entry.level.name}] - ${entry.message}")
                                        if (entry.details != null) {
                                            appendLine("   Detail: ${entry.details}")
                                        }
                                    }
                                }
                                clipboardManager.setText(AnnotatedString(fullLogText))
                                copyNotice = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("copy_logs_button")
                        ) {
                            Icon(
                                imageVector = if (copyNotice) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy Logs to Clipboard",
                                tint = if (copyNotice) StudioEmerald else Color(0xFF94A3B8)
                            )
                        }

                        // Clear Logs (optional)
                        if (onClearLogs != null) {
                            IconButton(
                                onClick = onClearLogs,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("clear_logs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Logs",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // Copy notification feedback
                    if (copyNotice) {
                        Surface(
                            color = StudioEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (isKn) "ಲಾಗ್‌ಗಳನ್ನು ಕ್ಲಿಪ್‌ಬೋರ್ಡ್‌ಗೆ ನಕಲಿಸಲಾಗಿದೆ!" else "Diagnostic logs copied to clipboard!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioEmerald,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Troubleshooting Advice Drawer
                    AnimatedVisibility(visible = showTroubleshooting) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioAmber.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = if (isKn) "ದೋಷ ಪರಿಹಾರ ಮಾರ್ಗದರ್ಶಿ (Troubleshooting Tips)" else "Export Failure Troubleshooting Tips",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isKn)
                                        "1. [MUXER] ದೋಷ: ವೀಡಿಯೊ ರಫ್ತು ಮಾಡುವಾಗ ಶೇಖರಣಾ ಸ್ಥಳ ಲಭ್ಯವಿದೆಯೇ ಎಂದು ಪರಿಶೀಲಿಸಿ.\n" +
                                        "2. [ENCODER] ಅಸಮರ್ಪಕತೆ: ಹಾರ್ಡ್‌ವೇರ್ H.264 ಕೊಡೆಕ್ ಬೆಂಬಲಿತವಾಗಿದೆ (1080p ಅಥವಾ 720p ಗೆ ಬದಲಾಯಿಸಿ).\n" +
                                        "3. [AUDIO] ಸಿಂಕ್ ಸಮಸ್ಯೆ: ನರೇಶನ್ WAV ಫೈಲ್ ಆವರ್ತನ 44.1kHz ಆಗಿರುವುದನ್ನು ಪೈಪ್‌ಲೈನ್ ಖಚಿತಪಡಿಸುತ್ತದೆ.\n" +
                                        "4. ಮೆಮೊರಿ ಮಿತಿ: ಹೆಚ್ಚು ದೃಶ್ಯಗಳಿದ್ದರೆ (10+) ರಫ್ತು ಅವಧಿಯನ್ನು 30s ಗೆ ಬದಲಾಯಿಸಿ."
                                    else
                                        "1. [MUXER] errors: Check device storage capacity; ensure Movies/CreatorStudio write access.\n" +
                                        "2. [ENCODER] initialization: Android MediaCodec uses hardware H.264 (avc1); 1080p is default with fallback to 720p.\n" +
                                        "3. [AUDIO] sync: Narration audio is synthesized as 44.1kHz 16-bit PCM WAV to prevent desync.\n" +
                                        "4. Memory budget: For projects with 8+ scenes, export at 30-60s to ensure smooth real-time encoding.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Terminal log console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (filteredLogs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKn) "ಯಾವುದೇ ಲಾಗ್‌ಗಳು ಲಭ್ಯವಿಲ್ಲ" else "No log messages recorded yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                items(filteredLogs, key = { it.id }) { log ->
                                    LogLineItem(log = log, timeFormatter = timeFormatter)
                                }
                            }
                        }
                    }

                    // Output file path if export succeeded
                    if (exportOutputPath != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Saved: $exportOutputPath",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioEmerald,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLineItem(
    log: DiagnosticLogEntry,
    timeFormatter: SimpleDateFormat
) {
    val timeStr = remember(log.timestamp) { timeFormatter.format(Date(log.timestamp)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Timestamp
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )

            // Tag badge
            Text(
                text = log.tag,
                style = MaterialTheme.typography.labelSmall,
                color = log.level.badgeColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )

            // Message
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = when (log.level) {
                    LogLevel.ERROR -> StudioCrimson
                    LogLevel.WARN -> StudioAmber
                    LogLevel.INFO -> Color(0xFFE2E8F0)
                    LogLevel.ALL -> Color(0xFFE2E8F0)
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
        }

        if (log.details != null) {
            Text(
                text = "  ↳ ${log.details}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 60.dp)
            )
        }
    }
}
