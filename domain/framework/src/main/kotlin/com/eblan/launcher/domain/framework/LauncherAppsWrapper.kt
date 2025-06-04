package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.EblanLauncherActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import kotlinx.coroutines.flow.Flow

interface LauncherAppsWrapper {
    val launcherAppsEvent: Flow<LauncherAppsEvent>

    suspend fun getActivityList(): List<EblanLauncherActivityInfo>
}