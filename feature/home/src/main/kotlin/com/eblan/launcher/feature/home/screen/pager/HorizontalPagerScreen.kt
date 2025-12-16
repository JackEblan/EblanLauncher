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
package com.eblan.launcher.feature.home.screen.pager

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.appwidget.AppWidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalWallpaperManager

@Composable
internal fun HorizontalPagerScreen(
    modifier: Modifier = Modifier,
    gridHorizontalPagerState: PagerState,
    currentPage: Int,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridWidth: Int,
    gridHeight: Int,
    paddingValues: PaddingValues,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    gridItemSource: GridItemSource?,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    statusBarNotifications: Map<String, Int>,
    eblanShortcutInfos: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gestureSettings: GestureSettings,
    swipeY: Float,
    screenHeight: Int,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    onTapFolderGridItem: (String) -> Unit,
    onEditGridItem: (String) -> Unit,
    onResize: () -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onShortcutConfigActivities: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetOverlay: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeight = homeSettings.dockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    var showGridItemPopup by remember { mutableStateOf(false) }

    var showSettingsPopup by remember { mutableStateOf(false) }

    var settingsPopupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    val launcherApps = LocalLauncherApps.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val context = LocalContext.current

    val view = LocalView.current

    var popupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupIntSize by remember { mutableStateOf(IntSize.Zero) }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val pageIndicatorHeight = 30.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    var eblanApplicationInfoGroup by remember { mutableStateOf<EblanApplicationInfoGroup?>(null) }

    LaunchedEffect(key1 = gridHorizontalPagerState) {
        handleWallpaperScroll(
            horizontalPagerState = gridHorizontalPagerState,
            wallpaperScroll = homeSettings.wallpaperScroll,
            wallpaperManagerWrapper = wallpaperManagerWrapper,
            pageCount = homeSettings.pageCount,
            infiniteScroll = homeSettings.infiniteScroll,
            windowToken = view.windowToken,
        )
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onLongPress = { offset ->
                        settingsPopupIntOffset = offset.round()

                        showSettingsPopup = true
                    },
                )
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
    ) {
        HorizontalPager(
            state = gridHorizontalPagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )

            GridLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    ),
                gridItems = gridItemsByPage[page],
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                { gridItem ->
                    val cellWidth = gridWidth / homeSettings.columns

                    val cellHeight =
                        (gridHeight - pageIndicatorHeightPx - dockHeightPx) / homeSettings.rows

                    val x = gridItem.startColumn * cellWidth

                    val y = gridItem.startRow * cellHeight

                    val width = gridItem.columnSpan * cellWidth

                    val height = gridItem.rowSpan * cellHeight

                    InteractiveGridItemContent(
                        gridItem = gridItem,
                        gridItemSettings = homeSettings.gridItemSettings,
                        textColor = textColor,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        drag = drag,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        statusBarNotifications = statusBarNotifications,
                        onTapApplicationInfo = { serialNumber, componentName ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + topPadding

                            launcherApps.startMainActivity(
                                serialNumber = serialNumber,
                                componentName = componentName,
                                sourceBounds = Rect(
                                    sourceBoundsX,
                                    sourceBoundsY,
                                    sourceBoundsX + width,
                                    sourceBoundsY + height,
                                ),
                            )
                        },
                        onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + topPadding

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                launcherApps.startShortcut(
                                    serialNumber = serialNumber,
                                    packageName = packageName,
                                    id = shortcutId,
                                    sourceBounds = Rect(
                                        sourceBoundsX,
                                        sourceBoundsY,
                                        sourceBoundsX + width,
                                        sourceBoundsY + height,
                                    ),
                                )
                            }
                        },
                        onTapShortcutConfig = { uri ->
                            context.startActivity(Intent.parseUri(uri, 0))
                        },
                        onTapFolderGridItem = {
                            onTapFolderGridItem(gridItem.id)
                        },
                        onUpdateGridItemOffset = { intOffset, intSize ->
                            popupIntOffset = intOffset

                            popupIntSize = intSize

                            onUpdateGridItemOffset(intOffset, intSize)

                            showGridItemPopup = true
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                GridItemSource.Existing(gridItem = gridItem),
                                imageBitmap,
                            )
                        },
                        onResetOverlay = onResetOverlay,
                        onDraggingGridItem = {
                            showGridItemPopup = false

                            onDraggingGridItem()
                        },
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(pageIndicatorHeight),
            pageCount = homeSettings.pageCount,
            currentPage = currentPage,
            pageOffset = gridHorizontalPagerState.currentPageOffsetFraction,
            color = getSystemTextColor(textColor = textColor),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight)
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                ),
        ) {
            GridLayout(
                modifier = Modifier.matchParentSize(),
                gridItems = dockGridItems,
                columns = homeSettings.dockColumns,
                rows = homeSettings.dockRows,
            ) { gridItem ->
                val cellWidth = gridWidth / homeSettings.dockColumns

                val cellHeight = dockHeightPx / homeSettings.dockRows

                val x = gridItem.startColumn * cellWidth

                val y = gridItem.startRow * cellHeight

                val width = gridItem.columnSpan * cellWidth

                val height = gridItem.rowSpan * cellHeight

                InteractiveGridItemContent(
                    gridItem = gridItem,
                    gridItemSettings = homeSettings.gridItemSettings,
                    textColor = textColor,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    drag = drag,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    statusBarNotifications = statusBarNotifications,
                    onTapApplicationInfo = { serialNumber, componentName ->
                        val sourceBoundsX = x + leftPadding

                        val sourceBoundsY = y + topPadding

                        launcherApps.startMainActivity(
                            serialNumber = serialNumber,
                            componentName = componentName,
                            sourceBounds = Rect(
                                sourceBoundsX,
                                sourceBoundsY,
                                sourceBoundsX + width,
                                sourceBoundsY + height,
                            ),
                        )
                    },
                    onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                        val sourceBoundsX = x + leftPadding

                        val sourceBoundsY = y + topPadding

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                            launcherApps.startShortcut(
                                serialNumber = serialNumber,
                                packageName = packageName,
                                id = shortcutId,
                                sourceBounds = Rect(
                                    sourceBoundsX,
                                    sourceBoundsY,
                                    sourceBoundsX + width,
                                    sourceBoundsY + height,
                                ),
                            )
                        }
                    },
                    onTapShortcutConfig = { uri ->
                        context.startActivity(Intent.parseUri(uri, 0))
                    },
                    onTapFolderGridItem = {
                        onTapFolderGridItem(gridItem.id)
                    },
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        popupIntOffset = intOffset

                        popupIntSize = intSize

                        onUpdateGridItemOffset(intOffset, intSize)

                        showGridItemPopup = true
                    },
                    onUpdateImageBitmap = { imageBitmap ->
                        onLongPressGridItem(
                            GridItemSource.Existing(gridItem = gridItem),
                            imageBitmap,
                        )
                    },
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = {
                        showGridItemPopup = false

                        onDraggingGridItem()
                    },
                )
            }

            Chevron(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.Center),
                gestureSettings = gestureSettings,
                gridItems = gridItems,
                dockGridItems = dockGridItems,
                swipeY = swipeY,
                screenHeight = screenHeight,
                textColor = textColor,
            )
        }
    }

    if (showGridItemPopup && gridItemSource?.gridItem != null) {
        GridItemPopup(
            gridItem = gridItemSource.gridItem,
            x = popupIntOffset.x,
            y = popupIntOffset.y,
            width = popupIntSize.width,
            height = popupIntSize.height,
            eblanShortcutInfos = eblanShortcutInfos,
            hasShortcutHostPermission = hasShortcutHostPermission,
            currentPage = currentPage,
            drag = drag,
            gridItemSettings = homeSettings.gridItemSettings,
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
            paddingValues = paddingValues,
            onEdit = onEditGridItem,
            onResize = onResize,
            onWidgets = { newEblanApplicationInfoGroup ->
                eblanApplicationInfoGroup = newEblanApplicationInfoGroup
            },
            onDeleteGridItem = onDeleteGridItem,
            onInfo = { serialNumber, componentName ->
                launcherApps.startAppDetailsActivity(
                    serialNumber = serialNumber,
                    componentName = componentName,
                    sourceBounds = Rect(
                        popupIntOffset.x,
                        popupIntOffset.y,
                        popupIntOffset.x + popupIntSize.width,
                        popupIntOffset.y + popupIntSize.height,
                    ),
                )
            },
            onDismissRequest = {
                showGridItemPopup = false
            },
            onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                val sourceBoundsX = popupIntOffset.x + leftPadding

                val sourceBoundsY = popupIntOffset.y + topPadding

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    launcherApps.startShortcut(
                        serialNumber = serialNumber,
                        packageName = packageName,
                        id = shortcutId,
                        sourceBounds = Rect(
                            sourceBoundsX,
                            sourceBoundsY,
                            sourceBoundsX + popupIntSize.width,
                            sourceBoundsY + popupIntSize.height,
                        ),
                    )
                }
            },
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onResetOverlay = onResetOverlay,
            onDraggingGridItem = onDraggingGridItem,
        )
    }

    if (showSettingsPopup) {
        SettingsPopup(
            popupSettingsIntOffset = settingsPopupIntOffset,
            gridItems = gridItems,
            hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
            onSettings = onSettings,
            onEditPage = onEditPage,
            onWidgets = onWidgets,
            onShortcutConfigActivities = onShortcutConfigActivities,
            onWallpaper = {
                val intent = Intent(Intent.ACTION_SET_WALLPAPER)

                val chooser = Intent.createChooser(intent, "Set Wallpaper")

                context.startActivity(chooser)
            },
            onDismissRequest = {
                showSettingsPopup = false
            },
        )
    }

    if (eblanApplicationInfoGroup != null) {
        AppWidgetScreen(
            currentPage = currentPage,
            eblanApplicationInfoGroup = eblanApplicationInfoGroup,
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            screenHeight = screenHeight,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDismiss = {
                eblanApplicationInfoGroup = null
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
        )
    }
}
