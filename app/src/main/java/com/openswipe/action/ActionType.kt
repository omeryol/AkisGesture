package com.openswipe.action

sealed class ActionType {
    data object None : ActionType()

    sealed class Navigation : ActionType() {
        data object Back : Navigation()
        data object Home : Navigation()
        data object Recents : Navigation()
        data object SwitchLastApp : Navigation()
        data object SplitScreen : Navigation()
        data object PowerDialog : Navigation()
        data object LockScreen : Navigation()
        data object TakeScreenshot : Navigation()
        data object Notifications : Navigation()
        data object QuickSettings : Navigation()
    }

    sealed class Media : ActionType() {
        data object PlayPause : Media()
        data object Previous : Media()
        data object Next : Media()
        data object VolumeUp : Media()
        data object VolumeDown : Media()
        data object ToggleMute : Media()
    }

    data class LaunchApp(val packageName: String, val activityName: String) : ActionType()

    data object ToggleFlashlight : ActionType()
    data object SwitchInputMethod : ActionType()
}
