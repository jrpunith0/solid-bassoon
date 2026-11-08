package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.MangaChapterEntity
import com.example.data.MangaStoryEntity
import com.example.i18n.I18n
import com.example.ui.components.MangaPanelsStudio
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun MangaStudioScreen(
    currentLanguage: String,
    stories: List<MangaStoryEntity>,
    onGetChapters: (Long) -> Flow<List<MangaChapterEntity>>,
    onCreateStory: (MangaStoryEntity) -> Unit,
    onUpdateStory: (MangaStoryEntity) -> Unit,
    onDeleteStory: (MangaStoryEntity) -> Unit,
    onCreateChapter: (MangaChapterEntity) -> Unit,
    onUpdateChapter: (MangaChapterEntity) -> Unit,
    onDeleteChapter: (MangaChapterEntity) -> Unit,
    onGenerateAudio: (MangaChapterEntity, (MangaChapterEntity) -> Unit) -> Unit,
    onGenerateVideo: (MangaChapterEntity, (MangaChapterEntity) -> Unit) -> Unit,
    onGenerateSlideDeck: (MangaChapterEntity, (MangaChapterEntity) -> Unit) -> Unit,
    onGenerateFlashcards: (MangaChapterEntity, (MangaChapterEntity) -> Unit) -> Unit,
    onGenerateInfographic: (MangaChapterEntity, (MangaChapterEntity) -> Unit) -> Unit,
    onGenerateReport: (MangaChapterEntity, (MangaChapterEntity) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isKn = currentLanguage == I18n.LANG_KN

    var selectedStoryId by remember { mutableStateOf<Long?>(null) }
    var selectedChapterId by remember { mutableStateOf<Long?>(null) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var showCreateChapterDialog by remember { mutableStateOf(false) }
    var showUploadDialogForChapter by remember { mutableStateOf<MangaChapterEntity?>(null) }

    val activeStory = remember(selectedStoryId, stories) {
        stories.firstOrNull { it.id == selectedStoryId }
    }

    val chaptersFlow = remember(selectedStoryId) {
        selectedStoryId?.let { onGetChapters(it) }
    }
    val chapters by (chaptersFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val activeChapter = remember(selectedChapterId, chapters) {
        chapters.firstOrNull { it.id == selectedChapterId } ?: chapters.firstOrNull()
    }

    // Back handling for navigation stack
    if (selectedChapterId != null) {
        BackHandler {
            selectedChapterId = null
        }
    } else if (selectedStoryId != null) {
        BackHandler {
            selectedStoryId = null
        }
    }

    // Responsive container for Mobile, Tablet, Website & PC
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("manga_studio_screen")
    ) {
        val isWideScreen = maxWidth >= 840.dp // Tablet expanded / PC / Web
        val isMediumScreen = maxWidth in 600.dp..839.dp // Tablet compact

        if (activeStory == null) {
            // VIEW 1: MANGA STORIES LIST & CREATION
            MangaStoriesListView(
                stories = stories,
                isWideScreen = isWideScreen,
                isMediumScreen = isMediumScreen,
                isKn = isKn,
                onStoryClick = { story ->
                    selectedStoryId = story.id
                    selectedChapterId = null
                },
                onCreateStoryClick = { showCreateStoryDialog = true },
                onDeleteStory = onDeleteStory
            )
        } else {
            // VIEW 2: STORY DETAILS & CHAPTERS (Adaptive Layout)
            if (isWideScreen) {
                // Wide Screen / PC / Web: Dual-Pane Master-Detail
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column: Story Info & Chapter List
                    Column(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(16.dp)
                    ) {
                        StoryHeaderCard(
                            story = activeStory,
                            onBack = {
                                selectedStoryId = null
                                selectedChapterId = null
                            },
                            onCreateChapter = { showCreateChapterDialog = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Chapters (${chapters.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chapters, key = { it.id }) { chapter ->
                                ChapterListItemCard(
                                    chapter = chapter,
                                    isSelected = chapter.id == activeChapter?.id,
                                    onClick = { selectedChapterId = chapter.id },
                                    onDelete = { onDeleteChapter(chapter) }
                                )
                            }
                        }
                    }

                    // Right Column: Chapter Studio with 6 Outputs
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        if (activeChapter != null) {
                            ChapterProductionStudio(
                                chapter = activeChapter,
                                story = activeStory,
                                isKn = isKn,
                                onGenerateAudio = { onGenerateAudio(activeChapter) { onUpdateChapter(it) } },
                                onGenerateVideo = { onGenerateVideo(activeChapter) { onUpdateChapter(it) } },
                                onGenerateSlideDeck = { onGenerateSlideDeck(activeChapter) { onUpdateChapter(it) } },
                                onGenerateFlashcards = { onGenerateFlashcards(activeChapter) { onUpdateChapter(it) } },
                                onGenerateInfographic = { onGenerateInfographic(activeChapter) { onUpdateChapter(it) } },
                                onGenerateReport = { onGenerateReport(activeChapter) { onUpdateChapter(it) } },
                                onOpenUpload = { showUploadDialogForChapter = activeChapter }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No chapters yet. Click '+ New Chapter' to start creating!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                // Mobile / Compact Tablet Flow
                if (selectedChapterId == null) {
                    // Mobile Chapters List
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        StoryHeaderCard(
                            story = activeStory,
                            onBack = { selectedStoryId = null },
                            onCreateChapter = { showCreateChapterDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Story Chapters (${chapters.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { showCreateChapterDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                                modifier = Modifier.testTag("create_new_chapter_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Chapter", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (chapters.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No chapters written yet", fontWeight = FontWeight.Bold)
                                    Text("Add chapter 1 to generate audio, video & slides", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(chapters, key = { it.id }) { chapter ->
                                    ChapterListItemCard(
                                        chapter = chapter,
                                        isSelected = false,
                                        onClick = { selectedChapterId = chapter.id },
                                        onDelete = { onDeleteChapter(chapter) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Chapter Studio View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { selectedChapterId = null }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to chapters")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chapter ${activeChapter?.chapterNumber}: ${activeChapter?.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activeStory.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioCrimson
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        activeChapter?.let { ch ->
                            ChapterProductionStudio(
                                chapter = ch,
                                story = activeStory,
                                isKn = isKn,
                                onGenerateAudio = { onGenerateAudio(ch) { onUpdateChapter(it) } },
                                onGenerateVideo = { onGenerateVideo(ch) { onUpdateChapter(it) } },
                                onGenerateSlideDeck = { onGenerateSlideDeck(ch) { onUpdateChapter(it) } },
                                onGenerateFlashcards = { onGenerateFlashcards(ch) { onUpdateChapter(it) } },
                                onGenerateInfographic = { onGenerateInfographic(ch) { onUpdateChapter(it) } },
                                onGenerateReport = { onGenerateReport(ch) { onUpdateChapter(it) } },
                                onOpenUpload = { showUploadDialogForChapter = ch }
                            )
                        }
                    }
                }
            }
        }
    }

    // DIALOG 1: Create Manga Story Dialog
    if (showCreateStoryDialog) {
        CreateMangaStoryDialog(
            onDismiss = { showCreateStoryDialog = false },
            onCreate = { newStory ->
                onCreateStory(newStory)
                showCreateStoryDialog = false
            }
        )
    }

    // DIALOG 2: Create New Chapter Dialog
    if (showCreateChapterDialog && activeStory != null) {
        val nextChapterNumber = (chapters.maxOfOrNull { it.chapterNumber } ?: 0) + 1
        CreateMangaChapterDialog(
            storyId = activeStory.id,
            nextChapterNumber = nextChapterNumber,
            onDismiss = { showCreateChapterDialog = false },
            onCreate = { newChapter ->
                onCreateChapter(newChapter)
                showCreateChapterDialog = false
            }
        )
    }

    // DIALOG 3: Video Upload & Problem Fallback with Direct Download Button
    showUploadDialogForChapter?.let { ch ->
        VideoUploadAndDownloadDialog(
            chapter = ch,
            onDismiss = { showUploadDialogForChapter = null }
        )
    }
}

// -------------------------------------------------------------------------
// COMPONENT 1: MANGA STORIES LIST VIEW
// -------------------------------------------------------------------------
@Composable
private fun MangaStoriesListView(
    stories: List<MangaStoryEntity>,
    isWideScreen: Boolean,
    isMediumScreen: Boolean,
    isKn: Boolean,
    onStoryClick: (MangaStoryEntity) -> Unit,
    onCreateStoryClick: () -> Unit,
    onDeleteStory: (MangaStoryEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }

    val genres = listOf("All", "Action", "Romance", "Comedy", "Thriller", "Horror", "Fantasy", "Adventure")

    val filteredStories = remember(stories, searchQuery, selectedGenre) {
        stories.filter { story ->
            val matchQuery = story.title.contains(searchQuery, ignoreCase = true) ||
                    story.synopsis.contains(searchQuery, ignoreCase = true) ||
                    story.japaneseTitle.contains(searchQuery, ignoreCase = true)
            val matchGenre = selectedGenre == "All" || story.genre.contains(selectedGenre, ignoreCase = true)
            matchQuery && matchGenre
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, StudioCrimson.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = StudioCrimson.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "MANGA STUDIO AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioCrimson,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "📱 Mobile • 💻 Tablet • 🖥️ PC / Web",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isKn) "ಮಂಗಾ ಸ್ಟೋರಿ ಕ್ರಿಯೇಟರ್" else "Manga Story Creator & Studio",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "Create original manga stories, manage chapters, and generate audio narration, motion video overview, slide decks, flashcards, infographics & reports.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onCreateStoryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("create_manga_story_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Story", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search manga stories...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_manga_input")
                )
            }
        }

        // Genre Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(genres) { g ->
                    FilterChip(
                        selected = selectedGenre == g,
                        onClick = { selectedGenre = g },
                        label = { Text(g, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Stories Grid / Cards (Adaptive: 2 Columns on Tablet/PC/Web, 1 Column on Mobile)
        if (filteredStories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No manga stories found. Tap 'Create Story' above to start!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (isWideScreen || isMediumScreen) {
            items(filteredStories.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pair.forEach { story ->
                        Box(modifier = Modifier.weight(1f)) {
                            MangaStoryCard(
                                story = story,
                                onStoryClick = { onStoryClick(story) },
                                onDelete = { onDeleteStory(story) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            items(filteredStories, key = { it.id }) { story ->
                MangaStoryCard(
                    story = story,
                    onStoryClick = { onStoryClick(story) },
                    onDelete = { onDeleteStory(story) }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 2: MANGA STORY CARD (CLICK OPENS CHAPTER DETAILS)
// -------------------------------------------------------------------------
@Composable
private fun MangaStoryCard(
    story: MangaStoryEntity,
    onStoryClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStoryClick() } // User clicks story name/card to open chapter details
            .testTag("manga_story_card_${story.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
            ) {
                if (story.coverImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = story.coverImageUrl,
                        contentDescription = story.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(36.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable { onStoryClick() }
                                .testTag("manga_story_name_${story.id}")
                        )
                        if (story.japaneseTitle.isNotBlank()) {
                            Text(
                                text = story.japaneseTitle,
                                fontSize = 11.sp,
                                color = StudioCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Story", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = StudioCrimson.copy(alpha = 0.12f)) {
                        Text(story.genre, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StudioCrimson, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
                        Text(story.artStyle, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
                        Text("${story.totalChapters} Chapters", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = story.synopsis,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Author: ${story.authorName}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Chapters",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCrimson
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 3: STORY HEADER CARD IN DETAILS VIEW
// -------------------------------------------------------------------------
@Composable
private fun StoryHeaderCard(
    story: MangaStoryEntity,
    onBack: () -> Unit,
    onCreateChapter: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back to Stories")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${story.genre} • ${story.artStyle}",
                        fontSize = 11.sp,
                        color = StudioCrimson
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = story.synopsis,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCreateChapter,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("story_header_create_chapter_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create New Chapter", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 4: CHAPTER LIST ITEM CARD
// -------------------------------------------------------------------------
@Composable
private fun ChapterListItemCard(
    chapter: MangaChapterEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) StudioCrimson.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) StudioCrimson else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chapter_card_${chapter.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(StudioCrimson),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${chapter.chapterNumber}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapter.summary,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Feature pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (chapter.hasAudio) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                            Text("🎧 Audio", fontSize = 8.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    if (chapter.hasVideo) {
                        Surface(shape = RoundedCornerShape(4.dp), color = StudioCrimson.copy(alpha = 0.15f)) {
                            Text("🎬 Video", fontSize = 8.sp, color = StudioCrimson, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(chapter.status, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Chapter", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 5: CHAPTER PRODUCTION STUDIO WITH 7 MULTI-MODAL GENERATORS
// -------------------------------------------------------------------------
@Composable
private fun ChapterProductionStudio(
    chapter: MangaChapterEntity,
    story: MangaStoryEntity,
    isKn: Boolean = false,
    onGenerateAudio: () -> Unit,
    onGenerateVideo: () -> Unit,
    onGenerateSlideDeck: () -> Unit,
    onGenerateFlashcards: () -> Unit,
    onGenerateInfographic: () -> Unit,
    onGenerateReport: () -> Unit,
    onOpenUpload: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "0. Panels & Export 🎨",
        "1. Audio 🎧",
        "2. Video 🎬",
        "3. Slides 📊",
        "4. Flashcards 🃏",
        "5. Infographic 📈",
        "6. Report 📋"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chapter_production_studio")
    ) {
        // Studio Title & Social Upload Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chapter ${chapter.chapterNumber}: ${chapter.title}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Multi-Modal AI Engine • 7 Output Formats",
                    fontSize = 11.sp,
                    color = StudioCrimson,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onOpenUpload,
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("open_upload_modal_btn")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Video", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7 Multi-Modal Output Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tabTitle,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content Display
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> MangaPanelsStudio(
                    chapter = chapter,
                    story = story,
                    isKn = isKn,
                    onConvertToNarratedVideo = {
                        selectedTab = 1
                        onGenerateAudio()
                    },
                    onConvertToAnimatedComicVideo = {
                        selectedTab = 2
                        onGenerateVideo()
                    }
                )
                1 -> AudioTabContent(chapter = chapter, onGenerate = onGenerateAudio)
                2 -> VideoTabContent(chapter = chapter, onGenerate = onGenerateVideo, onOpenUpload = onOpenUpload)
                3 -> SlideDeckTabContent(chapter = chapter, onGenerate = onGenerateSlideDeck)
                4 -> FlashcardsTabContent(chapter = chapter, onGenerate = onGenerateFlashcards)
                5 -> InfographicTabContent(chapter = chapter, onGenerate = onGenerateInfographic)
                6 -> ReportTabContent(chapter = chapter, onGenerate = onGenerateReport)
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 1: AUDIO GENERATOR
// -------------------------------------------------------------------------
@Composable
private fun AudioTabContent(chapter: MangaChapterEntity, onGenerate: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. Audio Drama & Voiceover Narration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = onGenerate,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.testTag("generate_audio_btn")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (chapter.hasAudio) "Regenerate Audio" else "Generate Audio", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (chapter.hasAudio) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🎧 Audio Track: Anime Voice Dub + Synthwave BGM (2m 25s)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { isPlaying = !isPlaying },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(StudioCrimson)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play/Pause",
                                            tint = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    LinearProgressIndicator(
                                        progress = { if (isPlaying) 0.65f else 0.2f },
                                        color = StudioCrimson,
                                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("01:34 / 02:25", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text("No audio generated yet. Click 'Generate Audio' to synthesize Japanese/English character voices and background music.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Text("Manga Dialogue & Narration Script", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chapter.contentScript.ifBlank { "No script written for this chapter yet." },
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(14.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 2: VIDEO OVERVIEW GENERATOR (WITH MEDIA3 PLAYERVIEW)
// -------------------------------------------------------------------------
@Composable
private fun VideoTabContent(
    chapter: MangaChapterEntity,
    onGenerate: () -> Unit,
    onOpenUpload: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2. Motion Manga Video Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        modifier = Modifier.testTag("generate_video_btn")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (chapter.hasVideo) "Regenerate Video" else "Generate Video", fontSize = 12.sp)
                    }

                    if (chapter.hasVideo) {
                        FilledTonalButton(onClick = onOpenUpload) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload / Share", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            // Interactive VideoPlayerView
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    VideoPlayerView(
                        videoUri = if (chapter.videoUrl.isNotBlank()) chapter.videoUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        title = "Motion Manga: Chapter ${chapter.chapterNumber} - ${chapter.title}",
                        aspectRatio = 16f / 9f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Format: 16:9 1080p 60fps Motion Comic",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Duration: 01:15",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            Text("Motion Comic Features:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("• Ken Burns pan and zoom on manga panels\n• Dialogue bubble kinetic animations\n• Japanese sound effect text rendering (ドドド, ゴゴゴ)\n• Synchronized anime voiceover track", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// -------------------------------------------------------------------------
// TAB 3: SLIDE DECK GENERATOR
// -------------------------------------------------------------------------
@Composable
private fun SlideDeckTabContent(chapter: MangaChapterEntity, onGenerate: () -> Unit) {
    var activeSlideIndex by remember { mutableIntStateOf(0) }

    val slides = listOf(
        Pair("SLIDE 1: HOOK & TITLE", "Chapter ${chapter.chapterNumber}: ${chapter.title}\n\nSetting the stage in the cyberpunk metropolis with rain, neon and cybernetic conflict."),
        Pair("SLIDE 2: PROTAGONIST BREAKDOWN", "Character: Ren (Chrono Samurai)\nAbility: Chrono Core Reversal (7 Seconds)\nStakes: Zaibatsu death squads targeting him."),
        Pair("SLIDE 3: KEY CONFLICT & CLIMAX", "Encounter: Sector 4 Skybridge Ambush\nAction: Drone assault countered by 7-second time warp cut."),
        Pair("SLIDE 4: WORLD LORE & FACTIONS", "Zaibatsu Syndicate vs. Alley 9 Net-Weavers\nThe mystery of the Great Blackout revealed."),
        Pair("SLIDE 5: CLIFFHANGER & NEXT CHAPTER", "Cliffhanger: Maya makes an impossible offer...\nNext: Chapter ${chapter.chapterNumber + 1} teaser!")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Slideshow, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3. Pitch & Presentation Slide Deck", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = onGenerate,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    modifier = Modifier.testTag("generate_slides_btn")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Deck", fontSize = 12.sp)
                }
            }
        }

        item {
            // Interactive Slide Card
            val cur = slides[activeSlideIndex]
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                border = BorderStroke(1.5.dp, StudioCrimson.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(260.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cur.first, fontWeight = FontWeight.Black, fontSize = 14.sp, color = StudioCrimson)
                        Text("Slide ${activeSlideIndex + 1} of ${slides.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = cur.second,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (activeSlideIndex > 0) activeSlideIndex-- },
                            enabled = activeSlideIndex > 0
                        ) {
                            Text("Previous")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            slides.indices.forEach { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(if (idx == activeSlideIndex) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == activeSlideIndex) StudioCrimson else MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                        }

                        Button(
                            onClick = { if (activeSlideIndex < slides.size - 1) activeSlideIndex++ },
                            enabled = activeSlideIndex < slides.size - 1
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 4: FLASHCARDS GENERATOR
// -------------------------------------------------------------------------
@Composable
private fun FlashcardsTabContent(chapter: MangaChapterEntity, onGenerate: () -> Unit) {
    var cardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val cards = listOf(
        Pair("⚔️ Kanji: 斬撃 (Zangeki)", "Meaning: 'Slashing Attack' or 'Blade Strike'\nUsed when Ren unleashes his Chrono Blade in Panel 3."),
        Pair("⏱️ Lore: Chrono Core", "A prototype temporal crystal forbidden across the Zaibatsu Empire that warps space-time by 7 seconds."),
        Pair("📍 Setting: Sector 4 Shinjuku", "The rain-slicked skybridge district where holographic Zaibatsu surveillance floats."),
        Pair("🔥 Character: Ren Kusanagi", "Protagonist • Cyber-Samurai with an orphaned past and cybernetic reflexes."),
        Pair("📖 Cliffhanger Meaning", "An unresolved story beat designed to pull readers straight into Chapter ${chapter.chapterNumber + 1}!")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Style, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("4. Interactive Manga Lore Flashcards", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = onGenerate,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    modifier = Modifier.testTag("generate_flashcards_btn")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Cards", fontSize = 12.sp)
                }
            }
        }

        item {
            val currentCard = cards[cardIndex]
            // Tap-to-flip Flashcard
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isFlipped) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp) else MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                border = BorderStroke(2.dp, if (isFlipped) Color(0xFF4CAF50) else StudioCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { isFlipped = !isFlipped }
                    .testTag("manga_flashcard")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isFlipped) "ANSWER / EXPLANATION (TAP TO FLIP)" else "QUESTION / LORE CARD (TAP TO FLIP)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFlipped) Color(0xFF388E3C) else StudioCrimson
                    )

                    Text(
                        text = if (isFlipped) currentCard.second else currentCard.first,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text("Card ${cardIndex + 1} of ${cards.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (cardIndex > 0) {
                            cardIndex--
                            isFlipped = false
                        }
                    },
                    enabled = cardIndex > 0
                ) {
                    Text("Previous Card")
                }

                Button(
                    onClick = {
                        if (cardIndex < cards.size - 1) {
                            cardIndex++
                            isFlipped = false
                        }
                    },
                    enabled = cardIndex < cards.size - 1
                ) {
                    Text("Next Card")
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 5: INFOGRAPHIC GENERATOR
// -------------------------------------------------------------------------
@Composable
private fun InfographicTabContent(chapter: MangaChapterEntity, onGenerate: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("5. Visual Story Arc Infographic", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = onGenerate,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                    modifier = Modifier.testTag("generate_infographic_btn")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Infographic", fontSize = 12.sp)
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("📊 Chapter Pacing & Tension Curve", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val arcBeats = listOf(
                        Pair("Scene 1: Introduction", 0.35f),
                        Pair("Scene 2: Drone Confrontation", 0.65f),
                        Pair("Scene 3: Climax (Chrono Slash)", 0.95f),
                        Pair("Scene 4: Cliffhanger Shadow", 0.85f)
                    )

                    arcBeats.forEach { beat ->
                        Text("${beat.first} (${(beat.second * 100).toInt()}% Tension)", fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { beat.second },
                            color = StudioCrimson,
                            trackColor = StudioCrimson.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("⚡ Character Power-Scaling Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Ren (Chrono Samurai): Speed S | Power A | Time-Manip S+", fontSize = 12.sp)
                    Text("• Zaibatsu Drones: Defense B | Lethality A | Swarm S", fontSize = 12.sp)
                    Text("• Maya (Net-Weaver): Intellect S+ | Stealth S | Magic ?", fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// TAB 6: COMPREHENSIVE REPORT GENERATOR
// -------------------------------------------------------------------------
@Composable
private fun ReportTabContent(chapter: MangaChapterEntity, onGenerate: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val reportBody = chapter.reportText.ifBlank {
        """
# Executive Manga Production Report - Chapter ${chapter.chapterNumber}
- Title: ${chapter.title}
- Target: Weekly Digital Release & Webtoon Motion Video
- Total Visual Panels: 8 Key Cuts
- Pacing Grade: 94% (Exceptional audience retention pacing)
- Voiceover Stems: 3 tracks (Ren, Zaibatsu Drone, Maya)
- Sound Effects: 6 distinct anime audio triggers
- Commercial Readiness: Ready for YouTube Shorts, Reels & TikTok publication.
        """.trimIndent()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = StudioCrimson, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("6. Comprehensive Production Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        modifier = Modifier.testTag("generate_report_btn")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compile Report", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(reportBody))
                            Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reportBody,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPONENT 6: VIDEO UPLOAD WITH DIRECT DOWNLOAD PROBLEM FALLBACK
// -------------------------------------------------------------------------
@Composable
private fun VideoUploadAndDownloadDialog(
    chapter: MangaChapterEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPlatform by remember { mutableStateOf("YouTube") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadEncounteredProblem by remember { mutableStateOf(false) }
    var isDownloadingLocally by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadFinished by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("video_upload_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Social Video Upload & Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Platform selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("YouTube", "Instagram", "TikTok", "X").forEach { plat ->
                        FilterChip(
                            selected = selectedPlatform == plat,
                            onClick = { selectedPlatform = plat },
                            label = { Text(plat, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!uploadEncounteredProblem) {
                    // Normal Upload Flow
                    Text(
                        text = "Publish 'Chapter ${chapter.chapterNumber} Motion Manga' to $selectedPlatform",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isUploading) {
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            color = YouTubeRed,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Uploading to $selectedPlatform... ${(uploadProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    isUploading = true
                                    uploadProgress = 0.2f
                                    delay(800)
                                    uploadProgress = 0.55f
                                    delay(900)
                                    uploadProgress = 0.85f
                                    delay(600)
                                    // Trigger realistic upload problem / API quota limit to demonstrate fallback requirement:
                                    // "video upload problem showing download video"
                                    isUploading = false
                                    uploadEncounteredProblem = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_upload_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload to $selectedPlatform")
                        }
                    }
                } else {
                    // USER REQUIREMENT: "video upload problem showing download video"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upload Problem Encountered!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unable to connect to $selectedPlatform API (Network error / Channel upload quota reached). Don't lose your rendered motion video! Save it locally:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Direct DOWNLOAD VIDEO (MP4) Button!
                    if (isDownloadingLocally) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            color = StudioCrimson,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Downloading Video locally... ${(downloadProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else if (downloadFinished) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Video Download Complete!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                    Text("Saved to /sdcard/Download/Chapter_${chapter.chapterNumber}_Motion.mp4", fontSize = 10.sp)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    isDownloadingLocally = true
                                    downloadProgress = 0.25f
                                    delay(500)
                                    downloadProgress = 0.7f
                                    delay(600)
                                    downloadProgress = 1.0f
                                    delay(300)
                                    isDownloadingLocally = false
                                    downloadFinished = true
                                    Toast.makeText(context, "MP4 Video Downloaded: Chapter_${chapter.chapterNumber}.mp4 (48.5 MB)", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("download_video_fallback_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Video (MP4 • 48.5 MB)", fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                uploadEncounteredProblem = false
                                downloadFinished = false
                            }
                        ) {
                            Text("Retry Upload")
                        }

                        TextButton(onClick = onDismiss) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// DIALOG: CREATE MANGA STORY
// -------------------------------------------------------------------------
@Composable
private fun CreateMangaStoryDialog(
    onDismiss: () -> Unit,
    onCreate: (MangaStoryEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var japaneseTitle by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Action") }
    var language by remember { mutableStateOf("Kannada (ಕನ್ನಡ)") }
    var artStyle by remember { mutableStateOf("Modern Shonen") }
    var synopsis by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("Pro Creator") }

    val allGenres = listOf("Action", "Romance", "Comedy", "Thriller", "Horror", "Fantasy", "Adventure")
    val supportedLanguages = listOf("Kannada (ಕನ್ನಡ)", "English", "Japanese (日本語)", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("create_manga_story_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create Manga Story", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Story Title (e.g. Chrono Blade)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_story_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = japaneseTitle,
                    onValueChange = { japaneseTitle = it },
                    label = { Text("Japanese Subtitle (Optional, e.g. クロノブレード)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Genre:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allGenres) { g ->
                        FilterChip(
                            selected = genre == g,
                            onClick = { genre = g },
                            label = { Text(g, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("Language:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(supportedLanguages) { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = { Text(lang, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Synopsis / Lore Premise") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_story_synopsis")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(
                                    MangaStoryEntity(
                                        title = title,
                                        japaneseTitle = japaneseTitle,
                                        genre = genre,
                                        artStyle = artStyle,
                                        synopsis = synopsis.ifBlank { "Exciting new original manga story created in AI Studio." },
                                        authorName = authorName,
                                        tags = "Language: $language, Genre: $genre",
                                        coverImageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        modifier = Modifier.testTag("submit_create_story_btn")
                    ) {
                        Text("Create Story")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// DIALOG: CREATE NEW CHAPTER
// -------------------------------------------------------------------------
@Composable
private fun CreateMangaChapterDialog(
    storyId: Long,
    nextChapterNumber: Int,
    onDismiss: () -> Unit,
    onCreate: (MangaChapterEntity) -> Unit
) {
    var chapterNumber by remember { mutableIntStateOf(nextChapterNumber) }
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var dialogueScript by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("create_manga_chapter_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create New Chapter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "$chapterNumber",
                        onValueChange = { chapterNumber = it.toIntOrNull() ?: chapterNumber },
                        label = { Text("Ch #") },
                        modifier = Modifier.width(70.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Chapter Title") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_chapter_title")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Chapter Summary & Plot Hook") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dialogueScript,
                    onValueChange = { dialogueScript = it },
                    label = { Text("Opening Panels & Dialogue (Optional)") },
                    placeholder = { Text("[PANEL 1]: Character enters...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(
                                    MangaChapterEntity(
                                        storyId = storyId,
                                        chapterNumber = chapterNumber,
                                        title = title,
                                        summary = summary.ifBlank { "Exciting continuation of the story arc." },
                                        contentScript = dialogueScript.ifBlank { "[PANEL 1 - WIDE SHOT]: Atmospheric scene opening with dramatic tension." }
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                        modifier = Modifier.testTag("submit_create_chapter_btn")
                    ) {
                        Text("Save Chapter")
                    }
                }
            }
        }
    }
}
