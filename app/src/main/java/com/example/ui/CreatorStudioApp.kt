package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CreatorSettingsEntity
import com.example.i18n.I18n
import com.example.ui.components.AccountLoginDialog
import com.example.ui.components.GlobalVideoProgressOverlay
import com.example.ui.components.SidebarDrawerContent
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.UserLoginDialog
import com.example.ui.screens.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CreatorStudioApp(
    viewModel: CreatorStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val calendarTasks by viewModel.calendarTasks.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    val savedScripts by viewModel.savedScripts.collectAsStateWithLifecycle()
    val autoVideoJobs by viewModel.autoVideoJobs.collectAsStateWithLifecycle()
    val promptTemplates by viewModel.promptTemplates.collectAsStateWithLifecycle()
    val isAutoSchedulerActive by viewModel.isAutoSchedulerActive.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
    val isGeneratingNow by viewModel.isGeneratingNow.collectAsStateWithLifecycle()
    val globalLoadingState by viewModel.globalLoadingState.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val mangaStories by viewModel.mangaStories.collectAsStateWithLifecycle()
    val cachedGitHubRepos by viewModel.cachedGitHubRepos.collectAsStateWithLifecycle()
    val gitHubTokenWarning by viewModel.gitHubTokenWarning.collectAsStateWithLifecycle()
    val isSyncingGitHubRepos by viewModel.isSyncingGitHubRepos.collectAsStateWithLifecycle()
    val isBatchReprocessing by viewModel.isBatchReprocessing.collectAsStateWithLifecycle()
    val batchReprocessTotal by viewModel.batchReprocessTotal.collectAsStateWithLifecycle()
    val batchReprocessCurrent by viewModel.batchReprocessCurrent.collectAsStateWithLifecycle()
    val batchReprocessProgress by viewModel.batchReprocessProgress.collectAsStateWithLifecycle()
    val batchReprocessStatus by viewModel.batchReprocessStatus.collectAsStateWithLifecycle()
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val cartoonCharacters by viewModel.cartoonCharacters.collectAsStateWithLifecycle()
    val socialPublishLogs by viewModel.socialPublishLogs.collectAsStateWithLifecycle()
    val aiPromptItems by viewModel.aiPromptItems.collectAsStateWithLifecycle()

    var showWizardDialog by remember { mutableStateOf(false) }
    var showUserLoginDialog by remember { mutableStateOf(false) }
    var selectedPlatformForLogin by remember { mutableStateOf<String?>(null) }
    var scriptGenInitialTopic by remember { mutableStateOf<String?>(null) }
    var scriptGenInitialFramework by remember { mutableStateOf<String?>(null) }

    // Automatic wizard popup disabled per user request; accessible manually via Settings


    // Toast notification receiver (Toast alert system required for Content Calendar status transitions)
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Back handling: pop back to dashboard if in secondary screen
    if (currentRoute != "dashboard") {
        BackHandler {
            viewModel.navigateTo("dashboard")
        }
    }

    val currentLang = settings.defaultLanguage
    val isDark = settings.theme == "dark"

    val topBarContent: @Composable () -> Unit = {
        TopHeaderBar(
            settings = settings,
            currentLanguage = currentLang,
            isDarkTheme = isDark,
            onToggleLanguage = {
                val nextLang = if (currentLang == I18n.LANG_KN) I18n.LANG_EN else I18n.LANG_KN
                viewModel.saveSettings(settings.copy(defaultLanguage = nextLang))
            },
            onToggleTheme = {
                val nextTheme = if (isDark) "light" else "dark"
                viewModel.saveSettings(settings.copy(theme = nextTheme))
            },
            onOpenNavDrawer = {
                scope.launch { drawerState.open() }
            },
            onOpenSettings = {
                viewModel.navigateTo("settings")
            },
            onOpenUserLogin = {
                showUserLoginDialog = true
            },
            onOpenPlatformLogin = { platform ->
                selectedPlatformForLogin = platform
            },
            onOpenGitHubLink = {
                viewModel.navigateTo("github_link")
            }
        )
    }

    val screensContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "dashboard" -> DashboardScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        projects = projects,
                        calendarTasks = calendarTasks,
                        autoVideoJobs = autoVideoJobs,
                        cachedGitHubRepos = cachedGitHubRepos,
                        gitHubTokenWarning = gitHubTokenWarning,
                        isSyncingGitHubRepos = isSyncingGitHubRepos,
                        onSyncGitHubRepos = { viewModel.syncGitHubRepositoryMetadata() },
                        onRevokeGitHubPAT = { viewModel.revokeGitHubPAT() },
                        isBatchReprocessing = isBatchReprocessing,
                        onReprocessLast100 = { viewModel.reprocessLast100Jobs() },
                        onReprocessAll = { viewModel.reprocessAllJobs() },
                        onOpenUserLogin = { showUserLoginDialog = true },
                        onNavigate = { route -> viewModel.navigateTo(route) }
                    )

                    "cross_platform_hub" -> CrossPlatformHubScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onNavigate = { route -> viewModel.navigateTo(route) }
                    )

                    "story_to_video" -> StoryToVideoScreen(
                        currentLanguage = currentLang,
                        settings = settings,
                        onSaveProject = { viewModel.addProject(it) },
                        onSaveSettings = { viewModel.saveSettings(it) }
                    )

                    "auto_generator" -> AutoVideoSchedulerScreen(
                        currentLanguage = currentLang,
                        settings = settings,
                        autoVideoJobs = autoVideoJobs,
                        promptTemplates = promptTemplates,
                        isSchedulerActive = isAutoSchedulerActive,
                        countdownSeconds = countdownSeconds,
                        isGeneratingNow = isGeneratingNow,
                        isBatchReprocessing = isBatchReprocessing,
                        batchReprocessTotal = batchReprocessTotal,
                        batchReprocessCurrent = batchReprocessCurrent,
                        batchReprocessProgress = batchReprocessProgress,
                        batchReprocessStatus = batchReprocessStatus,
                        onReprocessLast100 = { viewModel.reprocessLast100Jobs() },
                        onReprocessAll = { viewModel.reprocessAllJobs() },
                        onReprocessJob = { viewModel.reprocessJob(it) },
                        onCancelBatchReprocess = { viewModel.cancelBatchReprocess() },
                        onToggleScheduler = { viewModel.toggleAutoScheduler(it) },
                        onTriggerAutoRun = { topic, platform, format, lang ->
                            viewModel.trigger10MinuteAutoVideoRun(topic, platform, format, lang)
                        },
                        onDeleteJob = { viewModel.deleteAutoVideoJob(it) },
                        onClearAllJobs = { viewModel.clearAllAutoVideoJobs() },
                        onAddPrompt = { viewModel.addPromptTemplate(it) },
                        onDeletePrompt = { viewModel.deletePromptTemplate(it) },
                        onIncrementPromptUsage = { viewModel.incrementPromptUsage(it) },
                        onSaveSettings = { viewModel.saveSettings(it) }
                    )

                    "create_video" -> VideoCreatorScreen(
                        currentLanguage = currentLang,
                        settings = settings,
                        onSaveProject = { viewModel.addProject(it) },
                        onPublishSuccess = { task -> viewModel.addCalendarTask(task) },
                        onShowGlobalProgress = { title, sub, action, prog, cancel ->
                            viewModel.showGlobalVideoProgress(title, sub, action, prog, cancel)
                        },
                        onHideGlobalProgress = { viewModel.hideGlobalVideoProgress() }
                    )

                    "shorts_reels" -> ShortsReelsScreen(
                        currentLanguage = currentLang,
                        onSaveProject = { viewModel.addProject(it) },
                        onShowGlobalProgress = { title, sub, action, prog, cancel ->
                            viewModel.showGlobalVideoProgress(title, sub, action, prog, cancel)
                        },
                        onHideGlobalProgress = { viewModel.hideGlobalVideoProgress() }
                    )

                    "script_gen" -> ScriptGeneratorScreen(
                        currentLanguage = currentLang,
                        onSaveScript = { viewModel.addScript(it) },
                        initialTopic = scriptGenInitialTopic,
                        initialFramework = scriptGenInitialFramework
                    )

                    "thumbnail_creator" -> ThumbnailCreatorScreen(
                        currentLanguage = currentLang,
                        onSaveToMediaLibrary = { viewModel.addMedia(it) }
                    )

                    "youtube" -> YouTubeToolScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onPublishSuccess = { task -> viewModel.addCalendarTask(task) }
                    )

                    "instagram" -> InstagramToolScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onPublishSuccess = { task -> viewModel.addCalendarTask(task) }
                    )

                    "facebook" -> FacebookToolScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onPublishSuccess = { task -> viewModel.addCalendarTask(task) }
                    )

                    "projects" -> ContentProjectsScreen(
                        currentLanguage = currentLang,
                        projects = projects,
                        onAddProject = { viewModel.addProject(it) },
                        onUpdateProject = { viewModel.updateProject(it) },
                        onDeleteProject = { viewModel.deleteProject(it) }
                    )

                    "calendar" -> ContentCalendarScreen(
                        currentLanguage = currentLang,
                        tasks = calendarTasks,
                        onAddTask = { viewModel.addCalendarTask(it) },
                        onUpdateTask = { viewModel.updateCalendarTask(it) },
                        onDeleteTask = { viewModel.deleteCalendarTask(it) },
                        onStatusChanged = { task, newStatus ->
                            viewModel.updateCalendarTaskStatus(task, newStatus)
                        },
                        onDateChanged = { task, newDate ->
                            viewModel.updateCalendarTaskDate(task, newDate)
                        }
                    )

                    "media" -> MediaLibraryScreen(
                        currentLanguage = currentLang,
                        mediaItems = mediaItems,
                        onAddMedia = { viewModel.addMedia(it) },
                        onDeleteMedia = { viewModel.deleteMedia(it) }
                    )

                    "analytics" -> AnalyticsScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onUpdateSettings = { viewModel.saveSettings(it) }
                    )

                    "ai_tools" -> AiToolsScreen(
                        currentLanguage = currentLang
                    )

                    "social_channels" -> SocialChannelsScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onNavigateToSettings = { viewModel.navigateTo("settings") },
                        onOpenPlatformLogin = { platform -> selectedPlatformForLogin = platform },
                        onOpenUserLogin = { showUserLoginDialog = true },
                        onNavigateToRoute = { route -> viewModel.navigateTo(route) }
                    )

                    "video_editor" -> VideoEditorScreen(
                        currentLanguage = currentLang,
                        settings = settings,
                        videoDrafts = allVideos,
                        onSaveProject = { viewModel.addProject(it) },
                        onSaveVideoDraft = { viewModel.saveVideo(it) },
                        onUpdateDraftStatus = { id, st -> viewModel.updateVideoDraftStatus(id, st) },
                        onDeleteDraft = { viewModel.deleteVideo(it) },
                        onScheduleTask = { viewModel.addCalendarTask(it) },
                        onShowGlobalProgress = { title, sub, action, prog, cancel ->
                            viewModel.showGlobalVideoProgress(title, sub, action, prog, cancel)
                        },
                        onUpdateGlobalProgress = { prog, sub ->
                            viewModel.updateGlobalVideoProgress(prog, sub)
                        },
                        onHideGlobalProgress = { viewModel.hideGlobalVideoProgress() },
                        onAddMediaToLibrary = { viewModel.addMedia(it) }
                    )

                    "opencreator_hub" -> OpenCreatorHubScreen(
                        currentLanguage = currentLang,
                        onNavigateToVideoEditorWithPreset = { title, prompt, isVert ->
                            viewModel.navigateTo("video_editor")
                        },
                        onNavigateToScriptGenWithPreset = { topic, fw ->
                            scriptGenInitialTopic = topic
                            scriptGenInitialFramework = fw
                            viewModel.navigateTo("script_gen")
                        },
                        onNavigateToThumbnailWithStyle = { style ->
                            viewModel.navigateTo("thumbnail_creator")
                        },
                        isBatchReprocessing = isBatchReprocessing,
                        batchReprocessProgress = batchReprocessProgress,
                        batchReprocessStatus = batchReprocessStatus,
                        onReprocessAll = { viewModel.reprocessAllJobs() }
                    )

                    "manga_studio" -> MangaStudioScreen(
                        currentLanguage = currentLang,
                        stories = mangaStories,
                        onGetChapters = { storyId -> viewModel.getChaptersForStory(storyId) },
                        onCreateStory = { viewModel.addMangaStory(it) },
                        onUpdateStory = { viewModel.updateMangaStory(it) },
                        onDeleteStory = { viewModel.deleteMangaStory(it) },
                        onCreateChapter = { viewModel.addMangaChapter(it) },
                        onUpdateChapter = { viewModel.updateMangaChapter(it) },
                        onDeleteChapter = { viewModel.deleteMangaChapter(it) },
                        onGenerateAudio = { ch, cb -> viewModel.generateChapterAudio(ch, cb) },
                        onGenerateVideo = { ch, cb -> viewModel.generateChapterVideo(ch, cb) },
                        onGenerateSlideDeck = { ch, cb -> viewModel.generateChapterSlideDeck(ch, cb) },
                        onGenerateFlashcards = { ch, cb -> viewModel.generateChapterFlashcards(ch, cb) },
                        onGenerateInfographic = { ch, cb -> viewModel.generateChapterInfographic(ch, cb) },
                        onGenerateReport = { ch, cb -> viewModel.generateChapterReport(ch, cb) }
                    )

                    "github_link" -> GitHubLinkScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        projects = projects,
                        calendarTasks = calendarTasks,
                        savedScripts = savedScripts,
                        onSaveSettings = { viewModel.saveSettings(it) },
                        onRevokeGitHubPAT = { viewModel.revokeGitHubPAT() },
                        gitHubTokenWarning = gitHubTokenWarning,
                        isBatchReprocessing = isBatchReprocessing,
                        batchReprocessProgress = batchReprocessProgress,
                        batchReprocessStatus = batchReprocessStatus,
                        onReprocessAll = { viewModel.reprocessAllJobs() }
                    )

                    "backup_restore" -> BackupRestoreScreen(
                        currentLanguage = currentLang,
                        settings = settings,
                        projects = projects,
                        calendarTasks = calendarTasks,
                        mediaItems = mediaItems,
                        savedScripts = savedScripts,
                        onRestoreSampleData = { viewModel.seedSampleData() }
                    )

                    "ai_prompt_studio" -> AiPromptStudioScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        promptList = aiPromptItems,
                        onSavePromptToLibrary = { viewModel.addAiPromptItem(it) },
                        onDeletePromptFromLibrary = { viewModel.deleteAiPromptItem(it) },
                        onNavigateToSettings = { viewModel.navigateTo("api_settings") }
                    )

                    "cartoon_studio" -> CartoonVideoStudioScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        characters = cartoonCharacters,
                        onAddCharacter = { viewModel.addCartoonCharacter(it) },
                        onDeleteCharacter = { viewModel.deleteCartoonCharacter(it) },
                        onNavigateToVideoEditor = { viewModel.navigateTo("video_editor") }
                    )

                    "publishing_centre" -> PublishingCentreScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        publishLogs = socialPublishLogs,
                        onAddPublishLog = { viewModel.addSocialPublishLog(it) },
                        onClearPublishLogs = { viewModel.clearAllPublishLogs() },
                        onOpenSocialLogin = { platform -> selectedPlatformForLogin = platform }
                    )

                    "api_settings" -> ApiSettingsScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onSaveSettings = { viewModel.saveSettings(it) }
                    )

                    "settings" -> SettingsScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        onSaveSettings = { viewModel.saveSettings(it) },
                        onOpenWizard = { showWizardDialog = true },
                        onOpenUserLogin = { showUserLoginDialog = true }
                    )

                    else -> DashboardScreen(
                        settings = settings,
                        currentLanguage = currentLang,
                        projects = projects,
                        calendarTasks = calendarTasks,
                        autoVideoJobs = autoVideoJobs,
                        cachedGitHubRepos = cachedGitHubRepos,
                        gitHubTokenWarning = gitHubTokenWarning,
                        isSyncingGitHubRepos = isSyncingGitHubRepos,
                        onSyncGitHubRepos = { viewModel.syncGitHubRepositoryMetadata() },
                        onRevokeGitHubPAT = { viewModel.revokeGitHubPAT() },
                        isBatchReprocessing = isBatchReprocessing,
                        onReprocessLast100 = { viewModel.reprocessLast100Jobs() },
                        onConnectPlatform = { platform -> selectedPlatformForLogin = platform },
                        onOpenUserLogin = { showUserLoginDialog = true },
                        onNavigate = { route -> viewModel.navigateTo(route) }
                    )
                }

                // Global CircularProgressIndicator overlay in the UI that displays
                // whenever an API request for video generation or rendering is in progress
                GlobalVideoProgressOverlay(
                    state = globalLoadingState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Adaptive PC / Web / Tablet: Permanent side drawer + Scaffold
            Row(modifier = Modifier.fillMaxSize()) {
                SidebarDrawerContent(
                    currentRoute = currentRoute,
                    currentLanguage = currentLang,
                    creatorName = settings.channelName,
                    creatorEmail = settings.youTubeEmail,
                    isPermanent = true,
                    onOpenUserLogin = { showUserLoginDialog = true },
                    onSelectRoute = { route -> viewModel.navigateTo(route) }
                )

                Scaffold(
                    topBar = topBarContent,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { innerPadding ->
                    screensContent(innerPadding)
                }
            }
        } else {
            // Mobile / Phone: ModalNavigationDrawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SidebarDrawerContent(
                        currentRoute = currentRoute,
                        currentLanguage = currentLang,
                        creatorName = settings.channelName,
                        creatorEmail = settings.youTubeEmail,
                        onOpenUserLogin = {
                            showUserLoginDialog = true
                            scope.launch { drawerState.close() }
                        },
                        onSelectRoute = { route ->
                            viewModel.navigateTo(route)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                Scaffold(
                    topBar = topBarContent,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    screensContent(innerPadding)
                }
            }
        }
    }

    // First Run Wizard Dialog
    if (showWizardDialog) {
        SetupWizardDialog(
            initialSettings = settings,
            onSaveAndFinish = { updated ->
                viewModel.saveSettings(updated)
                showWizardDialog = false
            },
            onDismiss = {
                viewModel.saveSettings(settings.copy(wizardCompleted = true))
                showWizardDialog = false
            }
        )
    }

    // User Creator Login Dialog
    if (showUserLoginDialog) {
        UserLoginDialog(
            settings = settings,
            onLoginSuccess = { updated ->
                viewModel.saveSettings(updated)
                showUserLoginDialog = false
            },
            onDismiss = { showUserLoginDialog = false }
        )
    }

    // Social Platform Account Connection Dialog
    selectedPlatformForLogin?.let { platform ->
        AccountLoginDialog(
            platform = platform,
            settings = settings,
            currentLanguage = currentLang,
            onSaveSettings = { updated ->
                viewModel.saveSettings(updated)
                selectedPlatformForLogin = null
            },
            onDismiss = { selectedPlatformForLogin = null }
        )
    }
}
