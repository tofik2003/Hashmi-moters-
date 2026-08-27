package com.hashmimotors.app.ui.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.hashmimotors.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Manages sound effects for the app.
 * Generates audio programmatically (no need for audio files).
 * Sounds: tap, success, error, bill-saved, alert, whoosh
 */
@Singleton
class SoundManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    init {
        scope.launch {
            val settings = settingsRepository.getSettingsOnce()
            _enabled.value = settings.soundsEnabled
            _volume.value = (settings.soundVolume / 100f).coerceIn(0f, 1f)
        }
    }

    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
    }

    fun setVolume(volume: Int) {
        _volume.value = (volume / 100f).coerceIn(0f, 1f)
    }

    /**
     * Play a sound effect. Safe to call from main thread.
     */
    fun play(sound: SoundEffect) {
        if (!_enabled.value) return
        scope.launch { playInternal(sound) }
    }

    private fun playInternal(sound: SoundEffect) {
        val samples = when (sound) {
            SoundEffect.TAP -> generateTap()
            SoundEffect.SUCCESS -> generateSuccess()
            SoundEffect.ERROR -> generateError()
            SoundEffect.BILL_SAVED -> generateChaChing()
            SoundEffect.ALERT -> generateAlert()
            SoundEffect.WHOOSH -> generateWhoosh()
            SoundEffect.NOTIFICATION -> generateNotification()
        }
        playAudioTrack(samples, _volume.value)
    }

    private fun playAudioTrack(samples: ShortArray, volume: Float) {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val format = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val track = AudioTrack(
                attrs, format, samples.size * 2,
                AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            track.setVolume(volume)
            track.write(samples, 0, samples.size)
            track.play()
            // Auto release after playback
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                runCatching { track.stop() }
                runCatching { track.release() }
            }, 500L)
        } catch (e: Exception) {
            // Silently fail - sound is non-critical
        }
    }

    // ============== Sound Generators ==============

    /** Soft tap - quick sine wave pulse */
    private fun generateTap(): ShortArray {
        val duration = 0.05f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = exp(-t * 80f) // Fast decay
            data[i] = (sin(2.0 * PI * 1200.0 * t) * envelope * 8000).toInt().toShort()
        }
        return data
    }

    /** Success chime - ascending two-tone */
    private fun generateSuccess(): ShortArray {
        val duration = 0.3f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            val freq = if (t < 0.1) 880.0 else 1320.0
            val envelope = if (t < 0.1) (t / 0.1) else (1f - (t - 0.1f) / 0.2f)
            data[i] = (sin(2.0 * PI * freq * t) * envelope * 10000).toInt().toShort()
        }
        return data
    }

    /** Error buzz - low frequency rough sound */
    private fun generateError(): ShortArray {
        val duration = 0.25f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = exp(-t * 6f)
            val freq = 200.0
            val noise = (Math.random() - 0.5) * 0.3
            data[i] = ((sin(2.0 * PI * freq * t) + noise) * envelope * 12000).toInt().toShort()
        }
        return data
    }

    /** Cha-ching - cash register / coin sound */
    private fun generateChaChing(): ShortArray {
        val duration = 0.5f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        val notes = listOf(
            0.0f to 0.05f to 1500.0,
            0.05f to 0.1f to 1800.0,
            0.1f to 0.15f to 2000.0,
            0.15f to 0.4f to 2400.0
        )
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            var sum = 0.0
            for (note in notes) {
                val ((start, end), freq) = note
                if (t in start..end) {
                    val localT = t - start
                    val noteDuration = end - start
                    val envelope = exp(-localT * 8f)
                    sum += sin(2.0 * PI * freq * localT) * envelope
                }
            }
            data[i] = (sum * 8000).toInt().toShort()
        }
        return data
    }

    /** Alert ping - soft two-tone warning */
    private fun generateAlert(): ShortArray {
        val duration = 0.4f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            val cycle = (t / 0.15f).toInt()
            val phase = (t % 0.15f) / 0.15f
            val envelope = if (phase < 0.5f) phase * 2f else (1f - phase) * 2f
            val freq = if (cycle % 2 == 0) 800.0 else 600.0
            data[i] = (sin(2.0 * PI * freq * t) * envelope * 9000).toInt().toShort()
        }
        return data
    }

    /** Whoosh - sweeping frequency */
    private fun generateWhoosh(): ShortArray {
        val duration = 0.3f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = sin(PI * t / duration)
            val freq = 300.0 + 1500.0 * (t / duration)
            data[i] = (sin(2.0 * PI * freq * t) * envelope * 6000).toInt().toShort()
        }
        return data
    }

    /** Notification - quick pleasant ping */
    private fun generateNotification(): ShortArray {
        val duration = 0.2f
        val samples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = exp(-t * 12f)
            data[i] = (sin(2.0 * PI * 1000.0 * t) * envelope * 10000).toInt().toShort()
        }
        return data
    }

    companion object {
        private const val SAMPLE_RATE = 22050
    }
}

enum class SoundEffect {
    TAP,
    SUCCESS,
    ERROR,
    BILL_SAVED,
    ALERT,
    WHOOSH,
    NOTIFICATION
}
