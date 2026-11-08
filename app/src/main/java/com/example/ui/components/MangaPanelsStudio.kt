package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MangaChapterEntity
import com.example.data.MangaStoryEntity
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioPurple
import java.io.File
import java.io.FileOutputStream

data class MangaPanelModel(
    val panelNumber: Int,
    var speaker: String,
    var dialogue: String,
    var soundEffect: String,
    var actionPrompt: String,
    var bubbleType: String = "Speech" // "Speech", "Shout", "Thought"
)

@Composable
fun MangaPanelsStudio(
    chapter: MangaChapterEntity,
    story: MangaStoryEntity,
    isKn: Boolean,
    onConvertToNarratedVideo: () -> Unit,
    onConvertToAnimatedComicVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLayout by remember { mutableStateOf("4-Panel (Yonkoma)") }
    val layoutPresets = listOf("4-Panel (Yonkoma)", "3-Panel Action Flow", "2-Panel Dramatic", "Full Splash Page")

    var panels by remember(chapter.id) {
        mutableStateOf(
            listOf(
                MangaPanelModel(
                    panelNumber = 1,
                    speaker = if (isKn) "ರೆನ್ (Ren)" else "Ren",
                    dialogue = if (isKn) "ಸಮಯ ನನಗೆ ವಿರುದ್ಧವಾಗುತ್ತಿದೆ... ಆದರೂ ನಾನು ಬಿಡುವುದಿಲ್ಲ!" else "Time is turning against me... but I won't back down!",
                    soundEffect = "ドドド (DODODO)",
                    actionPrompt = "Close-up dramatic eye contact, cybernetic katana glowing with neon blue particles, rain-soaked alley."
                ),
                MangaPanelModel(
                    panelNumber = 2,
                    speaker = if (isKn) "ಮಾಯಾ (Maya)" else "Maya",
                    dialogue = if (isKn) "ರೆನ್! ಎಚ್ಚರ, ಆಕಾಶದಿಂದ ಸೈಬರ್ ಡ್ರೋನ್‌ಗಳು ದಾಳಿ ಮಾಡುತ್ತಿವೆ!" else "Ren! Look above, the hunter drones have locked on!",
                    soundEffect = "ಝಂ! (BZZT)",
                    actionPrompt = "High angle shot of rooftop, robotic drones firing crimson laser beams down through the fog."
                ),
                MangaPanelModel(
                    panelNumber = 3,
                    speaker = if (isKn) "ರೆನ್ (Ren)" else "Ren",
                    dialogue = if (isKn) "ಕ್ರೋನೋ ಬ್ಲೇಡ್: ೭ ಸೆಕೆಂಡ್ ರಿವರ್ಸಲ್!" else "Chrono Blade: Seven Seconds Reversal!",
                    soundEffect = "ಧಿಗ್ಗನೆ! (SHING!)",
                    actionPrompt = "Dynamic action slash with ink splatter effects, laser beams bending backward in time.",
                    bubbleType = "Shout"
                ),
                MangaPanelModel(
                    panelNumber = 4,
                    speaker = if (isKn) "ನಿರೂಪಕ (Narrator)" else "Narrator",
                    dialogue = if (isKn) "ಹಾಗೆ ನೆರಳುಗಳು ಕಣ್ಮರೆಯಾದವು, ಆದರೆ ಹೊಸ ಶತ್ರು ಎದುರಾಗುತ್ತಿದ್ದಾನೆ..." else "And so the shadows vanished, but an ancient nemesis stirs...",
                    soundEffect = "ゴゴゴ (GOGOGO)",
                    actionPrompt = "Silhouette standing atop the neon skyscraper overlooking the vast cyberpunk city skyline.",
                    bubbleType = "Thought"
                )
            )
        )
    }

    fun saveMockExportFile(extension: String, mimeType: String) {
        try {
            val exportDir = File(context.filesDir, "manga_exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val fileName = "manga_${story.title.replace(" ", "_")}_ch${chapter.chapterNumber}_page.${extension}"
            val file = File(exportDir, fileName)
            FileOutputStream(file).use { fos ->
                val header = "Manga Export: ${story.title} - Chapter ${chapter.chapterNumber}\nLayout: $selectedLayout\nPanels: ${panels.size}\n"
                fos.write(header.toByteArray())
            }
            val msg = if (isKn) {
                "ರಫ್ತು ಯಶಸ್ವಿಯಾಗಿದೆ: $fileName ($extension)"
            } else {
                "Exported successfully: $fileName ($extension)"
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("manga_panels_studio_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Studio Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isKn) "🎨 ಮಂಗಾ ಪ್ಯಾನಲ್ ಮತ್ತು ಪೇಜ್ ಲೇಔಟ್" else "🎨 MANGA PANELS & PAGE LAYOUT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = StudioCrimson
                            )
                            Text(
                                text = "${story.title} • ${story.genre} • ${chapter.title}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioCrimson.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${panels.size} PANELS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioCrimson,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Layout Presets Chips
                    Text(
                        text = if (isKn) "ಪೇಜ್ ಲೇಔಟ್ ಶೈಲಿ (Page Layout Style):" else "Page Layout Preset:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        layoutPresets.forEach { preset ->
                            FilterChip(
                                selected = selectedLayout == preset,
                                onClick = { selectedLayout = preset },
                                label = { Text(preset, fontSize = 10.sp) },
                                modifier = Modifier.testTag("layout_chip_${preset.take(4)}")
                            )
                        }
                    }
                }
            }
        }

        // 2. Export & Video Action Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isKn) "ರಫ್ತು ಮತ್ತು ವೀಡಿಯೊ ಪರಿವರ್ತನೆ (Export & Video Conversion)" else "PAGE EXPORT & VIDEO CONVERSION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Export Buttons Row (PNG, JPG, PDF)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { saveMockExportFile("png", "image/png") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_manga_png")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PNG Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { saveMockExportFile("jpg", "image/jpeg") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_manga_jpg")
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("JPG Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { saveMockExportFile("pdf", "application/pdf") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_manga_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Convert to Video Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onConvertToNarratedVideo,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioEmerald),
                            border = BorderStroke(1.dp, StudioEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("convert_manga_to_narrated_video")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKn) "🎙️ ನಿರೂಪಿತ ವೀಡಿಯೊ" else "🎙️ Narrated Video",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onConvertToAnimatedComicVideo,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPurple),
                            border = BorderStroke(1.dp, StudioPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("convert_manga_to_animated_comic_video")
                        ) {
                            Icon(Icons.Default.Animation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKn) "🎬 ಅನಿಮೇಟೆಡ್ ಕಾಮಿಕ್" else "🎬 Motion Comic",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Manga Panels List with Speech Bubbles, Sound Effects & Reordering
        itemsIndexed(panels) { index, panel ->
            MangaPanelEditorCard(
                panel = panel,
                index = index,
                totalPanels = panels.size,
                isKn = isKn,
                onMoveUp = {
                    if (index > 0) {
                        val list = panels.toMutableList()
                        val item = list.removeAt(index)
                        list.add(index - 1, item)
                        panels = list.mapIndexed { i, p -> p.copy(panelNumber = i + 1) }
                    }
                },
                onMoveDown = {
                    if (index < panels.size - 1) {
                        val list = panels.toMutableList()
                        val item = list.removeAt(index)
                        list.add(index + 1, item)
                        panels = list.mapIndexed { i, p -> p.copy(panelNumber = i + 1) }
                    }
                },
                onRegenerate = {
                    Toast.makeText(context, "Regenerating Panel ${panel.panelNumber}...", Toast.LENGTH_SHORT).show()
                },
                onUpdate = { updated ->
                    val list = panels.toMutableList()
                    list[index] = updated
                    panels = list
                }
            )
        }
    }
}

@Composable
private fun MangaPanelEditorCard(
    panel: MangaPanelModel,
    index: Int,
    totalPanels: Int,
    isKn: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRegenerate: () -> Unit,
    onUpdate: (MangaPanelModel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manga_panel_card_${panel.panelNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Panel Number, Reorder buttons, Regenerate button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(StudioCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${panel.panelNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = if (isKn) "ಪ್ಯಾನಲ್ ${panel.panelNumber}" else "PANEL ${panel.panelNumber}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )

                    // Bubble Type Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = panel.bubbleType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("panel_move_up_$index")
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalPanels - 1,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("panel_move_down_$index")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("panel_regenerate_$index")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate Panel", tint = StudioAmber, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Visual Frame Preview Simulation
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF111827),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    // Action Prompt Description
                    Text(
                        text = "🎬 ${panel.actionPrompt}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.BottomStart)
                    )

                    // Speech Bubble Rendering Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color.Black),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = "${panel.speaker}: \"${panel.dialogue}\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Sound Effect Display Preview
                    if (panel.soundEffect.isNotBlank()) {
                        Text(
                            text = panel.soundEffect,
                            color = StudioCrimson,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }

            // Dialogue & Speaker Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = panel.speaker,
                    onValueChange = { onUpdate(panel.copy(speaker = it)) },
                    label = { Text("Speaker", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.35f)
                )

                OutlinedTextField(
                    value = panel.soundEffect,
                    onValueChange = { onUpdate(panel.copy(soundEffect = it)) },
                    label = { Text("SFX / Onomatopoeia", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(0.65f)
                )
            }

            OutlinedTextField(
                value = panel.dialogue,
                onValueChange = { onUpdate(panel.copy(dialogue = it)) },
                label = { Text(if (isKn) "ಸಂಭಾಷಣೆ (Dialogue / Speech Bubble)" else "Dialogue / Speech Bubble") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = panel.actionPrompt,
                onValueChange = { onUpdate(panel.copy(actionPrompt = it)) },
                label = { Text(if (isKn) "ದೃಶ್ಯ ಪ್ರಾಂಪ್ಟ್ (Visual Prompt for AI)" else "Action & Visual Prompt for AI") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
