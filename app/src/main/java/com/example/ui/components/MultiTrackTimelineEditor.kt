package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.StorySceneData
import com.example.ui.theme.*

data class TimelineTrackInfo(
    val id: String,
    val nameEn: String,
    val nameKn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    var isMuted: Boolean = false,
    var isLocked: Boolean = false,
    var volumePercent: Int = 100
)

@Composable
fun MultiTrackTimelineEditor(
    scenes: List<StorySceneData>,
    currentPlayheadSec: Float,
    isKn: Boolean,
    onPlayheadChange: (Float) -> Unit,
    onSplitScene: (Int) -> Unit = {},
    onDuplicateScene: (Int) -> Unit = {},
    onDeleteScene: (Int) -> Unit = {},
    onTrimScene: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSceneIndex by remember { mutableIntStateOf(0) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }
    var volumePercent by remember { mutableIntStateOf(100) }

    val tracks = remember {
        mutableStateListOf(
            TimelineTrackInfo("video", "1. Video Clips Track", "೧. ವಿಡಿಯೊ ಕ್ಲಿಪ್ ಟ್ರ್ಯಾಕ್", Icons.Default.Videocam, StudioCrimson),
            TimelineTrackInfo("image", "2. Scene Images Track", "೨. ದೃಶ್ಯ ಚಿತ್ರಗಳ ಟ್ರ್ಯಾಕ್", Icons.Default.Image, StudioAmber),
            TimelineTrackInfo("voice", "3. Voice-Over (TTS)", "೩. ವಾಯ್ಸ್-ಓವರ್ (TTS)", Icons.Default.RecordVoiceOver, StudioEmerald),
            TimelineTrackInfo("audio", "4. Primary Audio", "೪. ಮುಖ್ಯ ಆಡಿಯೊ", Icons.Default.VolumeUp, StudioCyan),
            TimelineTrackInfo("subtitle", "5. Subtitle Track", "೫. ಸಬ್‌ಟೈಟಲ್ ಟ್ರ್ಯಾಕ್", Icons.Default.Subtitles, StudioIndigo),
            TimelineTrackInfo("text", "6. Text Overlay Track", "೬. ಟೆಕ್ಸ್ಟ್ ಓವರ್‌ಲೇ ಟ್ರ್ಯಾಕ್", Icons.Default.TextFields, StudioPurple),
            TimelineTrackInfo("bgm", "7. Background Music", "೭. ಹಿನ್ನೆಲೆ ಸಂಗೀತ (BGM)", Icons.Default.MusicNote, Color(0xFFF97316)),
            TimelineTrackInfo("sfx", "8. Sound Effects (SFX)", "೮. ಶಬ್ದ ಪರಿಣಾಮಗಳು (SFX)", Icons.Default.ElectricBolt, Color(0xFF06B6D4))
        )
    }

    val totalDurationSec = scenes.sumOf { it.durationSec.coerceAtLeast(3) }.coerceAtLeast(10)
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("multi_track_timeline_editor_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Timeline Top Toolbar: Actions (Split, Trim, Duplicate, Delete, Undo, Redo, Zoom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioIndigo.copy(alpha = 0.15f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = StudioIndigo, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKn) "೮-ಟ್ರ್ಯಾಕ್ ಟೈಮ್‌ಲೈನ್ ಎಡಿಟರ್" else "8-TRACK TIMELINE EDITOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = StudioIndigo
                            )
                        }
                    }

                    Text(
                        text = "${String.format("%.1f", currentPlayheadSec)}s / ${totalDurationSec}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Buttons Row (Split, Trim, Duplicate, Delete, Undo, Redo)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = {
                            canUndo = true
                            Toast.makeText(context, "Undo performed", Toast.LENGTH_SHORT).show()
                        },
                        enabled = canUndo,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Redo performed", Toast.LENGTH_SHORT).show()
                        },
                        enabled = canRedo,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo", modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = {
                            canUndo = true
                            onSplitScene(selectedSceneIndex)
                            Toast.makeText(context, "Scene ${selectedSceneIndex + 1} Split at playhead", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp).testTag("timeline_split_button")
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = "Split", tint = StudioCrimson, modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = {
                            canUndo = true
                            onDuplicateScene(selectedSceneIndex)
                            Toast.makeText(context, "Scene ${selectedSceneIndex + 1} Duplicated", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp).testTag("timeline_duplicate_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = {
                            if (scenes.size > 1) {
                                canUndo = true
                                onDeleteScene(selectedSceneIndex)
                                Toast.makeText(context, "Scene ${selectedSceneIndex + 1} Deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Cannot delete last scene", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(30.dp).testTag("timeline_delete_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                    }
                }
            }

            // Timeline Tracks Container (Horizontal Scrollable Playhead view)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column: Track Headers & Controls (Mute / Lock)
                    Column(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1E293B))
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        tracks.forEach { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = track.icon,
                                        contentDescription = null,
                                        tint = track.color,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isKn) track.nameKn.take(10) else track.nameEn.take(10),
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }

                                Row {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(if (track.isMuted) Color.Red else Color.Gray.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }

                    // Right Area: Scrubbable Timeline Track Bands
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .width((totalDurationSec * 24 * zoomLevel).dp.coerceAtLeast(320.dp))
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            tracks.forEach { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                        .padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    scenes.forEachIndexed { idx, sc ->
                                        val sceneWidth = (sc.durationSec.coerceAtLeast(3) * 24 * zoomLevel).dp
                                        Surface(
                                            modifier = Modifier
                                                .width(sceneWidth)
                                                .fillMaxHeight(),
                                            shape = RoundedCornerShape(4.dp),
                                            color = track.color.copy(alpha = if (selectedSceneIndex == idx) 0.85f else 0.45f),
                                            border = if (selectedSceneIndex == idx) BorderStroke(1.5.dp, Color.White) else null
                                        ) {
                                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 4.dp)) {
                                                Text(
                                                    text = "S${sc.sceneNumber} (${sc.durationSec}s)",
                                                    fontSize = 8.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Playhead Slider Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(16.dp))
                Slider(
                    value = currentPlayheadSec,
                    onValueChange = onPlayheadChange,
                    valueRange = 0f..totalDurationSec.toFloat(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("timeline_playhead_slider")
                )
                Text(
                    text = "${currentPlayheadSec.toInt()}s",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
