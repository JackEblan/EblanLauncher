package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.InMemoryApplicationInfo
import kotlinx.coroutines.flow.SharedFlow

interface InMemoryApplicationInfoRepository {
    val applicationInfos: SharedFlow<List<InMemoryApplicationInfo>>

    suspend fun updateInMemoryApplicationInfos(applicationInfos: List<InMemoryApplicationInfo>)
}