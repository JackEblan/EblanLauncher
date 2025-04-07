package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>

    fun updatePageCount(pageCount: Int)
}