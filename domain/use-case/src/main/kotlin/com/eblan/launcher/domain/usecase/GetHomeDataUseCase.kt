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
package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val resourcesWrapper: ResourcesWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<HomeData> {
        val gridItemsFlow = combine(
            applicationInfoGridItemRepository.applicationInfoGridItems,
            widgetGridItemRepository.widgetGridItems,
            shortcutInfoGridItemRepository.shortcutInfoGridItems,
            folderGridItemRepository.folderGridItems,
        ) { applicationInfoGridItems, widgetGridItems, shortcutInfoGridItems, folderGridItems ->
            (applicationInfoGridItems + widgetGridItems + shortcutInfoGridItems + folderGridItems)
                .filterNot { gridItem ->
                    gridItem.folderId != null
                }
        }

        return combine(
            userDataRepository.userData,
            gridItemsFlow,
            wallpaperManagerWrapper.getColorsChanged(),
        ) { userData, gridItems, colorHints ->
            val gridItemsSpanWithinBounds = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = userData.homeSettings.columns,
                    rows = userData.homeSettings.rows,
                ) && gridItem.associate == Associate.Grid
            }.groupBy { gridItem -> gridItem.page }

            val dockGridItemsWithinBounds = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = userData.homeSettings.dockColumns,
                    rows = userData.homeSettings.dockRows,
                ) && gridItem.associate == Associate.Dock
            }

            val gridItemSettings = userData.homeSettings.gridItemSettings

            val textColor = when (gridItemSettings.textColor) {
                TextColor.System -> {
                    getTextColorFromWallpaperColors(
                        darkThemeConfig = userData.generalSettings.darkThemeConfig,
                        colorHints = colorHints,
                    )
                }

                else -> gridItemSettings.textColor
            }

            HomeData(
                userData = userData,
                gridItems = gridItems,
                gridItemsByPage = gridItemsSpanWithinBounds,
                dockGridItems = dockGridItemsWithinBounds,
                hasShortcutHostPermission = launcherAppsWrapper.hasShortcutHostPermission,
                hasSystemFeatureAppWidgets = packageManagerWrapper.hasSystemFeatureAppWidgets,
                textColor = textColor,
            )
        }.flowOn(defaultDispatcher)
    }

    private fun getTextColorFromWallpaperColors(
        darkThemeConfig: DarkThemeConfig,
        colorHints: Int?,
    ): TextColor {
        return if (colorHints != null) {
            val hintSupportsDarkText =
                colorHints.and(wallpaperManagerWrapper.hintSupportsDarkText) != 0

            if (hintSupportsDarkText) {
                TextColor.Dark
            } else {
                TextColor.Light
            }
        } else {
            getTextColorFromSystemTheme(darkThemeConfig = darkThemeConfig)
        }
    }

    private fun getTextColorFromSystemTheme(darkThemeConfig: DarkThemeConfig): TextColor {
        return when (darkThemeConfig) {
            DarkThemeConfig.System -> {
                getTextColorFromSystemTheme(darkThemeConfig = resourcesWrapper.getSystemTheme())
            }

            DarkThemeConfig.Light -> {
                TextColor.Light
            }

            DarkThemeConfig.Dark -> {
                TextColor.Dark
            }
        }
    }
}
