package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
) {
    operator fun invoke(): Flow<HomeData> {
        val gridItemsFlow = gridCacheRepository.isCache.flatMapLatest { isCache ->
            if (isCache) {
                gridCacheRepository.gridCacheItems
            } else {
                combine(
                    applicationInfoGridItemRepository.applicationInfoGridItems,
                    widgetGridItemRepository.widgetGridItems,
                    shortcutInfoGridItemRepository.shortcutInfoGridItems,
                    folderGridItemRepository.folderGridItems,
                ) { applicationInfoGridItems, widgetGridItems, shortcutInfoGridItems, folderGridItems ->
                    applicationInfoGridItems + widgetGridItems + shortcutInfoGridItems + folderGridItems
                }
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
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                ) && gridItem.associate == Associate.Grid
            }.groupBy { gridItem -> gridItem.page }

            val dockGridItemsWithinBounds = gridItems.filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.homeSettings.dockRows,
                    columns = userData.homeSettings.dockColumns,
                ) && gridItem.associate == Associate.Dock
            }

            val textColor = when (userData.homeSettings.textColor) {
                TextColor.System -> {
                    getTextColorFromWallpaperColors(colorHints = colorHints)
                }

                TextColor.Light -> {
                    LIGHT
                }

                TextColor.Dark -> {
                    DARK
                }
            }

            HomeData(
                userData = userData,
                gridItems = gridItems,
                gridItemsByPage = gridItemsSpanWithinBounds,
                dockGridItems = dockGridItemsWithinBounds,
                hasShortcutHostPermission = launcherAppsWrapper.hasShortcutHostPermission,
                textColor = textColor,
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun getTextColorFromWallpaperColors(colorHints: Int?): Long {
        if (colorHints == null) return LIGHT

        val hintSupportsDarkText =
            (colorHints and wallpaperManagerWrapper.hintSupportsDarkText) != 0

        return if (hintSupportsDarkText) {
            DARK
        } else {
            LIGHT
        }
    }
}

private const val LIGHT = 0xFFFFFFFF
private const val DARK = 0xFF000000