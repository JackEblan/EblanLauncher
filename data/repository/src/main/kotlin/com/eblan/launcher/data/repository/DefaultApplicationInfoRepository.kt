package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.EblanLauncherApplicationInfo
import com.eblan.launcher.domain.repository.ApplicationInfoRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class DefaultApplicationInfoRepository @Inject constructor() : ApplicationInfoRepository {
    private val _applicationInfos = MutableSharedFlow<List<EblanLauncherApplicationInfo>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val applicationInfos = _applicationInfos.asSharedFlow()

    override suspend fun updateApplicationInfos(applicationInfos: List<EblanLauncherApplicationInfo>) {
        _applicationInfos.emit(applicationInfos)
    }
}