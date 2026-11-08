package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedScriptEntity
import com.example.generator.ContentGenerator
import com.example.i18n.I18n
import com.example.ui.theme.*

@Composable
fun ScriptGeneratorScreen(
    currentLanguage: String,
    onSaveScript: (SavedScriptEntity) -> Unit,
    initialTopic: String? = null,
    initialFramework: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isKn = currentLanguage == I18n.LANG_KN

    var topic by remember(initialTopic) { mutableStateOf(initialTopic ?: "") }
    var selectedPlatform by remember { mutableStateOf("YouTube") }
    var selectedFramework by remember(initialFramework) { mutableStateOf(initialFramework ?: "standard") }
    var duration by remember { mutableStateOf("60s") }
    var audience by remember { mutableStateOf("General Creators") }
    var tone by remember { mutableStateOf("Engaging & Punchy") }
    var scriptLanguage by remember { mutableStateOf(currentLanguage) }

    var generatedScript by remember {
        mutableStateOf(
            ContentGenerator.generateScript(
                topic = initialTopic ?: "Social Media Growth Secret",
                platform = "YouTube",
                language = currentLanguage,
                duration = "60s",
                tone = "Engaging",
                framework = initialFramework ?: "standard"
            )
        )
    }

    fun handleCopy() {
        clipboardManager.setText(AnnotatedString(generatedScript.fullText()))
        Toast.makeText(context, I18n.t("copied", currentLanguage), Toast.LENGTH_SHORT).show()
    }

    fun handleSave() {
        onSaveScript(
            SavedScriptEntity(
                title = generatedScript.title,
                topic = topic.ifBlank { "Social Media Growth" },
                platform = selectedPlatform,
                language = scriptLanguage,
                duration = duration,
                tone = tone,
                hook = generatedScript.openingHook,
                bodyContent = "${generatedScript.mainContent}\n\n${generatedScript.storyExplanation}",
                cta = generatedScript.callToAction
            )
        )
        Toast.makeText(context, I18n.t("saved", currentLanguage), Toast.LENGTH_SHORT).show()
    }

    fun applyModifier(mod: String) {
        generatedScript = ContentGenerator.generateScript(
            topic = topic,
            platform = selectedPlatform,
            language = scriptLanguage,
            duration = duration,
            tone = tone,
            modifier = mod,
            framework = selectedFramework
        )
        Toast.makeText(context, "Script regenerated ($mod)! [Framework: $selectedFramework]", Toast.LENGTH_SHORT).show()
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
                text = I18n.t("nav_script_gen", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isKn) "ಕನ್ನಡ & ಇಂಗ್ಲಿಷ್‌ನಲ್ಲಿ ಆಕರ್ಷಕ ಸ್ಕ್ರಿಪ್ಟ್‌ಗಳನ್ನು ರಚಿಸಿ" else "Generate viral, retention-engineered scripts for YouTube, IG & FB",
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
                        label = { Text(if (isKn) "ವಿಷಯ ಅಥವಾ ಐಡಿಯಾ (Topic)" else "Video Topic / Core Idea") },
                        placeholder = { Text(if (isKn) "ಉದಾ: ಮೊದಲ 1000 ಸಬ್‌ಸ್ಕ್ರೈಬರ್ ಪಡೆಯುವುದು ಹೇಗೆ" else "e.g., How to get your first 1,000 subscribers") },
                        modifier = Modifier.fillMaxWidth().testTag("script_topic_input"),
                        singleLine = true
                    )

                    // Platform Selection
                    Text(text = if (isKn) "ಪ್ಲಾಟ್‌ಫಾರ್ಮ್:" else "Platform:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("YouTube", "Instagram", "Facebook").forEach { p ->
                            FilterChip(
                                selected = selectedPlatform == p,
                                onClick = { selectedPlatform = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Language Selection
                    Text(text = if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ಭಾಷೆ (Script Language):" else "Language:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = scriptLanguage == I18n.LANG_KN,
                            onClick = { scriptLanguage = I18n.LANG_KN },
                            label = { Text("ಕನ್ನಡ (Kannada)") }
                        )
                        FilterChip(
                            selected = scriptLanguage == I18n.LANG_EN,
                            onClick = { scriptLanguage = I18n.LANG_EN },
                            label = { Text("English") }
                        )
                    }

                    // Duration presets
                    Text(text = if (isKn) "ಅವಧಿ (Duration):" else "Duration:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("30s", "60s", "90s", "3m", "8m").forEach { d ->
                            FilterChip(
                                selected = duration == d,
                                onClick = { duration = d },
                                label = { Text(d, fontSize = 11.sp) }
                            )
                        }
                    }

                    // OpenCreator Writing Skill Framework
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isKn) "OpenCreator ಬರವಣಿಗೆ ಚೌಕಟ್ಟು (Writing Skill):" else "OpenCreator Writing Skill:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = StudioCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "SOURCE SPEC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = StudioCrimson,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val frameworks = listOf(
                            "standard" to "Standard Viral",
                            "sun_style" to "Sun-Style (Objects)",
                            "khazix" to "Khazix (Conversational)",
                            "deep_insight" to "Deep Insight",
                            "research" to "Research Paper",
                            "tutorial" to "Practical Tutorial",
                            "news" to "News Analysis"
                        )
                        items(frameworks) { (id, label) ->
                            FilterChip(
                                selected = selectedFramework == id,
                                onClick = {
                                    selectedFramework = id
                                    applyModifier("standard")
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { applyModifier("standard") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("generate_script_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ರಚಿಸಿ (Generate Script)" else "GENERATE VIRAL SCRIPT",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Script Modifier Actions (Regenerate, Make Shorter, Make Longer, Viral, Professional, Emotional)
        item {
            Text(
                text = if (isKn) "ಸ್ಕ್ರಿಪ್ಟ್ ಶೈಲಿ ಬದಲಾಯಿಸಿ (Fine-Tune Style):" else "Fine-Tune Style:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilledTonalButton(onClick = { applyModifier("viral") }) {
                        Text("🔥 More Viral", fontSize = 11.sp)
                    }
                }
                item {
                    FilledTonalButton(onClick = { applyModifier("shorter") }) {
                        Text("✂ Shorter", fontSize = 11.sp)
                    }
                }
                item {
                    FilledTonalButton(onClick = { applyModifier("longer") }) {
                        Text("📖 Longer", fontSize = 11.sp)
                    }
                }
                item {
                    FilledTonalButton(onClick = { applyModifier("professional") }) {
                        Text("💼 Professional", fontSize = 11.sp)
                    }
                }
                item {
                    FilledTonalButton(onClick = { applyModifier("emotional") }) {
                        Text("❤️ Emotional", fontSize = 11.sp)
                    }
                }
                item {
                    FilledTonalButton(onClick = { applyModifier("standard") }) {
                        Text("🔄 Reset", fontSize = 11.sp)
                    }
                }
            }
        }

        // Generated Script Output (Sections: Opening Hook, Main Content, Story/Explanation, Ending, CTA)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = generatedScript.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(onClick = { handleCopy() }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Script", tint = StudioCrimson)
                            }
                            IconButton(onClick = { handleSave() }) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Script", tint = StudioEmerald)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Section 1: Opening Hook
                    ScriptSectionBlock(
                        title = "🎯 OPENING HOOK (0-5s)",
                        content = generatedScript.openingHook,
                        bgColor = Color(0xFFFEF3C7),
                        textColor = Color(0xFF92400E)
                    )

                    // Section 2: Main Content
                    ScriptSectionBlock(
                        title = "💡 MAIN CONTENT",
                        content = generatedScript.mainContent,
                        bgColor = Color(0xFFEFF6FF),
                        textColor = Color(0xFF1E40AF)
                    )

                    // Section 3: Story / Explanation
                    ScriptSectionBlock(
                        title = "📖 STORY / EXPLANATION",
                        content = generatedScript.storyExplanation,
                        bgColor = Color(0xFFF3E8FF),
                        textColor = Color(0xFF6B21A8)
                    )

                    // Section 4: Ending
                    ScriptSectionBlock(
                        title = "🏁 ENDING",
                        content = generatedScript.ending,
                        bgColor = Color(0xFFF1F5F9),
                        textColor = Color(0xFF334155)
                    )

                    // Section 5: Call To Action
                    ScriptSectionBlock(
                        title = "🚀 CALL TO ACTION",
                        content = generatedScript.callToAction,
                        bgColor = Color(0xFFDCFCE7),
                        textColor = Color(0xFF166534)
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { handleCopy() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("copy_script_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(I18n.t("copy", currentLanguage))
                        }

                        Button(
                            onClick = { handleSave() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("save_script_button")
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

@Composable
fun ScriptSectionBlock(
    title: String,
    content: String,
    bgColor: Color,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Color.Black.copy(alpha = 0.85f)
        )
    }
}
