package io.github.omeryol.akisgesture.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.omeryol.akisgesture.root.RootResult

class GestureTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        refresh()
        Handler(Looper.getMainLooper()).postDelayed({ refresh() }, 250L)
    }

    override fun onClick() {
        super.onClick()
        val enable = !AccessibilityControl.isEnabled(this)
        updateTileState(enable)
        Thread {
            when (AccessibilityControl.setEnabled(this, enable)) {
                RootResult.Success -> {
                    sendBroadcast(Intent(ACTION_TILE_STATE_CHANGED).setPackage(packageName))
                    Handler(Looper.getMainLooper()).post { refresh() }
                }
                is RootResult.Failure -> openAccessibilitySettings()
            }
        }.start()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    @Suppress("DEPRECATION")
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    private fun refresh() {
        updateTileState(AccessibilityControl.isEnabled(this))
    }

    private fun updateTileState(enabled: Boolean) {
        qsTile?.apply {
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = getString(io.github.omeryol.akisgesture.R.string.tile_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(if (enabled) io.github.omeryol.akisgesture.R.string.tile_on else io.github.omeryol.akisgesture.R.string.tile_off)
            }
            updateTile()
        }
    }

    companion object {
        const val ACTION_TILE_STATE_CHANGED =
            "io.github.omeryol.akisgesture.action.TILE_STATE_CHANGED"
    }
}
