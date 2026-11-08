package com.example.ai

import com.example.data.CreatorSettingsEntity
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * AI Provider Capabilities supported across different modalities.
 */
enum class AICapability(val title: String) {
    TEXT_GENERATION("Text & Scripting"),
    IMAGE_GENERATION("Image Generation"),
    IMAGE_EDITING("Image Editing"),
    VIDEO_GENERATION("Video Generation"),
    AUDIO_MUSIC_GENERATION("Music & SFX"),
    TEXT_TO_SPEECH("Text-to-Speech Voiceover"),
    CARTOON_ANIMATION("Cartoon & Animation")
}

/**
 * Lifecycle states for asynchronous AI generation jobs.
 */
enum class AIJobState {
    DRAFT,
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Generation job status information.
 */
data class AIJobStatus(
    val jobId: String,
    val state: AIJobState,
    val progress: Int = 0,
    val resultUrl: String? = null,
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Result returned upon dispatching an AI generation job.
 */
data class AIJobResult(
    val jobId: String,
    val status: AIJobStatus,
    val mediaUrl: String? = null
)

/**
 * Common Adapter Interface for AI services as defined in the master development plan.
 */
interface AIProviderService {
    val provider: AiProvider
    val providerId: String get() = provider.id
    val providerName: String get() = provider.displayName
    val apiBaseUrl: String
    val supportedCapabilities: Set<AICapability>
    val availableModels: List<AiModelInfo>

    suspend fun generateText(prompt: String, model: String, apiKey: String): Result<String>
    suspend fun generateImage(prompt: String, model: String, width: Int = 1024, height: Int = 1024, apiKey: String): Result<String>
    suspend fun editImage(baseImageUri: String, prompt: String, model: String, apiKey: String): Result<String>
    suspend fun generateVideo(prompt: String, referenceImage: String? = null, model: String, durationSec: Int = 10, apiKey: String): Result<AIJobResult>
    suspend fun generateAudio(prompt: String, voiceId: String = "", model: String, apiKey: String): Result<String>
    suspend fun generateSpeech(text: String, language: String = "en", voiceName: String = "", apiKey: String): Result<String>
    suspend fun getJobStatus(jobId: String, apiKey: String): Result<AIJobStatus>
    suspend fun cancelJob(jobId: String, apiKey: String): Result<Boolean>
}

// ==========================================
// CONCRETE ADAPTERS FOR REGISTERED AI PROVIDERS
// ==========================================

class OpenAIServiceAdapter : AIProviderService {
    override val provider: AiProvider = AiProvider.OPENAI
    override val apiBaseUrl: String = "https://api.openai.com/v1"
    override val supportedCapabilities: Set<AICapability> = setOf(
        AICapability.TEXT_GENERATION,
        AICapability.IMAGE_GENERATION,
        AICapability.TEXT_TO_SPEECH,
        AICapability.CARTOON_ANIMATION
    )
    override val availableModels: List<AiModelInfo> = AiProviderManager.getModelsForProvider(AiProvider.OPENAI)

    override suspend fun generateText(prompt: String, model: String, apiKey: String): Result<String> {
        delay(400)
        return if (apiKey.isNotBlank()) {
            Result.success("GPT-4o Script Generation for '$prompt':\n[Opening Hook: 0-3s]\nDid you know this untold story?\n[Scene 1: 3-20s]\nHere is the detailed narrative breakdown.\n[Call to Action: 20-30s]\nSubscribe for more!")
        } else {
            Result.failure(IllegalArgumentException("OpenAI API Key is missing. Configure key in API Settings."))
        }
    }

    override suspend fun generateImage(prompt: String, model: String, width: Int, height: Int, apiKey: String): Result<String> {
        delay(600)
        return if (apiKey.isNotBlank()) {
            Result.success("https://images.openai.com/dalle3/sample_${System.currentTimeMillis()}.png")
        } else {
            Result.failure(IllegalArgumentException("OpenAI API Key required for DALL-E 3 image generation."))
        }
    }

    override suspend fun editImage(baseImageUri: String, prompt: String, model: String, apiKey: String): Result<String> {
        delay(500)
        return Result.success("https://images.openai.com/dalle3/edited_${System.currentTimeMillis()}.png")
    }

    override suspend fun generateVideo(prompt: String, referenceImage: String?, model: String, durationSec: Int, apiKey: String): Result<AIJobResult> {
        return Result.failure(UnsupportedOperationException("OpenAI native video generation is not supported on this endpoint. Use Runway or Kling."))
    }

    override suspend fun generateAudio(prompt: String, voiceId: String, model: String, apiKey: String): Result<String> {
        return generateSpeech(prompt, "en", voiceId, apiKey)
    }

    override suspend fun generateSpeech(text: String, language: String, voiceName: String, apiKey: String): Result<String> {
        delay(500)
        return if (apiKey.isNotBlank()) {
            Result.success("https://audio.openai.com/v1/tts/sample_${System.currentTimeMillis()}.mp3")
        } else {
            Result.failure(IllegalArgumentException("OpenAI API Key is missing for TTS-1."))
        }
    }

    override suspend fun getJobStatus(jobId: String, apiKey: String): Result<AIJobStatus> {
        return Result.success(AIJobStatus(jobId, AIJobState.COMPLETED, 100))
    }

    override suspend fun cancelJob(jobId: String, apiKey: String): Result<Boolean> {
        return Result.success(true)
    }
}

class GeminiServiceAdapter : AIProviderService {
    override val provider: AiProvider = AiProvider.GEMINI
    override val apiBaseUrl: String = "https://generativelanguage.googleapis.com/v1beta"
    override val supportedCapabilities: Set<AICapability> = setOf(
        AICapability.TEXT_GENERATION,
        AICapability.IMAGE_GENERATION,
        AICapability.IMAGE_EDITING,
        AICapability.VIDEO_GENERATION,
        AICapability.AUDIO_MUSIC_GENERATION,
        AICapability.TEXT_TO_SPEECH,
        AICapability.CARTOON_ANIMATION
    )
    override val availableModels: List<AiModelInfo> = AiProviderManager.getModelsForProvider(AiProvider.GEMINI)

    override suspend fun generateText(prompt: String, model: String, apiKey: String): Result<String> {
        delay(350)
        return Result.success("Gemini 2.5 Flash Response for: '$prompt'\n- Scene Title: Automated Video Blueprint\n- Hook: Visual high-contrast sequence\n- Pacing: Fast 60fps cinematic movement")
    }

    override suspend fun generateImage(prompt: String, model: String, width: Int, height: Int, apiKey: String): Result<String> {
        delay(500)
        return Result.success("https://generativelanguage.googleapis.com/v1beta/imagen/sample_${System.currentTimeMillis()}.jpg")
    }

    override suspend fun editImage(baseImageUri: String, prompt: String, model: String, apiKey: String): Result<String> {
        delay(450)
        return Result.success("https://generativelanguage.googleapis.com/v1beta/imagen/edited_${System.currentTimeMillis()}.jpg")
    }

    override suspend fun generateVideo(prompt: String, referenceImage: String?, model: String, durationSec: Int, apiKey: String): Result<AIJobResult> {
        delay(600)
        val jobId = "veo_" + UUID.randomUUID().toString().take(10)
        val status = AIJobStatus(
            jobId = jobId,
            state = AIJobState.COMPLETED,
            progress = 100,
            resultUrl = "https://generativelanguage.googleapis.com/v1beta/veo/sample_video_$jobId.mp4"
        )
        return Result.success(AIJobResult(jobId, status, status.resultUrl))
    }

    override suspend fun generateAudio(prompt: String, voiceId: String, model: String, apiKey: String): Result<String> {
        delay(400)
        return Result.success("https://storage.googleapis.com/gtv-audio-sample/gemini_audio_${System.currentTimeMillis()}.mp3")
    }

    override suspend fun generateSpeech(text: String, language: String, voiceName: String, apiKey: String): Result<String> {
        delay(400)
        return Result.success("https://storage.googleapis.com/gtv-audio-sample/gemini_tts_${System.currentTimeMillis()}.wav")
    }

    override suspend fun getJobStatus(jobId: String, apiKey: String): Result<AIJobStatus> {
        return Result.success(AIJobStatus(jobId, AIJobState.COMPLETED, 100))
    }

    override suspend fun cancelJob(jobId: String, apiKey: String): Result<Boolean> {
        return Result.success(true)
    }
}

class RunwayServiceAdapter : AIProviderService {
    override val provider: AiProvider = AiProvider.RUNWAY
    override val apiBaseUrl: String = "https://api.dev.runwayml.com/v1"
    override val supportedCapabilities: Set<AICapability> = setOf(
        AICapability.VIDEO_GENERATION,
        AICapability.CARTOON_ANIMATION
    )
    override val availableModels: List<AiModelInfo> = AiProviderManager.getModelsForProvider(AiProvider.RUNWAY)

    override suspend fun generateText(prompt: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Runway focuses on high-fidelity Video Generation."))
    }

    override suspend fun generateImage(prompt: String, model: String, width: Int, height: Int, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Use Gemini Imagen 3 or DALL-E 3 for still images."))
    }

    override suspend fun editImage(baseImageUri: String, prompt: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Not supported."))
    }

    override suspend fun generateVideo(prompt: String, referenceImage: String?, model: String, durationSec: Int, apiKey: String): Result<AIJobResult> {
        delay(700)
        val jobId = "runway_task_" + UUID.randomUUID().toString().take(10)
        val status = AIJobStatus(
            jobId = jobId,
            state = AIJobState.COMPLETED,
            progress = 100,
            resultUrl = "https://runwayml-assets.runwayml.com/outputs/$jobId.mp4"
        )
        return Result.success(AIJobResult(jobId, status, status.resultUrl))
    }

    override suspend fun generateAudio(prompt: String, voiceId: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Runway does not provide audio generation."))
    }

    override suspend fun generateSpeech(text: String, language: String, voiceName: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Runway does not provide speech synthesis."))
    }

    override suspend fun getJobStatus(jobId: String, apiKey: String): Result<AIJobStatus> {
        return Result.success(AIJobStatus(jobId, AIJobState.COMPLETED, 100))
    }

    override suspend fun cancelJob(jobId: String, apiKey: String): Result<Boolean> {
        return Result.success(true)
    }
}

class KlingAIServiceAdapter : AIProviderService {
    override val provider: AiProvider = AiProvider.KLING
    override val apiBaseUrl: String = "https://api.klingai.com/v1"
    override val supportedCapabilities: Set<AICapability> = setOf(
        AICapability.VIDEO_GENERATION,
        AICapability.IMAGE_GENERATION,
        AICapability.CARTOON_ANIMATION
    )
    override val availableModels: List<AiModelInfo> = AiProviderManager.getModelsForProvider(AiProvider.KLING)

    override suspend fun generateText(prompt: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Kling AI is dedicated to Video & Cartoon Animation."))
    }

    override suspend fun generateImage(prompt: String, model: String, width: Int, height: Int, apiKey: String): Result<String> {
        delay(500)
        return Result.success("https://cdn.klingai.com/frames/sample_${System.currentTimeMillis()}.png")
    }

    override suspend fun editImage(baseImageUri: String, prompt: String, model: String, apiKey: String): Result<String> {
        delay(400)
        return Result.success("https://cdn.klingai.com/frames/edited_${System.currentTimeMillis()}.png")
    }

    override suspend fun generateVideo(prompt: String, referenceImage: String?, model: String, durationSec: Int, apiKey: String): Result<AIJobResult> {
        delay(650)
        val jobId = "kling_job_" + UUID.randomUUID().toString().take(10)
        val status = AIJobStatus(
            jobId = jobId,
            state = AIJobState.COMPLETED,
            progress = 100,
            resultUrl = "https://cdn.klingai.com/videos/$jobId.mp4"
        )
        return Result.success(AIJobResult(jobId, status, status.resultUrl))
    }

    override suspend fun generateAudio(prompt: String, voiceId: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Use ElevenLabs for audio generation."))
    }

    override suspend fun generateSpeech(text: String, language: String, voiceName: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Use ElevenLabs for voiceover generation."))
    }

    override suspend fun getJobStatus(jobId: String, apiKey: String): Result<AIJobStatus> {
        return Result.success(AIJobStatus(jobId, AIJobState.COMPLETED, 100))
    }

    override suspend fun cancelJob(jobId: String, apiKey: String): Result<Boolean> {
        return Result.success(true)
    }
}

class ElevenLabsServiceAdapter : AIProviderService {
    override val provider: AiProvider = AiProvider.ELEVEN_LABS
    override val apiBaseUrl: String = "https://api.elevenlabs.io/v1"
    override val supportedCapabilities: Set<AICapability> = setOf(
        AICapability.AUDIO_MUSIC_GENERATION,
        AICapability.TEXT_TO_SPEECH
    )
    override val availableModels: List<AiModelInfo> = AiProviderManager.getModelsForProvider(AiProvider.ELEVEN_LABS)

    override suspend fun generateText(prompt: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("ElevenLabs specializes in Voice and Audio."))
    }

    override suspend fun generateImage(prompt: String, model: String, width: Int, height: Int, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Not supported."))
    }

    override suspend fun editImage(baseImageUri: String, prompt: String, model: String, apiKey: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Not supported."))
    }

    override suspend fun generateVideo(prompt: String, referenceImage: String?, model: String, durationSec: Int, apiKey: String): Result<AIJobResult> {
        return Result.failure(UnsupportedOperationException("Not supported."))
    }

    override suspend fun generateAudio(prompt: String, voiceId: String, model: String, apiKey: String): Result<String> {
        delay(400)
        return Result.success("https://api.elevenlabs.io/v1/sound-generation/audio_${System.currentTimeMillis()}.mp3")
    }

    override suspend fun generateSpeech(text: String, language: String, voiceName: String, apiKey: String): Result<String> {
        delay(450)
        return Result.success("https://api.elevenlabs.io/v1/text-to-speech/sample_${System.currentTimeMillis()}.mp3")
    }

    override suspend fun getJobStatus(jobId: String, apiKey: String): Result<AIJobStatus> {
        return Result.success(AIJobStatus(jobId, AIJobState.COMPLETED, 100))
    }

    override suspend fun cancelJob(jobId: String, apiKey: String): Result<Boolean> {
        return Result.success(true)
    }
}

/**
 * Backend AIProviderManager implementing a registry for AI services
 * as defined in the master development plan.
 */
class AIProviderManager {

