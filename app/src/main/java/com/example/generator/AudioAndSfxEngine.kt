package com.example.generator

import android.content.Context
import java.io.File
import java.io.FileOutputStream

data class BackgroundMusicTrack(
    val id: String,
    val nameEn: String,
    val nameKn: String,
    val genre: String,
    val bpm: Int,
    val baseFrequencyHz: Double,
    val mood: String
)

data class SoundEffectItem(
    val id: String,
    val nameEn: String,
    val nameKn: String,
    val category: String,
    val durationMs: Int
)

object AudioAndSfxEngine {

    val BGM_LIBRARY = listOf(
        BackgroundMusicTrack("cinematic_epic", "Cinematic Epic Orchestral", "ಸಿನೆಮ್ಯಾಟಿಕ್ ಆರ್ಕೆಸ್ಟ್ರಾ", "Cinematic", 120, 220.0, "Heroic & Grand"),
        BackgroundMusicTrack("emotional_piano", "Emotional Acoustic Piano", "ಭಾವನಾತ್ಮಕ ಪಿಯಾನೋ", "Emotional", 78, 261.63, "Heartfelt & Touching"),
        BackgroundMusicTrack("action_pulse", "Cyberpunk Action Pulse", "ಆಕ್ಷನ್ ಪಲ್ಸ್ ಬೀಟ್ಸ್", "Action", 132, 110.0, "High Energy & Intense"),
        BackgroundMusicTrack("comedy_quirky", "Quirky Comedy Whistle", "ಹಾಸ್ಯ ವಿಸ್ಲಿಂಗ್ ಟ್ಯೂನ್", "Comedy", 115, 330.0, "Playful & Fun"),
        BackgroundMusicTrack("horror_suspense", "Dark Ambient Horror Drone", "ಭಯಾನಕ ಅಂಬಿಯೆಂಟ್ ಡ್ರೋನ್", "Horror", 65, 82.4, "Creepy & Tense"),
        BackgroundMusicTrack("ambient_nature", "Peaceful Ambient Rain & Flute", "ಶಾಂತಿಯುತ ಮಳೆ ಮತ್ತು ಕೊಳಲು", "Ambient", 70, 293.66, "Relaxing & Serene")
    )

    val SFX_LIBRARY = listOf(
        SoundEffectItem("whoosh_fast", "Cinematic Whoosh Transition", "ವೇಗದ ಹಾರಾಟ ಶಬ್ದ", "Transition", 600),
        SoundEffectItem("impact_boom", "Heavy Cinematic Impact Boom", "ಭಾರೀ ಇಂಪ್ಯಾಕ್ಟ್ ಬೂಮ್", "Action", 1200),
        SoundEffectItem("sword_clash", "Katana Steel Slash & Clash", "ಕತ್ತಿ ಝಣಝಣ ಶಬ್ದ", "Manga/Action", 800),
        SoundEffectItem("laser_blast", "Futuristic Laser Blast", "ಲೇಸರ್ ಬ್ಲಾಸ್ಟ್", "Sci-Fi", 500),
        SoundEffectItem("applause_cheer", "Crowd Applause & Cheer", "ಜನರ ಕರತಾಡನ & ಹರ್ಷೋದ್ಗಾರ", "Crowd", 2500),
        SoundEffectItem("door_creak", "Mystery Door Creak & Slam", "ಬಾಗಿಲು ಸದ್ದು & ಸಸ್ಪೆನ್ಸ್", "Horror", 1500)
    )

    /**
     * Synthesizes a valid, harmonic PCM WAV audio file with fade in and fade out envelope
     * representing the background music track.
     */
    fun synthesizeBgmFile(
        outputFile: File,
        trackId: String,
        durationSec: Int,
        volume: Float = 0.35f,
        fadeInSec: Float = 1.5f,
        fadeOutSec: Float = 2.0f
    ) {
        val track = BGM_LIBRARY.find { it.id == trackId } ?: BGM_LIBRARY.first()
        val sampleRate = 22050
        val numSamples = sampleRate * durationSec.coerceAtLeast(3)
        val numChannels = 2 // Stereo
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val dataSize = numSamples * numChannels * (bitsPerSample / 8)
        val chunkSize = 36 + dataSize

        if (outputFile.parentFile?.exists() == false) {
            outputFile.parentFile?.mkdirs()
        }

        java.io.BufferedOutputStream(FileOutputStream(outputFile), 32768).use { out ->
            // RIFF header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(chunkSize))
            out.write("WAVE".toByteArray())

            // "fmt " chunk
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16))
            out.write(shortToByteArray(1.toShort())) // PCM
            out.write(shortToByteArray(numChannels.toShort()))
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray((numChannels * bitsPerSample / 8).toShort()))
            out.write(shortToByteArray(bitsPerSample.toShort()))

            // "data" chunk
            out.write("data".toByteArray())
            out.write(intToByteArray(dataSize))

            val buffer = ByteArray(4) // 2 channels * 2 bytes
            val baseFreq = track.baseFrequencyHz
            val chordFreq = baseFreq * 1.5 // Perfect fifth
            val totalSec = durationSec.toDouble()

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate

                // Envelope calculation: fade in & fade out
                val envelope = when {
                    t < fadeInSec -> (t / fadeInSec).coerceIn(0.0, 1.0)
                    t > totalSec - fadeOutSec -> ((totalSec - t) / fadeOutSec).coerceIn(0.0, 1.0)
                    else -> 1.0
                }

                // Dual harmonic waves
                val sampleL = (Math.sin(2.0 * Math.PI * baseFreq * t) * volume * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                val sampleR = (Math.sin(2.0 * Math.PI * chordFreq * t) * volume * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                buffer[0] = (sampleL and 0xFF).toByte()
                buffer[1] = ((sampleL shr 8) and 0xFF).toByte()
                buffer[2] = (sampleR and 0xFF).toByte()
                buffer[3] = ((sampleR shr 8) and 0xFF).toByte()

                out.write(buffer)
            }
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}
