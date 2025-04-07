package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

internal class DefaultUserDataRepository @Inject constructor() : UserDataRepository {
    private val _userData =
        MutableSharedFlow<UserData>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val currentUserData
        get() = _userData.replayCache.firstOrNull() ?: UserData(
            rows = 5,
            columns = 5,
            pageCount = 3,
        )

    override val userData = _userData.asSharedFlow()

    init {
        _userData.tryEmit(UserData(rows = 5, columns = 5, pageCount = 3))
    }

    override fun updatePageCount(pageCount: Int) {
        _userData.tryEmit(currentUserData.copy(pageCount = pageCount))
    }
}