package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.EblanLauncherActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import kotlinx.coroutines.flow.Flow

interface LauncherAppsDomainWrapper {
    val launcherAppsEvent: Flow<LauncherAppsEvent>

    val hasShortcutHostPermission: Boolean

    suspend fun getActivityList(): List<EblanLauncherActivityInfo>

    fun startMainActivity(componentName: String?)

    suspend fun getShortcuts(): List<LauncherAppsShortcutInfo>?
}