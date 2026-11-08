package com.example.data

import kotlinx.coroutines.flow.Flow

class CreatorRepository(private val db: AppDatabase) {
    val settings: Flow<CreatorSettingsEntity?> = db.settingsDao().getSettings()
    val calendarTasks: Flow<List<CalendarTaskEntity>> = db.calendarTaskDao().getAllTasks()
    val projects: Flow<List<ContentProjectEntity>> = db.contentProjectDao().getAllProjects()
    val mediaItems: Flow<List<MediaItemEntity>> = db.mediaItemDao().getAllMedia()
    val savedScripts: Flow<List<SavedScriptEntity>> = db.savedScriptDao().getAllScripts()
    val autoVideoJobs: Flow<List<AutoVideoJobEntity>> = db.autoVideoJobDao().getAllJobs()
    val promptTemplates: Flow<List<PromptTemplateEntity>> = db.promptTemplateDao().getAllPrompts()
    val allVideos: Flow<List<VideoMetadataEntity>> = db.videoMetadataDao().getAllVideos()
    val draftVideos: Flow<List<VideoMetadataEntity>> = db.videoMetadataDao().getDraftVideos()
    val draftCount: Flow<Int> = db.videoMetadataDao().getDraftCount()
    val mangaStories: Flow<List<MangaStoryEntity>> = db.mangaStoryDao().getAllStories()

    suspend fun getSettingsOnce(): CreatorSettingsEntity? = db.settingsDao().getSettingsOnce()

    suspend fun saveSettings(settings: CreatorSettingsEntity) {
        db.settingsDao().insertOrUpdate(settings)
    }

    suspend fun addTask(task: CalendarTaskEntity): Long {
        return db.calendarTaskDao().insertTask(task)
    }

    suspend fun updateTask(task: CalendarTaskEntity) {
        db.calendarTaskDao().updateTask(task)
    }

    suspend fun deleteTask(task: CalendarTaskEntity) {
        db.calendarTaskDao().deleteTask(task)
    }

    suspend fun updateTaskStatus(taskId: Long, newStatus: String) {
        db.calendarTaskDao().updateStatus(taskId, newStatus)
    }

    suspend fun updateTaskDate(taskId: Long, newDate: String) {
        db.calendarTaskDao().updateDate(taskId, newDate)
    }

    suspend fun addProject(project: ContentProjectEntity): Long {
        return db.contentProjectDao().insertProject(project)
    }

    suspend fun updateProject(project: ContentProjectEntity) {
        db.contentProjectDao().updateProject(project)
    }

    suspend fun deleteProject(project: ContentProjectEntity) {
        db.contentProjectDao().deleteProject(project)
    }

    suspend fun addMedia(item: MediaItemEntity): Long {
        return db.mediaItemDao().insertMedia(item)
    }

    suspend fun deleteMedia(item: MediaItemEntity) {
        db.mediaItemDao().deleteMedia(item)
    }

    suspend fun addScript(script: SavedScriptEntity): Long {
        return db.savedScriptDao().insertScript(script)
    }

    suspend fun deleteScript(script: SavedScriptEntity) {
        db.savedScriptDao().deleteScript(script)
    }

    suspend fun addAutoVideoJob(job: AutoVideoJobEntity): Long {
        return db.autoVideoJobDao().insertJob(job)
    }

    suspend fun insertAllAutoVideoJobs(jobs: List<AutoVideoJobEntity>): List<Long> {
        return db.autoVideoJobDao().insertAll(jobs)
    }

    suspend fun getLastNJobs(limit: Int = 100): List<AutoVideoJobEntity> {
        return db.autoVideoJobDao().getLastNJobs(limit)
    }

    suspend fun getJobById(id: Long): AutoVideoJobEntity? {
        return db.autoVideoJobDao().getJobById(id)
    }

    suspend fun updateAutoVideoJob(job: AutoVideoJobEntity) {
        db.autoVideoJobDao().updateJob(job)
    }

    suspend fun deleteAutoVideoJob(job: AutoVideoJobEntity) {
        db.autoVideoJobDao().deleteJob(job)
    }

    suspend fun clearAllAutoVideoJobs() {
        db.autoVideoJobDao().clearAllJobs()
    }

    suspend fun addPromptTemplate(prompt: PromptTemplateEntity): Long {
        return db.promptTemplateDao().insertPrompt(prompt)
    }

    suspend fun updatePromptTemplate(prompt: PromptTemplateEntity) {
        db.promptTemplateDao().updatePrompt(prompt)
    }

    suspend fun deletePromptTemplate(prompt: PromptTemplateEntity) {
        db.promptTemplateDao().deletePrompt(prompt)
    }

    suspend fun incrementPromptUsage(id: Long) {
        db.promptTemplateDao().incrementUsage(id)
    }

