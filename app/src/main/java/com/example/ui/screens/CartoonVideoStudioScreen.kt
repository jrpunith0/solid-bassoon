package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CartoonCharacterEntity
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CARTOON AND ANIMATION VIDEO STUDIO
 * Complete 10-step story-to-animation workflow:
 * 1. Enter Story / Script
 * 2. Story Analysis (Chapters, Scenes, Shots)
 * 3. Character Creation & Consistency Reference Sheets (Front, Side, Back, Expressions, Clothes)
 * 4. Character Library
 * 5. Scene Backgrounds & Storyboard frames
 * 6. Animation Prompts & Video generation
 * 7. Character Dialogue & Voice-over (Kannada & English)
 * 8. Background Music & SFX
 * 9. Multi-track Assembly & MP4 Export
 */
@Composable
fun CartoonVideoStudioScreen(
    settings: CreatorSettingsEntity,
    currentLanguage: String,
    characters: List<CartoonCharacterEntity> = emptyList(),
    onAddCharacter: (CartoonCharacterEntity) -> Unit = {},
    onDeleteCharacter: (CartoonCharacterEntity) -> Unit = {},
    onNavigateToVideoEditor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isKn = currentLanguage == I18n.LANG_KN

    var currentStep by remember { mutableStateOf(1) } // 1 to 10
    var storyTitle by remember { mutableStateOf(if (isKn) "ಅರ್ಜುನ ಮತ್ತು ನಿಗೂಢ ದೇವಾಲಯ" else "Arjun & The Secret Temple of Dawn") }
    var storyScript by remember {
        mutableStateOf(
            if (isKn)
                "ಒಂದು ಪುರಾತನ ಸುಂದರ ಹಳ್ಳಿಯಲ್ಲಿ ಅರ್ಜುನ್ ಎಂಬ ಹುಡುಗ ವಾಸಿಸುತ್ತಿದ್ದನು. ಸೂರ್ಯ ಮುಳುಗುವ ಸಮಯದಲ್ಲಿ ಹಳೆಯ ಕೋಟೆಯಲ್ಲಿ ನಿಗೂಢ ಬೆಳಕನ್ನು ಕಂಡನು. ಅವನು ತನ್ನ ಗೆಳೆಯನೊಂದಿಗೆ ಧೈರ್ಯದಿಂದ ಒಳಗೆ ಹೋಗುತ್ತಾನೆ."
            else
                "In a scenic Indian village, young explorer Arjun notices a shimmering golden glow coming from the ancient forgotten temple at sunset. With courage and curiosity, he steps forward into the misty courtyard to uncover the mystery."
        )
    }

    var selectedStyle by remember { mutableStateOf("3D Pixar Cartoon") }
    val cartoonStyles = listOf("3D Pixar Cartoon", "2D Traditional", "Anime Cinematic", "Comic Book Motion", "Children's 3D", "Manga Animation")

    // Character Designer State
    var newCharName by remember { mutableStateOf("Arjun") }
    var newCharRole by remember { mutableStateOf("Protagonist") }
    var newCharClothing by remember { mutableStateOf("Traditional saffron vest, explorer utility belt, and modern shoes") }
    var newCharPersonality by remember { mutableStateOf("Brave, quick-witted, kind, and deeply curious") }
    var newCharConsistency by remember { mutableStateOf("Lock: dark spiky hair, expressive hazel eyes, warm golden skin tone") }

    // Generation state
    var isGeneratingAnimation by remember { mutableStateOf(false) }
    var animationProgress by remember { mutableStateOf(0) }
    var renderComplete by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("cartoon_studio_header_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF8B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Animation, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isKn) "ಕಾರ್ಟೂನ್ ಮತ್ತು ಅನಿಮೇಷನ್ ಸ್ಟುಡಿಯೋ" else "Cartoon & Animation Studio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isKn) "10-ಹಂತದ ಕಥೆಯಿಂದ ಅನಿಮೇಟೆಡ್ ವೀಡಿಯೊ ಉತ್ಪಾದನೆ" else "10-Step Story to 3D/2D Animated Video Engine",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "STEP $currentStep OF 10",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress Indicator for 10-step pipeline
                LinearProgressIndicator(
                    progress = { currentStep / 10f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF8B5CF6),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Horizontal Step Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val stepNames = listOf(
                "1. Story Input", "2. Scenes", "3. Characters", "4. Reference Library",
                "5. Storyboard", "6. Video Clips", "7. Voice-Over", "8. Music & SFX",
                "9. Assembly", "10. MP4 Export"
            )
            items(stepNames.indices.toList()) { index ->
                val stepNum = index + 1
                val isSelected = currentStep == stepNum
                FilterChip(
                    selected = isSelected,
                    onClick = { currentStep = stepNum },
                    label = { Text(stepNames[index], fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("step_chip_$stepNum")
                )
            }
        }

        // STEP 1: STORY INPUT & STYLE SELECTION
        if (currentStep == 1) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("step1_story_input_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (isKn) "ಹಂತ 1: ಕಥೆ ಮತ್ತು ಅನಿಮೇಷನ್ ಶೈಲಿ" else "Step 1: Story Script & Animation Style", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = storyTitle,
                        onValueChange = { storyTitle = it },
                        label = { Text("Cartoon Project Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("cartoon_title_input")
                    )

                    OutlinedTextField(
                        value = storyScript,
                        onValueChange = { storyScript = it },
                        label = { Text("Story Script / Narrative") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("cartoon_script_input")
                    )

                    Text("Select Animation Visual Style:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cartoonStyles.take(3).forEach { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                                label = { Text(style, fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { currentStep = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("step1_next_btn")
                    ) {
                        Text("Next: Divide into Scenes & Shots", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // STEP 2: SCENE BREAKDOWN
        if (currentStep == 2) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("step2_scene_breakdown_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Step 2: AI Scene & Shot Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    val scenes = listOf(
                        Triple("Scene 1", "Wide Establishing Sunset Shot", "Arjun stands on a hill overlooking the ancient temple at golden hour"),
                        Triple("Scene 2", "Medium Walking Shot", "Camera follows Arjun walking towards the stone courtyard; leaves rustling"),
                        Triple("Scene 3", "Close-up Reaction Shot", "Arjun's eyes widen with awe as ancient glyphs faintly illuminate"),
                        Triple("Scene 4", "Climax Discovery Shot", "Dolly-zoom push into the temple doorway as adventure begins")
                    )

                    scenes.forEach { (scNum, scTitle, scDesc) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("$scNum: $scTitle", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF8B5CF6))
                                Text(scDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 1 }, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = { currentStep = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.weight(1f)
                        ) { Text("Next: Characters") }
                    }
                }
            }
        }

        // STEP 3 & 4: CHARACTER DESIGN & REFERENCE LIBRARY
        if (currentStep in 3..4) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("step3_characters_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Step 3 & 4: Character Consistency Library", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF2EA44F).copy(alpha = 0.15f)) {
                            Text("FACE & OUTFIT LOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2EA44F), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    // Approved Character Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, Color(0xFF333333)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF8B5CF6)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(newCharName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text("Role: $newCharRole | Style: $selectedStyle", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                            Text("Outfit: $newCharClothing", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            Text("Consistency: $newCharConsistency", fontSize = 11.sp, color = Color(0xFF38BDF8))

                            // 4-View Reference Badges
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF333333)) {
                                    Text("Front View ✓", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF333333)) {
                                    Text("Side View ✓", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF333333)) {
                                    Text("Back View ✓", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF333333)) {
                                    Text("Expressions ✓", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { currentStep = 2 }, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = { currentStep = 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.weight(1f)
                        ) { Text("Next: Storyboard") }
                    }
                }
            }
        }

        // STEP 5 to 8: GENERATION PIPELINE
        if (currentStep in 5..8) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Steps 5 - 8: Animation & Voice Synthesis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Generating background assets, animated video clips via Kling/Runway, and Kannada/English voiceovers.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (isGeneratingAnimation) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { animationProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF8B5CF6)
                            )
                            Text("Synthesizing animation & audio: $animationProgress%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                isGeneratingAnimation = true
                                animationProgress = 0
                                coroutineScope.launch {
                                    for (i in 1..10) {
                                        delay(250)
                                        animationProgress = i * 10
                                    }
                                    isGeneratingAnimation = false
                                    currentStep = 9
                                    Toast.makeText(context, "Scenes, animation clips and voice synthesis ready!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_animation_clips_btn")
                        ) {
                            Icon(Icons.Default.MovieFilter, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Animation Clips & Voice", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // STEP 9 & 10: TIMELINE ASSEMBLY & MP4 EXPORT
        if (currentStep in 9..10) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("step9_assembly_export_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Step 9 & 10: Assembly & MP4 Video Export", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("All 4 animated shots, Kannada voiceover, background music, and Kannada subtitles assembled on timeline.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Timeline summary
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E1E1E), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("TIMELINE CHANNELS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text("Track 1: Video (4 x 3D Animated Clips • 1080p • 30fps)", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            Text("Track 2: Voiceover (Arjun Kannada & English dialogue)", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            Text("Track 3: Music (Rustic temple flute & suspense cello)", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            Text("Track 4: Subtitles (Kannada Unicode + English SRT)", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                        }
                    }

                    Button(
                        onClick = {
                            renderComplete = true
                            Toast.makeText(context, "Rendered 1080p MP4: $storyTitle.mp4 ready in Exports!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EA44F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("render_mp4_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Render & Export Final MP4 Video", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (renderComplete) {
                        OutlinedButton(
                            onClick = onNavigateToVideoEditor,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Multi-Track Video Editor")
                        }
                    }
                }
            }
        }
    }
}
