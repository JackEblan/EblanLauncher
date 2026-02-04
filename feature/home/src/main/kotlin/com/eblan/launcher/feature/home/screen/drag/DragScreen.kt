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
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.DragScreen(
    modifier: Modifier = Modifier,
    dragIntOffset: IntOffset,
    gridItemSource: GridItemSource?,
    gridItemCache: GridItemCache,
    drag: Drag,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    textColor: TextColor,
    moveGridItemResult: MoveGridItemResult?,
    homeSettings: HomeSettings,
    gridHorizontalPagerState: PagerState,
    dockGridHorizontalPagerState: PagerState,
    currentPage: Int,
    statusBarNotifications: Map<String, Int>,
    hasShortcutHostPermission: Boolean,
    iconPackFilePaths: Map<String, String>,
    lockMovement: Boolean,
    screen: Screen,
    associate: Associate?,
    configureResultCode: Int?,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
    onDragEndAfterMoveWidgetGridItem: (MoveGridItemResult) -> Unit,
    onDragCancelAfterMove: () -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onUpdateShortcutConfigGridItemDataCache: (
        byteArray: ByteArray?,
        moveGridItemResult: MoveGridItemResult,
        gridItem: GridItem,
        data: GridItemData.ShortcutConfig,
    ) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
    onShowFolderWhenDragging: (String) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateAssociate: (Associate) -> Unit,
    onResetConfigureResultCode: () -> Unit,
) {
    requireNotNull(gridItemSource)

    val context = LocalContext.current

    val appWidgetManager = LocalAppWidgetManager.current

    val density = LocalDensity.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val userManager = LocalUserManager.current

    val launcherApps = LocalLauncherApps.current

    val imageSerializer = LocalImageSerializer.current

    val fileManager = LocalFileManager.current

    val view = LocalView.current

    val activity = LocalActivity.current

    val appWidgetHost = LocalAppWidgetHost.current

    val scope = rememberCoroutineScope()

    var lastAppWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    var updatedWidgetGridItem by remember { mutableStateOf<GridItem?>(null) }

    val dockHeight = homeSettings.dockHeight.dp

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.dp.roundToPx()
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            result = result,
            gridItem = gridItemSource.gridItem,
            appWidgetManager = appWidgetManager,
            onUpdateWidgetGridItem = { gridItem ->
                updatedWidgetGridItem = gridItem
            },
            onDeleteAppWidgetId = {
                deleteAppWidgetId = true
            },
        )
    }

    val shortcutConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        scope.launch {
            handleShortcutConfigLauncherResult(
                imageSerializer = imageSerializer,
                moveGridItemResult = moveGridItemResult,
                result = result,
                gridItemSource = gridItemSource,
                onDeleteGridItemCache = onDeleteGridItemCache,
                onUpdateShortcutConfigGridItemDataCache = onUpdateShortcutConfigGridItemDataCache,
            )
        }
    }

    val shortcutConfigIntentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        scope.launch {
            handleShortcutConfigIntentSenderLauncherResult(
                moveGridItemResult = moveGridItemResult,
                result = result,
                userManagerWrapper = userManager,
                launcherAppsWrapper = launcherApps,
                imageSerializer = imageSerializer,
                fileManager = fileManager,
                gridItemSource = gridItemSource,
                onDeleteGridItemCache = onDeleteGridItemCache,
                onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
            )
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
            rows = homeSettings.rows,
            columns = homeSettings.columns,
            dockRows = homeSettings.dockRows,
            dockColumns = homeSettings.dockColumns,
            isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
            gridItemSource = gridItemSource,
            paddingValues = paddingValues,
            lockMovement = lockMovement,
            screen = screen,
            onMoveGridItem = onMoveGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onUpdateAssociate = onUpdateAssociate,
        )
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.End -> {
                handleDropGridItem(
                    moveGridItemResult = moveGridItemResult,
                    androidAppWidgetHostWrapper = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    gridItemSource = gridItemSource,
                    userManagerWrapper = userManager,
                    launcherAppsWrapper = launcherApps,
                    onLaunchWidgetIntent = appWidgetLauncher::launch,
                    onLaunchShortcutConfigIntent = shortcutConfigLauncher::launch,
                    onLaunchShortcutConfigIntentSenderRequest = shortcutConfigIntentSenderLauncher::launch,
                    onDragEndAfterMove = onDragEndAfterMove,
                    onDragCancelAfterMove = onDragCancelAfterMove,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateWidgetGridItem = { gridItem ->
                        updatedWidgetGridItem = gridItem
                    },
                    onUpdateAppWidgetId = { appWidgetId ->
                        lastAppWidgetId = appWidgetId
                    },
                    onToast = {
                        Toast.makeText(
                            context,
                            "Layout was canceled due to an invalid position",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                )

                onResetConfigureResultCode()
            }

            Drag.Cancel -> {
                onDragCancelAfterMove()
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

    LaunchedEffect(key1 = updatedWidgetGridItem) {
        handleBoundWidget(
            gridItemSource = gridItemSource,
            updatedWidgetGridItem = updatedWidgetGridItem,
            moveGridItemResult = moveGridItemResult,
            androidAppWidgetHostWrapper = appWidgetHost,
            activity = activity,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
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

    LaunchedEffect(key1 = moveGridItemResult, key2 = drag) {
        handleConflictingGridItem(
            drag = drag,
            moveGridItemResult = moveGridItemResult,
            onShowFolderWhenDragging = onShowFolderWhenDragging,
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleAnimateScrollToPage(
            density = density,
            paddingValues = paddingValues,
            screenWidth = screenWidth,
            dragIntOffset = dragIntOffset,
            associate = associate,
            gridHorizontalPagerState = gridHorizontalPagerState,
            dockGridHorizontalPagerState = dockGridHorizontalPagerState,
        )
    }

    LaunchedEffect(key1 = configureResultCode) {
        handleConfigureLauncherResult(
            moveGridItemResult = moveGridItemResult,
            updatedGridItem = updatedWidgetGridItem,
            resultCode = configureResultCode,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
            onResetConfigureResultCode = onResetConfigureResultCode,
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
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
            userScrollEnabled = false,
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )

            GridLayout(
                modifier = Modifier.fillMaxSize(),
                gridItems = gridItemCache.gridItemsCacheByPage[page],
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                content = { gridItem ->
                    val isDragging =
                        (drag == Drag.Start || drag == Drag.Dragging) && gridItem.id == gridItemSource.gridItem.id

                    GridItemContent(
                        gridItem = gridItem,
                        textColor = textColor,
                        gridItemSettings = homeSettings.gridItemSettings,
                        isDragging = isDragging,
                        statusBarNotifications = statusBarNotifications,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        drag = drag,
                        iconPackFilePaths = iconPackFilePaths,
                        screen = screen,
                        isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .height(PAGE_INDICATOR_HEIGHT.dp)
                .fillMaxWidth(),
            gridHorizontalPagerState = gridHorizontalPagerState,
            infiniteScroll = homeSettings.infiniteScroll,
            pageCount = homeSettings.pageCount,
            color = getSystemTextColor(
                systemTextColor = textColor,
                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
            ),
        )

        HorizontalPager(
            state = dockGridHorizontalPagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight),
            contentPadding = PaddingValues(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = homeSettings.dockInfiniteScroll,
                pageCount = homeSettings.dockPageCount,
            )

            GridLayout(
                modifier = Modifier.fillMaxWidth(),
                gridItems = gridItemCache.dockGridItemsCache[page],
                columns = homeSettings.dockColumns,
                rows = homeSettings.dockRows,
                { gridItem ->
                    val isDragging =
                        (drag == Drag.Start || drag == Drag.Dragging) && gridItem.id == gridItemSource.gridItem.id

                    GridItemContent(
                        gridItem = gridItem,
                        textColor = textColor,
                        gridItemSettings = homeSettings.gridItemSettings,
                        isDragging = isDragging,
                        statusBarNotifications = statusBarNotifications,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        drag = drag,
                        iconPackFilePaths = iconPackFilePaths,
                        screen = screen,
                        isScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
                    )
                },
            )
        }
    }
}
