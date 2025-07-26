package com.eblan.launcher.data.repository

import com.eblan.launcher.data.datastore.UserDataStore
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.repository.UserDataRepository
import javax.inject.Inject

internal class DefaultUserDataRepository @Inject constructor(private val userDataStore: UserDataStore) :
    UserDataRepository {
    override val userData = userDataStore.userData

    override suspend fun updateRows(rows: Int) {
        userDataStore.updateRows(rows = rows)
    }

    override suspend fun updateColumns(columns: Int) {
        userDataStore.updateColumns(columns = columns)
    }

    override suspend fun updatePageCount(pageCount: Int) {
        userDataStore.updatePageCount(pageCount = pageCount)
    }

    override suspend fun updateInfiniteScroll(infiniteScroll: Boolean) {
        userDataStore.updateInfiniteScroll(infiniteScroll = infiniteScroll)
    }

    override suspend fun updateDockRows(dockRows: Int) {
        userDataStore.updateDockRows(dockRows = dockRows)
    }

    override suspend fun updateDockColumns(dockColumns: Int) {
        userDataStore.updateDockColumns(dockColumns = dockColumns)
    }

    override suspend fun updateDockHeight(dockHeight: Int) {
        userDataStore.updateDockHeight(dockHeight = dockHeight)
    }

    override suspend fun updateInitialPage(initialPage: Int) {
        userDataStore.updateInitialPage(initialPage = initialPage)
    }

    override suspend fun updateSwipeUp(gestureAction: GestureAction) {
        userDataStore.updateSwipeUp(gestureAction = gestureAction)
    }

    override suspend fun updateSwipeDown(gestureAction: GestureAction) {
        userDataStore.updateSwipeDown(gestureAction = gestureAction)

    }
}