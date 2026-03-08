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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
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
import com.eblan.launcher.feature.home.model.FolderScreen
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.folderdrag.FolderDragScreen
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
    associate: Associate?,
    configureResultCode: Int?,
    currentPage: Int,
    dockGridHorizontalPagerState: PagerState,
    drag: Drag,
    dragIntOffset: IntOffset,
    folderGridHorizontalPagerState: PagerState,
    folderGridItem: GridItem?,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    gridHorizontalPagerState: PagerState,
    gridItemCache: GridItemCache,
    gridItemSource: GridItemSource?,
    hasShortcutHostPermission: Boolean,
    homeSettings: HomeSettings,
    iconPackFilePaths: Map<String, String>,
    lockMovement: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    paddingValues: PaddingValues,
    screen: Screen,
    screenHeight: Int,
    screenWidth: Int,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onDragCancelAfterMove: () -> Unit,
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
    onDragEndAfterMoveFolder: () -> Unit,
    onDragEndAfterMoveWidgetGridItem: (MoveGridItemResult) -> Unit,
    onMoveFolderGridItem: (
        folderGridItem: GridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (
        folderGridItem: GridItem,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
    ) -> Unit,
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
    onResetConfigureResultCode: () -> Unit,
    onShowFolderWhenDragging: (
        id: String,
        movingGridItem: GridItem,
        gridItemSource: GridItemSource,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateAssociate: (Associate) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
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

    var gridPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var dockPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var folderPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val dockHeight = homeSettings.dockHeight.dp

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            appWidgetManager = appWidgetManager,
            gridItemSource = gridItemSource,
            result = result,
            onDeleteAppWidgetId = {
                deleteAppWidgetId = true
            },
            onUpdateWidgetGridItem = { gridItem ->
                updatedWidgetGridItem = gridItem
            },
        )
    }

    val shortcutConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        scope.launch {
            handleShortcutConfigLauncherResult(
                gridItemSource = gridItemSource,
                imageSerializer = imageSerializer,
                moveGridItemResult = moveGridItemResult,
                result = result,
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
                fileManager = fileManager,
                gridItemSource = gridItemSource,
                imageSerializer = imageSerializer,
                launcherAppsWrapper = launcherApps,
                moveGridItemResult = moveGridItemResult,
                result = result,
                userManagerWrapper = userManager,
                onDeleteGridItemCache = onDeleteGridItemCache,
                onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
            )
        }
    }

    var folderTitleHeightPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = drag, key2 = dragIntOffset) {
        handleDragGridItem(
            columns = homeSettings.columns,
            currentPage = currentPage,
            density = density,
            dockColumns = homeSettings.dockColumns,
            dockHeight = dockHeight,
            dockRows = homeSettings.dockRows,
            drag = drag,
            dragIntOffset = dragIntOffset,
            folderCurrentPage = folderGridHorizontalPagerState.currentPage,
            folderGridItem = folderGridItem,
            folderPopupIntOffset = folderPopupIntOffset,
            folderPopupIntSize = folderPopupIntSize,
            folderTitleHeightPx = folderTitleHeightPx,
            gridItemSource = gridItemSource,
            isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
            lockMovement = lockMovement,
            paddingValues = paddingValues,
            rows = homeSettings.rows,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
            onMoveGridItem = onMoveGridItem,
            onUpdateAssociate = onUpdateAssociate,
            onUpdateGridItemSource = onUpdateGridItemSource,
        )
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.End -> {
                handleDropGridItem(
                    androidAppWidgetHostWrapper = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    gridItemSource = gridItemSource,
                    launcherAppsWrapper = launcherApps,
                    moveGridItemResult = moveGridItemResult,
                    userManagerWrapper = userManager,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onDragCancelAfterMove = onDragCancelAfterMove,
                    onDragEndAfterMove = onDragEndAfterMove,
                    onDragEndAfterMoveFolder = onDragEndAfterMoveFolder,
                    onLaunchShortcutConfigIntent = shortcutConfigLauncher::launch,
                    onLaunchShortcutConfigIntentSenderRequest = shortcutConfigIntentSenderLauncher::launch,
                    onLaunchWidgetIntent = appWidgetLauncher::launch,
                    onToast = {
                        Toast.makeText(
                            context,
                            "Layout was canceled due to an invalid position",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                    onUpdateAppWidgetId = { appWidgetId ->
                        lastAppWidgetId = appWidgetId
                    },
                    onUpdateWidgetGridItem = { gridItem ->
                        updatedWidgetGridItem = gridItem
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
            appWidgetId = lastAppWidgetId,
            deleteAppWidgetId = deleteAppWidgetId,
            gridItemSource = gridItemSource,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
        )
    }

    LaunchedEffect(key1 = updatedWidgetGridItem) {
        handleBoundWidget(
            activity = activity,
            androidAppWidgetHostWrapper = appWidgetHost,
            gridItemSource = gridItemSource,
            moveGridItemResult = moveGridItemResult,
            updatedWidgetGridItem = updatedWidgetGridItem,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
        )
    }

    LaunchedEffect(key1 = gridHorizontalPagerState) {
        handleWallpaperScroll(
            horizontalPagerState = gridHorizontalPagerState,
            infiniteScroll = homeSettings.infiniteScroll,
            pageCount = homeSettings.pageCount,
            wallpaperManagerWrapper = wallpaperManagerWrapper,
            wallpaperScroll = homeSettings.wallpaperScroll,
            windowToken = view.windowToken,
        )
    }

    LaunchedEffect(key1 = moveGridItemResult, key2 = drag) {
        handleConflictingGridItem(
            columns = homeSettings.columns,
            density = density,
            dockHeight = dockHeight,
            drag = drag,
            gridItemSource = gridItemSource,
            moveGridItemResult = moveGridItemResult,
            paddingValues = paddingValues,
            rows = homeSettings.rows,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            onShowFolderWhenDragging = onShowFolderWhenDragging,
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleAnimateScrollToPage(
            associate = associate,
            columns = homeSettings.columns,
            density = density,
            dragIntOffset = dragIntOffset,
            folderGridItem = folderGridItem,
            folderPopupIntOffset = folderPopupIntOffset,
            folderPopupIntSize = folderPopupIntSize,
            gridItemSource = gridItemSource,
            paddingValues = paddingValues,
            screenWidth = screenWidth,
            onUpdateDockPageDirection = { pageDirection ->
                dockPageDirection = pageDirection
            },
            onUpdateFolderPageDirection = { pageDirection ->
                folderPageDirection = pageDirection
            },
            onUpdateGridPageDirection = { pageDirection ->
                gridPageDirection = pageDirection
            },
        )
    }

    LaunchedEffect(key1 = configureResultCode) {
        handleConfigureLauncherResult(
            moveGridItemResult = moveGridItemResult,
            resultCode = configureResultCode,
            updatedGridItem = updatedWidgetGridItem,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
            onResetConfigureResultCode = onResetConfigureResultCode,
        )
    }

    LaunchedEffect(key1 = gridPageDirection) {
        handlePageDirection(
            pageDirection = gridPageDirection,
            pagerState = gridHorizontalPagerState,
        )
    }

    LaunchedEffect(key1 = dockPageDirection) {
        handlePageDirection(
            pageDirection = dockPageDirection,
            pagerState = dockGridHorizontalPagerState,
        )
    }

    LaunchedEffect(key1 = folderPageDirection) {
        handlePageDirection(
            pageDirection = folderPageDirection,
            pagerState = folderGridHorizontalPagerState,
        )
    }

    LaunchedEffect(key1 = gridItemSource) {
        when (gridItemSource) {
            is GridItemSource.Existing, is GridItemSource.New, is GridItemSource.Pin -> {
                onUpdateSharedElementKey(
                    SharedElementKey(
                        id = gridItemSource.gridItem.id,
                        screen = screen,
                    ),
                )
            }

            is GridItemSource.Folder -> {
                onUpdateSharedElementKey(
                    SharedElementKey(
                        id = gridItemSource.applicationInfoGridItem.id,
                        screen = FolderScreen.Drag,
                    ),
                )
            }
        }
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
                .height(PAGE_INDICATOR_HEIGHT)
                .fillMaxWidth(),
            gridHorizontalPagerState = gridHorizontalPagerState,
            infiniteScroll = homeSettings.infiniteScroll,
            pageCount = homeSettings.pageCount,
            color = getSystemTextColor(
                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                systemTextColor = textColor,
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

    if (folderGridItem != null) {
        FolderDragScreen(
            drag = drag,
            folderGridHorizontalPagerState = folderGridHorizontalPagerState,
            folderGridItem = folderGridItem,
            folderPopupIntOffset = folderPopupIntOffset,
            folderPopupIntSize = folderPopupIntSize,
            gridItemSettings = homeSettings.gridItemSettings,
            gridItemSource = gridItemSource,
            homeSettings = homeSettings,
            iconPackFilePaths = iconPackFilePaths,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            statusBarNotifications = statusBarNotifications,
            textColor = textColor,
            onUpdateFolderTitleHeight = { newFolderTitleHeightPx ->
                folderTitleHeightPx = newFolderTitleHeightPx
            },
        )
    }
}