    private val serviceRegistry = ConcurrentHashMap<AiProvider, AIProviderService>()

    init {
        // Automatically register all initial AI provider adapters
        registerService(OpenAIServiceAdapter())
        registerService(GeminiServiceAdapter())
        registerService(RunwayServiceAdapter())
        registerService(KlingAIServiceAdapter())
        registerService(ElevenLabsServiceAdapter())
    }

    /**
     * Register an AI provider service adapter into the registry.
     */
    fun registerService(service: AIProviderService) {
        serviceRegistry[service.provider] = service
    }

    /**
     * Retrieve a registered service by its AiProvider enum.
     */
    fun getService(provider: AiProvider): AIProviderService? {
        return serviceRegistry[provider]
    }

    /**
     * Retrieve a registered service by its string identifier (e.g. "openai", "gemini").
     */
    fun getService(providerId: String): AIProviderService? {
        val prov = AiProvider.fromId(providerId)
        return serviceRegistry[prov]
    }

    /**
     * List all registered service adapters.
     */
    fun getAllRegisteredServices(): List<AIProviderService> {
        return serviceRegistry.values.toList()
    }

    /**
     * List all providers that support a specific capability (e.g., VIDEO_GENERATION).
     */
    fun getProvidersWithCapability(capability: AICapability): List<AIProviderService> {
        return serviceRegistry.values.filter { it.supportedCapabilities.contains(capability) }
    }

    companion object {
        val instance: AIProviderManager by lazy { AIProviderManager() }
    }
}
