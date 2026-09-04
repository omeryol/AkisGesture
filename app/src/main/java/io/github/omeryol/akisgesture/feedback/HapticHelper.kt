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
                vibrateModern(vibrator, type, strength, curve)
            }
        }
        if (soundEnabled) playSound(context)
    }

    private fun vibrateModern(vibrator: Vibrator, type: HapticType, strength: Float, curve: Float) {
        // 1. Android 11+ (API 30): Hardware composition primitives (RichTap/Immersion/LRA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val primitive = when (type) {
                HapticType.LIGHT -> VibrationEffect.Composition.PRIMITIVE_LOW_TICK
                HapticType.MEDIUM -> VibrationEffect.Composition.PRIMITIVE_CLICK
                HapticType.HEAVY -> VibrationEffect.Composition.PRIMITIVE_THUD
            }
            if (runCatching { vibrator.areAllPrimitivesSupported(primitive) }.getOrDefault(false)) {
                runCatching {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(primitive, strength.coerceIn(0.1f, 1f))
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            }
        }

        // 2. Android 10 (API 29): System predefined tactile effects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val predefined = when (type) {
                HapticType.LIGHT -> VibrationEffect.EFFECT_TICK
                HapticType.MEDIUM -> VibrationEffect.EFFECT_CLICK
                HapticType.HEAVY -> VibrationEffect.EFFECT_HEAVY_CLICK
            }
            val played = runCatching {
                vibrator.vibrate(VibrationEffect.createPredefined(predefined))
                true
            }.getOrDefault(false)
            if (played) return
        }

        // 3. Fallback: Crisp micro-pulses (never a long muddy motor buzz)
        val durationMs = when (type) {
            HapticType.LIGHT -> (4 + 6 * curve).toLong()    // 4ms → 10ms
            HapticType.MEDIUM -> (6 + 8 * curve).toLong()   // 6ms → 14ms
            HapticType.HEAVY -> (8 + 12 * curve).toLong()   // 8ms → 20ms
        }
        val amplitude = when (type) {
            HapticType.LIGHT -> (40 + 160 * curve).toInt()
            HapticType.MEDIUM -> (70 + 170 * curve).toInt()
            HapticType.HEAVY -> (100 + 155 * curve).toInt()
        }.coerceIn(1, 255)

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
