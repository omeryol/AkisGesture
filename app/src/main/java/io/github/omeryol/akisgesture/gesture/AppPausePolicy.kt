package io.github.omeryol.akisgesture.gesture

enum class AppPauseMode {
    BLACKLIST,
    WHITELIST,
}

object AppPausePolicy {
    fun shouldPause(
        foregroundPackage: String?,
        pausedPackages: Set<String>,
        mode: AppPauseMode = AppPauseMode.BLACKLIST,
    ): Boolean {
        if (foregroundPackage.isNullOrBlank()) return false
        return when (mode) {
            AppPauseMode.BLACKLIST -> foregroundPackage in pausedPackages
            AppPauseMode.WHITELIST -> pausedPackages.isNotEmpty() && foregroundPackage !in pausedPackages
        }
    }
}

