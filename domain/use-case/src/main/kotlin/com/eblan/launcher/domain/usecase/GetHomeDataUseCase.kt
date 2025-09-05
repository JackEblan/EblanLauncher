package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val resourcesWrapper: ResourcesWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
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
                    (applicationInfoGridItems + widgetGridItems + shortcutInfoGridItems + folderGridItems)
                        .filterNot { gridItem ->
                            gridItem.folderId != null
                        }
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
            getTextColorFromSystemTheme(darkThemeConfig)
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