    suspend fun getVideoById(id: Long): VideoMetadataEntity? = db.videoMetadataDao().getVideoById(id)

    fun getVideoByIdFlow(id: Long): Flow<VideoMetadataEntity?> = db.videoMetadataDao().getVideoByIdFlow(id)

    suspend fun saveVideo(video: VideoMetadataEntity): Long = db.videoMetadataDao().insertVideo(video)

    suspend fun updateVideo(video: VideoMetadataEntity) = db.videoMetadataDao().updateVideo(video)

    suspend fun updateVideoDraftStatus(id: Long, newStatus: String, isDraft: Boolean = (newStatus != "Published")) =
        db.videoMetadataDao().updateDraftStatus(id, newStatus, isDraft)

    suspend fun updateVideoMetadata(id: Long, title: String, description: String) =
        db.videoMetadataDao().updateMetadata(id, title, description)

    suspend fun deleteVideo(video: VideoMetadataEntity) = db.videoMetadataDao().deleteVideo(video)

    suspend fun deleteVideoById(id: Long) = db.videoMetadataDao().deleteVideoById(id)

    suspend fun clearAllDrafts() = db.videoMetadataDao().clearAllDrafts()

    // Manga Stories & Chapters
    fun getChaptersForStory(storyId: Long): Flow<List<MangaChapterEntity>> = db.mangaChapterDao().getChaptersForStory(storyId)

    fun getStoryById(id: Long): Flow<MangaStoryEntity?> = db.mangaStoryDao().getStoryById(id)

    suspend fun getStoryByIdOnce(id: Long): MangaStoryEntity? = db.mangaStoryDao().getStoryByIdOnce(id)

    suspend fun addMangaStory(story: MangaStoryEntity): Long = db.mangaStoryDao().insertStory(story)

    suspend fun updateMangaStory(story: MangaStoryEntity) = db.mangaStoryDao().updateStory(story)

    suspend fun deleteMangaStory(story: MangaStoryEntity) {
        db.mangaChapterDao().deleteChaptersForStory(story.id)
        db.mangaStoryDao().deleteStory(story)
    }

    fun getChapterById(id: Long): Flow<MangaChapterEntity?> = db.mangaChapterDao().getChapterById(id)

    suspend fun getChapterByIdOnce(id: Long): MangaChapterEntity? = db.mangaChapterDao().getChapterByIdOnce(id)

    suspend fun addMangaChapter(chapter: MangaChapterEntity): Long = db.mangaChapterDao().insertChapter(chapter)

    suspend fun updateMangaChapter(chapter: MangaChapterEntity) = db.mangaChapterDao().updateChapter(chapter)

    suspend fun deleteMangaChapter(chapter: MangaChapterEntity) = db.mangaChapterDao().deleteChapter(chapter)

    // GitHub Cached Repositories
    val cachedGitHubRepos: Flow<List<CachedGitHubRepoEntity>> = db.gitHubRepoDao().getAllCachedRepos()

    suspend fun cacheGitHubRepos(repos: List<CachedGitHubRepoEntity>) = db.gitHubRepoDao().insertRepos(repos)

    suspend fun clearCachedGitHubRepos() = db.gitHubRepoDao().clearAllRepos()

    suspend fun getCachedRepoById(id: Long): CachedGitHubRepoEntity? = db.gitHubRepoDao().getRepoById(id)

    // Cartoon Characters
    val cartoonCharacters: Flow<List<CartoonCharacterEntity>> = db.cartoonCharacterDao().getAllCharacters()

    suspend fun addCartoonCharacter(char: CartoonCharacterEntity): Long = db.cartoonCharacterDao().insertCharacter(char)

    suspend fun updateCartoonCharacter(char: CartoonCharacterEntity) = db.cartoonCharacterDao().updateCharacter(char)

    suspend fun deleteCartoonCharacter(char: CartoonCharacterEntity) = db.cartoonCharacterDao().deleteCharacter(char)

    // Social Publish Logs
    val socialPublishLogs: Flow<List<SocialPublishLogEntity>> = db.socialPublishLogDao().getAllPublishLogs()

    suspend fun addSocialPublishLog(log: SocialPublishLogEntity): Long = db.socialPublishLogDao().insertLog(log)

    suspend fun deletePublishLogById(id: Long) = db.socialPublishLogDao().deleteLogById(id)

    suspend fun clearAllPublishLogs() = db.socialPublishLogDao().clearAllLogs()

    // AI Prompt Items
    val aiPromptItems: Flow<List<AiPromptItemEntity>> = db.aiPromptItemDao().getAllPrompts()

    suspend fun addAiPromptItem(item: AiPromptItemEntity): Long = db.aiPromptItemDao().insertPrompt(item)

    suspend fun deleteAiPromptItem(item: AiPromptItemEntity) = db.aiPromptItemDao().deletePrompt(item)
}



