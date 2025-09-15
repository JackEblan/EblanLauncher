/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.data.repository

import com.eblan.launcher.data.datastore.UserDataStore
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.ThemeBrand
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

    override suspend fun updateDoubleTap(gestureAction: GestureAction) {
        userDataStore.updateDoubleTap(gestureAction = gestureAction)
    }

    override suspend fun updateSwipeUp(gestureAction: GestureAction) {
        userDataStore.updateSwipeUp(gestureAction = gestureAction)
    }

    override suspend fun updateSwipeDown(gestureAction: GestureAction) {
        userDataStore.updateSwipeDown(gestureAction = gestureAction)
    }

    override suspend fun updateTextColor(textColor: TextColor) {
        userDataStore.updateTextColor(textColor = textColor)
    }

    override suspend fun updateWallpaperScroll(wallpaperScroll: Boolean) {
        userDataStore.updateWallpaperScroll(wallpaperScroll = wallpaperScroll)
    }

    override suspend fun updateFolderRows(folderRows: Int) {
        userDataStore.updateFolderRows(folderRows = folderRows)
    }

    override suspend fun updateFolderColumns(folderColumns: Int) {
        userDataStore.updateFolderColumns(folderColumns = folderColumns)
    }

    override suspend fun updateAppDrawerColumns(appDrawerColumns: Int) {
        userDataStore.updateAppDrawerColumns(appDrawerColumns = appDrawerColumns)
    }

    override suspend fun updateAppDrawerRowsHeight(appDrawerRowsHeight: Int) {
        userDataStore.updateAppDrawerRowsHeight(appDrawerRowsHeight = appDrawerRowsHeight)
    }

    override suspend fun updateIconSize(iconSize: Int) {
        userDataStore.updateIconSize(iconSize = iconSize)
    }

    override suspend fun updateTextSize(textSize: Int) {
        userDataStore.updateTextSize(textSize = textSize)
    }

    override suspend fun updateShowLabel(showLabel: Boolean) {
        userDataStore.updateShowLabel(showLabel = showLabel)
    }

    override suspend fun updateSingleLineLabel(singleLineLabel: Boolean) {
        userDataStore.updateSingleLineLabel(singleLineLabel = singleLineLabel)
    }

    override suspend fun updateThemeBrand(themeBrand: ThemeBrand) {
        userDataStore.updateThemeBrand(themeBrand = themeBrand)
    }

    override suspend fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userDataStore.updateDarkThemeConfig(darkThemeConfig = darkThemeConfig)
    }

    override suspend fun updateDynamicTheme(dynamicTheme: Boolean) {
        userDataStore.updateDynamicTheme(dynamicTheme = dynamicTheme)
    }

    override suspend fun updateAppDrawerTextColor(textColor: TextColor) {
        userDataStore.updateAppDrawerTextColor(textColor = textColor)
    }

    override suspend fun updateAppDrawerIconSize(iconSize: Int) {
        userDataStore.updateAppDrawerIconSize(iconSize = iconSize)
    }

    override suspend fun updateAppDrawerTextSize(textSize: Int) {
        userDataStore.updateAppDrawerTextSize(textSize = textSize)
    }

    override suspend fun updateAppDrawerShowLabel(showLabel: Boolean) {
        userDataStore.updateAppDrawerShowLabel(showLabel = showLabel)
    }

    override suspend fun updateAppDrawerSingleLineLabel(singleLineLabel: Boolean) {
        userDataStore.updateAppDrawerSingleLineLabel(singleLineLabel = singleLineLabel)
    }

    override suspend fun updateIconPackPackageName(iconPackPackageName: String) {
        userDataStore.updateIconPackPackageName(iconPackPackageName = iconPackPackageName)
    }
}
