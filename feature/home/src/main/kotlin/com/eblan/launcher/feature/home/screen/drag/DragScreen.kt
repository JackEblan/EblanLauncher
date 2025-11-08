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
package com.eblan.launcher.feature.home.screen.drag

import android.appwidget.AppWidgetManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.pageindicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalWallpaperManager

@Composable
internal fun DragScreen(
    modifier: Modifier = Modifier,
    dragIntOffset: IntOffset,
    gridItemSource: GridItemSource?,
    gridItemCache: GridItemCache,
    drag: Drag,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    dockGridItemsCache: List<GridItem>,
    textColor: TextColor,
    moveGridItemResult: MoveGridItemResult?,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    hasShortcutHostPermission: Boolean,
    gridHorizontalPagerState: PagerState,
    currentPage: Int,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    statusBarNotifications: Map<String, Int>,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onDragCancelAfterMove: () -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onResetOverlay: () -> Unit,
) {
    requireNotNull(gridItemSource)

    val context = LocalContext.current

    val appWidgetHostWrapper = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val density = LocalDensity.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val packageManager = LocalPackageManager.current

    val view = LocalView.current

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var lastAppWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    var updatedGridItem by remember { mutableStateOf<GridItem?>(null) }

    val dockHeight = homeSettings.dockHeight.dp

    val gridHorizontalPagerPaddingDp = 50.dp

    val gridPaddingDp = 8.dp

    val gridPadding = with(density) {
        (gridHorizontalPagerPaddingDp + gridPaddingDp).roundToPx()
    }

    val pageIndicatorHeight = 20.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureResult(
            moveGridItemResult = moveGridItemResult,
            updatedGridItem = updatedGridItem,
            resultCode = result.resultCode,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMove = onDragEndAfterMove,
        )
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            result = result,
            gridItem = gridItemSource.gridItem,
            onUpdateGridItemDataCache = { gridItem ->
                updatedGridItem = gridItem

                onUpdateGridItemDataCache(gridItem)
            },
            onDeleteAppWidgetId = {
                deleteAppWidgetId = true
            },
        )
    }

    val lastMoveGridItemResult = remember(key1 = moveGridItemResult) {
        if (moveGridItemResult != null && moveGridItemResult.isSuccess) {
            moveGridItemResult
        } else if (gridItemSource.gridItem.startColumn > -1 && gridItemSource.gridItem.startRow > -1) {
            MoveGridItemResult(
                isSuccess = true,
                movingGridItem = gridItemSource.gridItem,
                conflictingGridItem = null,
            )
        } else {
            null
        }
    }

    LaunchedEffect(key1 = drag, key2 = dragIntOffset) {
        handleDragGridItem(
            density = density,
            currentPage = currentPage,
            drag = drag,
            gridItem = gridItemSource.gridItem,
            dragIntOffset = dragIntOffset,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            pageIndicatorHeight = pageIndicatorHeightPx,
            dockHeight = dockHeight,
            gridPadding = gridPadding,
            rows = homeSettings.rows,
            columns = homeSettings.columns,
            dockRows = homeSettings.dockRows,
            dockColumns = homeSettings.dockColumns,
            isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
            gridItemSource = gridItemSource,
            paddingValues = paddingValues,
            onUpdatePageDirection = { newPageDirection ->
                pageDirection = newPageDirection
            },
            onMoveGridItem = onMoveGridItem,
        )
    }

    LaunchedEffect(key1 = pageDirection) {
        handlePageDirection(
            currentPage = gridHorizontalPagerState.currentPage,
            pageDirection = pageDirection,
            onAnimateScrollToPage = { page ->
                gridHorizontalPagerState.animateScrollToPage(page = page)

                pageDirection = null
            },
        )
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.End -> {
                handleDropGridItem(
                    moveGridItemResult = moveGridItemResult,
                    androidAppWidgetHostWrapper = appWidgetHostWrapper,
                    appWidgetManager = appWidgetManager,
                    gridItemSource = gridItemSource,
                    onLaunch = appWidgetLauncher::launch,
                    onDragEndAfterMove = onDragEndAfterMove,
                    onDragCancelAfterMove = {
                        Toast.makeText(
                            context,
                            "Can't place grid item at this position",
                            Toast.LENGTH_LONG,
                        ).show()

                        onDragCancelAfterMove()
                    },
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = { gridItem ->
                        updatedGridItem = gridItem

                        onUpdateGridItemDataCache(gridItem)
                    },
                    onUpdateAppWidgetId = { appWidgetId ->
                        lastAppWidgetId = appWidgetId
                    },
                )

                onResetOverlay()
            }

            Drag.Cancel -> {
                onDragCancelAfterMove()

                onResetOverlay()
            }

            else -> Unit
        }
    }

    LaunchedEffect(key1 = deleteAppWidgetId) {
        handleDeleteAppWidgetId(
            gridItem = gridItemSource.gridItem,
            appWidgetId = lastAppWidgetId,
            deleteAppWidgetId = deleteAppWidgetId,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
        )
    }

    LaunchedEffect(key1 = updatedGridItem) {
        handleBoundWidget(
            gridItemSource = gridItemSource,
            updatedGridItem = updatedGridItem,
            moveGridItemResult = moveGridItemResult,
            packageManager = packageManager,
            onConfigure = configureLauncher::launch,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDragEndAfterMove = onDragEndAfterMove,
        )
    }

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
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
    ) {
        HorizontalPager(
            state = gridHorizontalPagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                top = gridHorizontalPagerPaddingDp,
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + gridHorizontalPagerPaddingDp,
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + gridHorizontalPagerPaddingDp,
                bottom = gridHorizontalPagerPaddingDp,
            ),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )

            GridLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(gridPaddingDp)
                    .background(
                        color = getSystemTextColor(textColor = textColor).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = getSystemTextColor(textColor = textColor),
                        shape = RoundedCornerShape(8.dp),
                    ),
                gridItems = gridItemCache.gridItemsCacheByPage[page],
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                { gridItem ->
                    val gridItemSettings = if (gridItem.override) {
                        gridItem.gridItemSettings
                    } else {
                        homeSettings.gridItemSettings
                    }.run {
                        copy(
                            iconSize = iconSize / 2,
                            textSize = textSize / 2,
                        )
                    }

                    val textColor = if (gridItem.override) {
                        getGridItemTextColor(
                            systemTextColor = textColor,
                            gridItemTextColor = gridItem.gridItemSettings.textColor,
                        )
                    } else {
                        getSystemTextColor(textColor = textColor)
                    }

                    GridItemContent(
                        gridItem = gridItem,
                        textColor = textColor,
                        gridItemSettings = gridItemSettings,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        isDragging = gridItem.id == gridItemSource.gridItem.id,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        statusBarNotifications = statusBarNotifications,
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .height(pageIndicatorHeight)
                .fillMaxWidth(),
            pageCount = homeSettings.pageCount,
            currentPage = currentPage,
        )

        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight)
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                ),
            gridItems = dockGridItemsCache,
            columns = homeSettings.dockColumns,
            rows = homeSettings.dockRows,
            { gridItem ->
                val gridItemSettings = if (gridItem.override) {
                    gridItem.gridItemSettings
                } else {
                    homeSettings.gridItemSettings
                }

                val textColor = if (gridItem.override) {
                    getGridItemTextColor(
                        systemTextColor = textColor,
                        gridItemTextColor = gridItem.gridItemSettings.textColor,
                    )
                } else {
                    getSystemTextColor(textColor = textColor)
                }

                GridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSettings = gridItemSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    isDragging = gridItem.id == gridItemSource.gridItem.id,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    statusBarNotifications = statusBarNotifications,
                )
            },
        )
    }

    AnimatedDropGridItem(
        currentPage = currentPage,
        gridPadding = gridPadding,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        dockHeight = dockHeight,
        pageIndicatorHeight = pageIndicatorHeightPx,
        paddingValues = paddingValues,
        columns = homeSettings.columns,
        rows = homeSettings.rows,
        dockColumns = homeSettings.dockColumns,
        dockRows = homeSettings.dockRows,
        overlayIntOffset = overlayIntOffset,
        overlayIntSize = overlayIntSize,
        textColor = textColor,
        iconPackInfoPackageName = iconPackInfoPackageName,
        hasShortcutHostPermission = hasShortcutHostPermission,
        gridItemSettings = homeSettings.gridItemSettings,
        drag = drag,
        moveGridItemResult = lastMoveGridItemResult,
        gridItemSource = gridItemSource,
        statusBarNotifications = statusBarNotifications,
    )
}
