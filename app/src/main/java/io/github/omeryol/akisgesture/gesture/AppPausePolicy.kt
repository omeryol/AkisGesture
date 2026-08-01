package io.github.omeryol.akisgesture.gesture

object AppPausePolicy {
    fun shouldPause(foregroundPackage: String?, pausedPackages: Set<String>): Boolean {
        return !foregroundPackage.isNullOrBlank() && foregroundPackage in pausedPackages
    }
}
