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
package com.eblan.launcher.feature.home

import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.IBinder
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfoApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.model.PopupGridItemType
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.editpage.EditPageScreen
import com.eblan.launcher.feature.home.screen.folder.FolderScreen
import com.eblan.launcher.feature.home.screen.folderdrag.FolderDragScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.resize.ResizeScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.drawable.AndroidDrawableWrapper
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.service.EblanNotificationListenerService
import com.eblan.launcher.ui.local.LocalDrawable
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditApplicationInfo: (Long, String) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    val eblanApplicationComponentUiState by viewModel.eblanApplicationComponentUiState.collectAsStateWithLifecycle()

    val movedGridItemResult by viewModel.movedGridItemResult.collectAsStateWithLifecycle()

    val pageItems by viewModel.pageItems.collectAsStateWithLifecycle()

    val folders by viewModel.foldersDataById.collectAsStateWithLifecycle()

    val eblanApplicationInfosByLabel by viewModel.eblanApplicationInfosByLabel.collectAsStateWithLifecycle()

    val eblanAppWidgetProviderInfosByLabel by viewModel.eblanAppWidgetProviderInfosByLabel.collectAsStateWithLifecycle()

    val gridItemsCache by viewModel.gridItemsCache.collectAsStateWithLifecycle()

    val pinGridItem by viewModel.pinGridItem.collectAsStateWithLifecycle()

    val popupGridItem by viewModel.popupGridItem.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        movedGridItemResult = movedGridItemResult,
        pageItems = pageItems,
        foldersDataById = folders,
        eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
        eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
        gridItemsCache = gridItemsCache,
        pinGridItem = pinGridItem,
        popupGridItemType = popupGridItem,
        onMoveGridItem = viewModel::moveGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onShowGridCache = viewModel::showGridCache,
        onShowFolderGridCache = viewModel::showFolderGridCache,
        onResetGridCacheAfterResize = viewModel::resetGridCacheAfterResize,
        onResetGridCacheAfterMove = viewModel::resetGridCacheAfterMove,
        onResetGridCacheAfterMoveFolder = viewModel::resetGridCacheAfterMoveFolder,
        onCancelGridCache = viewModel::cancelGridCache,
        onCancelFolderDragGridCache = viewModel::cancelFolderDragGridCache,
        onEditGridItem = onEditGridItem,
        onSettings = onSettings,
        onEditPage = viewModel::showPageCache,
        onSaveEditPage = viewModel::saveEditPage,
        onUpdateScreen = viewModel::updateScreen,
        onDeleteGridItemCache = viewModel::deleteGridItemCache,
        onUpdateGridItemDataCache = viewModel::updateGridItemDataCache,
        onDeleteWidgetGridItemCache = viewModel::deleteWidgetGridItemCache,
        onShowFolder = viewModel::showFolder,
        onRemoveLastFolder = viewModel::removeLastFolder,
        onAddFolder = viewModel::addFolder,
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
        onGetEblanAppWidgetProviderInfosByLabel = viewModel::getEblanAppWidgetProviderInfosByLabel,
        onDeleteGridItem = viewModel::deleteGridItem,
        onGetPinGridItem = viewModel::getPinGridItem,
        onResetPinGridItem = viewModel::resetPinGridItem,
        onUpdateApplicationInfoPopupGridItem = viewModel::updateApplicationInfoPopupGridItem,
        onUpdatePopupGridItem = viewModel::updatePopupGridItem,
        onEditApplicationInfo = onEditApplicationInfo,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    movedGridItemResult: MoveGridItemResult?,
    pageItems: List<PageItem>,
    foldersDataById: ArrayDeque<FolderDataById>,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    gridItemsCache: GridItemCache,
    pinGridItem: GridItem?,
    popupGridItemType: PopupGridItemType?,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onShowGridCache: (
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onShowFolderGridCache: (
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCache: () -> Unit,
    onCancelFolderDragGridCache: () -> Unit,
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onShowFolder: (String) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onResetPinGridItem: () -> Unit,
    onUpdateApplicationInfoPopupGridItem: (
        showPopupGridItemMenu: Boolean,
        packageName: String?,
        serialNumber: Long,
        componentName: String?,
    ) -> Unit,
    onUpdatePopupGridItem: (PopupGridItemType?) -> Unit,
    onEditApplicationInfo: (Long, String) -> Unit,
) {
    val context = LocalContext.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntSize by remember { mutableStateOf(IntSize.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    val launcherApps = LocalLauncherApps.current

    val drawable = LocalDrawable.current

    val userManager = LocalUserManager.current

    val scope = rememberCoroutineScope()

    var statusBarNotifications by remember {
        mutableStateOf<Map<String, Int>>(emptyMap())
    }

    val target = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                val offset = with(event.toAndroidDragEvent()) {
                    IntOffset(x = x.roundToInt(), y = y.roundToInt())
                }

                drag = Drag.Start

                dragIntOffset = offset

                val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

                scope.launch {
                    handlePinItemRequest(
                        pinItemRequest = pinItemRequest,
                        context = context,
                        launcherAppsWrapper = launcherApps,
                        drawable = drawable,
                        userManager = userManager,
                        onGetPinGridItem = onGetPinGridItem,
                    )
                }
            }

            override fun onEnded(event: DragAndDropEvent) {
                drag = Drag.End

                val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

                if (pinItemRequest != null) {
                    onResetPinGridItem()

                    pinItemRequestWrapper.updatePinItemRequest(null)
                }
            }

            override fun onMoved(event: DragAndDropEvent) {
                val offset = with(event.toAndroidDragEvent()) {
                    IntOffset(x = x.roundToInt(), y = y.roundToInt())
                }

                drag = Drag.Dragging

                dragIntOffset = offset
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                return true
            }
        }
    }

    DisposableEffect(key1 = scope) {
        val connection = object : ServiceConnection {
            private var listener: EblanNotificationListenerService? = null

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                listener = (service as EblanNotificationListenerService.LocalBinder).getService()

                scope.launch {
                    listener?.statusBarNotifications?.collect { statusBarNotifications = it }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                listener = null
            }
        }

        val intent = Intent(context, EblanNotificationListenerService::class.java)

        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(connection)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        drag = Drag.Start

                        dragIntOffset = offset.round()
                    },
                    onDragEnd = {
                        drag = Drag.End
                    },
                    onDragCancel = {
                        drag = Drag.Cancel
                    },
                    onDrag = { _, dragAmount ->
                        drag = Drag.Dragging

                        dragIntOffset += dragAmount.round()

                        overlayIntOffset += dragAmount.round()
                    },
                )
            }
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = target,
            )
            .fillMaxSize(),
    ) {
        when (homeUiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is HomeUiState.Success -> {
                Success(
                    screen = screen,
                    homeData = homeUiState.homeData,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    pageItems = pageItems,
                    movedGridItemResult = movedGridItemResult,
                    screenWidth = this@BoxWithConstraints.constraints.maxWidth,
                    screenHeight = this@BoxWithConstraints.constraints.maxHeight,
                    paddingValues = paddingValues,
                    dragIntOffset = dragIntOffset,
                    drag = drag,
                    foldersDataById = foldersDataById,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
                    gridItemCache = gridItemsCache,
                    pinGridItem = pinGridItem,
                    overlayIntOffset = overlayIntOffset,
                    overlayIntSize = overlayIntSize,
                    popupGridItemType = popupGridItemType,
                    statusBarNotifications = statusBarNotifications,
                    onMoveGridItem = onMoveGridItem,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onResizeGridItem = onResizeGridItem,
                    onShowGridCache = onShowGridCache,
                    onShowFolderGridCache = onShowFolderGridCache,
                    onResetGridCacheAfterResize = onResetGridCacheAfterResize,
                    onResetGridCacheAfterMove = onResetGridCacheAfterMove,
                    onCancelGridCache = onCancelGridCache,
                    onCancelFolderDragGridCache = onCancelFolderDragGridCache,
                    onEditGridItem = onEditGridItem,
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onShowFolder = onShowFolder,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetGridCacheAfterMoveFolder = onResetGridCacheAfterMoveFolder,
                    onUpdateGridItemImageBitmap = { imageBitmap ->
                        overlayImageBitmap = imageBitmap
                    },
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        overlayIntOffset = intOffset

                        overlayIntSize = intSize
                    },
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onDeleteGridItem = onDeleteGridItem,
                    onResetOverlay = {
                        overlayIntOffset = IntOffset.Zero

                        overlayIntSize = IntSize.Zero

                        overlayImageBitmap = null
                    },
                    onUpdateApplicationInfoPopupGridItem = onUpdateApplicationInfoPopupGridItem,
                    onUpdatePopupGridItem = onUpdatePopupGridItem,
                    onEditApplicationInfo = onEditApplicationInfo,
                )
            }
        }

        OverlayImage(
            overlayIntOffset = overlayIntOffset,
            overlayIntSize = overlayIntSize,
            overlayImageBitmap = overlayImageBitmap,
        )
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeData: HomeData,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    pageItems: List<PageItem>,
    movedGridItemResult: MoveGridItemResult?,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    dragIntOffset: IntOffset,
    drag: Drag,
    foldersDataById: ArrayDeque<FolderDataById>,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    gridItemCache: GridItemCache,
    pinGridItem: GridItem?,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    popupGridItemType: PopupGridItemType?,
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
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onShowGridCache: (
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onShowFolderGridCache: (
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCache: () -> Unit,
    onCancelFolderDragGridCache: () -> Unit,
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onShowFolder: (String) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onUpdateGridItemImageBitmap: (ImageBitmap?) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetOverlay: () -> Unit,
    onUpdateApplicationInfoPopupGridItem: (
        showPopupGridItemMenu: Boolean,
        packageName: String?,
        serialNumber: Long,
        componentName: String?,
    ) -> Unit,
    onUpdatePopupGridItem: (PopupGridItemType?) -> Unit,
    onEditApplicationInfo: (Long, String) -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    val gridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeData.userData.homeSettings.infiniteScroll) {
            (Int.MAX_VALUE / 2) + homeData.userData.homeSettings.initialPage
        } else {
            homeData.userData.homeSettings.initialPage
        },
        pageCount = {
            if (homeData.userData.homeSettings.infiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeData.userData.homeSettings.pageCount
            }
        },
    )

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            foldersDataById.lastOrNull()?.pageCount ?: 0
        },
    )

    val currentPage by remember(
        key1 = gridHorizontalPagerState,
        key2 = homeData.userData.homeSettings,
    ) {
        derivedStateOf {
            calculatePage(
                index = gridHorizontalPagerState.currentPage,
                infiniteScroll = homeData.userData.homeSettings.infiniteScroll,
                pageCount = homeData.userData.homeSettings.pageCount,
            )
        }
    }

    LaunchedEffect(key1 = pinGridItem) {
        val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

        if (pinGridItem != null && pinItemRequest != null) {
            gridItemSource = GridItemSource.Pin(
                gridItem = pinGridItem,
                pinItemRequest = pinItemRequest,
            )

            onShowGridCache(
                homeData.gridItems,
                Screen.Drag,
            )
        }
    }

    AnimatedContent(
        modifier = modifier,
        targetState = screen,
    ) { targetState ->
        when (targetState) {
            Screen.Pager -> {
                PagerScreen(
                    gridItems = homeData.gridItems,
                    gridItemsByPage = homeData.gridItemsByPage,
                    drag = drag,
                    dockGridItems = homeData.dockGridItems,
                    textColor = homeData.textColor,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    appDrawerSettings = homeData.userData.appDrawerSettings,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    hasSystemFeatureAppWidgets = homeData.hasSystemFeatureAppWidgets,
                    gestureSettings = homeData.userData.gestureSettings,
                    gridItemSource = gridItemSource,
                    homeSettings = homeData.userData.homeSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    currentPage = currentPage,
                    popupGridItemType = popupGridItemType,
                    statusBarNotifications = statusBarNotifications,
                    onTapFolderGridItem = onShowFolder,
                    onDraggingGridItem = {
                        onShowGridCache(
                            homeData.gridItems,
                            Screen.Drag,
                        )
                    },
                    onEditGridItem = onEditGridItem,
                    onResize = {
                        onShowGridCache(
                            homeData.gridItems,
                            Screen.Resize,
                        )
                    },
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onLongPressGridItem = { newGridItemSource, imageBitmap ->
                        gridItemSource = newGridItemSource

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onDeleteGridItem = onDeleteGridItem,
                    onResetOverlay = onResetOverlay,
                    onUpdateApplicationInfoPopupGridItem = onUpdateApplicationInfoPopupGridItem,
                    onUpdatePopupGridItem = onUpdatePopupGridItem,
                    onEditApplicationInfo = onEditApplicationInfo,
                )
            }

            Screen.Drag -> {
                DragScreen(
                    gridItemCache = gridItemCache,
                    dragIntOffset = dragIntOffset,
                    gridItemSource = gridItemSource,
                    drag = drag,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    dockGridItemsCache = gridItemCache.dockGridItemsCache,
                    textColor = homeData.textColor,
                    moveGridItemResult = movedGridItemResult,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    currentPage = currentPage,
                    overlayIntOffset = overlayIntOffset,
                    overlayIntSize = overlayIntSize,
                    statusBarNotifications = statusBarNotifications,
                    onMoveGridItem = onMoveGridItem,
                    onDragEndAfterMove = onResetGridCacheAfterMove,
                    onDragCancelAfterMove = onCancelGridCache,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onResetOverlay = onResetOverlay,
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    currentPage = currentPage,
                    gridItemCache = gridItemCache,
                    gridItem = gridItemSource?.gridItem,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    dockGridItemsCache = gridItemCache.dockGridItemsCache,
                    textColor = homeData.textColor,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    statusBarNotifications = statusBarNotifications,
                    onResizeGridItem = onResizeGridItem,
                    onResizeEnd = onResetGridCacheAfterResize,
                    onResizeCancel = onCancelGridCache,
                )
            }

            Screen.Loading -> {
                LoadingScreen()
            }

            Screen.EditPage -> {
                EditPageScreen(
                    screenHeight = screenHeight,
                    pageItems = pageItems,
                    textColor = homeData.textColor,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                )
            }

            Screen.Folder -> {
                FolderScreen(
                    foldersDataById = foldersDataById,
                    drag = drag,
                    paddingValues = paddingValues,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    statusBarNotifications = statusBarNotifications,
                    onUpdateScreen = onUpdateScreen,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onLongPressGridItem = { newGridItemSource, imageBitmap ->
                        gridItemSource = newGridItemSource

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onDraggingGridItem = { folderGridItems ->
                        onShowFolderGridCache(
                            folderGridItems,
                            Screen.FolderDrag,
                        )
                    },
                    onResetOverlay = onResetOverlay,
                )
            }

            Screen.FolderDrag -> {
                FolderDragScreen(
                    gridItemCache = gridItemCache,
                    gridItemSource = gridItemSource,
                    textColor = homeData.textColor,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    folderDataById = foldersDataById.lastOrNull(),
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    moveGridItemResult = movedGridItemResult,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    overlayIntOffset = overlayIntOffset,
                    overlayIntSize = overlayIntSize,
                    statusBarNotifications = statusBarNotifications,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onDragEnd = onResetGridCacheAfterMoveFolder,
                    onDragCancel = onCancelFolderDragGridCache,
                    onMoveOutsideFolder = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        onShowGridCache(
                            homeData.gridItems,
                            Screen.Drag,
                        )
                    },
                    onResetOverlay = onResetOverlay,
                )
            }
        }
    }
}

