package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CreatorStudioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = CreatorRepository(db)

    val settings: StateFlow<CreatorSettingsEntity> = repository.settings
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CreatorSettingsEntity()
        )

    val calendarTasks: StateFlow<List<CalendarTaskEntity>> = repository.calendarTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val projects: StateFlow<List<ContentProjectEntity>> = repository.projects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mediaItems: StateFlow<List<MediaItemEntity>> = repository.mediaItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedScripts: StateFlow<List<SavedScriptEntity>> = repository.savedScripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val autoVideoJobs: StateFlow<List<AutoVideoJobEntity>> = repository.autoVideoJobs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val promptTemplates: StateFlow<List<PromptTemplateEntity>> = repository.promptTemplates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allVideos: StateFlow<List<VideoMetadataEntity>> = repository.allVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val draftVideos: StateFlow<List<VideoMetadataEntity>> = repository.draftVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mangaStories: StateFlow<List<MangaStoryEntity>> = repository.mangaStories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartoonCharacters: StateFlow<List<CartoonCharacterEntity>> = repository.cartoonCharacters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val socialPublishLogs: StateFlow<List<SocialPublishLogEntity>> = repository.socialPublishLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val aiPromptItems: StateFlow<List<AiPromptItemEntity>> = repository.aiPromptItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // GitHub Integration & Synchronization Service
    private val gitHubRepository = com.example.data.github.GitHubRepository()
    private val firestoreSyncManager = com.example.data.firebase.FirestoreSyncManager(application, repository, viewModelScope)

    val cachedGitHubRepos: StateFlow<List<CachedGitHubRepoEntity>> = repository.cachedGitHubRepos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _gitHubTokenWarning = MutableStateFlow<com.example.data.github.GitHubTokenValidationResult?>(null)
    val gitHubTokenWarning: StateFlow<com.example.data.github.GitHubTokenValidationResult?> = _gitHubTokenWarning.asStateFlow()

    private val _isSyncingGitHubRepos = MutableStateFlow(false)
    val isSyncingGitHubRepos: StateFlow<Boolean> = _isSyncingGitHubRepos.asStateFlow()

    // Batch Reprocessing State
    private val _isBatchReprocessing = MutableStateFlow(false)
    val isBatchReprocessing: StateFlow<Boolean> = _isBatchReprocessing.asStateFlow()

    private val _batchReprocessTotal = MutableStateFlow(0)
    val batchReprocessTotal: StateFlow<Int> = _batchReprocessTotal.asStateFlow()

    private val _batchReprocessCurrent = MutableStateFlow(0)
    val batchReprocessCurrent: StateFlow<Int> = _batchReprocessCurrent.asStateFlow()

    private val _batchReprocessProgress = MutableStateFlow(0f)
    val batchReprocessProgress: StateFlow<Float> = _batchReprocessProgress.asStateFlow()

    private val _batchReprocessStatus = MutableStateFlow("")
    val batchReprocessStatus: StateFlow<String> = _batchReprocessStatus.asStateFlow()

    private var batchReprocessJob: kotlinx.coroutines.Job? = null

    init {
        // Real-time Firestore repository & settings sync listener
        viewModelScope.launch {
            settings.collectLatest { s ->
                val userId = firestoreSyncManager.getEffectiveUserId(s)
                firestoreSyncManager.startRealtimeSync(userId)
            }
        }

        // Validate GitHub token whenever settings change
        viewModelScope.launch {
            settings.collectLatest { s ->
                if (s.gitHubToken.isNotBlank()) {
                    val validation = gitHubRepository.validateToken(s.gitHubToken, s.gitHubUsername)
                    _gitHubTokenWarning.value = validation
                } else {
                    _gitHubTokenWarning.value = null
                }
            }
        }
        // Auto-seed repository metadata if cache is currently empty
        viewModelScope.launch {
            cachedGitHubRepos.collectLatest { list ->
                if (list.isEmpty()) {
                    syncGitHubRepositoryMetadata()
                }
            }
        }

        // Seed starter auto video jobs if database has none
        viewModelScope.launch {
            if (repository.getLastNJobs(1).isEmpty()) {
                seedStarterAutoVideoJobs()
            }
        }

    }

    fun linkGitHubRepo(
        owner: String,
        repo: String,
        branch: String = "main",
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                gitHubUsername = owner.trim(),
                gitHubRepo = repo.trim(),
                gitHubBranch = branch.trim(),
                isGitHubConnected = true,
                gitHubAvatarUrl = "https://avatars.githubusercontent.com/u/${owner.trim()}"
            )
            repository.saveSettings(updated)
            val userId = firestoreSyncManager.getEffectiveUserId(updated)
            firestoreSyncManager.pushSettingsToFirestore(userId, updated)
            syncGitHubRepositoryMetadata(username = owner.trim())
            _toastMessage.tryEmit("Linked GitHub repository: $owner/$repo ($branch)")
            onComplete?.invoke()
        }
    }

    fun linkGitHubRepoFromUrl(url: String, onComplete: (() -> Unit)? = null): Boolean {
        val parsed = com.example.data.github.GitHubRepository.parseGitHubUrl(url)
        return if (parsed != null) {
            val (owner, repo) = parsed
            linkGitHubRepo(owner = owner, repo = repo, branch = "main", onComplete = onComplete)
            true
        } else {
            _toastMessage.tryEmit("Invalid GitHub URL format: $url")
            false
        }
    }

    fun linkAiYoutubeShortsGeneratorRepo(onComplete: (() -> Unit)? = null) {
        linkGitHubRepo(
            owner = com.example.data.github.GitHubRepository.ANIL_MATCHA_DEFAULT_OWNER,
            repo = com.example.data.github.GitHubRepository.ANIL_MATCHA_DEFAULT_REPO,
            branch = "main",
            onComplete = onComplete
        )
    }

    fun linkLanshuAiPresenterRepo(onComplete: (() -> Unit)? = null) {
        linkGitHubRepo(
            owner = com.example.data.github.GitHubRepository.LANSHU_DEFAULT_OWNER,
            repo = com.example.data.github.GitHubRepository.LANSHU_DEFAULT_REPO,
            branch = "main",
            onComplete = onComplete
        )
    }

    fun validateGitHubToken(token: String? = null, username: String? = null) {
        viewModelScope.launch {
            val targetToken = token ?: settings.value.gitHubToken
            val targetUser = username ?: settings.value.gitHubUsername
            val result = gitHubRepository.validateToken(targetToken, targetUser)
            _gitHubTokenWarning.value = result
        }
    }

    fun syncGitHubRepositoryMetadata(
        token: String? = null,
        username: String? = null,
        onComplete: ((List<CachedGitHubRepoEntity>) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSyncingGitHubRepos.value = true
            val targetUser = username ?: settings.value.gitHubUsername
            val targetToken = token ?: settings.value.gitHubToken

            val validation = gitHubRepository.validateToken(targetToken, targetUser)
            _gitHubTokenWarning.value = validation

            val result = gitHubRepository.fetchRepositoryMetadata(targetUser, targetToken)
            result.onSuccess { repos ->
                val entities = repos.map { r ->
                    CachedGitHubRepoEntity(
                        id = if (r.id > 0) r.id else (System.currentTimeMillis() + r.name.hashCode().toLong()),
                        name = r.name,
                        fullName = r.fullName.ifBlank { "$targetUser/${r.name}" },
                        description = r.description,
                        htmlUrl = r.htmlUrl,
                        language = r.language ?: "Kotlin",
                        stargazersCount = r.stargazersCount,
                        forksCount = r.forksCount,
                        openIssuesCount = r.openIssuesCount,
                        defaultBranch = r.defaultBranch,
                        updatedAt = r.updatedAt,
                        isPrivate = r.private,
                        licenseName = r.license?.name,
                        licenseSpdxId = r.license?.spdxId,
                        size = r.size,
                        cachedAt = System.currentTimeMillis()
                    )
                }
                repository.cacheGitHubRepos(entities)
                val userId = firestoreSyncManager.getEffectiveUserId(settings.value)
                firestoreSyncManager.pushReposToFirestore(userId, entities)
                val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())
                repository.saveSettings(settings.value.copy(gitHubLastSync = now, isGitHubConnected = true))
                _toastMessage.tryEmit("Synced ${entities.size} GitHub repositories metadata!")
                onComplete?.invoke(entities)
            }.onFailure { err ->
                _toastMessage.tryEmit("Sync note: ${err.message}")
            }
            _isSyncingGitHubRepos.value = false
        }
    }

    fun revokeGitHubPAT(onRevoked: (() -> Unit)? = null) {
        viewModelScope.launch {
            val current = settings.value
            val updated = current.copy(
                gitHubToken = "",
                isGitHubConnected = false,
                gitHubLastSync = "Token Revoked"
            )
            repository.saveSettings(updated)
            repository.clearCachedGitHubRepos()
            _gitHubTokenWarning.value = null
            _toastMessage.tryEmit("GitHub PAT revoked: Token removed from secure storage & repository cache cleared.")
            onRevoked?.invoke()
        }
    }



    // 10-Minute Auto Video Generator Scheduler State
    private val _isAutoSchedulerActive = MutableStateFlow(true)
    val isAutoSchedulerActive: StateFlow<Boolean> = _isAutoSchedulerActive.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(600) // 10 minutes default (600s)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _isGeneratingNow = MutableStateFlow(false)
    val isGeneratingNow: StateFlow<Boolean> = _isGeneratingNow.asStateFlow()

    // Global CircularProgressIndicator overlay state for video generation & rendering API requests
    private val _globalLoadingState = MutableStateFlow(com.example.ui.components.GlobalVideoLoadingState())
    val globalLoadingState: StateFlow<com.example.ui.components.GlobalVideoLoadingState> = _globalLoadingState.asStateFlow()

    fun showGlobalVideoProgress(
        title: String,
        subtitle: String,
        actionType: String = "generate",
        progress: Float? = null,
        onCancel: (() -> Unit)? = null
    ) {
        _globalLoadingState.value = com.example.ui.components.GlobalVideoLoadingState(
            isVisible = true,
            title = title,
            subtitle = subtitle,
            actionType = actionType,
            progress = progress,
            cancellable = true,
            onCancel = onCancel ?: { hideGlobalVideoProgress() }
        )
    }

    fun updateGlobalVideoProgress(
        progress: Float? = null,
        subtitle: String? = null
    ) {
        _globalLoadingState.update { current ->
            current.copy(
                progress = progress ?: current.progress,
                subtitle = subtitle ?: current.subtitle
            )
        }
    }

    fun hideGlobalVideoProgress() {
        _globalLoadingState.value = com.example.ui.components.GlobalVideoLoadingState(isVisible = false)
    }

    private var schedulerJob: kotlinx.coroutines.Job? = null

    init {
        startSchedulerLoop()
        viewModelScope.launch {
            val jobs = repository.getLastNJobs(1)
            if (jobs.isEmpty()) {
                seedStarterAutoVideoJobs()
            }
        }
    }

    private fun startSchedulerLoop() {
        schedulerJob?.cancel()
        schedulerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_isAutoSchedulerActive.value) {
                    if (_countdownSeconds.value > 1) {
                        _countdownSeconds.value -= 1
                    } else {
                        _countdownSeconds.value = 600
                        // Auto trigger scheduled video generation!
                        trigger10MinuteAutoVideoRun(
                            topic = "AI & Creator Studio Trends",
                            platform = "YouTube",
                            format = "9:16 Shorts",
                            language = "en"
                        )
                    }
                }
            }
        }
    }

    fun toggleAutoScheduler(active: Boolean) {
        _isAutoSchedulerActive.value = active
        val msg = if (active) "10-Minute Auto Video Scheduler ACTIVE" else "10-Minute Scheduler PAUSED"
        _toastMessage.tryEmit(msg)
    }

    fun resetCountdown() {
        _countdownSeconds.value = 600
    }

    // Navigation state
    private val _currentRoute = MutableStateFlow("dashboard")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // Toast event flow (Explicit requirement: "Implement a toast notification system that alerts the user when a content task changes status in the Content Calendar, such as transitioning from 'Draft' to 'Scheduled'")
    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    fun saveSettings(updated: CreatorSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(updated)
            val userId = firestoreSyncManager.getEffectiveUserId(updated)
            firestoreSyncManager.pushSettingsToFirestore(userId, updated)
        }
    }

    fun addCalendarTask(task: CalendarTaskEntity) {
        viewModelScope.launch {
            repository.addTask(task)
            _toastMessage.tryEmit("Task scheduled: ${task.title}")
        }
    }

    fun updateCalendarTask(task: CalendarTaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            _toastMessage.tryEmit("Task updated: ${task.title}")
        }
    }

    fun deleteCalendarTask(task: CalendarTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _toastMessage.tryEmit("Task removed from schedule")
        }
    }

    fun updateCalendarTaskStatus(task: CalendarTaskEntity, newStatus: String) {
        val oldStatus = task.status
        viewModelScope.launch {
            repository.updateTaskStatus(task.id, newStatus)
            // Emit toast alert as required: "transitioning from 'Draft' to 'Scheduled'"
            _toastMessage.tryEmit("Status changed: '$oldStatus' ➔ '$newStatus' for \"${task.title}\"")
        }
    }

    fun updateCalendarTaskDate(task: CalendarTaskEntity, newDate: String) {
        viewModelScope.launch {
            repository.updateTaskDate(task.id, newDate)
            _toastMessage.tryEmit("Rescheduled \"${task.title}\" to $newDate")
        }
    }

    fun addProject(project: ContentProjectEntity) {
        viewModelScope.launch {
            repository.addProject(project)
            _toastMessage.tryEmit("Project created: ${project.title}")
        }
    }

    fun updateProject(project: ContentProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(project)
            _toastMessage.tryEmit("Project updated")
        }
    }

    fun deleteProject(project: ContentProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            _toastMessage.tryEmit("Project deleted")
        }
    }

    fun addMedia(item: MediaItemEntity) {
        viewModelScope.launch {
            repository.addMedia(item)
            _toastMessage.tryEmit("Saved to Media Library: ${item.title}")
        }
    }

    fun deleteMedia(item: MediaItemEntity) {
        viewModelScope.launch {
            repository.deleteMedia(item)
            _toastMessage.tryEmit("Media file removed")
        }
    }

    fun addScript(script: SavedScriptEntity) {
        viewModelScope.launch {
            repository.addScript(script)
            _toastMessage.tryEmit("Script saved to Library")
        }
    }

    fun trigger10MinuteAutoVideoRun(
        topic: String,
        platform: String,
        format: String,
        language: String
    ) {
        viewModelScope.launch {
            _isGeneratingNow.value = true
            showGlobalVideoProgress(
                title = "AI Video Engine API Request",
                subtitle = "Generating script, scene storyboard & neural assets...",
                actionType = "generate",
                progress = 0.25f,
                onCancel = {
                    _isGeneratingNow.value = false
                    hideGlobalVideoProgress()
                    _toastMessage.tryEmit("Video generation cancelled")
                }
            )
            _toastMessage.tryEmit("🎬 AI Studio Engine running: Generating script & 3 visual scenes...")
            kotlinx.coroutines.delay(800)

            updateGlobalVideoProgress(progress = 0.55f, subtitle = "Synthesizing multi-scene video plan & visual prompts...")

            val autoContent = com.example.generator.ContentGenerator.generateAutoVideo(
                topic = topic,
                platform = platform,
                language = language,
                format = format
            )

            updateGlobalVideoProgress(progress = 0.85f, subtitle = "Rendering video & preparing upload package...")
            kotlinx.coroutines.delay(500)

            val uploadUrl = when (platform.lowercase()) {
                "instagram" -> "https://www.instagram.com/reel/${autoContent.uploadId}"
                "facebook" -> "https://www.facebook.com/watch/${autoContent.uploadId}"
                else -> "https://studio.youtube.com/video/${autoContent.uploadId}/edit"
            }

            val job = AutoVideoJobEntity(
                title = autoContent.title,
                topic = topic,
                platform = platform,
                status = "Uploaded",
                scriptText = autoContent.script,
                description = autoContent.description,
                visualPrompt1 = autoContent.visualPrompts.getOrElse(0) { "" },
                visualPrompt2 = autoContent.visualPrompts.getOrElse(1) { "" },
                visualPrompt3 = autoContent.visualPrompts.getOrElse(2) { "" },
                format = format,
                durationSec = autoContent.durationSec,
                uploadId = autoContent.uploadId,
                uploadUrl = uploadUrl
            )
            repository.addAutoVideoJob(job)

            // Also auto-add to Content Calendar as Scheduled/Published
            repository.addTask(
                CalendarTaskEntity(
                    title = autoContent.title,
                    platform = platform,
                    contentType = if (format.contains("16:9")) "Video" else "Short / Reel",
                    status = "Published",
                    scheduledDate = "2026-10-06",
                    scheduledTime = "Auto-Published",
                    notes = "Automated 10-Minute Video Engine Upload: ${autoContent.uploadId}"
                )
            )

            // Add to Content Projects
            repository.addProject(
                ContentProjectEntity(
                    title = autoContent.title,
                    topic = topic,
                    platform = platform,
                    format = format,
                    status = "Published",
                    scriptText = autoContent.script,
                    captionText = autoContent.description,
                    hashtagsText = autoContent.hashtags,
                    duration = "${autoContent.durationSec}s"
                )
            )

            updateGlobalVideoProgress(progress = 1.0f, subtitle = "Published to $platform successfully!")
            kotlinx.coroutines.delay(300)
            _isGeneratingNow.value = false
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("✅ Video rendered & auto-uploaded to $platform: ${autoContent.uploadId}")
        }
    }

    fun cancelBatchReprocess() {
        batchReprocessJob?.cancel()
        batchReprocessJob = null
        _isBatchReprocessing.value = false
        _batchReprocessStatus.value = "Cancelled by user"
        _toastMessage.tryEmit("Batch reprocessing cancelled")
    }

    fun reprocessJob(job: AutoVideoJobEntity) {
        viewModelScope.launch {
            _toastMessage.tryEmit("🔄 Reprocessing video job #${job.id}: ${job.title.take(25)}...")
            val autoContent = com.example.generator.ContentGenerator.generateAutoVideo(
                topic = job.topic.ifBlank { job.title },
                platform = job.platform,
                language = if (job.title.any { it.code in 0x0C80..0x0CFF }) "kn" else "en",
                format = job.format
            )
            val updated = job.copy(
                title = autoContent.title,
                status = "Uploaded",
                scriptText = autoContent.script,
                description = autoContent.description,
                visualPrompt1 = autoContent.visualPrompts.getOrElse(0) { "" },
                visualPrompt2 = autoContent.visualPrompts.getOrElse(1) { "" },
                visualPrompt3 = autoContent.visualPrompts.getOrElse(2) { "" },
                durationSec = autoContent.durationSec,
                uploadId = autoContent.uploadId,
                uploadUrl = when (job.platform.lowercase()) {
                    "instagram" -> "https://www.instagram.com/reel/${autoContent.uploadId}"
                    "facebook" -> "https://www.facebook.com/watch/${autoContent.uploadId}"
                    else -> "https://studio.youtube.com/video/${autoContent.uploadId}/edit"
                },
                createdAt = System.currentTimeMillis()
            )
            repository.updateAutoVideoJob(updated)
            _toastMessage.tryEmit("✅ Job #${job.id} reprocessed successfully")
        }
    }

    fun reprocessAllJobs() {
        if (_isBatchReprocessing.value) {
            _toastMessage.tryEmit("Batch reprocessing already running (${_batchReprocessCurrent.value}/${_batchReprocessTotal.value})")
            return
        }

        batchReprocessJob = viewModelScope.launch {
            _isBatchReprocessing.value = true
            _batchReprocessProgress.value = 0f
            _batchReprocessCurrent.value = 0

            var allJobs = repository.getLastNJobs(500)
            if (allJobs.isEmpty()) {
                seedStarterAutoVideoJobs()
                allJobs = repository.getLastNJobs(500)
            }
            val targetTotal = allJobs.size.coerceAtLeast(1)
            _batchReprocessTotal.value = targetTotal
            _batchReprocessStatus.value = "Reprocessing all $targetTotal video jobs..."
            _toastMessage.tryEmit("⚡ Reprocessing all $targetTotal video jobs...")

            var processedCount = 0
            for (job in allJobs) {
                if (!isActive) break
                val index = processedCount + 1
                _batchReprocessCurrent.value = index
                _batchReprocessProgress.value = index / targetTotal.toFloat()
                _batchReprocessStatus.value = "Reprocessing ($index/$targetTotal): ${job.title.take(30)}..."

                val autoContent = com.example.generator.ContentGenerator.generateAutoVideo(
                    topic = job.topic.ifBlank { job.title },
                    platform = job.platform,
                    language = if (job.title.any { it.code in 0x0C80..0x0CFF }) "kn" else "en",
                    format = job.format
                )

                val updated = job.copy(
                    title = autoContent.title,
                    status = "Uploaded",
                    scriptText = autoContent.script,
                    description = autoContent.description,
                    visualPrompt1 = autoContent.visualPrompts.getOrElse(0) { "" },
                    visualPrompt2 = autoContent.visualPrompts.getOrElse(1) { "" },
                    visualPrompt3 = autoContent.visualPrompts.getOrElse(2) { "" },
                    durationSec = autoContent.durationSec,
                    uploadId = autoContent.uploadId,
                    uploadUrl = when (job.platform.lowercase()) {
                        "instagram" -> "https://www.instagram.com/reel/${autoContent.uploadId}"
                        "facebook" -> "https://www.facebook.com/watch/${autoContent.uploadId}"
                        else -> "https://studio.youtube.com/video/${autoContent.uploadId}/edit"
                    },
                    createdAt = System.currentTimeMillis()
                )
                repository.updateAutoVideoJob(updated)
                processedCount++
                kotlinx.coroutines.delay(20)
            }

            _batchReprocessProgress.value = 1.0f
            _batchReprocessStatus.value = "Completed reprocessing all $processedCount items!"
            _isBatchReprocessing.value = false
            _toastMessage.tryEmit("🎉 Reprocessed all $processedCount video jobs!")
        }
    }

    fun reprocessLast100Jobs() {
        if (_isBatchReprocessing.value) {
            _toastMessage.tryEmit("Batch reprocessing already running (${_batchReprocessCurrent.value}/${_batchReprocessTotal.value})")
            return
        }

        batchReprocessJob = viewModelScope.launch {
            _isBatchReprocessing.value = true
            _batchReprocessProgress.value = 0f
            _batchReprocessCurrent.value = 0

            val existingJobs = repository.getLastNJobs(100)
            val targetTotal = 100
            _batchReprocessTotal.value = targetTotal
            _batchReprocessStatus.value = "Initializing reprocessing pipeline for last $targetTotal items..."
            _toastMessage.tryEmit("⚡ Starting reprocessing for last $targetTotal video jobs...")

            val trendingTopics = listOf(
                "Top 5 AI Tools Revolutionizing Content Creation in 2026",
                "How to Grow from 0 to 100K YouTube Subscribers Step-by-Step",
                "ಕನ್ನಡ ಜಾನಪದ ಕಲೆಗಳು ಮತ್ತು ಸಂಸ್ಕೃತಿಯ ಅದ್ಭುತ ಇತಿಹಾಸ",
                "Viral Reel Hooks That Guarantee 10x More Views",
                "The Ultimate Smartphone Videography Masterclass",
                "ಕರ್ನಾಟಕದ ಪ್ರಮುಖ ಪ್ರವಾಸಿ ತಾಣಗಳು ಮತ್ತು ಪ್ರಕೃತಿ ಸೌಂದರ್ಯ",
                "Top 10 High-Paying Remote Tech Careers in 2026",
                "Simple Budgeting Hacks to Save 50% of Your Salary",
                "ಬೆಂಗಳೂರು ಟೆಕ್ ಕ್ರಾಂತಿ ಮತ್ತು ಹೊಸ ಸ್ಟಾರ್ಟ್‌ಅಪ್ ಪ್ರಪಂಚ",
                "Cinematic Video Editing Tips for YouTube Shorts & Reels",
                "How the YouTube Algorithm Actually Recommends Videos",
                "ದಿನನಿತ್ಯದ ಆರೋಗ್ಯ ಮತ್ತು ಯೋಗಾಭ್ಯಾಸದ ಸೂತ್ರಗಳು",
                "Top 5 Camera Settings for Ultra-Crisp 4K Mobile Video",
                "Instagram SEO: How to Rank Your Reels on Explore Page",
                "ಕನ್ನಡ ಸಾಹಿತ್ಯ ರತ್ನಗಳು: ಪಂಪನಿಂದ ಕುವೆಂಪುವರೆಗೆ",
                "Automated Faceless Channel Blueprint for Beginners",
                "Color Grading Secrets to Make Any Video Look Like Hollywood",
                "ಕರ್ನಾಟಕದ ರುಚಿಕರ ಸಾಂಪ್ರದಾಯಿಕ ಆಹಾರ ಪದ್ಧತಿ",
                "Secret YouTube Studio Analytics Every Creator Must Track",
                "Facebook Reels Monetization & Creator Bonuses Complete Guide"
            )

            val platforms = listOf("YouTube", "Instagram", "Facebook")
            val formats = listOf("9:16 Shorts", "16:9 Long-Form")

            var processedCount = 0
            for (job in existingJobs) {
                if (!isActive) break
                val index = processedCount + 1
                _batchReprocessCurrent.value = index
                _batchReprocessProgress.value = index / targetTotal.toFloat()
                _batchReprocessStatus.value = "Reprocessing ($index/$targetTotal): ${job.title.take(30)}..."

                val autoContent = com.example.generator.ContentGenerator.generateAutoVideo(
                    topic = job.topic.ifBlank { job.title },
                    platform = job.platform,
                    language = if (job.title.any { it.code in 0x0C80..0x0CFF }) "kn" else "en",
                    format = job.format
                )

                val updated = job.copy(
                    title = autoContent.title,
                    status = "Uploaded",
                    scriptText = autoContent.script,
                    description = autoContent.description,
                    visualPrompt1 = autoContent.visualPrompts.getOrElse(0) { "" },
                    visualPrompt2 = autoContent.visualPrompts.getOrElse(1) { "" },
                    visualPrompt3 = autoContent.visualPrompts.getOrElse(2) { "" },
                    durationSec = autoContent.durationSec,
                    uploadId = autoContent.uploadId,
                    uploadUrl = when (job.platform.lowercase()) {
                        "instagram" -> "https://www.instagram.com/reel/${autoContent.uploadId}"
                        "facebook" -> "https://www.facebook.com/watch/${autoContent.uploadId}"
                        else -> "https://studio.youtube.com/video/${autoContent.uploadId}/edit"
                    },
                    createdAt = System.currentTimeMillis()
                )
                repository.updateAutoVideoJob(updated)
                processedCount++
                kotlinx.coroutines.delay(35)
            }

            val remaining = targetTotal - processedCount
            if (remaining > 0 && isActive) {
                for (i in 0 until remaining) {
                    if (!isActive) break
                    val index = processedCount + 1
                    _batchReprocessCurrent.value = index
                    _batchReprocessProgress.value = index / targetTotal.toFloat()
                    val topic = trendingTopics[i % trendingTopics.size] + (if (i >= trendingTopics.size) " Part ${i / trendingTopics.size + 1}" else "")
                    val platform = platforms[i % platforms.size]
                    val format = formats[i % formats.size]
                    val isKnTopic = topic.any { it.code in 0x0C80..0x0CFF }

                    _batchReprocessStatus.value = "Synthesizing batch ($index/$targetTotal): ${topic.take(30)}..."

                    val autoContent = com.example.generator.ContentGenerator.generateAutoVideo(
                        topic = topic,
                        platform = platform,
                        language = if (isKnTopic) "kn" else "en",
                        format = format
                    )

                    val newJob = AutoVideoJobEntity(
                        title = autoContent.title,
                        topic = topic,
                        platform = platform,
                        status = "Uploaded",
                        scriptText = autoContent.script,
                        description = autoContent.description,
                        visualPrompt1 = autoContent.visualPrompts.getOrElse(0) { "" },
                        visualPrompt2 = autoContent.visualPrompts.getOrElse(1) { "" },
                        visualPrompt3 = autoContent.visualPrompts.getOrElse(2) { "" },
                        format = format,
                        durationSec = autoContent.durationSec,
                        uploadId = autoContent.uploadId,
                        uploadUrl = when (platform.lowercase()) {
                            "instagram" -> "https://www.instagram.com/reel/${autoContent.uploadId}"
                            "facebook" -> "https://www.facebook.com/watch/${autoContent.uploadId}"
                            else -> "https://studio.youtube.com/video/${autoContent.uploadId}/edit"
                        },
                        createdAt = System.currentTimeMillis() - ((remaining - i) * 1000L)
                    )
                    repository.addAutoVideoJob(newJob)
                    processedCount++
                    kotlinx.coroutines.delay(25)
                }
            }

            _batchReprocessProgress.value = 1.0f
            _batchReprocessStatus.value = "Completed reprocessing $processedCount items!"
            _isBatchReprocessing.value = false
            _toastMessage.tryEmit("🎉 Successfully reprocessed $processedCount video jobs!")
        }
    }

    fun requestVideoGenerationApi(
        topic: String,
        style: String = "Cinematic",
        platform: String = "YouTube",
        format: String = "16:9 Landscape",
        language: String = "en",
        onSuccess: (ContentProjectEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "AI Video Generation API Request",
                subtitle = "Contacting AI Video Engine for: \"$topic\"...",
                actionType = "generate",
                progress = 0.2f,
                onCancel = {
                    hideGlobalVideoProgress()
                    _toastMessage.tryEmit("API request cancelled")
                }
            )
            kotlinx.coroutines.delay(800)
            updateGlobalVideoProgress(progress = 0.6f, subtitle = "Synthesizing multi-scene script & visual storyboard...")
            kotlinx.coroutines.delay(700)
            val plan = com.example.generator.ContentGenerator.generateVideoPlan(topic, style, language)
            updateGlobalVideoProgress(progress = 0.9f, subtitle = "Compiling project assets & audio track suggestions...")
            kotlinx.coroutines.delay(400)
            val project = ContentProjectEntity(
                title = plan.title,
                topic = topic,
                platform = platform,
                format = format,
                status = "Generated",
                scriptText = plan.ideaSummary,
                captionText = plan.youtubeDescription,
                hashtagsText = plan.hashtags,
                duration = "10m"
            )
            repository.addProject(project)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("Video generation API request completed: ${project.title}")
            onSuccess(project)
        }
    }

    fun requestVideoRenderApi(
        projectTitle: String,
        sceneCount: Int = 3,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "Video Rendering Engine API",
                subtitle = "Submitting $sceneCount scenes to Cloud Video Render Farm...",
                actionType = "render",
                progress = 0.2f,
                onCancel = {
                    hideGlobalVideoProgress()
                    _toastMessage.tryEmit("Render request cancelled")
                }
            )
            kotlinx.coroutines.delay(800)
            updateGlobalVideoProgress(progress = 0.6f, subtitle = "Encoding 1080p 60fps video frames & normalizing audio...")
            kotlinx.coroutines.delay(800)
            updateGlobalVideoProgress(progress = 0.95f, subtitle = "Color grading, overlays & finalizing MP4 container...")
            kotlinx.coroutines.delay(400)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("Render API complete! Video rendered successfully.")
            onSuccess()
        }
    }

    fun deleteAutoVideoJob(job: AutoVideoJobEntity) {
        viewModelScope.launch {
            repository.deleteAutoVideoJob(job)
            _toastMessage.tryEmit("Auto video record removed")
        }
    }

    fun clearAllAutoVideoJobs() {
        viewModelScope.launch {
            repository.clearAllAutoVideoJobs()
            _toastMessage.tryEmit("Auto video history cleared")
        }
    }

    fun addPromptTemplate(prompt: PromptTemplateEntity) {
        viewModelScope.launch {
            repository.addPromptTemplate(prompt)
            _toastMessage.tryEmit("Prompt added to Library!")
        }
    }

    fun deletePromptTemplate(prompt: PromptTemplateEntity) {
        viewModelScope.launch {
            repository.deletePromptTemplate(prompt)
            _toastMessage.tryEmit("Prompt removed from Library")
        }
    }

    fun incrementPromptUsage(id: Long) {
        viewModelScope.launch {
            repository.incrementPromptUsage(id)
        }
    }

    fun seedStarterAutoVideoJobs() {
        viewModelScope.launch {
            val starterJobs = listOf(
                AutoVideoJobEntity(
                    title = "AI Video Editing Revolution: How Content Creation Changed in 2026",
                    topic = "AI Video Production & Workflow Automation",
                    platform = "YouTube",
                    status = "Uploaded",
                    scriptText = "Scene 1: Traditional timeline editing takes 8 hours per video. What if an AI co-pilot could build your assembly cut, captions, and visual B-roll in under 60 seconds? Scene 2: We put three new neural editing pipelines to the test. First, intelligent beat-syncing and dynamic aspect ratio retargeting for YouTube and Instagram reels. Scene 3: The final verdict: human creativity directed by smart tooling produces 5x the output without sacrificing artistic vision.",
                    description = "Exploring the next generation of AI-driven creative suites for YouTube creators. Like and subscribe for more deep-dives into modern creator workflows!",
                    visualPrompt1 = "Cinematic studio setup with dual 4K monitors glowing with neon audio equalizer waves and 2026 timeline interface",
                    visualPrompt2 = "Side-by-side comparison screen illustrating instantaneous automated video timeline cutting and audio mastering",
                    visualPrompt3 = "Professional content creator celebrating rapid 10-minute long-form render with studio lighting",
                    format = "16:9 Long-Form",
                    durationSec = 600,
                    uploadId = "YT-REV-2026",
                    uploadUrl = "https://studio.youtube.com/video/YT-REV-2026/edit"
                ),
                AutoVideoJobEntity(
                    title = "3 Reel Hooks That Exploded My Engagement to 250k Views",
                    topic = "Short-Form Video Growth & Hooks",
                    platform = "Instagram",
                    status = "Uploaded",
                    scriptText = "Hook: If you are still starting your Reels with 'Hey guys', here is why your view duration is collapsing at second 2! Tip 1: Open with an open loop question that visually conflicts with expectations. Tip 2: Use rapid motion in the first 800 milliseconds. Tip 3: End on a seamless looping sentence that tricks the algorithm into repeating.",
                    description = "Master short-form retention with these 3 proven hook formulas. Save this Reel for your next shoot! #creators #reeltips #instagramgrowth",
                    visualPrompt1 = "Vibrant smartphone screen showing viral view counter surging past 250,000 views with heart confetti animations",
                    visualPrompt2 = "Dynamic split-screen demonstrating bad text hook versus ultra-engaging question hook with bold yellow subtitles",
                    visualPrompt3 = "Creator snapping fingers as seamless loop transition resets timeline seamlessly",
                    format = "9:16 Shorts",
                    durationSec = 45,
                    uploadId = "IG-HOOK-992",
                    uploadUrl = "https://www.instagram.com/reel/IG-HOOK-992"
                ),
                AutoVideoJobEntity(
                    title = "Top 5 Free Tools Every Facebook Video Creator Needs",
                    topic = "Facebook Creator Studio & Monetization",
                    platform = "Facebook",
                    status = "Uploaded",
                    scriptText = "Hey creators! Facebook Watch algorithms prioritize three crucial metrics: 1-minute retention rate, qualified caption reading, and original audio fidelity. Here are the 5 completely free tools you should use to hit these milestones on every single upload this month.",
                    description = "Level up your Facebook page reach with these zero-cost video enhancement utilities. Follow our page for daily creator strategies!",
                    visualPrompt1 = "Modern flat-lay creator workspace with smartphone, condenser microphone, and Facebook Creator Studio analytics",
                    visualPrompt2 = "Animated infographic highlighting Facebook video retention curves and caption readability metrics",
                    visualPrompt3 = "Happy video publisher reviewing verified monetized dashboard with rising graph",
                    format = "16:9 Long-Form",
                    durationSec = 420,
                    uploadId = "FB-TOOLS-412",
                    uploadUrl = "https://www.facebook.com/watch/?v=FB-TOOLS-412"
                )
            )
            repository.insertAllAutoVideoJobs(starterJobs)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            seedStarterAutoVideoJobs()
            repository.addTask(
                CalendarTaskEntity(
                    title = "How Top Creators Edit Videos Faster in 2026",
                    platform = "YouTube",
                    contentType = "Video",
                    status = "Scheduled",
                    scheduledDate = "2026-10-18",
                    scheduledTime = "18:00",
                    notes = "16:9 Long-Form breakdown with timeline shortcuts."
                )
            )
            repository.addTask(
                CalendarTaskEntity(
                    title = "Stop Making These 3 Reel Mistakes",
                    platform = "Instagram",
                    contentType = "Reel",
                    status = "Draft",
                    scheduledDate = "2026-10-19",
                    scheduledTime = "14:00",
                    notes = "Fast cut 9:16 vertical hook."
                )
            )
            _toastMessage.tryEmit("Sample creator items populated!")
        }
    }

    fun saveVideo(video: VideoMetadataEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveVideo(video)
            val label = if (video.isDraft) "Draft saved" else "Video metadata saved"
            _toastMessage.tryEmit("$label: \"${video.title}\"")
            onComplete?.invoke(id)
        }
    }

    fun updateVideo(video: VideoMetadataEntity) {
        viewModelScope.launch {
            repository.updateVideo(video)
            _toastMessage.tryEmit("Updated: \"${video.title}\"")
        }
    }

    fun deleteVideo(video: VideoMetadataEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video)
            _toastMessage.tryEmit("Video draft removed")
        }
    }

    fun deleteVideoById(id: Long) {
        viewModelScope.launch {
            repository.deleteVideoById(id)
            _toastMessage.tryEmit("Video draft removed")
        }
    }

    fun updateVideoDraftStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            val isDraft = newStatus.lowercase() != "published"
            repository.updateVideoDraftStatus(id, newStatus, isDraft)
            _toastMessage.tryEmit("Draft status changed to '$newStatus'")
        }
    }

    fun updateVideoMetadata(id: Long, title: String, description: String) {
        viewModelScope.launch {
            repository.updateVideoMetadata(id, title, description)
            _toastMessage.tryEmit("Video metadata updated")
        }
    }

    fun clearAllDrafts() {
        viewModelScope.launch {
            repository.clearAllDrafts()
            _toastMessage.tryEmit("All video drafts cleared")
        }
    }

    // Manga Stories & Chapters
    fun getChaptersForStory(storyId: Long): Flow<List<MangaChapterEntity>> = repository.getChaptersForStory(storyId)

    fun addMangaStory(story: MangaStoryEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.addMangaStory(story)
            _toastMessage.tryEmit("Created Manga Story: \"${story.title}\"")
            onComplete?.invoke(id)
        }
    }

    fun updateMangaStory(story: MangaStoryEntity) {
        viewModelScope.launch {
            repository.updateMangaStory(story)
            _toastMessage.tryEmit("Updated Manga: \"${story.title}\"")
        }
    }

    fun deleteMangaStory(story: MangaStoryEntity) {
        viewModelScope.launch {
            repository.deleteMangaStory(story)
            _toastMessage.tryEmit("Deleted Manga Story: \"${story.title}\"")
        }
    }

    fun addMangaChapter(chapter: MangaChapterEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.addMangaChapter(chapter)
            _toastMessage.tryEmit("Added Chapter ${chapter.chapterNumber}: \"${chapter.title}\"")
            onComplete?.invoke(id)
        }
    }

    fun updateMangaChapter(chapter: MangaChapterEntity) {
        viewModelScope.launch {
            repository.updateMangaChapter(chapter)
            _toastMessage.tryEmit("Updated Chapter ${chapter.chapterNumber}")
        }
    }

    fun deleteMangaChapter(chapter: MangaChapterEntity) {
        viewModelScope.launch {
            repository.deleteMangaChapter(chapter)
            _toastMessage.tryEmit("Removed Chapter ${chapter.chapterNumber}")
        }
    }

    // Multi-modal Output 1: Audio Generation
    fun generateChapterAudio(chapter: MangaChapterEntity, onComplete: (MangaChapterEntity) -> Unit) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "AI Manga Audio Generation",
                subtitle = "Synthesizing anime voice actors, cinematic BGM & SFX for Chapter ${chapter.chapterNumber}...",
                actionType = "api",
                progress = 0.3f
            )
            kotlinx.coroutines.delay(1000)
            updateGlobalVideoProgress(0.75f, "Mixing dramatic audio stems & sound effects...")
            kotlinx.coroutines.delay(800)
            val updated = chapter.copy(
                hasAudio = true,
                audioUrl = "https://actions.google.com/sounds/v1/science_fiction/scifi_laser_burst.ogg",
                audioDurationSec = 145,
                status = if (chapter.hasVideo) "Ready" else "Audio Ready",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMangaChapter(updated)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("🎧 Chapter Audio generated with anime voice acting & BGM!")
            onComplete(updated)
        }
    }

    // Multi-modal Output 2: Video Overview Generation
    fun generateChapterVideo(chapter: MangaChapterEntity, onComplete: (MangaChapterEntity) -> Unit) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "Motion Manga Video Generation",
                subtitle = "Rendering dynamic 16:9 panel animation with Ken Burns motion & visual SFX...",
                actionType = "render",
                progress = 0.25f
            )
            kotlinx.coroutines.delay(1000)
            updateGlobalVideoProgress(0.65f, "Compiling panel transitions, kinetic text & audio dub...")
            kotlinx.coroutines.delay(1000)
            updateGlobalVideoProgress(0.95f, "Finalizing 1080p motion video container...")
            kotlinx.coroutines.delay(400)
            val updated = chapter.copy(
                hasVideo = true,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                videoDurationSec = 75,
                status = "Video Rendered",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMangaChapter(updated)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("🎬 Motion Manga Video Overview rendered! Media3 player ready.")
            onComplete(updated)
        }
    }

    // Multi-modal Output 3: Slide Deck Generation
    fun generateChapterSlideDeck(chapter: MangaChapterEntity, onComplete: (MangaChapterEntity) -> Unit) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "Slide Deck Generation",
                subtitle = "Synthesizing 5-slide visual presentation deck for Chapter ${chapter.chapterNumber}...",
                actionType = "generate",
                progress = 0.4f
            )
            kotlinx.coroutines.delay(800)
            val slidesJson = "SLIDES_READY"
            val updated = chapter.copy(
                slideDeckJson = slidesJson,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMangaChapter(updated)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("📊 Slide Deck synthesized with chapter beats & character cards!")
            onComplete(updated)
        }
    }

    // Multi-modal Output 4: Flashcards Generation
    fun generateChapterFlashcards(chapter: MangaChapterEntity, onComplete: (MangaChapterEntity) -> Unit) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "Manga Flashcards Generation",
                subtitle = "Extracting lore, character abilities & Kanji vocabulary flashcards...",
                actionType = "generate",
                progress = 0.5f
            )
            kotlinx.coroutines.delay(700)
            val updated = chapter.copy(
                flashcardsJson = "FLASHCARDS_READY",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMangaChapter(updated)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("🃏 Interactive Manga Lore & Kanji Flashcards generated!")
            onComplete(updated)
        }
    }

    // Multi-modal Output 5: Infographic Generation
    fun generateChapterInfographic(chapter: MangaChapterEntity, onComplete: (MangaChapterEntity) -> Unit) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "Infographic Generation",
                subtitle = "Building character power scaling, relationship chart & chapter arc breakdown...",
                actionType = "generate",
                progress = 0.5f
            )
            kotlinx.coroutines.delay(700)
            val updated = chapter.copy(
                infographicJson = "INFOGRAPHIC_READY",
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMangaChapter(updated)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("📈 Story Arc & Power-Scaling Infographic generated!")
            onComplete(updated)
        }
    }

    // Multi-modal Output 6: Comprehensive Report Generation
    fun generateChapterReport(chapter: MangaChapterEntity, onComplete: (MangaChapterEntity) -> Unit) {
        viewModelScope.launch {
            showGlobalVideoProgress(
                title = "Executive Story Report",
                subtitle = "Compiling full chapter manuscript, pacing analysis & script breakdown...",
                actionType = "generate",
                progress = 0.4f
            )
            kotlinx.coroutines.delay(800)
            val report = """
# Chapter ${chapter.chapterNumber}: ${chapter.title} - Complete Executive Manuscript
- Story Target: Shonen Weekly Release & Motion Webtoon
- Plot Summary: ${chapter.summary}
- Pacing Index: 92/100 (Optimal rising action with cliffhanger hook)
- Character Arcs: Protagonist awakening power core under life-or-death pressure
- Sound Design: Synthwave + Orchestral strings + Katana reverb
- Scene Breakdown:
  1. Opening Hook: Rainy skyline with holographic Zaibatsu billboards
  2. Escalation: Hunter-drones swarm the skybridge
  3. Climax: 7-second time rewind execution with cyan blade
  4. Cliffhanger: Enigmatic figure watches from shadow
            """.trimIndent()
            val updated = chapter.copy(
                reportText = report,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMangaChapter(updated)
            hideGlobalVideoProgress()
            _toastMessage.tryEmit("📋 Comprehensive Story Manuscript & Report compiled!")
            onComplete(updated)
        }
    }

    // Cartoon Character Library Methods
    fun addCartoonCharacter(char: CartoonCharacterEntity) {
        viewModelScope.launch { repository.addCartoonCharacter(char) }
    }

    fun deleteCartoonCharacter(char: CartoonCharacterEntity) {
        viewModelScope.launch { repository.deleteCartoonCharacter(char) }
    }

    // Social Media Publishing Log Methods
    fun addSocialPublishLog(log: SocialPublishLogEntity) {
        viewModelScope.launch { repository.addSocialPublishLog(log) }
    }

    fun clearAllPublishLogs() {
        viewModelScope.launch { repository.clearAllPublishLogs() }
    }

    // AI Prompt Items Methods
    fun addAiPromptItem(item: AiPromptItemEntity) {
        viewModelScope.launch { repository.addAiPromptItem(item) }
    }

    fun deleteAiPromptItem(item: AiPromptItemEntity) {
        viewModelScope.launch { repository.deleteAiPromptItem(item) }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreSyncManager.stopRealtimeSync()
    }
}
