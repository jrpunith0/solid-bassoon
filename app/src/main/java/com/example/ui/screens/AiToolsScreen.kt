package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.ContentGenerator
import com.example.i18n.I18n
import com.example.ui.components.GOOGLE_VEO_STUDIO_URL
import com.example.ui.theme.*

data class AiStudioTool(
    val id: String,
    val name: String,
    val description: String,
    val defaultPrompt: String
)

@Composable
fun AiToolsScreen(
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    val allTools = remember {
        listOf(
            AiStudioTool("hooks", "Viral Hook Generator", "Generate 5 high-converting opening hooks for videos", "Social Media Growth"),
            AiStudioTool("titles", "Title Generator", "Produce 5 clickable, curiosity-inducing video titles", "How to Edit Faster"),
            AiStudioTool("hashtags", "Hashtag Generator", "Get niche, trending, and viral platform hashtags", "Creator Studio"),
            AiStudioTool("repurposer", "Content Repurposer", "Turn 1 long video into YouTube, Reel, Post & Poll", "My Journey as a Creator"),
            AiStudioTool("cta", "CTA Generator", "Engaging call-to-actions for comments, likes & follows", "Weekly Vlog"),
            AiStudioTool("ideas", "Video Idea Generator", "10 fresh content angles and concepts in your niche", "Photography & Gear"),
            AiStudioTool("desc", "Description Generator", "SEO-optimized YouTube video description with chapters", "Top 5 Mistakes"),
            AiStudioTool("thumb_prompt", "Thumbnail Prompt Generator", "Detailed generative visual prompts for thumbnails", "Shocking Truth"),
            AiStudioTool("story", "Story Generator", "Relatable creator narrative for community building", "Behind the Scenes"),
            AiStudioTool("reel", "Reel Generator", "Complete 30-second vertical retention sequence", "Quick Morning Hack"),
            AiStudioTool("shorts", "Shorts Generator", "Fast-paced YouTube Short with visual cut timestamps", "Secret Shortcut"),
            AiStudioTool("script", "Script Generator", "Structured script with hook, body, and conclusion", "How to Speak on Camera"),
            AiStudioTool("opencreator", "OpenCreator Motion Presets", "28 cinematic camera motion blueprints & templates", "Aerial Pullback"),
            AiStudioTool("ai_shorts_gen", "AI Shorts Video Generator", "Automated text-to-shorts pipeline from Anil-matcha/AI-Youtube-Shorts-Generator", "3 Secrets Nobody Tells You"),
            AiStudioTool("google_veo", "Google Veo 3.1 Video AI", "Imagine.art Google Veo 3.1 & Kling deep generative video pipeline", "Cyberpunk cinematic city with flying drones in 8K resolution")
        )
    }

    var selectedToolId by remember { mutableStateOf(allTools.first().id) }
    val currentTool = allTools.first { it.id == selectedToolId }

    var inputTopic by remember { mutableStateOf(currentTool.defaultPrompt) }
    var generatedResult by remember { mutableStateOf("") }

    fun executeGeneration() {
        val topic = inputTopic.ifBlank { "Creator Content" }
        generatedResult = when (currentTool.id) {
            "hooks" -> """
1. Stop doing this if you want to grow your audience in 2026.
2. 99% of people get this wrong about $topic.
3. This 10-second habit completely doubled my reach.
4. I wish someone told me this before I started creating videos.
5. Watch until the end, because point #3 will save you hours.
            """.trimIndent()

            "titles" -> """
• Why Top Creators Are Obsessed with $topic
• The Secret $topic Blueprint (Step-by-Step)
• DON'T Make This $topic Mistake!
• How Anyone Can Master $topic in 7 Days
• The Truth About $topic Nobody Is Talking About
            """.trimIndent()

            "hashtags" -> {
                ContentGenerator.generateHashtags(topic, "All").joinToString(" ")
            }

            "repurposer" -> """
📺 YOUTUBE:
Title: The Ultimate Guide to $topic
Hook: How to scale without burning out

📱 INSTAGRAM REEL:
Hook: 3 shortcuts to $topic
Audio: Upbeat trending lo-fi
CTA: Comment 'GUIDE' for the free cheat sheet!

📘 FACEBOOK POST:
Community discussion on $topic and personal lessons learned.

📊 COMMUNITY POLL:
"What is your biggest struggle with $topic?"
[A] Getting Started  [B] Consistency  [C] Quality  [D] Time
            """.trimIndent()

            "cta" -> """
1. Drop a comment below with your favorite tip!
2. Save this post right now so you don't lose it when you create today.
3. Share this with one friend who needs to hear this.
4. Hit subscribe and tap the bell icon for weekly creator tutorials!
            """.trimIndent()

            "ideas" -> """
1. "A Day in My Life as a Creator focusing on $topic"
2. "I Tried the Most Popular $topic Advice (Here's What Happened)"
3. "The Ultimate Beginner Gear Guide for $topic"
4. "5 Things I Stopped Doing to Gain 100K Followers"
5. "$topic Tier List: The Best and Worst Strategies"
            """.trimIndent()

            "thumb_prompt" -> """
Hyper-detailed 16:9 thumbnail, dramatic split lighting, high-contrast expressive facial expression, neon glowing text reading '$topic', clean cinematic studio depth of field, 8k resolution, photorealistic.
            """.trimIndent()

            "opencreator" -> """
🎬 OPENCREATOR 28-PRESET MOTION CATALOG ($topic):
• Aerial Pullback and Rise Reveal: Smooth backward/upward crane movement along subject axis.
• Anime Skateboard Chase NYC: Dynamic velocity lines, low camera angle, high shutter speed.
• Dolly Zoom Space Warp: Vertigo optical effect compressing perspective on '$topic'.
• Drone Orbit Around Subject: 360-degree fluid horizon tracking with foreground parallax.
• Retro Illustration Micro-Motion: Nostalgic hand-drawn textures with subtle character breathing.
• Sci-Fi Motorcycle Action: Rain-slicked asphalt, neon rim lighting, fast pan tracking.

📂 Imported directly from OpenCreator source repository:
Attachment: public/assets/aistudio/OpenCreator_All_Source_Code_8b88ab46.txt
            """.trimIndent()

            "ai_shorts_gen" -> """
🎬 AI YOUTUBE SHORTS GENERATOR (Anil-matcha/AI-Youtube-Shorts-Generator)
═════════════════════════════════════════════════════════
📌 Topic: $topic
🎯 Format: 9:16 Vertical Viral YouTube Short (60 seconds)
🗣️ Audio Engine: AI Voiceover Speech Synthesis (TTS) & Styled Subtitles

⏱️ SCENE 1 [00:00 - 00:05]
• Visual Cut: High contrast cinematic close-up with neon cyan backlight
• Subtitle Overlay: "Stop scrolling! This 1 AI trick changes everything."
• Voiceover: "If you're still creating YouTube Shorts manually, you're wasting 10 hours a week."

⏱️ SCENE 2 [00:05 - 00:25]
• Visual Cut: Dynamic split screen action with smooth motion blur and zoom punch
• Subtitle Overlay: "Step 1: Automated script generation from trending prompts."
• Voiceover: "Here is the exact framework top creators use to automate their daily Shorts pipeline."

⏱️ SCENE 3 [00:25 - 00:50]
• Visual Cut: Step-by-step studio workflow with animated highlight boxes and glowing pointers
• Subtitle Overlay: "Step 2: Sync audio keyframes with visual cuts."
• Voiceover: "Every beat drop triggers a visual transition. Notice how retention stays above 90%."

⏱️ SCENE 4 [00:50 - 00:60]
• Visual Cut: Creator smiling in studio, pointing to subscribe button with animated graphics
• Subtitle Overlay: "Follow for daily AI video creator secrets!"
• Voiceover: "Double tap if this helped, and subscribe for tomorrow's AI breakdown!"

🚀 GITHUB REPO DATA LINK:
• Repo: https://github.com/Anil-matcha/AI-Youtube-Shorts-Generator
• Pipeline: Text-to-Video Engine • Ready to sync & publish!
            """.trimIndent()

            "google_veo" -> """
🎬 GOOGLE VEO 3.1 AI GENERATIVE VIDEO PIPELINE (Imagine.art)
═════════════════════════════════════════════════════════
📌 Visual Prompt: $topic
🎯 Engine: Google DeepMind Veo 3.1 & Kling Architecture
📐 Recommended Aspect Ratio: 16:9 Cinematic / 9:16 Shorts
⏱️ Target Duration: 5 - 15 seconds per shot
🎥 Camera Physics: Volumetric raytracing, organic motion physics, hyper-realistic depth

💡 OPTIMIZED VEO 3.1 PROMPT:
"Photorealistic 8K cinematic footage of $topic, shot on 35mm anamorphic lens, shallow depth of field, dramatic atmospheric haze, volumetric key lighting, smooth continuous camera motion, hyper-detailed textures, octane render realism."

🚀 DIRECT STUDIO LINK:
https://www.imagine.art/video/create/google-veo-3-1
Tap 'LAUNCH GOOGLE VEO 3.1 STUDIO' below to generate your video on Imagine.art!
            """.trimIndent()

            else -> """
Generated Content Plan for '$topic':
- Target Audience: Engaged digital creators
- Core Value: Actionable, step-by-step guidance
- Retention Pacing: Dynamic scene transitions every 3 seconds
- Primary Takeaway: Consistent execution with clear feedback loops
            """.trimIndent()
        }
        Toast.makeText(context, "${currentTool.name} generated!", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(selectedToolId) {
        inputTopic = currentTool.defaultPrompt
        executeGeneration()
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
                text = I18n.t("nav_ai_tools", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isKn) "12 ಸೃಜನಶೀಲ ಪರಿಕರಗಳು: ಐಡಿಯಾಗಳು, ಹುಕ್‌ಗಳು, ಹ್ಯಾಶ್‌ಟ್ಯಾಗ್‌ಗಳು ಮತ್ತು ಮರುಬಳಕೆ" else "12 specialized creator studio engines: Hooks, Titles, Tags, Repurposer & CTAs",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Horizontal Tool Selector Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(allTools) { tool ->
                    FilterChip(
                        selected = selectedToolId == tool.id,
                        onClick = { selectedToolId = tool.id },
                        label = { Text(tool.name, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Active Tool Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = currentTool.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StudioCrimson)
                    Text(text = currentTool.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = inputTopic,
                        onValueChange = { inputTopic = it },
                        label = { Text("Topic or Keyword") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = { executeGeneration() },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ai_tool_generate_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GENERATE ${currentTool.name.uppercase()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Results Card
        if (generatedResult.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "GENERATED OUTPUT", fontSize = 12.sp, fontWeight = FontWeight.Black, color = StudioEmerald)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(generatedResult))
                                Toast.makeText(context, "Copied output!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = StudioCrimson)
                            }
                        }

                        Text(
                            text = generatedResult,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (currentTool.id == "google_veo") {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_VEO_STUDIO_URL))
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Opening Google Veo 3.1 Studio on Imagine.art...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("launch_veo_from_ai_tools")
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LAUNCH GOOGLE VEO 3.1 STUDIO (IMAGINE.ART)", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedResult))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(I18n.t("copy", currentLanguage))
                        }
                    }
                }
            }
        }
    }
}
