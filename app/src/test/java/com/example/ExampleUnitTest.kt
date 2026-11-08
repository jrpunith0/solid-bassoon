package com.example

import com.example.generator.ContentGenerator
import com.example.i18n.I18n
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testContentGeneratorScriptCreation() {
        val script = ContentGenerator.generateScript(
            topic = "YouTube Editing Workflow",
            platform = "YouTube",
            language = I18n.LANG_EN,
            duration = "60s",
            tone = "Viral"
        )
        assertNotNull(script.openingHook)
        assertTrue(script.openingHook.isNotBlank())
        assertTrue(script.mainContent.isNotBlank())
        assertTrue(script.callToAction.isNotBlank())
    }

    @Test
    fun testHashtagGeneration() {
        val hashtags = ContentGenerator.generateHashtags("Creator", "YouTube")
        assertTrue(hashtags.isNotEmpty())
        assertTrue(hashtags.any { it.contains("creator", ignoreCase = true) })
    }

    @Test
    fun testTenMinuteVideoPlanGeneration() {
        val plan = ContentGenerator.generateTenMinuteVideoPlan(
            topic = "The 10 Minute Viral Masterclass",
            style = "Cinematic",
            language = I18n.LANG_EN
        )
        assertNotNull(plan)
        assertEquals(10, plan.scenes.size)
        assertTrue(plan.scenes.first().onScreenText.contains("00:00"))
        assertTrue(plan.scenes.last().onScreenText.contains("09:00"))
        assertTrue(plan.youtubeDescription.contains("TIMESTAMPS"))
    }

    @Test
    fun testGitHubAndVideoEditorTranslations() {
        assertEquals("Video Editor & AI", I18n.t("nav_video_editor", I18n.LANG_EN))
        assertEquals("GitHub Data Link", I18n.t("nav_github_link", I18n.LANG_EN))
        assertEquals("ವೀಡಿಯೊ ಸಂಪಾದಕ & AI", I18n.t("nav_video_editor", I18n.LANG_KN))
        assertEquals("ಗಿಟ್‌ಹಬ್ ಡೇಟಾ ಲಿಂಕ್", I18n.t("nav_github_link", I18n.LANG_KN))
    }

    @Test
    fun testCreatorSettingsDefaultGitHubAndGoogleFields() {
        val settings = com.example.data.CreatorSettingsEntity()
        assertEquals("", settings.youTubeEmail)
        assertEquals("Not Logged In", settings.googleAccountName)
        assertEquals("Anil-matcha", settings.gitHubUsername)
        assertEquals("AI-Youtube-Shorts-Generator", settings.gitHubRepo)
        assertEquals("main", settings.gitHubBranch)
    }

    @Test
    fun testVideoAspectRatioMapping() {
        val shorts = com.example.ui.components.VideoAspectRatio.fromFormatString("9:16 Shorts/Reels")
        assertEquals(com.example.ui.components.VideoAspectRatio.SHORTS_9_16, shorts)
        assertEquals(9f / 16f, shorts.ratio, 0.001f)
        assertEquals("1080 × 1920", shorts.standardResolution)

        val landscape = com.example.ui.components.VideoAspectRatio.fromFormatString("16:9 Long-Form")
        assertEquals(com.example.ui.components.VideoAspectRatio.LANDSCAPE_16_9, landscape)
        assertEquals(16f / 9f, landscape.ratio, 0.001f)
        assertEquals("1920 × 1080", landscape.standardResolution)

        val square = com.example.ui.components.VideoAspectRatio.fromFormatString("1:1 Square")
        assertEquals(com.example.ui.components.VideoAspectRatio.SQUARE_1_1, square)
        assertEquals(1.0f, square.ratio, 0.001f)

        val cinema = com.example.ui.components.VideoAspectRatio.fromFormatString("21:9 Cinema")
        assertEquals(com.example.ui.components.VideoAspectRatio.CINEMA_21_9, cinema)

        val social = com.example.ui.components.VideoAspectRatio.fromFormatString("4:5 Social")
        assertEquals(com.example.ui.components.VideoAspectRatio.PORTRAIT_4_5, social)
    }

    @Test
    fun testVideoAspectRatioFromRatio() {
        assertEquals(com.example.ui.components.VideoAspectRatio.SHORTS_9_16, com.example.ui.components.VideoAspectRatio.fromRatio(9f / 16f))
        assertEquals(com.example.ui.components.VideoAspectRatio.LANDSCAPE_16_9, com.example.ui.components.VideoAspectRatio.fromRatio(16f / 9f))
        assertEquals(com.example.ui.components.VideoAspectRatio.SQUARE_1_1, com.example.ui.components.VideoAspectRatio.fromRatio(1.0f))
    }

    @Test
    fun testGeminiApiServiceEffectiveApiKeyResolution() {
        // User key provided
        val customKey = "AIzaSyCustomKey12345"
        assertEquals(customKey, com.example.generator.GeminiApiService.getEffectiveApiKey(customKey))

        // Whitespace trimmed
        assertEquals(customKey, com.example.generator.GeminiApiService.getEffectiveApiKey("  $customKey  "))

        // Placeholder ignored
        val placeholder = "MY_GEMINI_API_KEY"
        val resolved = com.example.generator.GeminiApiService.getEffectiveApiKey(placeholder)
        assertNotEquals(placeholder, resolved)
    }

    @Test
    fun testStoryToVideoPipelineStructuredStoryEnglish() {
        val script = com.example.generator.StoryToVideoPipeline.generateStructuredStory(
            topic = "Exploring Deep Space",
            genre = com.example.generator.StoryGenre.EDUCATIONAL,
            language = I18n.LANG_EN,
            durationPreset = com.example.generator.StoryDurationPreset.SEC_15,
            format = com.example.generator.StoryVideoFormat.PORTRAIT_9_16
        )

        assertNotNull(script)
        assertTrue(script.title.contains("Deep Space", ignoreCase = true))
        assertEquals(3, script.scenes.size)
        assertEquals(15, script.targetDurationSec)
        assertTrue(script.characterAppearancePrompt.isNotBlank())
        assertTrue(script.introduction.isNotBlank())
        assertTrue(script.closingScene.isNotBlank())

        for (scene in script.scenes) {
            assertTrue(scene.title.isNotBlank())
            assertTrue(scene.voiceover.isNotBlank())
            assertTrue(scene.dialogue.isNotBlank())
            assertTrue(scene.visualPrompt.isNotBlank())
            assertTrue(scene.durationSec > 0)
        }
    }

    @Test
    fun testStoryToVideoPipelineStructuredStoryKannada() {
        val script = com.example.generator.StoryToVideoPipeline.generateStructuredStory(
            topic = "ಬಾಹ್ಯಾಕಾಶ ಯಾತ್ರೆ",
            genre = com.example.generator.StoryGenre.MOTIVATION,
            language = I18n.LANG_KN,
            durationPreset = com.example.generator.StoryDurationPreset.SEC_60,
            format = com.example.generator.StoryVideoFormat.LANDSCAPE_16_9
        )

        assertNotNull(script)
        assertEquals(I18n.LANG_KN, script.language)
        assertEquals(4, script.scenes.size)
        assertEquals(60, script.targetDurationSec)
        assertTrue(script.title.contains("ಬಾಹ್ಯಾಕಾಶ ಯಾತ್ರೆ"))
        assertTrue(script.scenes.first().voiceover.isNotBlank())
        assertTrue(script.scenes.first().dialogue.contains("!"))
    }

    @Test
    fun testFFmpegExportCommandGeneration() {
        val script = com.example.generator.StoryToVideoPipeline.generateStructuredStory(
            topic = "Tech Tutorial",
            genre = com.example.generator.StoryGenre.ACTION,
            language = I18n.LANG_EN,
            durationPreset = com.example.generator.StoryDurationPreset.SEC_30,
            format = com.example.generator.StoryVideoFormat.PORTRAIT_9_16
        )

        val cmd = com.example.generator.StoryToVideoPipeline.getFFmpegExportCommand(script, "final_render.mp4")
        assertTrue(cmd.startsWith("ffmpeg -y"))
        assertTrue(cmd.contains("-i scene_%02d.png"))
        assertTrue(cmd.contains("-i narration.wav"))
        assertTrue(cmd.contains("-c:v libx264"))
        assertTrue(cmd.contains("-t 30"))
        assertTrue(cmd.contains("\"final_render.mp4\""))
    }

    @Test
    fun testTaskStatusDashboardStateTransitions() {
        val stages = listOf(
            com.example.ui.components.PipelineStepId.STORY_GENERATION,
            com.example.ui.components.PipelineStepId.IMAGE_GENERATION,
            com.example.ui.components.PipelineStepId.NARRATION_AUDIO,
            com.example.ui.components.PipelineStepId.FFMPEG_RENDERING
        )
        assertEquals(4, stages.size)
        assertEquals(com.example.ui.components.PipelineStepId.STORY_GENERATION, stages[0])
        assertEquals(com.example.ui.components.PipelineStepId.IMAGE_GENERATION, stages[1])
        assertEquals(com.example.ui.components.PipelineStepId.NARRATION_AUDIO, stages[2])
        assertEquals(com.example.ui.components.PipelineStepId.FFMPEG_RENDERING, stages[3])

        val states = com.example.ui.components.PipelineStepState.values()
        assertTrue(states.contains(com.example.ui.components.PipelineStepState.PENDING))
        assertTrue(states.contains(com.example.ui.components.PipelineStepState.RUNNING))
        assertTrue(states.contains(com.example.ui.components.PipelineStepState.COMPLETED))
        assertTrue(states.contains(com.example.ui.components.PipelineStepState.FAILED))
    }

    @Test
    fun testFFmpegDiagnosticLogLevels() {
        val levels = com.example.ui.components.LogLevel.values()
        assertTrue(levels.contains(com.example.ui.components.LogLevel.ALL))
        assertTrue(levels.contains(com.example.ui.components.LogLevel.INFO))
        assertTrue(levels.contains(com.example.ui.components.LogLevel.WARN))
        assertTrue(levels.contains(com.example.ui.components.LogLevel.ERROR))

        val entry = com.example.ui.components.DiagnosticLogEntry(
            tag = "[RENDER]",
            level = com.example.ui.components.LogLevel.INFO,
            message = "Export completed successfully"
        )
        assertEquals(com.example.ui.components.LogLevel.INFO, entry.level)
        assertEquals("[RENDER]", entry.tag)
        assertEquals("Export completed successfully", entry.message)
    }

    @Test
    fun testStoryAssetStorageServiceFormatBytes() {
        assertEquals("0 B", com.example.generator.StoryAssetStorageService.formatBytes(0L))
        assertEquals("500 B", com.example.generator.StoryAssetStorageService.formatBytes(500L))
        assertEquals("1.0 KB", com.example.generator.StoryAssetStorageService.formatBytes(1024L))
        assertEquals("1.5 MB", com.example.generator.StoryAssetStorageService.formatBytes((1.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun testStoryScriptDataModificationsBeforeImageGeneration() {
        val original = com.example.generator.StoryToVideoPipeline.generateStructuredStory(
            topic = "Origin Story",
            genre = com.example.generator.StoryGenre.MOTIVATION,
            language = I18n.LANG_EN,
            durationPreset = com.example.generator.StoryDurationPreset.SEC_15,
            format = com.example.generator.StoryVideoFormat.PORTRAIT_9_16
        )

        // Modify title, narration, dialogue, visual prompt
        val updatedTitle = "Custom Masterpiece Title"
        val updatedNarration = "This is the newly polished narration written by the user."
        val updatedPrompt = "Hyperrealistic portrait shot, volumetric lighting, 8k"

        val scenesCopy = original.scenes.map { it.copy() }.toMutableList()
        scenesCopy[0].title = "Custom Act 1"
        scenesCopy[0].voiceover = updatedNarration
        scenesCopy[0].visualPrompt = updatedPrompt
        scenesCopy[0].durationSec = 8

        val modifiedScript = original.copy(
            title = updatedTitle,
            characterAppearancePrompt = "Hero with radiant aura",
            scenes = scenesCopy,
            isApproved = true
        )

        assertEquals("Custom Masterpiece Title", modifiedScript.title)
        assertTrue(modifiedScript.isApproved)
        assertEquals("Custom Act 1", modifiedScript.scenes[0].title)
        assertEquals(updatedNarration, modifiedScript.scenes[0].voiceover)
        assertEquals(updatedPrompt, modifiedScript.scenes[0].visualPrompt)
        assertEquals(8, modifiedScript.scenes[0].durationSec)
    }

    @Test
    fun testPromptSuggestionEngineEnglishNarration() {
        val prompt = com.example.generator.PromptSuggestionEngine.suggestImagePrompt(
            narration = "The spaceship blasted past the asteroid belt into deep space.",
            sceneTitle = "Space Odyssey",
            genre = com.example.generator.StoryGenre.EDUCATIONAL,
            characterPrompt = "Astronaut in sleek suit",
            language = I18n.LANG_EN
        )
        assertNotNull(prompt)
        assertTrue(prompt.contains("Astronaut in sleek suit"))
        assertTrue(prompt.contains("8k"))
        assertTrue(prompt.contains("photorealistic"))
        assertTrue(prompt.contains("Wide-angle cosmic panoramic shot"))
    }

    @Test
    fun testPromptSuggestionEngineKannadaNarration() {
        val prompt = com.example.generator.PromptSuggestionEngine.suggestImagePrompt(
            narration = "ಕಠಿಣ ಪರಿಶ್ರಮ ಮತ್ತು ಛಲದಿಂದ ಅಂತಿಮವಾಗಿ ಮಹತ್ತರ ಗೆಲುವು ಸಾಧಿಸಲಾಯಿತು.",
            sceneTitle = "ಅಂತಿಮ ವಿಜಯ",
            genre = com.example.generator.StoryGenre.MOTIVATION,
            characterPrompt = "ಯುವ ಸಾಹಸಿ",
            language = I18n.LANG_KN
        )
        assertNotNull(prompt)
        assertTrue(prompt.contains("Heroic upward-angled golden hour shot"))
        assertTrue(prompt.contains("ಯುವ ಸಾಹಸಿ"))
        assertTrue(prompt.contains("8k"))
    }

    @Test
    fun testPromptSuggestionEngineSuggestAllPrompts() {
        val script = com.example.generator.StoryToVideoPipeline.generateStructuredStory(
            topic = "Cyber Security",
            genre = com.example.generator.StoryGenre.THRILLER,
            language = I18n.LANG_EN,
            durationPreset = com.example.generator.StoryDurationPreset.SEC_15,
            format = com.example.generator.StoryVideoFormat.PORTRAIT_9_16
        )

        val updated = com.example.generator.PromptSuggestionEngine.suggestAllPrompts(script)
        assertEquals(script.scenes.size, updated.scenes.size)
        for (scene in updated.scenes) {
            assertTrue(scene.visualPrompt.isNotBlank())
            assertTrue(scene.visualPrompt.contains("photorealistic 8k render"))
        }
    }

    @Test
    fun testSceneReorderAndRenumberingLogic() {
        val s1 = com.example.generator.StorySceneData(sceneNumber = 1, title = "First", characterDescription = "", dialogue = "", voiceover = "One", visualPrompt = "")
        val s2 = com.example.generator.StorySceneData(sceneNumber = 2, title = "Second", characterDescription = "", dialogue = "", voiceover = "Two", visualPrompt = "")
        val s3 = com.example.generator.StorySceneData(sceneNumber = 3, title = "Third", characterDescription = "", dialogue = "", voiceover = "Three", visualPrompt = "")

        val list = mutableListOf(s1, s2, s3)

        // Move s1 down to position 2
        val moved = list.removeAt(0)
        list.add(1, moved)

        // Re-index
        val reindexed = list.mapIndexed { idx, s -> s.copy(sceneNumber = idx + 1) }

        assertEquals("Second", reindexed[0].title)
        assertEquals(1, reindexed[0].sceneNumber)

        assertEquals("First", reindexed[1].title)
        assertEquals(2, reindexed[1].sceneNumber)

        assertEquals("Third", reindexed[2].title)
        assertEquals(3, reindexed[2].sceneNumber)
    }

    @Test
    fun testAssetStatusEnumValues() {
        val statuses = com.example.generator.AssetStatus.values()
        assertTrue(statuses.contains(com.example.generator.AssetStatus.PENDING))
        assertTrue(statuses.contains(com.example.generator.AssetStatus.GENERATING))
        assertTrue(statuses.contains(com.example.generator.AssetStatus.READY))
        assertTrue(statuses.contains(com.example.generator.AssetStatus.FAILED))
    }

    @Test
    fun testTenVideoCreationCategories() {
        val categories = com.example.generator.VideoCreationCategory.values()
        assertEquals(10, categories.size)

        val ids = categories.map { it.id }
        assertTrue(ids.contains("manga"))
        assertTrue(ids.contains("youtube_long"))
        assertTrue(ids.contains("facebook"))
        assertTrue(ids.contains("instagram"))
        assertTrue(ids.contains("shorts"))
        assertTrue(ids.contains("reels"))
        assertTrue(ids.contains("cinematic"))
        assertTrue(ids.contains("photo_to_video"))
        assertTrue(ids.contains("comic"))
        assertTrue(ids.contains("custom"))

        for (cat in categories) {
            assertTrue(cat.titleEn.isNotBlank())
            assertTrue(cat.titleKn.isNotBlank())
            assertTrue(cat.iconEmoji.isNotBlank())
            assertNotNull(cat.defaultAspect)
        }
    }

    @Test
    fun testSubtitleGeneratorServiceSrtAndVttFormatting() {
        val scenes = listOf(
            com.example.generator.StorySceneData(
                sceneNumber = 1,
                title = "Scene 1",
                voiceover = "First scene narration line in Kannada and English",
                durationSec = 5
            ),
            com.example.generator.StorySceneData(
                sceneNumber = 2,
                title = "Scene 2",
                voiceover = "Second scene narration line with full subtitle timing",
                durationSec = 6
            )
        )

        val entries = com.example.generator.SubtitleGeneratorService.generateSubtitleEntries(scenes)
        assertTrue(entries.isNotEmpty())
        assertEquals(0L, entries[0].startMs)
        assertEquals(5000L, entries[0].endMs)

        val srt = com.example.generator.SubtitleGeneratorService.generateSrtContent(entries)
        assertTrue(srt.contains("00:00:00,000 --> 00:00:05,000"))
        assertTrue(srt.contains("First scene narration line"))

        val vtt = com.example.generator.SubtitleGeneratorService.generateVttContent(entries)
        assertTrue(vtt.startsWith("WEBVTT"))
        assertTrue(vtt.contains("00:00:00.000 --> 00:00:05.000"))
    }

    @Test
    fun testAudioAndSfxEngineLibrary() {
        val bgmTracks = com.example.generator.AudioAndSfxEngine.BGM_LIBRARY
        assertTrue(bgmTracks.size >= 6)
        assertTrue(bgmTracks.any { it.genre == "Cinematic" })
        assertTrue(bgmTracks.any { it.genre == "Emotional" })
        assertTrue(bgmTracks.any { it.genre == "Action" })

        val sfx = com.example.generator.AudioAndSfxEngine.SFX_LIBRARY
        assertTrue(sfx.size >= 6)
        assertTrue(sfx.any { it.id == "whoosh_fast" })
        assertTrue(sfx.any { it.id == "impact_boom" })
    }

    @Test
    fun testAllSeventeenScenePropertiesGenerated() {
        val script = com.example.generator.StoryToVideoPipeline.generateStructuredStory(
            topic = "Cyberpunk Neo Bengaluru 2077",
            genre = com.example.generator.StoryGenre.ACTION,
            language = I18n.LANG_EN,
            durationPreset = com.example.generator.StoryDurationPreset.SEC_15,
            format = com.example.generator.StoryVideoFormat.LANDSCAPE_16_9
        )

        for (scene in script.scenes) {
            assertTrue(scene.sceneNumber > 0)
            assertTrue(scene.title.isNotBlank())
            assertTrue(scene.description.isNotBlank())
            assertTrue(scene.charactersAppearing.isNotBlank())
            assertTrue(scene.locationBackground.isNotBlank())
            assertTrue(scene.characterActions.isNotBlank())
            assertTrue(scene.cameraAngle.isNotBlank())
            assertTrue(scene.cameraMovement.isNotBlank())
            assertTrue(scene.lightingAtmosphere.isNotBlank())
            assertTrue(scene.visualPrompt.isNotBlank())
            assertTrue(scene.negativePrompt.isNotBlank())
            assertTrue(scene.narration.isNotBlank())
            assertTrue(scene.dialogue.isNotBlank())
            assertTrue(scene.subtitle.isNotBlank())
            assertTrue(scene.durationSec > 0)
            assertTrue(scene.bgmSoundEffects.isNotBlank())
            assertEquals(com.example.generator.AssetStatus.PENDING, scene.imageStatus)
            assertEquals(com.example.generator.AssetStatus.PENDING, scene.videoStatus)
        }
    }

    @Test
    fun testGitHubContentItemExtensionAndDirectoryDetection() {
        val dirItem = com.example.data.github.GitHubContentItem(
            name = "src",
            path = "app/src",
            type = "dir"
        )
        assertTrue(dirItem.isDirectory)
        assertEquals("", dirItem.fileExtension)

        val ktFile = com.example.data.github.GitHubContentItem(
            name = "MainActivity.kt",
            path = "app/src/main/java/com/example/MainActivity.kt",
            type = "file"
        )
        assertFalse(ktFile.isDirectory)
        assertEquals("kt", ktFile.fileExtension)

        val xmlFile = com.example.data.github.GitHubContentItem(
            name = "AndroidManifest.xml",
            path = "app/src/main/AndroidManifest.xml",
            type = "file"
        )
        assertFalse(xmlFile.isDirectory)
        assertEquals("xml", xmlFile.fileExtension)

        val mdFile = com.example.data.github.GitHubContentItem(
            name = "README.md",
            path = "README.md",
            type = "file"
        )
        assertFalse(mdFile.isDirectory)
        assertEquals("md", mdFile.fileExtension)
    }

    @Test
    fun testFileExtensionInfoMapping() {
        val dir = com.example.data.github.GitHubContentItem(name = "workflows", type = "dir")
        val dirInfo = com.example.ui.components.getFileExtensionInfo(dir)
        assertEquals("DIR", dirInfo.badgeLabel)

        val kt = com.example.data.github.GitHubContentItem(name = "App.kt", type = "file")
        val ktInfo = com.example.ui.components.getFileExtensionInfo(kt)
        assertEquals("KT", ktInfo.badgeLabel)

        val xml = com.example.data.github.GitHubContentItem(name = "strings.xml", type = "file")
        val xmlInfo = com.example.ui.components.getFileExtensionInfo(xml)
        assertEquals("XML", xmlInfo.badgeLabel)

        val md = com.example.data.github.GitHubContentItem(name = "README.md", type = "file")
        val mdInfo = com.example.ui.components.getFileExtensionInfo(md)
        assertEquals("MD", mdInfo.badgeLabel)

        val yml = com.example.data.github.GitHubContentItem(name = "webpack.yml", type = "file")
        val ymlInfo = com.example.ui.components.getFileExtensionInfo(yml)
        assertEquals("YML", ymlInfo.badgeLabel)

        val py = com.example.data.github.GitHubContentItem(name = "generate_shorts.py", type = "file")
        val pyInfo = com.example.ui.components.getFileExtensionInfo(py)
        assertEquals("PY", pyInfo.badgeLabel)
    }

    @Test
    fun testGitHubRepositoryFetchContentsRootAndNested() {
        kotlinx.coroutines.runBlocking {
            val repoManager = com.example.data.github.GitHubRepository()
            val rootRes = repoManager.fetchRepositoryContents(
                owner = "Anil-matcha",
                repo = "AI-Youtube-Shorts-Generator",
                path = ""
            )
            assertTrue(rootRes.isSuccess)
            val rootItems = rootRes.getOrNull()
            assertNotNull(rootItems)
            assertTrue(rootItems!!.isNotEmpty())

            // Test nested directory contents
            val workflowsRes = repoManager.fetchRepositoryContents(
                owner = "Anil-matcha",
                repo = "AI-Youtube-Shorts-Generator",
                path = ".github/workflows"
            )
            assertTrue(workflowsRes.isSuccess)
            val workflowItems = workflowsRes.getOrNull()
            assertNotNull(workflowItems)
            assertTrue(workflowItems!!.isNotEmpty())
        }
    }

    @Test
    fun testBreadcrumbPathSplittingAndNavigation() {
        val path = "app/src/main/java/com/example"
        val segments = path.split("/").filter { it.isNotBlank() }
        assertEquals(6, segments.size)
        assertEquals("app", segments[0])
        assertEquals("src", segments[1])
        assertEquals("main", segments[2])
        assertEquals("java", segments[3])
        assertEquals("com", segments[4])
        assertEquals("example", segments[5])

        // Jumping back to level 2 ("app/src")
        val level2 = segments.take(2).joinToString("/")
        assertEquals("app/src", level2)

        // Jumping back to level 0 (root)
        val level0 = segments.take(0).joinToString("/")
        assertEquals("", level0)
    }

    @Test
    fun testFileSizeFormattingAndLargeFileCategoryColors() {
        assertEquals("--", com.example.ui.components.formatFileSize(0L))
        assertEquals("512 B", com.example.ui.components.formatFileSize(512L))
        assertEquals("4.2 KB", com.example.ui.components.formatFileSize(4300L))
        assertEquals("2.5 MB", com.example.ui.components.formatFileSize((2.5 * 1024 * 1024).toLong()))

        // Large file (> 1 MB) alert color test
        val (largeText, largeBg) = com.example.ui.components.getFileSizeCategoryColor(2 * 1024 * 1024L)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFDC2626), largeText)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFEE2E2), largeBg)

        // Medium-large file (> 100 KB) alert color test
        val (medText, medBg) = com.example.ui.components.getFileSizeCategoryColor(250 * 1024L)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFD97706), medText)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFEF3C7), medBg)

        // Small file (< 10 KB) normal color test
        val (smallText, smallBg) = com.example.ui.components.getFileSizeCategoryColor(1024L)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF475569), smallText)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFF1F5F9), smallBg)
    }

    @Test
    fun testRecentFilesListCappingAtFiveAndDeduplication() {
        val f1 = com.example.data.github.GitHubContentItem(name = "File1.kt", path = "File1.kt", size = 100L)
        val f2 = com.example.data.github.GitHubContentItem(name = "File2.kt", path = "File2.kt", size = 200L)
        val f3 = com.example.data.github.GitHubContentItem(name = "File3.kt", path = "File3.kt", size = 300L)
        val f4 = com.example.data.github.GitHubContentItem(name = "File4.kt", path = "File4.kt", size = 400L)
        val f5 = com.example.data.github.GitHubContentItem(name = "File5.kt", path = "File5.kt", size = 500L)
        val f6 = com.example.data.github.GitHubContentItem(name = "File6.kt", path = "File6.kt", size = 600L)

        var recents = listOf(f1, f2, f3, f4, f5)
        assertEquals(5, recents.size)

        // Access f6 (new file): should be prepended and capped at 5
        recents = (listOf(f6) + recents.filter { it.path != f6.path }).take(5)
        assertEquals(5, recents.size)
        assertEquals("File6.kt", recents.first().name)
        assertFalse(recents.any { it.name == "File5.kt" }) // Oldest dropped

        // Access f2 (existing file): should move to first without duplicating
        recents = (listOf(f2) + recents.filter { it.path != f2.path }).take(5)
        assertEquals(5, recents.size)
        assertEquals("File2.kt", recents.first().name)
        assertEquals(1, recents.count { it.name == "File2.kt" })
    }

    @Test
    fun testIsTextBasedFilePreviewEligibility() {
        val ktFile = com.example.data.github.GitHubContentItem(name = "MainActivity.kt", path = "app/MainActivity.kt", type = "file")
        val xmlFile = com.example.data.github.GitHubContentItem(name = "AndroidManifest.xml", path = "app/AndroidManifest.xml", type = "file")
        val mdFile = com.example.data.github.GitHubContentItem(name = "README.md", path = "README.md", type = "file")
        val ymlFile = com.example.data.github.GitHubContentItem(name = "webpack.yml", path = ".github/workflows/webpack.yml", type = "file")
        val jsonFile = com.example.data.github.GitHubContentItem(name = "package.json", path = "package.json", type = "file")
        val pyFile = com.example.data.github.GitHubContentItem(name = "generate_shorts.py", path = "scripts/generate_shorts.py", type = "file")
        val tomlFile = com.example.data.github.GitHubContentItem(name = "libs.versions.toml", path = "gradle/libs.versions.toml", type = "file")
        val licenseFile = com.example.data.github.GitHubContentItem(name = "LICENSE", path = "LICENSE", type = "file")
        
        // Binary and directory items
        val dirItem = com.example.data.github.GitHubContentItem(name = "src", path = "src", type = "dir")
        val pngFile = com.example.data.github.GitHubContentItem(name = "banner.png", path = "assets/banner.png", type = "file")
        val mp4File = com.example.data.github.GitHubContentItem(name = "intro.mp4", path = "videos/intro.mp4", type = "file")
        val zipFile = com.example.data.github.GitHubContentItem(name = "archive.zip", path = "downloads/archive.zip", type = "file")

        // Should return true for text files
        assertTrue(com.example.ui.components.isTextBasedFile(ktFile))
        assertTrue(com.example.ui.components.isTextBasedFile(xmlFile))
        assertTrue(com.example.ui.components.isTextBasedFile(mdFile))
        assertTrue(com.example.ui.components.isTextBasedFile(ymlFile))
        assertTrue(com.example.ui.components.isTextBasedFile(jsonFile))
        assertTrue(com.example.ui.components.isTextBasedFile(pyFile))
        assertTrue(com.example.ui.components.isTextBasedFile(tomlFile))
        assertTrue(com.example.ui.components.isTextBasedFile(licenseFile))

        // Should return false for directory and binaries
        assertFalse(com.example.ui.components.isTextBasedFile(dirItem))
        assertFalse(com.example.ui.components.isTextBasedFile(pngFile))
        assertFalse(com.example.ui.components.isTextBasedFile(mp4File))
        assertFalse(com.example.ui.components.isTextBasedFile(zipFile))
    }

    @Test
    fun testGetFileContentPreviewContentGeneration() {
        val yml = com.example.data.github.GitHubContentItem(name = "webpack.yml", path = ".github/workflows/webpack.yml", type = "file")
        val ymlPreview = com.example.ui.components.getFileContentPreview(yml)
        assertTrue(ymlPreview.contains("NodeJS with Webpack"))
        assertTrue(ymlPreview.contains("npx webpack"))

        val readme = com.example.data.github.GitHubContentItem(name = "README.md", path = "README.md", type = "file")
        val readmePreview = com.example.ui.components.getFileContentPreview(readme)
        assertTrue(readmePreview.contains("Creator Studio Pro"))

        val kt = com.example.data.github.GitHubContentItem(name = "MainActivity.kt", path = "app/src/main/java/com/example/MainActivity.kt", type = "file")
        val ktPreview = com.example.ui.components.getFileContentPreview(kt)
        assertTrue(ktPreview.contains("CreatorStudioTheme"))

        val py = com.example.data.github.GitHubContentItem(name = "generate_shorts.py", path = "scripts/generate_shorts.py", type = "file")
        val pyPreview = com.example.ui.components.getFileContentPreview(py)
        assertTrue(pyPreview.contains("generate_video_shorts"))

        val generic = com.example.data.github.GitHubContentItem(name = "custom_script.sh", path = "custom_script.sh", type = "file")
        val genericPreview = com.example.ui.components.getFileContentPreview(generic)
        assertTrue(genericPreview.isNotBlank())
        assertTrue(genericPreview.contains("custom_script.sh"))
    }

    @Test
    fun testMultiSelectAndBulkOperationsLogic() {
        val f1 = com.example.data.github.GitHubContentItem(name = "file1.kt", path = "src/file1.kt")
        val f2 = com.example.data.github.GitHubContentItem(name = "file2.kt", path = "src/file2.kt")
        val f3 = com.example.data.github.GitHubContentItem(name = "file3.kt", path = "src/file3.kt")
        val files = listOf(f1, f2, f3)

        var selectedPaths = setOf<String>()

        // 1. Toggle single item
        selectedPaths = if (selectedPaths.contains(f1.path)) selectedPaths - f1.path else selectedPaths + f1.path
        assertTrue(selectedPaths.contains("src/file1.kt"))
        assertEquals(1, selectedPaths.size)

        // 2. Select All
        selectedPaths = files.map { it.path }.toSet()
        assertEquals(3, selectedPaths.size)
        assertTrue(selectedPaths.containsAll(listOf("src/file1.kt", "src/file2.kt", "src/file3.kt")))

        // 3. Deselect All
        selectedPaths = emptySet()
        assertTrue(selectedPaths.isEmpty())

        // 4. Bulk Delete operation
        selectedPaths = setOf("src/file1.kt", "src/file3.kt")
        val remainingFiles = files.filterNot { selectedPaths.contains(it.path) }
        assertEquals(1, remainingFiles.size)
        assertEquals("file2.kt", remainingFiles.first().name)

        // 5. Bulk Move operation
        val targetFolder = "archive"
        val movedFiles = files.map { item ->
            if (selectedPaths.contains(item.path)) {
                item.copy(path = "$targetFolder/${item.name}")
            } else {
                item
            }
        }
        assertEquals("archive/file1.kt", movedFiles[0].path)
        assertEquals("src/file2.kt", movedFiles[1].path)
        assertEquals("archive/file3.kt", movedFiles[2].path)
    }

    @Test
    fun testWelcomeToCreatorStudioPopupOmissionByDefault() {
        val defaultSettings = com.example.data.CreatorSettingsEntity()
        // wizardCompleted must be true so the welcome dialog shown in the screenshot is skipped on launch
        assertTrue(defaultSettings.wizardCompleted)
    }

    @Test
    fun testAiProviderManagerPromptOptimizationForAllProviders() {
        val params = com.example.ai.PromptBuilderParams(
            storyOrTopic = "Mysterious temple ruins",
            mainCharacter = "Arjun",
            characterAppearance = "Indian youth with saffron jacket",
            location = "Heritage village at sunset",
            sceneDescription = "Approaches ancient carved stone pillars",
            moodAndEmotion = "Suspenseful & Epic",
            artStyle = "Cinematic 3D Animation",
            cameraAngle = "Wide establishing drone shot",
            cameraMovement = "Slow tracking push-in",
            lighting = "Warm sunset rim light with volumetric mist",
            language = "kn"
        )

        // 1. OpenAI
        val openAiPkg = com.example.ai.AiProviderManager.buildOptimizedPrompt(com.example.ai.AiProvider.OPENAI, params)
        assertEquals(com.example.ai.AiProvider.OPENAI, openAiPkg.provider)
        assertTrue(openAiPkg.mainPrompt.contains("Mysterious temple ruins"))
        assertTrue(openAiPkg.mainPrompt.contains("Arjun"))
        assertTrue(openAiPkg.negativePrompt.isNotBlank())
        assertEquals(4, openAiPkg.sceneBreakdown.size)

        // 2. Gemini
        val geminiPkg = com.example.ai.AiProviderManager.buildOptimizedPrompt(com.example.ai.AiProvider.GEMINI, params)
        assertEquals(com.example.ai.AiProvider.GEMINI, geminiPkg.provider)
        assertTrue(geminiPkg.mainPrompt.contains("Google Veo"))
        assertTrue(geminiPkg.technicalSettings.containsKey("Model"))
        assertEquals("veo-2.0", geminiPkg.technicalSettings["Model"])

        // 3. Runway
        val runwayPkg = com.example.ai.AiProviderManager.buildOptimizedPrompt(com.example.ai.AiProvider.RUNWAY, params)
        assertEquals(com.example.ai.AiProvider.RUNWAY, runwayPkg.provider)
        assertTrue(runwayPkg.mainPrompt.contains("Cinematic 3D Animation"))
        assertTrue(runwayPkg.cameraMovement.contains("Motion Brush"))

        // 4. Kling AI
        val klingPkg = com.example.ai.AiProviderManager.buildOptimizedPrompt(com.example.ai.AiProvider.KLING, params)
        assertEquals(com.example.ai.AiProvider.KLING, klingPkg.provider)
        assertTrue(klingPkg.characterConsistency.contains("Kling Character ID"))

        // 5. ElevenLabs (Kannada)
        val elevenPkg = com.example.ai.AiProviderManager.buildOptimizedPrompt(com.example.ai.AiProvider.ELEVEN_LABS, params)
        assertEquals(com.example.ai.AiProvider.ELEVEN_LABS, elevenPkg.provider)
        assertTrue(elevenPkg.mainPrompt.contains("ಕನ್ನಡ ಸಂಭಾಷಣೆ") || elevenPkg.mainPrompt.contains("Kannada Voiceover"))
    }

    @Test
    fun testMultiProviderSideBySideComparisonSet() {
        val params = com.example.ai.PromptBuilderParams(storyOrTopic = "AI Revolution")
        val comparison = com.example.ai.AiProviderManager.generateMultiProviderComparison(params)
        assertEquals(5, comparison.size)
        assertTrue(comparison.containsKey(com.example.ai.AiProvider.OPENAI))
        assertTrue(comparison.containsKey(com.example.ai.AiProvider.GEMINI))
        assertTrue(comparison.containsKey(com.example.ai.AiProvider.RUNWAY))
        assertTrue(comparison.containsKey(com.example.ai.AiProvider.KLING))
        assertTrue(comparison.containsKey(com.example.ai.AiProvider.ELEVEN_LABS))
    }

    @Test
    fun testMobilePhoneAuthAndCountryCodeDefaults() {
        val settings = com.example.data.CreatorSettingsEntity(
            userPhone = "9876543210",
            phoneCountryCode = "+91",
            isPhoneVerified = true
        )
        assertEquals("+91", settings.phoneCountryCode)
        assertEquals("9876543210", settings.userPhone)
        assertTrue(settings.isPhoneVerified)
    }

    @Test
    fun testCartoonCharacterAndPublishLogEntities() {
        val character = com.example.data.CartoonCharacterEntity(
            id = 1,
            name = "Arjun",
            role = "Protagonist",
            visualStyle = "3D Pixar Cartoon",
            isLocked = true
        )
        assertEquals("Arjun", character.name)
        assertTrue(character.isLocked)

        val log = com.example.data.SocialPublishLogEntity(
            id = 1,
            platform = "YouTube",
            title = "Sample Video Shorts",
            publishedUrl = "https://youtube.com/shorts/sample123",
            status = "Published"
        )
        assertEquals("YouTube", log.platform)
        assertEquals("Published", log.status)
        assertTrue(log.publishedUrl.startsWith("https://youtube.com"))
    }

    @Test
    fun testCrossPlatformHubNavAndTranslations() {
        assertEquals("Cross-Platform Hub", I18n.t("nav_cross_platform_hub", I18n.LANG_EN))
        assertEquals("ಕ್ರಾಸ್-ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ಕೇಂದ್ರ", I18n.t("nav_cross_platform_hub", I18n.LANG_KN))

        val hasCrossPlatformNav = com.example.ui.components.AllNavItems.any { it.id == "cross_platform_hub" }
        assertTrue("AllNavItems must contain cross_platform_hub", hasCrossPlatformNav)
    }

    @Test
    fun testCrossPlatformArchitectureFiles() {
        val webIndex = java.io.File("../web/index.html")
        val webCss = java.io.File("../web/style.css")
        val webJs = java.io.File("../web/app.js")
        val iosPlist = java.io.File("../ios/App/App/Info.plist")
        val iosPodfile = java.io.File("../ios/Podfile")
        val syncRoutes = java.io.File("../backend/routes/syncRoutes.js")

        assertTrue("Web index.html must exist", webIndex.exists())
        assertTrue("Web style.css must exist", webCss.exists())
        assertTrue("Web app.js must exist", webJs.exists())
        assertTrue("iOS Info.plist must exist", iosPlist.exists())
        assertTrue("iOS Podfile must exist", iosPodfile.exists())
        assertTrue("Backend syncRoutes must exist", syncRoutes.exists())
    }

    @Test
    fun testWebAndMobileDualUsability() {
        val publicIndex = java.io.File("../public/index.html")
        val publicJs = java.io.File("../public/app.js")
        val publicCss = java.io.File("../public/style.css")

        assertTrue("Public index.html must exist for web access", publicIndex.exists())
        assertTrue("Public app.js must exist for web access", publicJs.exists())
        assertTrue("Public style.css must exist for web access", publicCss.exists())

        // Verify Kannada translation exists for web studio
        val knTitle = I18n.t("nav_cross_platform_hub", I18n.LANG_KN)
        assertTrue(knTitle.isNotBlank())
    }
}




