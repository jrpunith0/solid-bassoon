package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContentProjectEntity
import com.example.generator.ContentGenerator
import com.example.i18n.I18n
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShortsReelsScreen(
    currentLanguage: String,
    onSaveProject: (ContentProjectEntity) -> Unit,
    onShowGlobalProgress: (title: String, subtitle: String, actionType: String, progress: Float?, onCancel: (() -> Unit)?) -> Unit = { _, _, _, _, _ -> },
    onHideGlobalProgress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var topic by remember { mutableStateOf("3 Editing Hacks for Insane Retention") }
    var selectedPlatform by remember { mutableStateOf("Instagram Reels") }
    var selectedDuration by remember { mutableStateOf("30s") }

    var plan by remember {
        mutableStateOf(
            ContentGenerator.generateShortReel(
                topic = topic,
                platform = selectedPlatform,
                language = currentLanguage
            )
        )
    }

    fun handleGenerate() {
        scope.launch {
            onShowGlobalProgress(
                "Shorts & Reels API Generation",
                "Analyzing trending audio & generating 9:16 vertical script...",
                "generate",
                0.4f
            ) {
                onHideGlobalProgress()
            }
            kotlinx.coroutines.delay(700)
            plan = ContentGenerator.generateShortReel(
                topic = topic,
                platform = selectedPlatform,
                language = currentLanguage
            )
            onHideGlobalProgress()
            Toast.makeText(context, "Short/Reel blueprint generated!", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSave() {
        onSaveProject(
            ContentProjectEntity(
                title = plan.title,
                topic = topic,
                platform = if (selectedPlatform.contains("YouTube")) "YouTube" else "Instagram",
                format = "Short / Reel 9:16",
                status = "Script Ready",
                scriptText = if (selectedDuration == "30s") plan.script30s else plan.script60s,
                hookText = plan.hook,
                ctaText = plan.cta,
                captionText = "${plan.hook}\n\n${plan.cta}",
                hashtagsText = plan.hashtags,
                thumbnailTitle = plan.onScreenText,
                duration = selectedDuration
            )
        )
        Toast.makeText(context, "Saved to Projects!", Toast.LENGTH_SHORT).show()
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
                text = I18n.t("nav_shorts_reels", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isKn) "YouTube Shorts, Instagram Reels & Facebook Reels ರಚಿಸಿ" else "Vertical 9:16 short-form retention creator & auto-hook sequencer",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Inputs Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Shorts / Reel Topic") },
                        modifier = Modifier.fillMaxWidth().testTag("short_topic_input"),
                        singleLine = true
                    )

                    // Platform Selection
                    Text(text = "Target Platform:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("YouTube Shorts", "Instagram Reels", "Facebook Reels").forEach { p ->
                            FilterChip(
                                selected = selectedPlatform == p,
                                onClick = { selectedPlatform = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Duration presets: 15 / 30 / 45 / 60 / 90 sec
                    Text(text = "Target Duration:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("15s", "30s", "45s", "60s", "90s").forEach { d ->
                            FilterChip(
                                selected = selectedDuration == d,
                                onClick = { selectedDuration = d },
                                label = { Text(d, fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { handleGenerate() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("generate_short_reel_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GENERATE 9:16 BLUEPRINT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 9:16 Vertical Preview Area
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "9:16 VERTICAL PREVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated 9:16 Phone Screen Frame
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(340.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF831843))
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        // Top bar inside frame
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDuration,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(12.dp))
                        }

                        // On-Screen Hook in middle
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = plan.onScreenText,
                                    color = Color(0xFFFDE047),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Bottom CTA & Sound
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = plan.cta,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🎵 ${plan.audioIdea.take(32)}...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }

        // Script Details & Pacing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎯 RETENTION SCRIPT (30s & 60s):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPurple
                    )

                    Text(
                        text = if (selectedDuration == "30s" || selectedDuration == "15s") plan.script30s else plan.script60s,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider()

                    Text(text = "HASHTAGS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = plan.hashtags, fontSize = 12.sp, color = StudioCrimson)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${plan.hook}\n\n${plan.script30s}\n\n${plan.cta}\n\n${plan.hashtags}"))
                                Toast.makeText(context, "Copied entire reel text!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(I18n.t("copy", currentLanguage))
                        }

                        Button(
                            onClick = { handleSave() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(I18n.t("save", currentLanguage))
                        }
                    }
                }
            }
        }
    }
}
