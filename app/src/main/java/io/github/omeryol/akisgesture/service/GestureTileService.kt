package io.github.omeryol.akisgesture.service

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.omeryol.akisgesture.root.RootResult

class GestureTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    override fun onClick() {
        super.onClick()
        val enable = !AccessibilityControl.isEnabled(this)
        Thread {
            when (AccessibilityControl.setEnabled(this, enable)) {
                RootResult.Success -> refresh()
                is RootResult.Failure -> {
                    startActivityAndCollapse(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }.start()
    }

    private fun refresh() {
        val enabled = AccessibilityControl.isEnabled(this)
        qsTile?.apply {
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Akış"
            subtitle = if (enabled) "Hareketler açık" else "Hareketler kapalı"
            updateTile()
        }
    }
}
