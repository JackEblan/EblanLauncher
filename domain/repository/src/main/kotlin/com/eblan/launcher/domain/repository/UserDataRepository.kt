package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>

    suspend fun updateRows(rows: Int)

    suspend fun updateColumns(columns: Int)

    suspend fun updatePageCount(pageCount: Int)

    suspend fun updateInfiniteScroll(infiniteScroll: Boolean)

    suspend fun updateDockRows(dockRows: Int)

    suspend fun updateDockColumns(dockColumns: Int)

    suspend fun updateDockHeight(dockHeight: Int)

    suspend fun updateInitialPage(initialPage: Int)
}