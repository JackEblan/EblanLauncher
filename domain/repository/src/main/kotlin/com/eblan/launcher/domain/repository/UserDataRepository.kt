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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.ThemeBrand
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

    suspend fun updateThemeBrand(themeBrand: ThemeBrand)

    suspend fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    suspend fun updateDynamicTheme(dynamicTheme: Boolean)

    suspend fun updateAppDrawerTextColor(textColor: TextColor)

    suspend fun updateAppDrawerIconSize(iconSize: Int)

    suspend fun updateAppDrawerTextSize(textSize: Int)

    suspend fun updateAppDrawerShowLabel(showLabel: Boolean)

    suspend fun updateAppDrawerSingleLineLabel(singleLineLabel: Boolean)

    suspend fun updateIconPackInfoPackageName(iconPackInfoPackageName: String)
}
