package com.example.generator

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class SubtitleEntry(
    val index: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val speaker: String? = null
)

object SubtitleGeneratorService {

    /**
     * Converts milliseconds to standard SRT timestamp: HH:mm:ss,SSS
     */
    fun formatSrtTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    /**
     * Converts milliseconds to standard WebVTT timestamp: HH:mm:ss.SSS
     */
    fun formatVttTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }

    /**
     * Builds a list of SubtitleEntry from story scenes based on scene duration.
     */
    fun generateSubtitleEntries(scenes: List<StorySceneData>): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        var currentMs = 0L

        scenes.forEachIndexed { i, scene ->
            val sceneDurationMs = scene.durationSec.coerceAtLeast(3) * 1000L
            val subtitleText = scene.subtitle.ifBlank { scene.voiceover }

            // If subtitle text is long, split into 2 segments for readability
            if (subtitleText.length > 70) {
                val midPoint = subtitleText.lastIndexOf(' ', subtitleText.length / 2).takeIf { it > 0 } ?: (subtitleText.length / 2)
                val part1 = subtitleText.substring(0, midPoint).trim()
                val part2 = subtitleText.substring(midPoint).trim()
                val halfDuration = sceneDurationMs / 2

                entries.add(
                    SubtitleEntry(
                        index = entries.size + 1,
                        startMs = currentMs,
                        endMs = currentMs + halfDuration,
                        text = part1,
                        speaker = scene.charactersAppearing.takeIf { it.isNotBlank() }
                    )
                )
                entries.add(
                    SubtitleEntry(
                        index = entries.size + 1,
                        startMs = currentMs + halfDuration,
                        endMs = currentMs + sceneDurationMs,
                        text = part2,
                        speaker = scene.charactersAppearing.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                entries.add(
                    SubtitleEntry(
                        index = entries.size + 1,
                        startMs = currentMs,
                        endMs = currentMs + sceneDurationMs,
                        text = subtitleText,
                        speaker = scene.charactersAppearing.takeIf { it.isNotBlank() }
                    )
                )
            }

            currentMs += sceneDurationMs
        }

        return entries
    }

    /**
     * Generates a valid SubRip (.srt) subtitle string.
     */
    fun generateSrtContent(entries: List<SubtitleEntry>): String {
        val sb = StringBuilder()
        entries.forEach { entry ->
            sb.append("${entry.index}\n")
            sb.append("${formatSrtTime(entry.startMs)} --> ${formatSrtTime(entry.endMs)}\n")
            sb.append("${entry.text}\n\n")
        }
        return sb.toString()
    }

    /**
     * Generates a valid WebVTT (.vtt) subtitle string.
     */
    fun generateVttContent(entries: List<SubtitleEntry>): String {
        val sb = StringBuilder()
        sb.append("WEBVTT\n\n")
        entries.forEach { entry ->
            sb.append("${entry.index}\n")
            sb.append("${formatVttTime(entry.startMs)} --> ${formatVttTime(entry.endMs)}\n")
            sb.append("${entry.text}\n\n")
        }
        return sb.toString()
    }

    /**
     * Exports subtitle files (both .srt and .vtt) to the project storage directory.
     */
    fun exportSubtitles(
        context: Context,
        scriptId: String,
        title: String,
        entries: List<SubtitleEntry>
    ): Pair<File, File> {
        val projectDir = StoryAssetStorageService.getProjectDirectory(context, scriptId)
        val subtitlesDir = File(projectDir, "subtitles")
        if (!subtitlesDir.exists()) subtitlesDir.mkdirs()

        val safeName = title.take(20).replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val srtFile = File(subtitlesDir, "${safeName}_subtitles.srt")
        val vttFile = File(subtitlesDir, "${safeName}_subtitles.vtt")

        FileOutputStream(srtFile).use { it.write(generateSrtContent(entries).toByteArray(Charsets.UTF_8)) }
        FileOutputStream(vttFile).use { it.write(generateVttContent(entries).toByteArray(Charsets.UTF_8)) }

        return Pair(srtFile, vttFile)
    }
}
