package com.example.generator

import com.example.i18n.I18n
import java.util.Locale

/**
 * Intelligent Prompt Suggestion Engine:
 * Analyzes narration text, scene context, character descriptions, and story genre
 * to automatically generate rich, cinematic AI image prompts tailored for Imagen 3 and Gemini Vision.
 */
object PromptSuggestionEngine {

    /**
     * Generates an enhanced cinematic visual prompt from narration text and scene metadata.
     */
    fun suggestImagePrompt(
        narration: String,
        sceneTitle: String = "",
        genre: StoryGenre = StoryGenre.THRILLER,
        characterPrompt: String = "",
        language: String = I18n.LANG_EN
    ): String {
        val cleanNarration = narration.trim().replace("\"", "").replace("\n", " ")
        if (cleanNarration.isBlank()) {
            return "Cinematic establishing shot of ${characterPrompt.ifBlank { "heroic character" }}, dramatic cinematic lighting, 8k resolution, photorealistic"
        }

        val isKn = language == I18n.LANG_KN

        // 1. Detect Mood / Action keywords
        val lowerText = cleanNarration.lowercase(Locale.ROOT)
        val cameraAngle = when {
            lowerText.contains("sky") || lowerText.contains("space") || lowerText.contains("ಬಾಹ್ಯಾಕಾಶ") || lowerText.contains("ಆಕಾಶ") ->
                "Wide-angle cosmic panoramic shot"
            lowerText.contains("battle") || lowerText.contains("fight") || lowerText.contains("storm") || lowerText.contains("ಸವಾಲು") || lowerText.contains("ಯುದ್ಧ") ->
                "Dynamic low-angle high-speed action shot"
            lowerText.contains("victory") || lowerText.contains("triumph") || lowerText.contains("won") || lowerText.contains("ಗೆಲುವು") || lowerText.contains("ಸಾಧನೆ") ->
                "Heroic upward-angled golden hour shot"
            lowerText.contains("secret") || lowerText.contains("night") || lowerText.contains("dark") || lowerText.contains("ಕತ್ತಲೆ") || lowerText.contains("ರಹಸ್ಯ") ->
                "Moody atmospheric close-up with shallow depth of field"
            lowerText.contains("journey") || lowerText.contains("walk") || lowerText.contains("road") || lowerText.contains("ಹಾದಿ") || lowerText.contains("ಪಯಣ") ->
                "Cinematic tracking shot along expansive pathway"
            else -> "Medium cinematic shot with 85mm portrait lens"
        }

        // 2. Genre-specific atmosphere tokens
        val atmosphereTokens = when (genre) {
            StoryGenre.THRILLER -> "film noir lighting, volumetric fog, deep contrasting shadows, neon rim-light reflections, mystery ambience"
            StoryGenre.ACTION -> "high-energy dynamic sparks, motion blur streaks, explosive debris, fiery embers, intense cinematic color grade"
            StoryGenre.COMEDY -> "bright warm ambient sunlight, vibrant saturated colors, playful perspective, clean crisp depth of field"
            StoryGenre.DEVOTIONAL -> "divine radiant golden rays, sacred diya lamp glow, temple architecture, spiritual aura, ethereal mist"
            StoryGenre.EDUCATIONAL -> "clean futuristic UI holograms, sleek studio lighting, crisp high-tech detailing, ultra-sharp focus"
            StoryGenre.MOTIVATION -> "inspiring golden sunrise backdrop, towering mountain peaks, lens flare, radiant halo glow, triumphant mood"
            StoryGenre.DRAMA -> "emotional soft diffused window lighting, moody dusk palette, cinematic bokeh, hyper-detailed facial expressions"
        }

        // 3. Subject reference
        val subject = if (characterPrompt.isNotBlank()) {
            characterPrompt.take(80)
        } else {
            if (isKn) "ಕಥೆಯ ನಾಯಕ/ಪಾತ್ರ" else "main protagonist"
        }

        // 4. Summarize narration core essence (strip punctuation)
        val narrationSummary = cleanNarration
            .replace(Regex("[,.!?;\"]"), "")
            .split("\\s+".toRegex())
            .take(12)
            .joinToString(" ")

        return "$cameraAngle of $subject, depicting $narrationSummary, $atmosphereTokens, highly detailed textures, masterclass composition, photorealistic 8k render"
    }

    /**
     * Automatically enriches all scenes in a script with newly suggested prompts.
     */
    fun suggestAllPrompts(script: StoryScriptData): StoryScriptData {
        val updatedScenes = script.scenes.map { scene ->
            val suggested = suggestImagePrompt(
                narration = scene.voiceover,
                sceneTitle = scene.title,
                genre = script.genre,
                characterPrompt = script.characterAppearancePrompt,
                language = script.language
            )
            scene.copy(visualPrompt = suggested)
        }
        return script.copy(scenes = updatedScenes)
    }
}
