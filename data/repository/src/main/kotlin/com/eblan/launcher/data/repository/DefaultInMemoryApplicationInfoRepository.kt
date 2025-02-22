package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.InMemoryApplicationInfo
import com.eblan.launcher.domain.repository.InMemoryApplicationInfoRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class DefaultInMemoryApplicationInfoRepository @Inject constructor() :
    InMemoryApplicationInfoRepository {
    private val _applicationInfos = MutableSharedFlow<List<InMemoryApplicationInfo>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val applicationInfos = _applicationInfos.asSharedFlow()

    override suspend fun updateInMemoryApplicationInfos(applicationInfos: List<InMemoryApplicationInfo>) {
        _applicationInfos.emit(applicationInfos)
    }
}