package io.github.omeryol.akisgesture.action.handler

import android.media.AudioManager
import android.view.KeyEvent
import io.github.omeryol.akisgesture.action.ActionResult

class MediaActionHandler(
    private val audioManager: AudioManager,
) {
    fun handleMediaPlayPause(): ActionResult = mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)

    fun handleMediaPrevious(): ActionResult = mediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    fun handleMediaNext(): ActionResult = mediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)

    fun handleVolumeUp(): ActionResult = audioAction {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    fun handleVolumeDown(): ActionResult = audioAction {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    fun handleToggleMute(): ActionResult = audioAction {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
    }

    private fun mediaKey(keyCode: Int): ActionResult = audioAction {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private inline fun audioAction(block: () -> Unit): ActionResult = try {
        block()
        ActionResult.Success
    } catch (error: Exception) {
        ActionResult.Failed(error.message ?: "Ses veya medya aksiyonu çalıştırılamadı")
    }
}
