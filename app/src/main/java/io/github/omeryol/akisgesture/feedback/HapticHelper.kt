package io.github.omeryol.akisgesture.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
        if (enabled && intensity > 0f) {
            getVibrator(context)?.takeIf { it.hasVibrator() }?.let { vibrator ->
                val strength = intensity.coerceIn(0f, 1f)
                // Quadratic curve: perceived intensity scales better with strength²
                val curve = strength * strength
                val durationMs = when (type) {
                    HapticType.LIGHT  -> (5 + 30 * curve).toLong()    // 5ms → 35ms
                    HapticType.MEDIUM -> (8 + 40 * curve).toLong()    // 8ms → 48ms
                    HapticType.HEAVY  -> (12 + 48 * curve).toLong()   // 12ms → 60ms
                }
                val amplitude = when (type) {
                    HapticType.LIGHT  -> (15 + 200 * curve).toInt()   // 15 → 215
                    HapticType.MEDIUM -> (25 + 220 * curve).toInt()   // 25 → 245
                    HapticType.HEAVY  -> (40 + 215 * curve).toInt()   // 40 → 255
                }.coerceIn(1, 255)
                vibrateOnce(vibrator, durationMs, amplitude)
            }
        }
        if (soundEnabled) playSound(context)
    }

    private fun vibrateOnce(vibrator: Vibrator, durationMs: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(durationMs, amplitude)
                } else {
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            }
        } else {
            runCatching {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    }

    fun cancel(context: Context) {
        getVibrator(context)?.takeIf { it.hasVibrator() }?.let { vibrator ->
            runCatching { vibrator.cancel() }
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
