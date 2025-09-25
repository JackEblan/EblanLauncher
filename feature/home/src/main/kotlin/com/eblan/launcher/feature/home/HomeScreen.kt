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
import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.GridItemCacheType
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.PinItemRequestType
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
import com.eblan.launcher.ui.local.LocalPinItemRequest
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    val eblanApplicationComponentUiState by viewModel.eblanApplicationComponentUiState.collectAsStateWithLifecycle()

    val movedGridItemResult by viewModel.movedGridItemResult.collectAsStateWithLifecycle()

    val pageItems by viewModel.pageItems.collectAsStateWithLifecycle()

    val folders by viewModel.foldersDataById.collectAsStateWithLifecycle()

    val eblanApplicationInfosByLabel by viewModel.eblanApplicationInfosByLabel.collectAsStateWithLifecycle()

    val eblanAppWidgetProviderInfosByLabel by viewModel.eblanAppWidgetProviderInfosByLabel.collectAsStateWithLifecycle()

    val eblanShortcutInfosByLabel by viewModel.eblanShortcutInfosByLabel.collectAsStateWithLifecycle()

    val gridItemsCache by viewModel.gridItemsCache.collectAsStateWithLifecycle()

    val pinGridItem by viewModel.pinGridItem.collectAsStateWithLifecycle()

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
        eblanShortcutInfosByLabel = eblanShortcutInfosByLabel,
        gridItemsCache = gridItemsCache,
        pinGridItem = pinGridItem,
        onMoveGridItem = viewModel::moveGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onShowGridCache = viewModel::showGridCache,
        onResetGridCacheAfterResize = viewModel::resetGridCacheAfterResize,
        onResetGridCacheAfterMove = viewModel::resetGridCacheAfterMove,
        onResetGridCacheAfterMoveFolder = viewModel::resetGridCacheAfterMoveFolder,
        onCancelGridCache = viewModel::cancelGridCache,
        onEdit = onEdit,
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
        onMoveOutsideFolder = viewModel::moveGridItemOutsideFolder,
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
        onGetEblanAppWidgetProviderInfosByLabel = viewModel::getEblanAppWidgetProviderInfosByLabel,
        onGetEblanShortcutInfosByLabel = viewModel::getEblanShortcutInfosByLabel,
        onDeleteGridItem = viewModel::deleteGridItem,
        onGetPinGridItem = viewModel::getPinGridItem,
        onResetPinGridItem = viewModel::resetPinGridItem,
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
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosByLabel: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    gridItemsCache: GridItemCache,
    pinGridItem: GridItem?,
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
        gridItemCacheType: GridItemCacheType,
        screen: Screen,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCache: () -> Unit,
    onEdit: (String) -> Unit,
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
    onMoveOutsideFolder: (List<GridItem>) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanShortcutInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onResetPinGridItem: () -> Unit,
) {
    val context = LocalContext.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    val target = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                val offset = with(event.toAndroidDragEvent()) {
                    IntOffset(x = x.roundToInt(), y = y.roundToInt())
                }

                drag = Drag.Start

                dragIntOffset = offset

                val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

                handlePinItemRequest(
                    pinItemRequest = pinItemRequest,
                    context = context,
                    onGetPinGridItem = onGetPinGridItem,
                )
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
                    eblanShortcutInfosByLabel = eblanShortcutInfosByLabel,
                    gridItemsCache = gridItemsCache,
                    pinGridItem = pinGridItem,
                    onMoveGridItem = onMoveGridItem,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onResizeGridItem = onResizeGridItem,
                    onShowGridCache = onShowGridCache,
                    onResetGridCacheAfterResize = onResetGridCacheAfterResize,
                    onResetGridCacheAfterMove = onResetGridCacheAfterMove,
                    onCancelGridCache = onCancelGridCache,
                    onEdit = onEdit,
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
                    onMoveOutsideFolder = onMoveOutsideFolder,
                    onUpdateGridItemImageBitmap = { imageBitmap ->
                        overlayImageBitmap = imageBitmap
                    },
                    onUpdateGridItemOffset = { intOffset ->
                        overlayIntOffset = intOffset
                    },
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanShortcutInfosByLabel = onGetEblanShortcutInfosByLabel,
                    onDeleteGridItem = onDeleteGridItem,
                )
            }
        }

        OverlayImage(
            drag = drag,
            overlayIntOffset = overlayIntOffset,
            overlayImageBitmap = overlayImageBitmap,
            onResetOverlay = {
                overlayImageBitmap = null
            },
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
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosByLabel: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    gridItemsCache: GridItemCache,
    pinGridItem: GridItem?,
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
        gridItemCacheType: GridItemCacheType,
        screen: Screen,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCache: () -> Unit,
    onEdit: (String) -> Unit,
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
    onMoveOutsideFolder: (List<GridItem>) -> Unit,
    onUpdateGridItemImageBitmap: (ImageBitmap?) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanShortcutInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val pinItemRequestWrapper = LocalPinItemRequest.current

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var targetPage by remember {
        mutableIntStateOf(
            homeData.userData.homeSettings.initialPage,
        )
    }

    var folderTargetPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = pinGridItem) {
        val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

        if (pinGridItem != null && pinItemRequest != null) {
            gridItemSource = GridItemSource.Pin(
                gridItem = pinGridItem,
                pinItemRequest = pinItemRequest,
            )

            onShowGridCache(
                homeData.gridItems,
                GridItemCacheType.Grid,
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
                    targetPage = targetPage,
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
                    gestureSettings = homeData.userData.gestureSettings,
                    gridItemSource = gridItemSource,
                    homeSettings = homeData.userData.homeSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
                    eblanShortcutInfosByLabel = eblanShortcutInfosByLabel,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    onLongPressGrid = { newCurrentPage ->
                        targetPage = newCurrentPage
                    },
                    onTapFolderGridItem = { newCurrentPage, id ->
                        targetPage = newCurrentPage

                        onShowFolder(id)
                    },
                    onDraggingGridItem = {
                        onShowGridCache(
                            homeData.gridItems,
                            GridItemCacheType.Grid,
                            Screen.Drag,
                        )
                    },
                    onEdit = onEdit,
                    onResize = { newTargetPage ->
                        targetPage = newTargetPage

                        onShowGridCache(
                            homeData.gridItems,
                            GridItemCacheType.Grid,
                            Screen.Resize,
                        )
                    },
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap ->
                        targetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanShortcutInfosByLabel = onGetEblanShortcutInfosByLabel,
                    onDeleteGridItem = onDeleteGridItem,
                )
            }

            Screen.Drag -> {
                DragScreen(
                    startCurrentPage = targetPage,
                    gridItemsCacheByPage = gridItemsCache.gridItemsCacheByPage,
                    dragIntOffset = dragIntOffset,
                    gridItemSource = gridItemSource,
                    drag = drag,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    dockGridItemsCache = gridItemsCache.dockGridItemsCache,
                    textColor = homeData.textColor,
                    moveGridItemResult = movedGridItemResult,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    onMoveGridItem = onMoveGridItem,
                    onDragEndAfterMove = { newTargetPage, movingGridItem, conflictingGridItem ->
                        targetPage = newTargetPage

                        onResetGridCacheAfterMove(movingGridItem, conflictingGridItem)
                    },
                    onMoveGridItemsFailed = { newTargetPage ->
                        targetPage = newTargetPage

                        onCancelGridCache()
                    },
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onDeleteWidgetGridItemCache = { newTargetPage, gridItem, appWidgetId ->
                        targetPage = newTargetPage

                        onDeleteWidgetGridItemCache(gridItem, appWidgetId)
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    gridItemsCacheByPage = gridItemsCache.gridItemsCacheByPage[targetPage],
                    gridItem = gridItemSource?.gridItem,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    dockGridItemsCache = gridItemsCache.dockGridItemsCache,
                    textColor = homeData.textColor,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
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
                    startCurrentPage = folderTargetPage,
                    foldersDataById = foldersDataById,
                    drag = drag,
                    paddingValues = paddingValues,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackInfoPackageName = homeData.userData.generalSettings.iconPackInfoPackageName,
                    onUpdateScreen = onUpdateScreen,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetTargetPage = {
                        folderTargetPage = 0
                    },
                    onLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap ->
                        folderTargetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onDraggingGridItem = { folderGridItems ->
                        onShowGridCache(
                            folderGridItems,
                            GridItemCacheType.Folder,
                            Screen.FolderDrag,
                        )
                    },
                )
            }

            Screen.FolderDrag -> {
                FolderDragScreen(
                    startCurrentPage = folderTargetPage,
                    gridItemsCacheByPage = gridItemsCache.gridItemsCacheByPage,
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
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onDragEnd = { newTargetPage ->
                        folderTargetPage = newTargetPage

                        onResetGridCacheAfterMoveFolder()
                    },
                    onDragCancel = onCancelGridCache,
                    onMoveOutsideFolder = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        onMoveOutsideFolder(homeData.gridItems)
                    },
                )
            }
        }
    }
}

