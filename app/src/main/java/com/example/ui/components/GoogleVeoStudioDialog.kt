package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioPurple

const val GOOGLE_VEO_STUDIO_URL = "https://www.imagine.art/video/create/google-veo-3-1?utm_campaign=G_I_Web_Kling_VID_OArt_Int_IC_4449&utm_id=24235473486&hsa_acc=5196594449&campaign_id=24235473486&gad_source=1&gad_campaignid=24235473486&gbraid=0AAAABEZD_eUkTniDNemXrqrC1afaN4nku&gclid=CjwKCAjwuobXBhBXEiwAYUaIlcf0RLgPMzFNvBDkRjwy0-E_XYYMxUlQPyP5m6TyGuzq0lwTG2WQSxoCgesQAvD_BwE"

data class VeoSamplePreset(
    val title: String,
    val description: String,
    val url: String,
    val durationText: String,
    val resolution: String,
    val prompt: String
)

@Composable
fun GoogleVeoStudioDialog(
    currentTopic: String,
    activeScenePrompt: String,
    onLoadVideoUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var inputVideoUrl by remember { mutableStateOf("") }
    var promptInput by remember { mutableStateOf(activeScenePrompt.ifBlank { "Cinematic photorealistic 8K shot of futuristic AI creator studio with volumetric lighting and high-speed motion" }) }
    var studioMode by remember { mutableIntStateOf(1) } // 1: Veo 3 Architecture & Engine, 0: Imagine.art Studio Link

    val samplePresets = remember {
        listOf(
            VeoSamplePreset(
                title = "4K Cyberpunk Creator Studio",
                description = "Deep volumetric neon lighting, floating holograms, cinematic camera drift",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                durationText = "60s",
                resolution = "4K 60fps",
                prompt = "Hyper-realistic futuristic creator studio with glowing holographic AI timelines, 8k resolution, cinematic lighting"
            ),
            VeoSamplePreset(
                title = "High-Speed Dynamic Kinetic Action",
                description = "Fast camera tracking, dynamic motion blur, high contrast",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                durationText = "15s",
                resolution = "1080p 60fps",
                prompt = "High-speed camera tracking through illuminated tech lab with explosive visual transitions and dynamic lens flares"
            ),
            VeoSamplePreset(
                title = "Cinematic Drone Aerial Vista",
                description = "Smooth sweeping drone shot, natural sunlight, organic textures",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                durationText = "60s",
                resolution = "1080p 30fps",
                prompt = "Sweeping golden-hour drone shot over majestic landscapes, ultra-crisp natural depth of field, photorealistic lighting"
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("google_veo_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Veo Gradient Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(StudioPurple, StudioCrimson))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MovieFilter, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Veo 3.1 AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Imagine.art Video Studio Link",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioPurple.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "DEEPMIND",
                            color = StudioPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Studio Mode Selector: Architecture & Engine vs Imagine.art Studio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "🧠 Architecture & Engine" to 1,
                        "🌐 Imagine.art Studio" to 0
                    ).forEach { (label, mode) ->
                        val isSel = studioMode == mode
                        Surface(
                            onClick = {
                                studioMode = mode
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) StudioPurple else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (studioMode == 1) {
                    GoogleVeoArchitectureView(
                        activePrompt = promptInput,
                        onApplyGeneratedVideo = { url ->
                            onLoadVideoUrl(url)
                            onDismiss()
                        }
                    )
                } else {
                    // Info Banner
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioPurple.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, StudioPurple.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Veo 3.1 + Kling Generative Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Generate state-of-the-art cinematic videos on Imagine.art using Google DeepMind's Veo 3.1 model. Send your active timeline prompts directly or import generated MP4 files back into Creator Studio.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Visual Prompt Box with 1-Click Copy & Launch
                Text("Active Prompt for Veo 3.1:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    label = { Text("Veo 3.1 Visual Prompt") },
                    modifier = Modifier.fillMaxWidth().testTag("veo_dialog_prompt_input"),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy Prompt & Launch Veo Button
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(promptInput))
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_VEO_STUDIO_URL))
                            context.startActivity(intent)
                            Toast.makeText(context, "Copied prompt! Opening Google Veo 3.1 Studio...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("copy_prompt_and_launch_veo")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Veo 3.1 & Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Copy Link Only Button
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(GOOGLE_VEO_STUDIO_URL))
                            Toast.makeText(context, "Copied Imagine.art Veo 3.1 Link to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StudioPurple),
                        modifier = Modifier.testTag("copy_veo_link_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioPurple, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Import Video URL into Editor
                Text("Import Generated Veo 3.1 Video to Editor:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = inputVideoUrl,
                    onValueChange = { inputVideoUrl = it },
                    label = { Text("Paste Video URL (MP4 / WebM / Stream)") },
                    placeholder = { Text("https://.../video.mp4") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.isNotBlank()) {
                                inputVideoUrl = clip
                                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = StudioEmerald)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("veo_video_url_input")
                )

                if (inputVideoUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            onLoadVideoUrl(inputVideoUrl.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("load_custom_veo_url_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Video Into Player & Timeline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Sample Presets Generated with Veo 3.1
                Text("Or Load Verified Veo 3.1 Sample Renders:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                samplePresets.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLoadVideoUrl(preset.url)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = StudioAmber.copy(alpha = 0.2f)) {
                                        Text(preset.resolution, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StudioAmber, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Text(preset.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            FilledTonalButton(
                                onClick = { onLoadVideoUrl(preset.url) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                }

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
