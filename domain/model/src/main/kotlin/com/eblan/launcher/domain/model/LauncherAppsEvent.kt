package com.eblan.launcher.domain.model

sealed interface LauncherAppsEvent {
    data class PackageAdded(val packageName: String) : LauncherAppsEvent

    data class PackageRemoved(val packageName: String) : LauncherAppsEvent

    data class PackageChanged(val packageName: String) : LauncherAppsEvent

    data class ShortcutsChanged(
        val packageName: String,
        val launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>,
    ) : LauncherAppsEvent
}