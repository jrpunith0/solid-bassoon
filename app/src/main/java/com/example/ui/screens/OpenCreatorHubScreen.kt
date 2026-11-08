package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.opencreator.*
import com.example.ui.theme.*
import java.io.File

enum class OpenCreatorCategoryTab(val label: String, val icon: String) {
    VIDEO("Video Motion", "🎬"),
    WRITING("Writing Skills", "✍️"),
    PIPELINES("Pipelines", "⚡"),
    COVERS("Cover Styles", "🖼️"),
    VOICES("TTS Voices", "🎙️"),
    SOURCES("Platforms", "🌐"),
    IMAGES("Image Presets", "🎨"),
    SOURCE_CODE("Source File", "📄")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenCreatorHubScreen(
    currentLanguage: String,
    onNavigateToVideoEditorWithPreset: ((presetTitle: String, prompt: String, isVertical: Boolean) -> Unit)? = null,
    onNavigateToScriptGenWithPreset: ((topic: String, framework: String) -> Unit)? = null,
    onNavigateToThumbnailWithStyle: ((styleName: String) -> Unit)? = null,
    isBatchReprocessing: Boolean = false,
    batchReprocessProgress: Float = 0f,
    batchReprocessStatus: String = "",
    onReprocessAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(OpenCreatorCategoryTab.VIDEO) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }
    var expandedItemDetails by remember { mutableStateOf<String?>(null) }

