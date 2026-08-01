package io.github.omeryol.akisgesture.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {

    enum class HapticType {
        LIGHT,
        MEDIUM,
        HEAVY,
    }

    /** Titreşim şiddeti 0f..1f (Ayarlar slider'ı) */
    var intensity: Float = 1f

    /** Tıklama sesi anahtarı (Ayarlar switch'i) */
    var soundEnabled: Boolean = false

    private var toneGenerator: ToneGenerator? = null
    private var lastSoundMs = 0L

    /**
     * View üzerinden hem haptik geri bildirimi hem donanım titreşimini tetikler.
     */
    fun performHaptic(view: View, type: HapticType) {
        if (intensity > 0f) {
            runCatching {
                view.isHapticFeedbackEnabled = true
                val constant = when (type) {
                    HapticType.LIGHT -> HapticFeedbackConstants.KEYBOARD_TAP
                    HapticType.MEDIUM -> HapticFeedbackConstants.VIRTUAL_KEY
                    HapticType.HEAVY -> HapticFeedbackConstants.LONG_PRESS
                }
                @Suppress("DEPRECATION")
                view.performHapticFeedback(
                    constant,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }
        }
        performHaptic(view.context, type)
    }

    /**
     * Context üzerinden donanım vibratörü ve ses motorunu çalıştırır.
     */
    fun performHaptic(context: Context, type: HapticType) {
        Log.d(TAG, "performHaptic type=$type intensity=$intensity soundEnabled=$soundEnabled")

        // 1. Titreşim Donanımı
        if (intensity > 0f) {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                val durationMs = when (type) {
                    HapticType.LIGHT -> (30 + 30 * intensity).toLong()    // 30ms - 60ms
                    HapticType.MEDIUM -> (60 + 40 * intensity).toLong()   // 60ms - 100ms
                    HapticType.HEAVY -> (100 + 60 * intensity).toLong()   // 100ms - 160ms
                }

                val amplitude = when (type) {
                    HapticType.LIGHT -> (100 + 155 * intensity).toInt().coerceIn(1, 255)
                    HapticType.MEDIUM -> (160 + 95 * intensity).toInt().coerceIn(1, 255)
                    HapticType.HEAVY -> 255
                }

                vibrate(vibrator, durationMs, amplitude)
            }
        }

        // 2. Ses Motoru (Titreşimden tamamen bağımsız)
        if (soundEnabled) {
            playSound(context)
        }
    }

    private fun vibrate(vibrator: Vibrator, durationMs: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(durationMs, amplitude)
                } else {
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
                Log.d(TAG, "vibrate_effect duration=$durationMs amp=$amplitude")
            }
        }

        // Eski cihazlar ve varsayılan donanım darbesi için yedek çağrı
        runCatching {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
            Log.d(TAG, "vibrate_pulse duration=$durationMs")
        }
    }

    private fun playSound(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastSoundMs < 50) return // debounce
        lastSoundMs = now

        // 1. Öncelik: Sistem standart dokunma klik sesi (en doğal ses)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val soundEffectPlayed = runCatching {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
            true
        }.getOrDefault(false)

        // 2. Öncelik: Sistem klik sesi çalmazsa ToneGenerator
        if (!soundEffectPlayed) {
            runCatching {
                if (toneGenerator == null) {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 85)
                }
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 35)
                Log.d(TAG, "tone_played TONE_PROP_ACK")
            }
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
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

    private const val TAG = "AkisGesture"
}
