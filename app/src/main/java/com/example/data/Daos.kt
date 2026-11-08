package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM creator_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<CreatorSettingsEntity?>

    @Query("SELECT * FROM creator_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): CreatorSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: CreatorSettingsEntity)
}

@Dao
interface CalendarTaskDao {
    @Query("SELECT * FROM calendar_tasks ORDER BY scheduledDate ASC, scheduledTime ASC")
    fun getAllTasks(): Flow<List<CalendarTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CalendarTaskEntity): Long

    @Update
    suspend fun updateTask(task: CalendarTaskEntity)

    @Delete
    suspend fun deleteTask(task: CalendarTaskEntity)

    @Query("UPDATE calendar_tasks SET status = :newStatus WHERE id = :taskId")
    suspend fun updateStatus(taskId: Long, newStatus: String)

    @Query("UPDATE calendar_tasks SET scheduledDate = :newDate WHERE id = :taskId")
    suspend fun updateDate(taskId: Long, newDate: String)
}

@Dao
interface ContentProjectDao {
    @Query("SELECT * FROM content_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ContentProjectEntity>>

    @Query("SELECT * FROM content_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ContentProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ContentProjectEntity): Long

    @Update
    suspend fun updateProject(project: ContentProjectEntity)

    @Delete
    suspend fun deleteProject(project: ContentProjectEntity)
}

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItemEntity): Long

    @Delete
    suspend fun deleteMedia(item: MediaItemEntity)
}

@Dao
interface SavedScriptDao {
    @Query("SELECT * FROM saved_scripts ORDER BY createdAt DESC")
    fun getAllScripts(): Flow<List<SavedScriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScriptEntity): Long

    @Delete
    suspend fun deleteScript(script: SavedScriptEntity)
}