@Composable
private fun OverlayImage(
    modifier: Modifier = Modifier,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    overlayImageBitmap: ImageBitmap?,
) {
    val density = LocalDensity.current

    val size = with(density) {
        DpSize(width = overlayIntSize.width.toDp(), height = overlayIntSize.height.toDp())
    }

    if (overlayImageBitmap != null) {
        Image(
            modifier = modifier
                .offset {
                    overlayIntOffset
                }
                .size(size),
            bitmap = overlayImageBitmap,
            contentDescription = null,
        )
    }
}

private suspend fun handlePinItemRequest(
    pinItemRequest: PinItemRequest?,
    context: Context,
    launcherAppsWrapper: AndroidLauncherAppsWrapper,
    drawable: AndroidDrawableWrapper,
    userManager: AndroidUserManagerWrapper,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null) {
        when (pinItemRequest.requestType) {
            PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                val appWidgetProviderInfo =
                    pinItemRequest.getAppWidgetProviderInfo(context)

                if (appWidgetProviderInfo != null) {
                    val preview = appWidgetProviderInfo.loadPreviewImage(context, 0)?.let {
                        drawable.createByteArray(drawable = it)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        onGetPinGridItem(
                            PinItemRequestType.Widget(
                                appWidgetId = 0,
                                className = appWidgetProviderInfo.provider.className,
                                componentName = appWidgetProviderInfo.provider.flattenToString(),
                                packageName = appWidgetProviderInfo.provider.packageName,
                                serialNumber = userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile),
                                configure = appWidgetProviderInfo.configure.flattenToString(),
                                minWidth = appWidgetProviderInfo.minWidth,
                                minHeight = appWidgetProviderInfo.minHeight,
                                resizeMode = appWidgetProviderInfo.resizeMode,
                                minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                                minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                                maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                                maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                                targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                                targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                                preview = preview
                            ),
                        )
                    } else {
                        onGetPinGridItem(
                            PinItemRequestType.Widget(
                                appWidgetId = 0,
                                className = appWidgetProviderInfo.provider.className,
                                componentName = appWidgetProviderInfo.provider.flattenToString(),
                                packageName = appWidgetProviderInfo.provider.packageName,
                                serialNumber = userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile),
                                configure = appWidgetProviderInfo.configure.flattenToString(),
                                minWidth = appWidgetProviderInfo.minWidth,
                                minHeight = appWidgetProviderInfo.minHeight,
                                resizeMode = appWidgetProviderInfo.resizeMode,
                                minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                                minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                                maxResizeWidth = 0,
                                maxResizeHeight = 0,
                                targetCellHeight = 0,
                                targetCellWidth = 0,
                                preview = preview
                            ),
                        )
                    }
                }
            }

            PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                val shortcutInfo = pinItemRequest.shortcutInfo

                if (shortcutInfo != null) {
                    onGetPinGridItem(
                        PinItemRequestType.ShortcutInfo(
                            serialNumber = userManager.getSerialNumberForUser(userHandle = shortcutInfo.userHandle),
                            shortcutId = shortcutInfo.id,
                            packageName = shortcutInfo.`package`,
                            shortLabel = shortcutInfo.shortLabel.toString(),
                            longLabel = shortcutInfo.longLabel.toString(),
                            isEnabled = shortcutInfo.isEnabled,
                            disabledMessage = shortcutInfo.disabledMessage?.toString(),
                            icon = launcherAppsWrapper.getShortcutIconDrawable(
                                shortcutInfo = shortcutInfo,
                                density = 0,
                            )?.let {
                                drawable.createByteArray(drawable = it)
                            },
                        ),
                    )
                }
            }
        }
    }
}
