package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.EblanLauncherActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import kotlinx.coroutines.flow.Flow

interface LauncherAppsWrapper {
    val launcherAppsEvent: Flow<LauncherAppsEvent>

    val hasShortcutHostPermission: Boolean

    suspend fun getActivityList(): List<EblanLauncherActivityInfo>

    suspend fun getShortcuts(): List<LauncherAppsShortcutInfo>?
}