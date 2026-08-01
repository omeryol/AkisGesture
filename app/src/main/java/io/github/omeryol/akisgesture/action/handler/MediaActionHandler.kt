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

    fun handleVolumeUp(): ActionResult {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        ActionResult.Success
        return ActionResult.Success
    }

    fun handleVolumeDown(): ActionResult {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        return ActionResult.Success
    }

    fun handleToggleMute(): ActionResult {
        audioManager.adjustVolume(AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
        return ActionResult.Success
    }

    private fun mediaKey(keyCode: Int): ActionResult {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        return ActionResult.Success
    }
}
