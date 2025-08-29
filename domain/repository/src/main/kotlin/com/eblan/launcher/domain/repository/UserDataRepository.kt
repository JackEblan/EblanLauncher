package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.TextColor
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

    suspend fun updateDoubleTap(gestureAction: GestureAction)

    suspend fun updateSwipeUp(gestureAction: GestureAction)

    suspend fun updateSwipeDown(gestureAction: GestureAction)

    suspend fun updateTextColor(textColor: TextColor)

    suspend fun updateWallpaperScroll(wallpaperScroll: Boolean)

    suspend fun updateFolderRows(folderRows: Int)

    suspend fun updateFolderColumns(folderColumns: Int)

    suspend fun updateAppDrawerColumns(appDrawerColumns: Int)

    suspend fun updateAppDrawerRowsHeight(appDrawerRowsHeight: Int)

    suspend fun updateIconSize(iconSize: Int)

    suspend fun updateTextSize(textSize: Int)

    suspend fun updateShowLabel(showLabel: Boolean)

    suspend fun updateSingleLineLabel(singleLineLabel: Boolean)
}