@Composable
private fun OverlayImage(
    modifier: Modifier = Modifier,
    drag: Drag,
    overlayIntOffset: IntOffset,
    overlayImageBitmap: ImageBitmap?,
    onResetOverlay: () -> Unit,
) {
    if (overlayImageBitmap != null) {
        when (drag) {
            Drag.End, Drag.Cancel -> {
                onResetOverlay()
            }

            else -> Unit
        }

        Image(
            modifier = modifier
                .offset {
                    overlayIntOffset
                },
            bitmap = overlayImageBitmap,
            contentDescription = null,
        )
    }
}

private fun handlePinItemRequest(
    pinItemRequest: PinItemRequest?,
    context: Context,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null) {
        when (pinItemRequest.requestType) {
            PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                val appWidgetProviderInfo =
                    pinItemRequest.getAppWidgetProviderInfo(context)

                if (appWidgetProviderInfo != null) {
                    onGetPinGridItem(PinItemRequestType.Widget(className = appWidgetProviderInfo.provider.className))
                }
            }

            PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                val shortcutInfo = pinItemRequest.shortcutInfo

                if (shortcutInfo != null) {
                    onGetPinGridItem(
                        PinItemRequestType.ShortcutInfo(
                            shortcutId = shortcutInfo.id,
                            packageName = shortcutInfo.`package`,
                            shortLabel = shortcutInfo.shortLabel.toString(),
                            longLabel = shortcutInfo.longLabel.toString(),
                        ),
                    )
                }
            }
        }
    }
}
