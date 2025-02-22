package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanLauncherApplicationInfo

interface ApplicationInfoRepository {
    suspend fun getApplicationInfo(gridItemId: Int): EblanLauncherApplicationInfo

    suspend fun upsertApplicationInfo(
        gridItemId: Int,
        applicationInfo: EblanLauncherApplicationInfo,
    )
}