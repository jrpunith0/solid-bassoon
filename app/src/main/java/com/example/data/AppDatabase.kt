package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CreatorSettingsEntity::class,
        CalendarTaskEntity::class,
        ContentProjectEntity::class,
        MediaItemEntity::class,
        SavedScriptEntity::class,
        AutoVideoJobEntity::class,
        PromptTemplateEntity::class,
        VideoMetadataEntity::class,
        MangaStoryEntity::class,
        MangaChapterEntity::class,
        CachedGitHubRepoEntity::class,
        CartoonCharacterEntity::class,
        SocialPublishLogEntity::class,
        AiPromptItemEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun calendarTaskDao(): CalendarTaskDao
    abstract fun contentProjectDao(): ContentProjectDao
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun savedScriptDao(): SavedScriptDao
    abstract fun autoVideoJobDao(): AutoVideoJobDao
    abstract fun promptTemplateDao(): PromptTemplateDao
    abstract fun videoMetadataDao(): VideoMetadataDao
    abstract fun mangaStoryDao(): MangaStoryDao
    abstract fun mangaChapterDao(): MangaChapterDao
    abstract fun gitHubRepoDao(): GitHubRepoDao
    abstract fun cartoonCharacterDao(): CartoonCharacterDao
    abstract fun socialPublishLogDao(): SocialPublishLogDao
    abstract fun aiPromptItemDao(): AiPromptItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "creator_studio.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate initial starter data
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            prepopulate(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulate(db: AppDatabase) {
            val settingsDao = db.settingsDao()
            val calendarDao = db.calendarTaskDao()
            val projectDao = db.contentProjectDao()
            val mediaDao = db.mediaItemDao()
            val scriptDao = db.savedScriptDao()

            // Default Settings: Google Gmail Not Logged In, Linked Platforms Ready to Login Add
            settingsDao.insertOrUpdate(
                CreatorSettingsEntity(
                    id = 1,
                    youtubeChannelUrl = "https://www.youtube.com",
                    youtubeStudioUrl = "https://studio.youtube.com",
                    instagramUrl = "https://www.instagram.com",
                    facebookUrl = "https://www.facebook.com",
                    channelName = "Creator Studio",
                    defaultLanguage = "en",
                    theme = "dark",
                    wizardCompleted = true,
                    youtubeSubscribers = 24500,
                    instagramFollowers = 18200,
                    facebookFollowers = 9800,
                    monthlyViews = 340000,
                    engagementRate = 7.8f,
                    isGoogleAccountLoggedIn = false,
                    isYouTubeConnected = false,
                    isInstagramConnected = false,
                    isFacebookConnected = false,
                    gitHubUsername = "Anil-matcha",
                    gitHubRepo = "AI-Youtube-Shorts-Generator",
                    gitHubBranch = "main",
                    isGitHubConnected = true,
                    gitHubLastSync = "Ready to Sync",
                    gitHubAvatarUrl = "https://avatars.githubusercontent.com/u/Anil-matcha",
                    gitHubBio = "AI-Youtube-Shorts-Generator: Automated text-to-shorts AI video generator pipeline with subtitles and audio (Anil-matcha/AI-Youtube-Shorts-Generator)",
                    gitHubPublicRepos = 24,
                    youTubeEmail = "",
                    googleAccountName = "Not Logged In"
                )
            )

            // Starter Cartoon Character for Animation Studio
            db.cartoonCharacterDao().insertCharacter(
                CartoonCharacterEntity(
                    id = 1,
                    name = "Arjun",
                    role = "Protagonist",
                    visualStyle = "3D Pixar Cartoon",
                    clothingDetails = "Saffron creator hoodie with denim joggers",
                    personality = "Clever, curious Indian youth exploring AI mysteries",
                    promptConsistencyNotes = "Maintain expressive hazel eyes, spiky hair, and warm skin tones across all scenes",
                    isLocked = true
                )
            )

            // Starter Social Publish Log
            db.socialPublishLogDao().insertLog(
                SocialPublishLogEntity(
                    id = 1,
                    platform = "YouTube",
                    title = "AI Video Shorts in 60 Seconds - Complete Guide",
                    caption = "Created and published with Creator Studio Pro AI Engine!",
                    hashtags = "#Shorts #AI #CreatorStudio #VideoEditing",
                    publishedUrl = "https://youtube.com/shorts/sample_ai_video",
                    channelOrPageName = "@CreatorStudio",
                    status = "Published"
                )
            )


            // Starter Calendar Tasks with Draft, Scheduled, Published
            calendarDao.insertTask(
                CalendarTaskEntity(
                    title = "Viral Editing Workflow in 2026",
                    platform = "YouTube",
                    contentType = "Video",
                    status = "Scheduled",
                    scheduledDate = "2026-10-08",
                    scheduledTime = "17:00",
                    notes = "Hook: 3 mistakes killing your watch time. 16:9 ratio."
                )
            )
            calendarDao.insertTask(
                CalendarTaskEntity(
                    title = "Behind the Scenes Studio Tour",
                    platform = "Instagram",
                    contentType = "Reel",
                    status = "Draft",
                    scheduledDate = "2026-10-09",
                    scheduledTime = "12:30",
                    notes = "Fast cuts with trending audio and sticker CTA."
                )
            )
            calendarDao.insertTask(
                CalendarTaskEntity(
                    title = "My Biggest Creator Milestone Ever!",
                    platform = "Facebook",
                    contentType = "Post",
                    status = "Published",
                    scheduledDate = "2026-10-05",
                    scheduledTime = "19:00",
                    notes = "Photo gallery with community appreciation message."
                )
            )
            calendarDao.insertTask(
                CalendarTaskEntity(
                    title = "5 Camera Settings You Must Turn Off",
                    platform = "YouTube",
                    contentType = "Short",
                    status = "Draft",
                    scheduledDate = "2026-10-10",
                    scheduledTime = "15:00",
                    notes = "Vertical 9:16 format with auto captions."
                )
            )
            calendarDao.insertTask(
                CalendarTaskEntity(
                    title = "Weekly Creator Q&A Live Session",
                    platform = "YouTube",
                    contentType = "Video",
                    status = "Scheduled",
                    scheduledDate = "2026-10-12",
                    scheduledTime = "20:00",
                    notes = "Answer questions from community comments."
                )
            )

            // Starter Projects
            projectDao.insertProject(
                ContentProjectEntity(
                    title = "Ultimate Setup Guide for New Creators",
                    topic = "Audio, Lighting, and Camera Gear",
                    platform = "YouTube",
                    format = "Video",
                    status = "Production",
                    scriptText = "Hook: Stop wasting thousands on gear you don't need.\nScene 1: Natural light vs softbox...\nScene 2: Dynamic vs Condenser mic...\nCTA: Subscribe for weekly creator gear tests.",
                    hookText = "Stop wasting money on camera gear until you watch this.",
                    ctaText = "Drop your current setup in the comments below!",
                    captionText = "The honest truth about what you actually need to start your channel in 2026. Link in bio!",
                    hashtagsText = "#creator #studio #youtube #filmmaking #techgear",
                    thumbnailTitle = "DON'T BUY THIS!",
                    thumbnailStyle = "YouTube Viral",
                    thumbnailBadge = "4K",
                    duration = "8m 30s"
                )
            )

            // Starter Media items
            mediaDao.insertMedia(
                MediaItemEntity(
                    title = "Studio B-Roll Master Clip 01",
                    category = "Videos",
                    fileType = "video/mp4",
                    uriOrPath = "local://broll_studio.mp4",
                    sizeFormatted = "48 MB",
                    durationFormatted = "0:45",
                    tags = "broll, camera, lighting"
                )
            )
            mediaDao.insertMedia(
                MediaItemEntity(
                    title = "Neon YouTube Thumbnail Concept",
                    category = "Thumbnails",
                    fileType = "image/png",
                    uriOrPath = "local://thumb_neon.png",
                    sizeFormatted = "1.8 MB",
                    tags = "thumbnail, 16:9, high-ctr"
                )
            )

            // Starter Script
            scriptDao.insertScript(
                SavedScriptEntity(
                    title = "Hook Strategy for Shorts and Reels",
                    topic = "First 3 Seconds Retention",
                    platform = "YouTube",
                    language = "en",
                    duration = "45s",
                    tone = "Energetic",
                    hook = "If your viewers leave in 3 seconds, here is the exact reason why.",
                    bodyContent = "Most creators start with an intro. Instead, show the climax first, give 3 rapid visual cuts, and state the payoff.",
                    cta = "Save this reel and try this structure on your next video."
                )
            )

            // Starter Prompt Templates ("add all prompt this app")
            val promptDao = db.promptTemplateDao()
            val starterPrompts = listOf(
                PromptTemplateEntity(
                    title = "Viral Short Retention Hook",
                    category = "Viral Shorts",
                    templateText = "Create a viral short-form video script about: \"{topic}\". Return JSON with title, SEO description, spoken voiceover script under 50 words, and 3 distinct visual generation prompts.",
                    sampleTopic = "Secret productivity hacks used by top creators",
                    platform = "YouTube Shorts",
                    tags = "viral, hook, retention",
                    isFavorite = true,
                    usageCount = 14
                ),
                PromptTemplateEntity(
                    title = "10-Minute Deep-Dive Masterclass",
                    category = "10-Min Video",
                    templateText = "Write a complete 10-minute long-form YouTube video plan about \"{topic}\" with 10 structured scenes, B-roll cues, on-screen graphics, and mid-roll retention hooks.",
                    sampleTopic = "How to build a profitable creator business in 2026",
                    platform = "YouTube",
                    tags = "10min, long-form, masterclass",
                    isFavorite = true,
                    usageCount = 9
                ),
                PromptTemplateEntity(
                    title = "High-Converting Instagram Reel",
                    category = "Reels",
                    templateText = "Generate an Instagram Reel caption and spoken voiceover script with a question hook, 3 key takeaways with emojis, and a save/share call-to-action.",
                    sampleTopic = "3 camera settings every smartphone creator must turn on",
                    platform = "Instagram",
                    tags = "instagram, reels, viral",
                    isFavorite = true,
                    usageCount = 12
                ),
                PromptTemplateEntity(
                    title = "Kannada Viral Tech & Culture Short",
                    category = "Kannada Culture",
                    templateText = "ಕನ್ನಡದಲ್ಲಿ ಆಕರ್ಷಕ ಮತ್ತು ವೈರಲ್ ಶಾರ್ಟ್ಸ್ ಸ್ಕ್ರಿಪ್ಟ್ ರಚಿಸಿ: \"{topic}\". ಪ್ರಾರಂಭದ ಹುಕ್, ಮುಖ್ಯ ವಿಷಯ, ಮತ್ತು ಕೊನೆಯ ಕಾಲ್-ಟು-ಆಕ್ಷನ್ ಒಳಗೊಂಡಿರಬೇಕು.",
                    sampleTopic = "ಕನ್ನಡದಲ್ಲಿ AI ಬಳಸಿಕೊಂಡು ವೀಡಿಯೊ ರಚಿಸುವುದು ಹೇಗೆ",
                    platform = "YouTube Shorts",
                    tags = "kannada, local, regional",
                    isFavorite = true,
                    usageCount = 18
                ),
                PromptTemplateEntity(
                    title = "3 Visual Scene Cinematic Prompts",
                    category = "Visual Prompts",
                    templateText = "Generate 3 photorealistic AI image generation prompts for video scenes about \"{topic}\": Scene 1 neon studio master wide-angle, Scene 2 close-up macro action, Scene 3 vibrant high-tech hero frame.",
                    sampleTopic = "Cyberpunk content studio setup with ambient lighting",
                    platform = "All",
                    tags = "visual, scene, image-gen",
                    isFavorite = false,
                    usageCount = 6
                ),
                PromptTemplateEntity(
                    title = "High-CTR YouTube Title & SEO Optimizer",
                    category = "YouTube SEO",
                    templateText = "Generate 5 high CTR YouTube titles under 60 characters with power words, curiosity gap, and search-volume keywords, plus 15 high-ranking tags.",
                    sampleTopic = "Best microphone for YouTube under 5000",
                    platform = "YouTube",
                    tags = "seo, titles, ctr",
                    isFavorite = false,
                    usageCount = 11
                ),
                PromptTemplateEntity(
                    title = "Facebook Community Engagement Post",
                    category = "Facebook",
                    templateText = "Write an engaging Facebook post that sparks comments and debate about \"{topic}\", formatted with clear paragraph breaks and a discussion question.",
                    sampleTopic = "Will AI replace video editors in the next 2 years?",
                    platform = "Facebook",
                    tags = "facebook, discussion, community",
                    isFavorite = false,
                    usageCount = 4
                )
            )
            promptDao.insertAll(starterPrompts)

            // Starter Auto Video Jobs (Simulated 10-Min Auto Generator Runs)
            val autoJobDao = db.autoVideoJobDao()
            autoJobDao.insertJob(
                AutoVideoJobEntity(
                    title = "The 3 Camera Secrets That Changed My Views Forever #Shorts",
                    topic = "Camera Settings for Creators",
                    platform = "YouTube",
                    status = "Uploaded",
                    scriptText = "Stop shooting in auto mode! Turn off auto white balance, lock your exposure, and set shutter speed to 1/100. Watch your video quality double instantly.",
                    description = "Master these 3 smartphone camera settings in 60 seconds. Subscribe for daily creator tips! #CreatorTips #YouTubeShorts #TechHacks",
                    visualPrompt1 = "Close-up of camera lens with neon cyan reflections and cinematic bokeh",
                    visualPrompt2 = "Side-by-side comparison of blurry video vs ultra sharp 4k footage",
                    visualPrompt3 = "Creator smiling in modern studio pointing to subscribe button with graphics",
                    format = "9:16 Shorts",
                    durationSec = 52,
                    uploadId = "YT-AUTO-8921",
                    uploadUrl = "https://studio.youtube.com/video/YT-AUTO-8921/edit"
                )
            )
            autoJobDao.insertJob(
                AutoVideoJobEntity(
                    title = "Stop Making This Audio Mistake in 2026",
                    topic = "Audio & Microphone Setup",
                    platform = "Instagram",
                    status = "Uploaded",
                    scriptText = "Good audio beats good video every single time. Here is the 1-second filter that removes background room echo from any cheap microphone.",
                    description = "Save this audio hack for your next reel! Tag a friend who needs cleaner mic audio. #ReelsTips #AudioHacks #CreatorGrowth",
                    visualPrompt1 = "Microphone waveform glowing in ambient purple studio",
                    visualPrompt2 = "Sound equalizer bouncing on smartphone interface",
                    visualPrompt3 = "Creator wearing headphones nodding with high satisfaction",
                    format = "9:16 Shorts",
                    durationSec = 45,
                    uploadId = "IG-REEL-4402",
                    uploadUrl = "https://www.instagram.com/reel/IG-REEL-4402"
                )
            )

            // Starter Video Metadata & Drafts
            val videoDao = db.videoMetadataDao()
            videoDao.insertVideo(
                VideoMetadataEntity(
                    title = "AI Video Production Masterclass",
                    description = "Comprehensive guide on building automated creator workflows with neural generation tools and timeline sequencing.",
                    topic = "AI Video Creation",
                    format = "16:9 Long-Form",
                    resolution = "1080p",
                    fps = 60,
                    durationSec = 615,
                    fileUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnailUri = "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=800&q=80",
                    fileSizeBytes = 145_000_000L,
                    tags = "AI, VideoEditing, CreatorStudio, Masterclass, 2026",
                    platform = "YouTube",
                    draftStatus = "Draft",
                    isDraft = true,
                    completionProgress = 0.75f,
                    audioTrackTitle = "Cyber Lo-Fi Dreamscape 120 BPM",
                    voiceoverTone = "Storyteller & Confident",
                    aspectRatio = "16:9",
                    notes = "Scene 1 & 2 fully animated. Need to fine-tune final CTA overlay."
                )
            )
            videoDao.insertVideo(
                VideoMetadataEntity(
                    title = "3 Retention Secrets for Vertical Reels",
                    description = "Fast-paced breakdown of opening hooks that boost 3-second hold rate above 70%.",
                    topic = "Instagram Algorithm",
                    format = "9:16 Shorts",
                    resolution = "1080p",
                    fps = 60,
                    durationSec = 35,
                    fileUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    thumbnailUri = "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800&q=80",
                    fileSizeBytes = 28_000_000L,
                    tags = "ReelsTips, ViralHooks, InstagramGrowth",
                    platform = "Instagram",
                    draftStatus = "Editing",
                    isDraft = true,
                    completionProgress = 0.45f,
                    audioTrackTitle = "Viral Phonk Bass Boost",
                    voiceoverTone = "High Energy & Direct",
                    aspectRatio = "9:16",
                    notes = "Rough cut timeline ready. Adding kinetic caption text."
                )
            )
            videoDao.insertVideo(
                VideoMetadataEntity(
                    title = "The 2026 Creator Economy Survival Kit",
                    description = "Complete tech stack recommendations for solo creators scaling multi-channel presence.",
                    topic = "Creator Tech Stack",
                    format = "16:9 Long-Form",
                    resolution = "4K",
                    fps = 60,
                    durationSec = 820,
                    fileUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    thumbnailUri = "https://images.unsplash.com/photo-1598550476439-6847785fcea6?w=800&q=80",
                    fileSizeBytes = 412_000_000L,
                    tags = "Gear, Cameras, Microphones, Productivity",
                    platform = "YouTube",
                    draftStatus = "Ready",
                    isDraft = false,
                    completionProgress = 1.0f,
                    audioTrackTitle = "Acoustic Warm Studio Flow",
                    voiceoverTone = "Relaxed & Authoritative",
                    aspectRatio = "16:9",
                    notes = "Color grading and loudness normalized. Ready for upload."
                )
            )

            // Starter Manga Stories & Chapters
            val storyDao = db.mangaStoryDao()
            val chapterDao = db.mangaChapterDao()

            val story1Id = storyDao.insertStory(
                MangaStoryEntity(
                    title = "Neo-Tokyo: Chrono Blade",
                    japaneseTitle = "ネオ東京：クロノブレード",
                    genre = "Cyberpunk / Action",
                    artStyle = "Modern Shonen",
                    synopsis = "In the cybernetic ruins of 2099 Neo-Tokyo, an orphaned cyber-samurai named Ren awakens with a forbidden Chrono Core capable of reversing time by 7 seconds. Hunted by the Zaibatsu Syndicate, he fights to uncover the truth behind the Great Blackout.",
                    coverImageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80",
                    authorName = "Ren Kusanagi",
                    status = "Ongoing",
                    totalChapters = 2,
                    tags = "Cyberpunk, Shonen, TimeManipulation, Samurai, Anime"
                )
            )

            chapterDao.insertChapter(
                MangaChapterEntity(
                    storyId = story1Id,
                    chapterNumber = 1,
                    title = "The Shattered Clocktower",
                    summary = "Ren cornered on the Shinjuku skybridge activates his Chrono Blade for the first time as mechanical hounds encircle him.",
                    contentScript = """
[PANEL 1 - WIDE SHOT]: Rain-slicked holographic billboards towering above Sector 4. Thunder crackles in violet arcs.
REN: (Internal Monologue) "Rain never washes away the rust... or the blood."

[PANEL 2 - CLOSE UP]: Three cybernetic hunter-drones descend, red targeting lasers locking onto Ren's collar.
SFX: *BZZZZZZ-SHK*
DRONE-ALPHA: "Subject 09-Omega located. Lethal force authorized."

[PANEL 3 - ACTION SLASH]: Ren reaches for his hilt. The Chrono Core pulses cyan. Time distorts into slow-motion shards!
REN: "Seven seconds... that is all the time I need to dismantle you."
SFX: *CHRONO-BURST!* *CLANGGGG!*
                    """.trimIndent(),
                    audioUrl = "https://actions.google.com/sounds/v1/science_fiction/scifi_laser_burst.ogg",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    audioDurationSec = 145,
                    videoDurationSec = 75,
                    hasAudio = true,
                    hasVideo = true,
                    status = "Video Rendered",
                    reportText = """
# Manga Chapter 1 Production Report
- Story Arc: The Awakening Arc (Act I)
- Scene Count: 8 Key Manga Panels
- Pacing Score: 94/100 (Optimal Shonen Cliffhanger)
- Sound Design: Synthwave Cyber Beat with Katana Reverb
- Key Takeaway: Opening 3 panels hook reader interest with visual spectacle and stakes.
                    """.trimIndent()
                )
            )

            chapterDao.insertChapter(
                MangaChapterEntity(
                    storyId = story1Id,
                    chapterNumber = 2,
                    title = "Echoes in the Neon Slums",
                    summary = "Ren seeks refuge in the subterranean hackers den of Alley 9, meeting the enigmatic net-weaver Maya.",
                    contentScript = """
[PANEL 1]: Low-lit underground workshop cluttered with severed android limbs and flickering neon cables.
MAYA: "Look what the cyber-rats dragged in. You're bleeding Chrono-fluid on my antique carpet, samurai."
REN: "Zaibatsu sent their death squad. I need an EMP suppressor."
MAYA: "Nothing in Sector 4 is free. What are you offering?"
                    """.trimIndent(),
                    hasAudio = false,
                    hasVideo = false,
                    status = "Draft"
                )
            )

            val story2Id = storyDao.insertStory(
                MangaStoryEntity(
                    title = "Shadow Alchemist: Soul Rebirth",
                    japaneseTitle = "影の錬金術師",
                    genre = "Dark Fantasy",
                    artStyle = "Seinen Noir",
                    synopsis = "Betrayed by the Imperial Council and left for dead in the Abyss, Master Alchemist Eric transmutes his own shadow into an autonomous familiar to reclaim the stolen Philosophic Core.",
                    coverImageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80",
                    authorName = "Kaelen Drake",
                    status = "Ongoing",
                    totalChapters = 1,
                    tags = "Alchemy, DarkFantasy, Seinen, Vengeance, Magic"
                )
            )

            chapterDao.insertChapter(
                MangaChapterEntity(
                    storyId = story2Id,
                    chapterNumber = 1,
                    title = "Forbidden Circle of Ash",
                    summary = "Eric draws an alchemical sigil using his own blood under the Blood Moon, awakening the Shadow Homunculus.",
                    contentScript = """
[PANEL 1 - FULL BLEED]: Blood-red full moon over the blackened spire of the Abyss.
ERIC: "Blood pays the debt. Ash seals the covenant."
SFX: *FWOOOOSH!*
[PANEL 2]: The ground ripples like liquid ink. Two glowing crimson eyes open within Eric's own shadow!
SHADOW FAMILIAR: "At your command, Master... who shall we devour first?"
                    """.trimIndent(),
                    audioUrl = "https://actions.google.com/sounds/v1/science_fiction/deep_space_hum.ogg",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    audioDurationSec = 110,
                    videoDurationSec = 60,
                    hasAudio = true,
                    hasVideo = true,
                    status = "Ready"
                )
            )

            // Seed Initial Cached GitHub Repositories
            val gitHubDao = db.gitHubRepoDao()
            gitHubDao.insertAll(
                listOf(
                    CachedGitHubRepoEntity(
                        id = 301L,
                        name = "AI-Youtube-Shorts-Generator",
                        fullName = "Anil-matcha/AI-Youtube-Shorts-Generator",
                        description = "AI YouTube Shorts Generator: Full automated text-to-shorts AI video generator pipeline with voiceover TTS, subtitles, and viral video rendering",
                        htmlUrl = "https://github.com/Anil-matcha/AI-Youtube-Shorts-Generator",
                        language = "Python",
                        stargazersCount = 980,
                        forksCount = 210,
                        openIssuesCount = 5,
                        defaultBranch = "main",
                        updatedAt = "2026-10-25 14:10 UTC",
                        isPrivate = false,
                        licenseName = "MIT License",
                        licenseSpdxId = "MIT",
                        size = 38400,
                        cachedAt = System.currentTimeMillis()
                    ),
                    CachedGitHubRepoEntity(
                        id = 201L,
                        name = "lanshu-create-ai-presenter-video",
                        fullName = "cclank/lanshu-create-ai-presenter-video",
                        description = "Lanshu AI Presenter Video: Automated AI digital human video generator from scripts, PPT and speech with avatar lip-sync",
                        htmlUrl = "https://github.com/cclank/lanshu-create-ai-presenter-video",
                        language = "Python",
                        stargazersCount = 680,
                        forksCount = 138,
                        openIssuesCount = 4,
                        defaultBranch = "main",
                        updatedAt = "2026-10-22 11:20 UTC",
                        isPrivate = false,
                        licenseName = "MIT License",
                        licenseSpdxId = "MIT",
                        size = 28400,
                        cachedAt = System.currentTimeMillis()
                    ),
                    CachedGitHubRepoEntity(
                        id = 101L,
                        name = "creator-studio-content",
                        fullName = "cclank/creator-studio-content",
                        description = "Automated video scripts, thumbnail prompts, and production packages for YouTube & Instagram",
                        htmlUrl = "https://github.com/cclank/creator-studio-content",
                        language = "Kotlin",
                        stargazersCount = 142,
                        forksCount = 28,
                        openIssuesCount = 3,
                        defaultBranch = "main",
                        updatedAt = "2026-10-21 04:30 UTC",
                        isPrivate = false,
                        licenseName = "MIT License",
                        licenseSpdxId = "MIT",
                        size = 14200,
                        cachedAt = System.currentTimeMillis()
                    ),
                    CachedGitHubRepoEntity(
                        id = 102L,
                        name = "ai-video-scheduler",
                        fullName = "cclank/ai-video-scheduler",
                        description = "10-minute automated video generation engine and multi-platform social publishing pipeline",
                        htmlUrl = "https://github.com/cclank/ai-video-scheduler",
                        language = "TypeScript",
                        stargazersCount = 89,
                        forksCount = 19,
                        openIssuesCount = 5,
                        defaultBranch = "main",
                        updatedAt = "2026-10-20 18:15 UTC",
                        isPrivate = false,
                        licenseName = "Apache License 2.0",
                        licenseSpdxId = "Apache-2.0",
                        size = 8900,
                        cachedAt = System.currentTimeMillis()
                    )
                )
            )
        }
    }
}
