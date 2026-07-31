package com.omer.akisgesture.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View

object HapticHelper {

    enum class HapticType {
        LIGHT,
        MEDIUM,
        HEAVY,
    }

    /** Global intensity 0f..1f. Set by GestureConfig. */
    var intensity: Float = 1f
    /** Whether to play a click sound alongside vibration. */
    var soundEnabled: Boolean = false

    private var toneGenerator: ToneGenerator? = null
    private var toneIntensity: Float = -1f
    private var lastSoundMs = 0L

    /**
     * Primary entry point — vibrates via the direct Vibrator API.
     * The View parameter is used only to obtain a Context; no view-level
     * haptic feedback is triggered (overlay windows handle it unreliably).
     */
    fun performHaptic(view: View, type: HapticType) {
        performHaptic(view.context, type)
    }

    fun performHaptic(context: Context, type: HapticType) {
        if (intensity <= 0f) return
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        val (durationMs, amplitude) = when (type) {
            HapticType.LIGHT -> 20L to 90
            HapticType.MEDIUM -> 35L to 160
            HapticType.HEAVY -> 60L to 245
        }
        vibrate(vibrator, durationMs, (amplitude * intensity).toInt().coerceIn(1, 255))
        playClickSound()
    }

    private fun vibrate(vibrator: Vibrator, durationMs: Long, amplitude: Int) {
        val safeAmplitude = amplitude.coerceIn(1, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(durationMs, safeAmplitude)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+: use VibrationAttributes with USAGE_TOUCH to bypass
                    // the system "disable touch vibration" setting.
                    val attrs = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else {
                    vibrator.vibrate(effect)
                }
            } catch (_: Exception) {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    private fun playClickSound() {
        if (!soundEnabled) return
        val now = System.currentTimeMillis()
        if (now - lastSoundMs < 50) return // debounce
        lastSoundMs = now
        runCatching {
            // Recreate ToneGenerator when intensity changes so volume stays in sync
            if (toneGenerator == null || toneIntensity != intensity) {
                toneGenerator?.release()
                toneGenerator = ToneGenerator(
                    AudioManager.STREAM_MUSIC,
                    (85 * intensity).toInt().coerceIn(30, 100),
                )
                toneIntensity = intensity
            }
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 25)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        toneIntensity = -1f
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
