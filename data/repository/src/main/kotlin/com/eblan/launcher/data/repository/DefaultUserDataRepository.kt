package com.eblan.launcher.data.repository

import com.eblan.launcher.data.datastore.UserPreferencesDataSource
import com.eblan.launcher.domain.repository.UserDataRepository
import javax.inject.Inject

internal class DefaultUserDataRepository @Inject constructor(private val userPreferencesDataSource: UserPreferencesDataSource) :
    UserDataRepository {
    override val userData = userPreferencesDataSource.userData

    override suspend fun updateRows(rows: Int) {
        userPreferencesDataSource.updateRows(rows = rows)
    }

    override suspend fun updateColumns(columns: Int) {
        userPreferencesDataSource.updateColumns(columns = columns)
    }

    override suspend fun updatePageCount(pageCount: Int) {
        userPreferencesDataSource.updatePageCount(pageCount = pageCount)
    }

    override suspend fun updateInfiniteScroll(infiniteScroll: Boolean) {
        userPreferencesDataSource.updateInfiniteScroll(infiniteScroll = infiniteScroll)
    }

    override suspend fun updateDockRows(dockRows: Int) {
        userPreferencesDataSource.updateDockRows(dockRows = dockRows)
    }

    override suspend fun updateDockColumns(dockColumns: Int) {
        userPreferencesDataSource.updateDockColumns(dockColumns = dockColumns)
    }

    override suspend fun updateDockHeight(dockHeight: Int) {
        userPreferencesDataSource.updateDockHeight(dockHeight = dockHeight)
    }
}