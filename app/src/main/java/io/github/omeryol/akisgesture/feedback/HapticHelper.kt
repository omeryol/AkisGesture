package io.github.omeryol.akisgesture.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View

object HapticHelper {
    enum class HapticType { LIGHT, MEDIUM, HEAVY }

    var intensity: Float = 1f
    var enabled: Boolean = true
    var soundEnabled: Boolean = false

    private var toneGenerator: ToneGenerator? = null
    private var lastSoundMs = 0L

    fun performHaptic(view: View, type: HapticType) {
        // One physical vibration path only. Combining View feedback with a
        // Vibrator pulse causes duplicate/queued pulses on some OEM devices.
        performHaptic(view.context, type)
    }

    fun performHaptic(context: Context, type: HapticType) {
        Log.d(TAG, "performHaptic type=$type enabled=$enabled intensity=$intensity soundEnabled=$soundEnabled")
        if (enabled && intensity > 0f) {
            getVibrator(context)?.takeIf { it.hasVibrator() }?.let { vibrator ->
                val strength = intensity.coerceIn(0f, 1f)
                val durationMs = when (type) {
                    HapticType.LIGHT -> (14 + 12 * strength).toLong()
                    HapticType.MEDIUM -> (20 + 16 * strength).toLong()
                    HapticType.HEAVY -> (28 + 20 * strength).toLong()
                }
                val amplitude = when (type) {
                    HapticType.LIGHT -> (85 + 115 * strength).toInt()
                    HapticType.MEDIUM -> (125 + 120 * strength).toInt()
                    HapticType.HEAVY -> (175 + 80 * strength).toInt()
                }.coerceIn(1, 255)
                vibrateOnce(vibrator, durationMs, amplitude)
            }
        }
        if (soundEnabled) playSound(context)
    }

    private fun vibrateOnce(vibrator: Vibrator, durationMs: Long, amplitude: Int) {
        // Clear a pulse left by an interrupted gesture, then enqueue exactly one.
        runCatching { vibrator.cancel() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(durationMs, amplitude)
                } else {
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            }.onFailure { Log.w(TAG, "vibrate failed", it) }
        } else {
            runCatching {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }.onFailure { Log.w(TAG, "legacy vibrate failed", it) }
        }
    }

    private fun playSound(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastSoundMs < 50) return
        lastSoundMs = now
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val played = audioManager != null && runCatching {
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
            true
        }.getOrDefault(false)
        if (!played) runCatching {
            if (toneGenerator == null) toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 85)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 35)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun getVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        }

    private const val TAG = "AkisGesture"
}