@Dao
interface AutoVideoJobDao {
    @Query("SELECT * FROM auto_video_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<AutoVideoJobEntity>>

    @Query("SELECT * FROM auto_video_jobs ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getLastNJobs(limit: Int = 100): List<AutoVideoJobEntity>

    @Query("SELECT * FROM auto_video_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): AutoVideoJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: AutoVideoJobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<AutoVideoJobEntity>): List<Long>

    @Update
    suspend fun updateJob(job: AutoVideoJobEntity)

    @Delete
    suspend fun deleteJob(job: AutoVideoJobEntity)

    @Query("DELETE FROM auto_video_jobs")
    suspend fun clearAllJobs()
}

@Dao
interface PromptTemplateDao {
    @Query("SELECT * FROM prompt_templates ORDER BY isFavorite DESC, usageCount DESC, id ASC")
    fun getAllPrompts(): Flow<List<PromptTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prompts: List<PromptTemplateEntity>)

    @Update
    suspend fun updatePrompt(prompt: PromptTemplateEntity)

    @Delete
    suspend fun deletePrompt(prompt: PromptTemplateEntity)

    @Query("UPDATE prompt_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)
}

@Dao
interface VideoMetadataDao {
    @Query("SELECT * FROM video_metadata ORDER BY updatedAt DESC")
    fun getAllVideos(): Flow<List<VideoMetadataEntity>>

    @Query("SELECT * FROM video_metadata WHERE isDraft = 1 ORDER BY updatedAt DESC")
    fun getDraftVideos(): Flow<List<VideoMetadataEntity>>

    @Query("SELECT * FROM video_metadata WHERE draftStatus = :status ORDER BY updatedAt DESC")
    fun getVideosByStatus(status: String): Flow<List<VideoMetadataEntity>>

    @Query("SELECT * FROM video_metadata WHERE platform = :platform ORDER BY updatedAt DESC")
    fun getVideosByPlatform(platform: String): Flow<List<VideoMetadataEntity>>

    @Query("SELECT * FROM video_metadata WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoMetadataEntity?

    @Query("SELECT * FROM video_metadata WHERE id = :id LIMIT 1")
    fun getVideoByIdFlow(id: Long): Flow<VideoMetadataEntity?>

    @Query("SELECT COUNT(*) FROM video_metadata WHERE isDraft = 1")
    fun getDraftCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoMetadataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoMetadataEntity>): List<Long>

    @Update
    suspend fun updateVideo(video: VideoMetadataEntity)

    @Query("UPDATE video_metadata SET draftStatus = :newStatus, isDraft = :isDraft, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDraftStatus(
        id: Long,
        newStatus: String,
        isDraft: Boolean = (newStatus != "Published"),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE video_metadata SET title = :title, description = :description, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMetadata(
        id: Long,
        title: String,
        description: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteVideo(video: VideoMetadataEntity)

    @Query("DELETE FROM video_metadata WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM video_metadata WHERE draftStatus = 'Draft' AND isDraft = 1")
    suspend fun clearAllDrafts()
}

@Dao
interface MangaStoryDao {
    @Query("SELECT * FROM manga_stories ORDER BY updatedAt DESC")
    fun getAllStories(): Flow<List<MangaStoryEntity>>

    @Query("SELECT * FROM manga_stories WHERE id = :id LIMIT 1")
    fun getStoryById(id: Long): Flow<MangaStoryEntity?>

    @Query("SELECT * FROM manga_stories WHERE id = :id LIMIT 1")
    suspend fun getStoryByIdOnce(id: Long): MangaStoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: MangaStoryEntity): Long

    @Update
    suspend fun updateStory(story: MangaStoryEntity)

    @Delete
    suspend fun deleteStory(story: MangaStoryEntity)

    @Query("DELETE FROM manga_stories WHERE id = :id")
    suspend fun deleteStoryById(id: Long)
}

@Dao
interface MangaChapterDao {
    @Query("SELECT * FROM manga_chapters WHERE storyId = :storyId ORDER BY chapterNumber ASC")
    fun getChaptersForStory(storyId: Long): Flow<List<MangaChapterEntity>>

    @Query("SELECT * FROM manga_chapters WHERE id = :id LIMIT 1")
    fun getChapterById(id: Long): Flow<MangaChapterEntity?>

    @Query("SELECT * FROM manga_chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterByIdOnce(id: Long): MangaChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: MangaChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: MangaChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: MangaChapterEntity)

    @Query("DELETE FROM manga_chapters WHERE id = :id")
    suspend fun deleteChapterById(id: Long)

    @Query("DELETE FROM manga_chapters WHERE storyId = :storyId")
    suspend fun deleteChaptersForStory(storyId: Long)
}

@Dao
interface GitHubRepoDao {
    @Query("SELECT * FROM cached_github_repos ORDER BY stargazersCount DESC, name ASC")
    fun getAllCachedRepos(): Flow<List<CachedGitHubRepoEntity>>

    @Query("SELECT * FROM cached_github_repos WHERE id = :id LIMIT 1")
    suspend fun getRepoById(id: Long): CachedGitHubRepoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepos(repos: List<CachedGitHubRepoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<CachedGitHubRepoEntity>)

    @Query("DELETE FROM cached_github_repos")
    suspend fun clearAllRepos()

    @Query("DELETE FROM cached_github_repos WHERE id = :id")
    suspend fun deleteRepoById(id: Long)
}

@Dao
interface CartoonCharacterDao {
    @Query("SELECT * FROM cartoon_characters ORDER BY createdAt DESC")
    fun getAllCharacters(): Flow<List<CartoonCharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CartoonCharacterEntity): Long

    @Update
    suspend fun updateCharacter(character: CartoonCharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CartoonCharacterEntity)
}

@Dao
interface SocialPublishLogDao {
    @Query("SELECT * FROM social_publish_logs ORDER BY timestamp DESC")
    fun getAllPublishLogs(): Flow<List<SocialPublishLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SocialPublishLogEntity): Long

    @Query("DELETE FROM social_publish_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM social_publish_logs")
    suspend fun clearAllLogs()
}

@Dao
interface AiPromptItemDao {
    @Query("SELECT * FROM ai_prompt_items ORDER BY createdAt DESC")
    fun getAllPrompts(): Flow<List<AiPromptItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(item: AiPromptItemEntity): Long

    @Delete
    suspend fun deletePrompt(item: AiPromptItemEntity)
}




