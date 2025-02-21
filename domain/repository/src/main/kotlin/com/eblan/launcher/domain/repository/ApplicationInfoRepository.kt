package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanLauncherApplicationInfo
import kotlinx.coroutines.flow.SharedFlow

interface ApplicationInfoRepository {
    val applicationInfos: SharedFlow<List<EblanLauncherApplicationInfo>>

    suspend fun insertApplicationInfos()
}