    // Check if the source code file exists on disk
    val localFileExists = remember {
        val f = File(OpenCreatorCatalog.SOURCE_FILE_PATH)
        f.exists() || File("/" + OpenCreatorCatalog.SOURCE_FILE_PATH).exists()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "OpenCreator Architecture & Presets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = StudioCrimson.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "DATA SYNCED",
                                    color = StudioCrimson,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Source: ${OpenCreatorCatalog.SOURCE_FILE_PATH} (${OpenCreatorCatalog.TOTAL_SOURCE_LINES} lines)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("opencreator_hub_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Hero banner explaining source integration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(StudioCrimson.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Hub,
                            contentDescription = null,
                            tint = StudioCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "OpenCreator Master Repository",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            "28 video generation templates, 5 editorial cover styles, 28 voice profiles, 7 writing skills & 4 video pipelines imported directly.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        if (onReprocessAll != null) {
                            Spacer(Modifier.height(8.dp))
                            if (isBatchReprocessing) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    LinearProgressIndicator(
                                        progress = { batchReprocessProgress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = StudioCrimson
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        batchReprocessStatus.ifBlank { "Reprocessing with OpenCreator data..." },
                                        fontSize = 10.sp,
                                        color = StudioCrimson,
                                        maxLines = 1
                                    )
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = onReprocessAll,
                                    modifier = Modifier.fillMaxWidth().testTag("reprocess_all_opencreator_button"),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = StudioCrimson.copy(alpha = 0.15f),
                                        contentColor = StudioCrimson
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("⚡ Reprocess All with OpenCreator Spec", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("opencreator_search_field"),
                placeholder = { Text("Search 28+ presets, styles, tags, or voices...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp,
                divider = {}
            ) {
                OpenCreatorCategoryTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            selectedTagFilter = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.icon, fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Content Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    OpenCreatorCategoryTab.VIDEO -> VideoPresetsContent(
                        searchQuery = searchQuery,
                        selectedTag = selectedTagFilter,
                        onSelectTag = { selectedTagFilter = if (selectedTagFilter == it) null else it },
                        onApplyPreset = { preset ->
                            clipboardManager.setText(AnnotatedString(preset.defaultPrompt))
                            Toast.makeText(context, "Prompt copied! Opening Editor...", Toast.LENGTH_SHORT).show()
                            onNavigateToVideoEditorWithPreset?.invoke(preset.titleEn, preset.defaultPrompt, preset.isVertical)
                        },
                        onSendToScript = { preset ->
                            onNavigateToScriptGenWithPreset?.invoke(preset.titleEn, "standard")
                        }
                    )

                    OpenCreatorCategoryTab.WRITING -> WritingTemplatesContent(
                        searchQuery = searchQuery,
                        onUseTemplate = { template ->
                            val fwId = when (template.id) {
                                "community.sun-style-writing" -> "sun_style"
                                "community.khazix-writer" -> "khazix"
                                "community.research-paper-writing" -> "research"
                                "official.insight" -> "deep_insight"
                                "official.tutorial" -> "tutorial"
                                "official.news-analysis" -> "news"
                                else -> "standard"
                            }
                            onNavigateToScriptGenWithPreset?.invoke(template.nameEn, fwId)
                        }
                    )

                    OpenCreatorCategoryTab.PIPELINES -> PipelinesContent(
                        onOpenInVideoEditor = {
                            onNavigateToVideoEditorWithPreset?.invoke("KrillinAI Pipeline Render", "Remotion Render", false)
                        }
                    )

                    OpenCreatorCategoryTab.COVERS -> CoverStylesContent(
                        searchQuery = searchQuery,
                        onUseStyleInThumbnail = { style ->
                            onNavigateToThumbnailWithStyle?.invoke(style.name)
                        }
                    )

                    OpenCreatorCategoryTab.VOICES -> TtsVoicesContent(
                        searchQuery = searchQuery
                    )

                    OpenCreatorCategoryTab.SOURCES -> PlatformSourcesContent(
                        searchQuery = searchQuery
                    )

                    OpenCreatorCategoryTab.IMAGES -> ImagePresetsContent(
                        searchQuery = searchQuery
                    )

                    OpenCreatorCategoryTab.SOURCE_CODE -> SourceInspectorContent(
                        localFileExists = localFileExists
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPresetsContent(
    searchQuery: String,
    selectedTag: String?,
    onSelectTag: (String) -> Unit,
    onApplyPreset: (OpenCreatorVideoPreset) -> Unit,
    onSendToScript: (OpenCreatorVideoPreset) -> Unit
) {
    val allTags = remember {
        OpenCreatorCatalog.videoPresets.flatMap { it.tags }.distinct().sorted()
    }

    val filteredPresets = remember(searchQuery, selectedTag) {
        OpenCreatorCatalog.videoPresets.filter { preset ->
            val matchQuery = searchQuery.isBlank() ||
                    preset.titleEn.contains(searchQuery, ignoreCase = true) ||
                    preset.titleZh.contains(searchQuery, ignoreCase = true) ||
                    preset.descriptionEn.contains(searchQuery, ignoreCase = true) ||
                    preset.tags.any { it.contains(searchQuery, ignoreCase = true) }
            val matchTag = selectedTag == null || preset.tags.contains(selectedTag)
            matchQuery && matchTag
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tag filters row
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allTags.take(15)) { tag ->
                val isSelected = tag == selectedTag
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTag(tag) },
                    label = { Text("#$tag", fontSize = 11.sp) }
                )
            }
        }

        Text(
            "Found ${filteredPresets.size} video templates",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredPresets, key = { it.id }) { preset ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                preset.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = if (preset.isVertical) StudioCyan.copy(alpha = 0.15f) else StudioPurple.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (preset.isVertical) "9:16 VERTICAL" else "16:9 HORIZONTAL",
                                    color = if (preset.isVertical) StudioCyan else StudioPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (preset.titleZh.isNotBlank()) {
                            Text(
                                preset.titleZh,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            preset.descriptionEn,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                tint = StudioCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Camera: ${preset.cameraMovement}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Tags
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            preset.tags.take(4).forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "#$tag",
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onSendToScript(preset) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Use in Script", fontSize = 11.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onApplyPreset(preset) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Apply to Editor", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverStylesContent(
    searchQuery: String,
    onUseStyleInThumbnail: (OpenCreatorCoverStyle) -> Unit
) {
    val filtered = remember(searchQuery) {
        OpenCreatorCatalog.coverStyles.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Editorial Cover & Thumbnail Styles from OpenCreator Protocol",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                "Curated color harmonies, typography instructions, and visual contrast rules for viral thumbnails.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filtered, key = { it.id }) { style ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(style.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(style.description, fontSize = 12.sp, lineHeight = 16.sp)

                    Spacer(Modifier.height(10.dp))
                    Text("Official Color Palette:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        style.colorPalette.forEach { hex ->
                            val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(color, RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                )
                                Text(hex, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatPaint, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(style.typographyHint, fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onUseStyleInThumbnail(style) },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Use in Thumbnail Creator", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TtsVoicesContent(searchQuery: String) {
    val filtered = remember(searchQuery) {
        OpenCreatorCatalog.ttsVoices.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.scenario.contains(searchQuery, ignoreCase = true) ||
                    it.provider.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("OpenCreator TTS Voice Catalog (${OpenCreatorCatalog.ttsVoices.size} voices)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Multi-model voice library powering OpenCreator smart dubbing and narrations.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(filtered, key = { "${it.provider}_${it.id}" }) { voice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (voice.provider == "openai") StudioCrimson.copy(alpha = 0.15f) else StudioCyan.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (voice.gender == "female") Icons.Default.Face else Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (voice.provider == "openai") StudioCrimson else StudioCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(voice.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (voice.isRecommended) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = StudioAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "RECOMMENDED",
                                        color = StudioAmber,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text("Scenario: ${voice.scenario}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            voice.provider.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformSourcesContent(searchQuery: String) {
    val filtered = remember(searchQuery) {
        OpenCreatorCatalog.platformSources.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("OpenCreator Platform Extractors & Sources", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Engineered extraction plugins for multi-platform repurposing.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(filtered, key = { it.id }) { platform ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = StudioCrimson)
                        Spacer(Modifier.width(8.dp))
                        Text(platform.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(platform.description, fontSize = 12.sp, lineHeight = 16.sp)

                    Spacer(Modifier.height(8.dp))
                    Text("Supported Media Streams:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        platform.supportedTypes.forEach { type ->
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                                Text(type, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePresetsContent(searchQuery: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filtered = remember(searchQuery) {
        OpenCreatorCatalog.imagePresets.filter {
            searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Featured Image Generation Presets", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Selected from OpenCreator's 79 image template catalogue.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(filtered, key = { it.id }) { img ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(img.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Surface(color = StudioCrimson.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                            Text(img.category, color = StudioCrimson, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(img.description, fontSize = 12.sp, lineHeight = 16.sp)

                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            img.promptTemplate,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(img.promptTemplate))
                            Toast.makeText(context, "Image prompt template copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy Prompt", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceInspectorContent(localFileExists: Boolean) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = StudioCrimson)
                        Spacer(Modifier.width(8.dp))
                        Text("Attached File Specification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))

                    Text("File Path:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        OpenCreatorCatalog.SOURCE_FILE_PATH,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioCrimson
                    )

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Source Lines", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("621,254 lines", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Raw File Size", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("32.57 MB", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (localFileExists) "✓ Verified" else "Integrated",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioEmerald
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Architecture Breakdown:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("• apps/daemon: Node.js server, MCP agent tools, scheduled tasks, OpenRouter contracts", fontSize = 11.sp)
                    Text("• apps/web: React 18, timeline editor, TTS voice picker, approval panel, diagnostics", fontSize = 11.sp)
                    Text("• runtime/krillinai: Go media engine, audio2subtitle, dubbing, Whisper integration", fontSize = 11.sp)
                    Text("• template/: 28 video generation templates, 79 image templates, cover style presets", fontSize = 11.sp)

                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(OpenCreatorCatalog.SOURCE_FILE_PATH))
                            Toast.makeText(context, "Path copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy File Path", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WritingTemplatesContent(
    searchQuery: String,
    onUseTemplate: (OpenCreatorWritingTemplate) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(searchQuery) {
        OpenCreatorCatalog.writingTemplates.filter { t ->
            searchQuery.isBlank() ||
                    t.nameEn.contains(searchQuery, ignoreCase = true) ||
                    t.nameZh.contains(searchQuery, ignoreCase = true) ||
                    t.descriptionEn.contains(searchQuery, ignoreCase = true) ||
                    t.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StudioPurple.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, StudioPurple.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✍️", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "OpenCreator Writing Templates & Skills",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Extracted from packages/writing-templates (Sun-style, Khazix WeChat long-form, Research paper, Deep insight, Narrative story, Practical tutorial, News analysis).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { template ->
            val isExpanded = expandedId == template.id
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(template.nameEn, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(template.nameZh, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = StudioPurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                template.categoryId.uppercase(),
                                color = StudioPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(template.descriptionEn, fontSize = 12.sp)

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        template.tags.take(3).forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("#$tag", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Structure Flow:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        template.structure.forEachIndexed { idx, step ->
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "${idx + 1}. $step",
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(10.dp))

                        Text("Instructions & Writing Methodology:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(template.instructions, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(8.dp))
                        Text("Author / Origin:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(template.author, fontSize = 10.sp, color = StudioCrimson)

                        Spacer(Modifier.height(8.dp))
                        Text("Topic Rule:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(template.stagePromptTopics, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(Modifier.height(6.dp))
                        Text("QA & Review Criteria:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(template.stagePromptReview, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { expandedId = if (isExpanded) null else template.id }) {
                            Text(if (isExpanded) "Show Less" else "View Rulebook", fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("${template.nameEn}\n${template.instructions}\n\nStructure:\n${template.structure.joinToString("\n")}"))
                                    Toast.makeText(context, "Copied template spec", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onUseTemplate(template) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Use in Script Gen", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelinesContent(
    onOpenInVideoEditor: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StudioCyan.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "KrillinAI Video Rendering & Subtitle Pipelines",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Architecture from OpenCreator runtime/krillinai and skills/krillinai-* covering automated Remotion rendering, FunASR alignment, and multi-track mastering.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(OpenCreatorCatalog.pipelineSpecs, key = { it.id }) { spec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(spec.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            color = StudioCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                spec.targetAspect,
                                color = StudioCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(spec.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(8.dp))
                    Text("Engine: ${spec.engine}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StudioEmerald)
                    Text("Output: ${spec.outputFormat}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(10.dp))
                    Text("Pipeline Execution Stages:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    spec.stages.forEachIndexed { idx, stage ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${idx + 1}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(stage, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${spec.title}\nEngine: ${spec.engine}\nStages:\n${spec.stages.joinToString("\n")}\nFormat: ${spec.outputFormat}"))
                                Toast.makeText(context, "Copied pipeline spec", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy Spec", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onOpenInVideoEditor,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Open in Editor", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
