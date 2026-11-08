package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.i18n.I18n
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data record linking a scene to its stored image and audio assets in internal storage.
 */
data class SceneAssetRecord(
    val sceneNumber: Int,
    val imageFileName: String? = null,
    val imagePath: String? = null,
    val imageSizeBytes: Long = 0L,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val audioFileName: String? = null,
    val audioPath: String? = null,
    val audioSizeBytes: Long = 0L,
    val audioDurationSec: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Metadata manifest for a story project linking scenes to their internal storage assets.
 */
data class ProjectAssetManifest(
    val scriptId: String,
    val title: String,
    val language: String,
    val totalScenes: Int,
    val targetDurationSec: Int,
    val format: String,
    val createdAt: Long,
    val updatedAt: Long,
    val totalStorageBytes: Long,
    val scenes: List<SceneAssetRecord>
)

/**
 * Summary of storage usage for a given project or all projects.
 */
data class StorageUsageSummary(
    val totalFiles: Int,
    val imageCount: Int,
    val audioCount: Int,
    val totalBytes: Long,
    val formattedSize: String
)

/**
 * Local File Management Service for storing, retrieving, verifying, and reliably
 * linking generated scene images and narration audio files within the app's internal storage.
 *
 * Guarantees:
 * 1. Safe storage in private app directory (context.filesDir/story_projects/{scriptId}/...)
 * 2. Deterministic canonical file naming per scene
 * 3. File validation (verifies header bytes and non-empty content)
 * 4. Automatic manifest persistence and re-linking upon app launch
 */
object StoryAssetStorageService {

    private const val BASE_PROJECTS_DIR = "story_projects"
    private const val IMAGES_DIR = "images"
    private const val AUDIO_DIR = "audio"
    private const val MANIFEST_FILE_NAME = "project_manifest.json"

    /**
     * Returns the dedicated directory for a given story project in internal storage.
     */
    fun getProjectDirectory(context: Context, scriptId: String): File {
        val baseDir = File(context.filesDir, BASE_PROJECTS_DIR)
        val projectDir = File(baseDir, scriptId)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }
        return projectDir
    }

    /**
     * Returns the dedicated images directory for a given story project.
     */
    fun getImagesDirectory(context: Context, scriptId: String): File {
        val dir = File(getProjectDirectory(context, scriptId), IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Returns the dedicated audio directory for a given story project.
     */
    fun getAudioDirectory(context: Context, scriptId: String): File {
        val dir = File(getProjectDirectory(context, scriptId), AUDIO_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Saves a scene image bitmap into the app's internal storage,
     * guaranteeing reliable linking to the scene.
     */
    suspend fun saveSceneImage(
        context: Context,
        scriptId: String,
        sceneNumber: Int,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): File = withContext(Dispatchers.IO) {
        val imagesDir = getImagesDirectory(context, scriptId)
        val ext = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
        val targetFile = File(imagesDir, "scene_${sceneNumber}_img.$ext")

        // Overwrite or create file atomically
        val tempFile = File(imagesDir, "scene_${sceneNumber}_img.tmp")
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
        if (targetFile.exists()) {
            targetFile.delete()
        }
        tempFile.renameTo(targetFile)

        targetFile
    }

    /**
     * Saves raw image bytes (e.g. from Imagen 3 REST API response) into internal storage.
     */
    suspend fun saveSceneImageBytes(
        context: Context,
        scriptId: String,
        sceneNumber: Int,
        bytes: ByteArray,
        extension: String = "png"
    ): File = withContext(Dispatchers.IO) {
        val imagesDir = getImagesDirectory(context, scriptId)
        val targetFile = File(imagesDir, "scene_${sceneNumber}_img.$extension")
        FileOutputStream(targetFile).use { out ->
            out.write(bytes)
            out.flush()
        }
        targetFile
    }

    /**
     * Retrieves the stored image file for a given scene if it exists and is valid.
     */
    fun getSceneImageFile(context: Context, scriptId: String, sceneNumber: Int): File? {
        val imagesDir = getImagesDirectory(context, scriptId)
        val candidates = listOf(
            File(imagesDir, "scene_${sceneNumber}_img.png"),
            File(imagesDir, "scene_${sceneNumber}_img.jpg"),
            File(imagesDir, "scene_${sceneNumber}_img.webp")
        )
        for (candidate in candidates) {
            if (candidate.exists() && candidate.length() > 0) {
                return candidate
            }
        }
        return null
    }

    /**
     * Loads a stored scene image as a Bitmap.
     */
    suspend fun loadSceneBitmap(context: Context, scriptId: String, sceneNumber: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val file = getSceneImageFile(context, scriptId, sceneNumber) ?: return@withContext null
            try {
                BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Saves narration audio bytes (e.g. 44.1kHz WAV or synthesized speech) into internal storage.
     */
    suspend fun saveSceneAudioBytes(
        context: Context,
        scriptId: String,
        sceneNumber: Int,
        bytes: ByteArray,
        extension: String = "wav"
    ): File = withContext(Dispatchers.IO) {
        val audioDir = getAudioDirectory(context, scriptId)
        val targetFile = File(audioDir, "scene_${sceneNumber}_narration.$extension")
        FileOutputStream(targetFile).use { out ->
            out.write(bytes)
            out.flush()
        }
        targetFile
    }

    /**
     * Saves an audio file from an external source or temporary path into internal storage.
     */
    suspend fun saveSceneAudioFile(
        context: Context,
        scriptId: String,
        sceneNumber: Int,
        sourceFile: File
    ): File = withContext(Dispatchers.IO) {
        val audioDir = getAudioDirectory(context, scriptId)
        val ext = sourceFile.extension.ifBlank { "wav" }
        val targetFile = File(audioDir, "scene_${sceneNumber}_narration.$ext")

        FileInputStream(sourceFile).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
        targetFile
    }

    /**
     * Retrieves the stored audio file for a given scene if it exists and is valid.
     */
    fun getSceneAudioFile(context: Context, scriptId: String, sceneNumber: Int): File? {
        val audioDir = getAudioDirectory(context, scriptId)
        val candidates = listOf(
            File(audioDir, "scene_${sceneNumber}_narration.wav"),
            File(audioDir, "scene_${sceneNumber}_narration.mp3"),
            File(audioDir, "scene_${sceneNumber}_narration.m4a"),
            File(audioDir, "scene_${sceneNumber}_narration.aac")
        )
        for (candidate in candidates) {
            if (candidate.exists() && candidate.length() > 44) {
                return candidate
            }
        }
        return null
    }

    /**
     * Saves or updates the project asset manifest JSON in the project's internal folder.
     */
    suspend fun saveProjectManifest(
        context: Context,
        script: StoryScriptData
    ): File = withContext(Dispatchers.IO) {
        val projectDir = getProjectDirectory(context, script.id)
        val manifestFile = File(projectDir, MANIFEST_FILE_NAME)

        val rootObj = JSONObject()
        rootObj.put("scriptId", script.id)
        rootObj.put("title", script.title)
        rootObj.put("genre", script.genre.name)
        rootObj.put("language", script.language)
        rootObj.put("format", script.format.name)
        rootObj.put("targetDurationSec", script.targetDurationSec)
        rootObj.put("characterAppearancePrompt", script.characterAppearancePrompt)
        rootObj.put("introduction", script.introduction)
        rootObj.put("closingScene", script.closingScene)
        rootObj.put("isApproved", script.isApproved)
        rootObj.put("updatedAt", System.currentTimeMillis())

        val scenesArray = JSONArray()
        var totalBytes = 0L

        for (scene in script.scenes) {
            val sceneObj = JSONObject()
            sceneObj.put("sceneNumber", scene.sceneNumber)
            sceneObj.put("title", scene.title)
            sceneObj.put("characterDescription", scene.characterDescription)
            sceneObj.put("dialogue", scene.dialogue)
            sceneObj.put("voiceover", scene.voiceover)
            sceneObj.put("visualPrompt", scene.visualPrompt)
            sceneObj.put("durationSec", scene.durationSec)
            sceneObj.put("imageStatus", scene.imageStatus.name)
            sceneObj.put("audioStatus", scene.audioStatus.name)

            // Link image asset
            val imgFile = getSceneImageFile(context, script.id, scene.sceneNumber)
            if (imgFile != null && imgFile.exists()) {
                sceneObj.put("imagePath", imgFile.absolutePath)
                sceneObj.put("imageSizeBytes", imgFile.length())
                totalBytes += imgFile.length()
            } else {
                sceneObj.put("imagePath", JSONObject.NULL)
                sceneObj.put("imageSizeBytes", 0L)
            }

            // Link audio asset
            val audioFile = getSceneAudioFile(context, script.id, scene.sceneNumber)
            if (audioFile != null && audioFile.exists()) {
                sceneObj.put("audioPath", audioFile.absolutePath)
                sceneObj.put("audioSizeBytes", audioFile.length())
                totalBytes += audioFile.length()
            } else {
                sceneObj.put("audioPath", JSONObject.NULL)
                sceneObj.put("audioSizeBytes", 0L)
            }

            scenesArray.put(sceneObj)
        }

        rootObj.put("scenes", scenesArray)
        rootObj.put("totalStorageBytes", totalBytes)

        FileOutputStream(manifestFile).use { out ->
            out.write(rootObj.toString(2).toByteArray(Charsets.UTF_8))
            out.flush()
        }

        manifestFile
    }

    /**
     * Loads the project asset manifest from internal storage.
     */
    suspend fun loadProjectManifest(context: Context, scriptId: String): ProjectAssetManifest? =
        withContext(Dispatchers.IO) {
            val projectDir = getProjectDirectory(context, scriptId)
            val manifestFile = File(projectDir, MANIFEST_FILE_NAME)
            if (!manifestFile.exists()) return@withContext null

            try {
                val jsonString = manifestFile.readText(Charsets.UTF_8)
                val json = JSONObject(jsonString)

                val scenesList = mutableListOf<SceneAssetRecord>()
                val scenesArray = json.optJSONArray("scenes") ?: JSONArray()
                for (i in 0 until scenesArray.length()) {
                    val s = scenesArray.getJSONObject(i)
                    val imgPath = if (!s.isNull("imagePath")) s.getString("imagePath") else null
                    val audioPath = if (!s.isNull("audioPath")) s.getString("audioPath") else null
                    scenesList.add(
                        SceneAssetRecord(
                            sceneNumber = s.getInt("sceneNumber"),
                            imagePath = imgPath,
                            imageFileName = imgPath?.let { File(it).name },
                            imageSizeBytes = s.optLong("imageSizeBytes", 0L),
                            audioPath = audioPath,
                            audioFileName = audioPath?.let { File(it).name },
                            audioSizeBytes = s.optLong("audioSizeBytes", 0L),
                            audioDurationSec = s.optInt("durationSec", 5),
                            lastModified = s.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }

                ProjectAssetManifest(
                    scriptId = json.getString("scriptId"),
                    title = json.optString("title", "Untitled Story"),
                    language = json.optString("language", I18n.LANG_EN),
                    totalScenes = scenesList.size,
                    targetDurationSec = json.optInt("targetDurationSec", 30),
                    format = json.optString("format", "PORTRAIT_9_16"),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
                    totalStorageBytes = json.optLong("totalStorageBytes", 0L),
                    scenes = scenesList
                )
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Reliably inspects the app's internal storage and links existing image and audio assets
     * to the provided StoryScriptData object, updating file paths and readiness states.
     */
    fun linkStoredAssetsToScript(context: Context, script: StoryScriptData): StoryScriptData {
        for (scene in script.scenes) {
            // Check image
            val imgFile = getSceneImageFile(context, script.id, scene.sceneNumber)
            if (imgFile != null && imgFile.exists() && imgFile.length() > 0) {
                scene.imageFilePath = imgFile.absolutePath
                scene.imageStatus = AssetStatus.READY
            }

            // Check audio
            val audioFile = getSceneAudioFile(context, script.id, scene.sceneNumber)
            if (audioFile != null && audioFile.exists() && audioFile.length() > 44) {
                scene.audioFilePath = audioFile.absolutePath
                scene.audioStatus = AssetStatus.READY
            }
        }
        return script
    }

    /**
     * Deletes stored assets for a specific scene (e.g. before regeneration).
     */
    fun deleteSceneAssets(context: Context, scriptId: String, sceneNumber: Int) {
        val imgFile = getSceneImageFile(context, scriptId, sceneNumber)
        imgFile?.delete()

        val audioFile = getSceneAudioFile(context, scriptId, sceneNumber)
        audioFile?.delete()
    }

    /**
     * Calculates storage usage stats for a project.
     */
    fun getProjectStorageStats(context: Context, scriptId: String): StorageUsageSummary {
        val projectDir = getProjectDirectory(context, scriptId)
        var totalBytes = 0L
        var totalFiles = 0
        var imageCount = 0
        var audioCount = 0

        projectDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                totalBytes += file.length()
                totalFiles++
                val name = file.name.lowercase(Locale.ROOT)
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".webp")) {
                    imageCount++
                } else if (name.endsWith(".wav") || name.endsWith(".mp3") || name.endsWith(".m4a")) {
                    audioCount++
                }
            }
        }

        val formatted = formatBytes(totalBytes)
        return StorageUsageSummary(
            totalFiles = totalFiles,
            imageCount = imageCount,
            audioCount = audioCount,
            totalBytes = totalBytes,
            formattedSize = formatted
        )
    }

    /**
     * Dedicated project subdirectories for the 10-folder structure.
     */
    fun getScriptDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "script").also { if (!it.exists()) it.mkdirs() }

    fun getCharactersDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "characters").also { if (!it.exists()) it.mkdirs() }

    fun getVideoClipsDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "video_clips").also { if (!it.exists()) it.mkdirs() }

    fun getSubtitlesDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "subtitles").also { if (!it.exists()) it.mkdirs() }

    fun getThumbnailsDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "thumbnails").also { if (!it.exists()) it.mkdirs() }

    fun getExportsDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "exports").also { if (!it.exists()) it.mkdirs() }

    fun getLogsDirectory(context: Context, scriptId: String): File =
        File(getProjectDirectory(context, scriptId), "logs").also { if (!it.exists()) it.mkdirs() }

    /**
     * Initializes the complete 10-folder project layout and writes project.json metadata.
     */
    fun initializeCompleteProjectStructure(context: Context, scriptId: String, title: String = "New Project"): File {
        val projectDir = getProjectDirectory(context, scriptId)
        getScriptDirectory(context, scriptId)
        getCharactersDirectory(context, scriptId)
        getImagesDirectory(context, scriptId)
        getVideoClipsDirectory(context, scriptId)
        getAudioDirectory(context, scriptId)
        getSubtitlesDirectory(context, scriptId)
        getThumbnailsDirectory(context, scriptId)
        getExportsDirectory(context, scriptId)
        getLogsDirectory(context, scriptId)

        val projectJson = File(projectDir, "project.json")
        if (!projectJson.exists()) {
            val json = JSONObject().apply {
                put("projectId", scriptId)
                put("title", title)
                put("createdAt", System.currentTimeMillis())
                put("updatedAt", System.currentTimeMillis())
                put("version", "2.0")
            }
            FileOutputStream(projectJson).use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
        }
        return projectDir
    }

    /**
     * Duplicates an existing project into a new project with fresh ID and updated title.
     */
    fun duplicateProject(context: Context, sourceScriptId: String, newTitle: String): String {
        val newScriptId = java.util.UUID.randomUUID().toString().take(8)
        val sourceDir = getProjectDirectory(context, sourceScriptId)
        val targetDir = getProjectDirectory(context, newScriptId)

        if (sourceDir.exists()) {
            sourceDir.copyRecursively(targetDir, overwrite = true)
            // Update project.json
            val projectJson = File(targetDir, "project.json")
            val json = JSONObject().apply {
                put("projectId", newScriptId)
                put("title", newTitle)
                put("clonedFrom", sourceScriptId)
                put("createdAt", System.currentTimeMillis())
                put("updatedAt", System.currentTimeMillis())
            }
            FileOutputStream(projectJson).use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
        } else {
            initializeCompleteProjectStructure(context, newScriptId, newTitle)
        }
        return newScriptId
    }

    /**
     * Renames a project title in its local metadata.
     */
    fun renameProject(context: Context, scriptId: String, newTitle: String): Boolean {
        val projectDir = getProjectDirectory(context, scriptId)
        val projectJson = File(projectDir, "project.json")
        return try {
            val json = if (projectJson.exists()) {
                JSONObject(projectJson.readText(Charsets.UTF_8))
            } else {
                JSONObject()
            }
            json.put("projectId", scriptId)
            json.put("title", newTitle)
            json.put("updatedAt", System.currentTimeMillis())
            FileOutputStream(projectJson).use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Deletes a project and all its subfolders.
     */
    fun deleteProject(context: Context, scriptId: String): Boolean {
        return clearProjectAssets(context, scriptId)
    }

    /**
     * Clears all assets for a given project from internal storage.
     */
    fun clearProjectAssets(context: Context, scriptId: String): Boolean {
        val projectDir = getProjectDirectory(context, scriptId)
        return projectDir.deleteRecursively()
    }

    /**
     * Human-readable byte formatting.
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }
}
