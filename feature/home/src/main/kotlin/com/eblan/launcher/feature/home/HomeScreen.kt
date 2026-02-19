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

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.IBinder
import android.os.UserHandle
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.EblanUser
import com.eblan.launcher.domain.model.GetEblanApplicationInfosByLabel
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.ManagedProfileResult
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.editpage.EditPageScreen
import com.eblan.launcher.feature.home.screen.folder.FolderScreen
import com.eblan.launcher.feature.home.screen.folderdrag.FolderDragScreen
import com.eblan.launcher.feature.home.screen.folderresize.FolderResizeScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.resize.ResizeScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.service.EblanNotificationListenerService
import com.eblan.launcher.ui.dialog.TextDialog
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    configureResultCode: Int?,
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onResetConfigureResultCode: () -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    val movedGridItemResult by viewModel.movedGridItemResult.collectAsStateWithLifecycle()

    val pageItems by viewModel.pageItems.collectAsStateWithLifecycle()

    val gridItemsCache by viewModel.gridItemsCache.collectAsStateWithLifecycle()

    val pinGridItem by viewModel.pinGridItem.collectAsStateWithLifecycle()

    val getEblanApplicationInfos by viewModel.getEblanApplicationInfosByLabel.collectAsStateWithLifecycle()

    val eblanShortcutConfigs by viewModel.eblanShortcutConfigs.collectAsStateWithLifecycle()

    val eblanAppWidgetProviderInfos by viewModel.eblanAppWidgetProviderInfos.collectAsStateWithLifecycle()

    val eblanShortcutInfosGroup by viewModel.eblanShortcutInfosGroup.collectAsStateWithLifecycle()

    val eblanAppWidgetProviderInfosGroup by viewModel.eblanAppWidgetProviderInfosGroup.collectAsStateWithLifecycle()

    val iconPackFilePaths by viewModel.iconPackFilePaths.collectAsStateWithLifecycle()

    val eblanApplicationInfoTags by viewModel.eblanApplicationInfoTags.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        movedGridItemResult = movedGridItemResult,
        pageItems = pageItems,
        gridItemsCache = gridItemsCache,
        pinGridItem = pinGridItem,
        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
        eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
        iconPackFilePaths = iconPackFilePaths,
        getEblanApplicationInfosByLabel = getEblanApplicationInfos,
        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        eblanShortcutConfigs = eblanShortcutConfigs,
        eblanApplicationInfoTags = eblanApplicationInfoTags,
        configureResultCode = configureResultCode,
        onMoveGridItem = viewModel::moveGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onResizeFolderGridItem = viewModel::resizeFolderGridItem,
        onShowGridCache = viewModel::showGridCache,
        onShowFolderGridCache = viewModel::showFolderGridCache,
        onResetGridCacheAfterResize = viewModel::resetGridCacheAfterResize,
        onResetGridCacheAfterMove = viewModel::resetGridCacheAfterMove,
        onResetGridCacheAfterMoveWidgetGridItem = viewModel::resetGridCacheAfterMoveWidgetGridItem,
        onResetGridCacheAfterMoveFolder = viewModel::resetGridCacheAfterMoveFolder,
        onResetGridCacheAfterResizeFolder = viewModel::resetGridCacheAfterResizeFolder,
        onCancelGridCache = viewModel::cancelGridCache,
        onCancelFolderGridCache = viewModel::cancelFolderGridCache,
        onEditGridItem = onEditGridItem,
        onSettings = onSettings,
        onEditPage = viewModel::showPageCache,
        onSaveEditPage = viewModel::saveEditPage,
        onUpdateScreen = viewModel::updateScreen,
        onDeleteGridItemCache = viewModel::deleteGridItemCache,
        onDeleteWidgetGridItemCache = viewModel::deleteWidgetGridItemCache,
        onShowFolder = viewModel::showFolder,
        onRemoveLastFolder = viewModel::removeLastFolder,
        onAddFolder = viewModel::addFolder,
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
        onGetEblanAppWidgetProviderInfosByLabel = viewModel::getEblanAppWidgetProviderInfosByLabel,
        onGetEblanShortcutConfigsByLabel = viewModel::getEblanShortcutConfigsByLabel,
        onDeleteGridItem = viewModel::deleteGridItem,
        onGetPinGridItem = viewModel::getPinGridItem,
        onResetPinGridItem = viewModel::resetPinGridItem,
        onUpdateShortcutConfigGridItemDataCache = viewModel::updateShortcutConfigGridItemDataCache,
        onUpdateShortcutConfigIntoShortcutInfoGridItem = viewModel::updateShortcutConfigIntoShortcutInfoGridItem,
        onEditApplicationInfo = onEditApplicationInfo,
        onMoveGridItemOutsideFolder = viewModel::moveGridItemOutsideFolder,
        onShowFolderWhenDragging = viewModel::showFolderWhenDragging,
        onGetEblanApplicationInfosByTagIds = viewModel::getEblanApplicationInfosByTagId,
        onResetConfigureResultCode = onResetConfigureResultCode,
        onStartSyncData = viewModel::startSyncData,
        onStopSyncData = viewModel::stopSyncData,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    movedGridItemResult: MoveGridItemResult?,
    pageItems: List<PageItem>,
    gridItemsCache: GridItemCache,
    pinGridItem: GridItem?,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    iconPackFilePaths: Map<String, String>,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
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
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onResizeFolderGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onShowGridCache: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onShowFolderGridCache: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterMoveWidgetGridItem: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onResetGridCacheAfterResizeFolder: (GridItem) -> Unit,
    onCancelGridCache: () -> Unit,
    onCancelFolderGridCache: () -> Unit,
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onShowFolder: (String) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onResetPinGridItem: () -> Unit,
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
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onMoveGridItemOutsideFolder: (
        folderId: String,
        movingGridItem: GridItem,
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onShowFolderWhenDragging: (String) -> Unit,
    onGetEblanApplicationInfosByTagIds: (List<Long>) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
) {
    val density = LocalDensity.current

    val context = LocalContext.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntSize by remember { mutableStateOf(IntSize.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    val launcherApps = LocalLauncherApps.current

    val imageSerializer = LocalImageSerializer.current

    val userManager = LocalUserManager.current

    val fileManager = LocalFileManager.current

    val scope = rememberCoroutineScope()

    val touchSlop = with(density) {
        50.dp.toPx()
    }

    var accumulatedDragOffset by remember { mutableStateOf(Offset.Zero) }

    var sharedElementKey by remember { mutableStateOf<SharedElementKey?>(null) }

    var screenIntSize by remember { mutableStateOf(IntSize.Zero) }

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
                        imageSerializer = imageSerializer,
                        userManager = userManager,
                        fileManager = fileManager,
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

            override fun onDrop(event: DragAndDropEvent): Boolean = true
        }
    }

    SharedTransitionLayout(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        drag = Drag.Start

                        dragIntOffset = offset.round()

                        accumulatedDragOffset = Offset.Zero
                    },
                    onDragEnd = {
                        drag = Drag.End
                    },
                    onDragCancel = {
                        drag = Drag.Cancel
                    },
                    onDrag = { _, dragAmount ->
                        accumulatedDragOffset += dragAmount

                        if (accumulatedDragOffset.getDistance() >= touchSlop) {
                            drag = Drag.Dragging
                        }

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
            .onSizeChanged { intSize ->
                screenIntSize = intSize
            }
            .fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (homeUiState is HomeUiState.Success && screenIntSize != IntSize.Zero) {
                Success(
                    screen = screen,
                    homeData = homeUiState.homeData,
                    pageItems = pageItems,
                    movedGridItemResult = movedGridItemResult,
                    screenWidth = screenIntSize.width,
                    screenHeight = screenIntSize.height,
                    paddingValues = paddingValues,
                    dragIntOffset = dragIntOffset,
                    drag = drag,
                    gridItemCache = gridItemsCache,
                    pinGridItem = pinGridItem,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    iconPackFilePaths = iconPackFilePaths,
                    getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    configureResultCode = configureResultCode,
                    onMoveGridItem = onMoveGridItem,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onResizeGridItem = onResizeGridItem,
                    onResizeFolderGridItem = onResizeFolderGridItem,
                    onShowGridCache = onShowGridCache,
                    onShowFolderGridCache = onShowFolderGridCache,
                    onResetGridCacheAfterResize = onResetGridCacheAfterResize,
                    onResetGridCacheAfterMove = onResetGridCacheAfterMove,
                    onResetGridCacheAfterMoveWidgetGridItem = onResetGridCacheAfterMoveWidgetGridItem,
                    onCancelGridCache = onCancelGridCache,
                    onCancelFolderGridCache = onCancelFolderGridCache,
                    onEditGridItem = onEditGridItem,
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onShowFolder = onShowFolder,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetGridCacheAfterMoveFolder = onResetGridCacheAfterMoveFolder,
                    onResetGridCacheAfterResizeFolder = onResetGridCacheAfterResizeFolder,
                    onUpdateGridItemImageBitmap = { imageBitmap ->
                        overlayImageBitmap = imageBitmap
                    },
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        overlayIntOffset = intOffset

                        overlayIntSize = intSize
                    },
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                    onDeleteGridItem = onDeleteGridItem,
                    onUpdateShortcutConfigGridItemDataCache = onUpdateShortcutConfigGridItemDataCache,
                    onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onUpdateSharedElementKey = { newSharedElementKey ->
                        sharedElementKey = newSharedElementKey
                    },
                    onMoveGridItemOutsideFolder = onMoveGridItemOutsideFolder,
                    onShowFolderWhenDragging = onShowFolderWhenDragging,
                    onResetOverlay = {
                        overlayIntOffset = IntOffset.Zero

                        overlayIntSize = IntSize.Zero

                        overlayImageBitmap = null

                        sharedElementKey = null
                    },
                    onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
                    onResetConfigureResultCode = onResetConfigureResultCode,
                    onStartSyncData = onStartSyncData,
                    onStopSyncData = onStopSyncData,
                )

                OverlayImage(
                    overlayIntOffset = overlayIntOffset,
                    overlayIntSize = overlayIntSize,
                    overlayImageBitmap = overlayImageBitmap,
                    sharedElementKey = sharedElementKey,
                    drag = drag,
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Success(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeData: HomeData,
    pageItems: List<PageItem>,
    movedGridItemResult: MoveGridItemResult?,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    dragIntOffset: IntOffset,
    drag: Drag,
    gridItemCache: GridItemCache,
    pinGridItem: GridItem?,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    iconPackFilePaths: Map<String, String>,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
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
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onResizeFolderGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onShowGridCache: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onShowFolderGridCache: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterMoveWidgetGridItem: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onResetGridCacheAfterResizeFolder: (GridItem) -> Unit,
    onCancelGridCache: () -> Unit,
    onCancelFolderGridCache: () -> Unit,
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
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
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
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
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onMoveGridItemOutsideFolder: (
        folderId: String,
        movingGridItem: GridItem,
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onShowFolderWhenDragging: (String) -> Unit,
    onResetOverlay: () -> Unit,
    onGetEblanApplicationInfosByTagIds: (List<Long>) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
) {
    val context = LocalContext.current

    val activity = LocalActivity.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    val userManager = LocalUserManager.current

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

    val dockGridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeData.userData.homeSettings.dockInfiniteScroll) {
            (Int.MAX_VALUE / 2) + homeData.userData.homeSettings.dockInitialPage
        } else {
            homeData.userData.homeSettings.dockInitialPage
        },
        pageCount = {
            if (homeData.userData.homeSettings.dockInfiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeData.userData.homeSettings.dockPageCount
            }
        },
    )

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            when (screen) {
                is Screen.Folder -> {
                    screen.folderDataById.pageCount
                }

                is Screen.FolderDrag -> {
                    screen.folderDataById.pageCount
                }

                is Screen.FolderResize -> {
                    screen.folderDataById.pageCount
                }

                else -> {
                    0
                }
            }
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

    val dockCurrentPage by remember(
        key1 = dockGridHorizontalPagerState,
        key2 = homeData.userData.homeSettings,
    ) {
        derivedStateOf {
            calculatePage(
                index = dockGridHorizontalPagerState.currentPage,
                infiniteScroll = homeData.userData.homeSettings.dockInfiniteScroll,
                pageCount = homeData.userData.homeSettings.dockPageCount,
            )
        }
    }

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var managedProfileResult by remember { mutableStateOf<ManagedProfileResult?>(null) }

    var statusBarNotifications by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var associate by remember { mutableStateOf<Associate?>(null) }

    val gridItemSourceCurrentPage by remember {
        derivedStateOf {
            when (associate) {
                Associate.Grid -> {
                    currentPage
                }

                Associate.Dock -> {
                    dockCurrentPage
                }

                null -> {
                    0
                }
            }
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
                Screen.Drag,
                homeData.gridItems,
            )
        }
    }

    LifecycleEffect(
        syncDataEnabled = homeData.userData.experimentalSettings.syncData,
        userManagerWrapper = userManager,
        onManagedProfileResultChange = { newManagedProfileResult ->
            managedProfileResult = newManagedProfileResult
        },
        onStatusBarNotificationsChange = { newStatusBarNotifications ->
            statusBarNotifications = newStatusBarNotifications
        },
        onStartSyncData = onStartSyncData,
        onStopSyncData = onStopSyncData,
    )

    LaunchedEffect(key1 = Unit) {
        if (homeData.userData.homeSettings.lockScreenOrientation) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }

    LaunchedEffect(key1 = screen) {
        handleKlwpBroadcasts(
            klwpIntegration = homeData.userData.experimentalSettings.klwpIntegration,
            screen = screen,
            context = context,
        )
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
                    dockGridItemsByPage = homeData.dockGridItemsByPage,
                    textColor = homeData.textColor,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    appDrawerSettings = homeData.userData.appDrawerSettings,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    hasSystemFeatureAppWidgets = homeData.hasSystemFeatureAppWidgets,
                    gestureSettings = homeData.userData.gestureSettings,
                    gridItemSource = gridItemSource,
                    homeSettings = homeData.userData.homeSettings,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    dockGridHorizontalPagerState = dockGridHorizontalPagerState,
                    currentPage = gridItemSourceCurrentPage,
                    statusBarNotifications = statusBarNotifications,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    iconPackFilePaths = iconPackFilePaths,
                    managedProfileResult = managedProfileResult,
                    screen = targetState,
                    experimentalSettings = homeData.userData.experimentalSettings,
                    getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    onTapFolderGridItem = onShowFolder,
                    onDraggingGridItem = onShowGridCache,
                    onEditGridItem = onEditGridItem,
                    onResize = onShowGridCache,
                    onSettings = onSettings,
                    onEditPage = { gridItems, newAssociate ->
                        associate = newAssociate

                        onEditPage(gridItems, newAssociate)
                    },
                    onLongPressGridItem = { newGridItemSource, imageBitmap ->
                        gridItemSource = newGridItemSource

                        associate = newGridItemSource.gridItem.associate

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                    onDeleteGridItem = onDeleteGridItem,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onResetOverlay = onResetOverlay,
                    onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
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
                    textColor = homeData.textColor,
                    moveGridItemResult = movedGridItemResult,
                    homeSettings = homeData.userData.homeSettings,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    dockGridHorizontalPagerState = dockGridHorizontalPagerState,
                    currentPage = gridItemSourceCurrentPage,
                    statusBarNotifications = statusBarNotifications,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    iconPackFilePaths = iconPackFilePaths,
                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
                    screen = targetState,
                    associate = associate,
                    configureResultCode = configureResultCode,
                    onMoveGridItem = onMoveGridItem,
                    onDragEndAfterMove = onResetGridCacheAfterMove,
                    onDragEndAfterMoveWidgetGridItem = onResetGridCacheAfterMoveWidgetGridItem,
                    onDragCancelAfterMove = onCancelGridCache,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onUpdateShortcutConfigGridItemDataCache = onUpdateShortcutConfigGridItemDataCache,
                    onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                    onShowFolderWhenDragging = onShowFolderWhenDragging,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onUpdateAssociate = { newAssociate ->
                        associate = newAssociate
                    },
                    onResetConfigureResultCode = onResetConfigureResultCode,
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    currentPage = currentPage,
                    dockCurrentPage = dockCurrentPage,
                    gridItemCache = gridItemCache,
                    gridItem = gridItemSource?.gridItem,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    statusBarNotifications = statusBarNotifications,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    iconPackFilePaths = iconPackFilePaths,
                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
                    moveGridItemResult = movedGridItemResult,
                    screen = targetState,
                    gridHorizontalPagerState = gridHorizontalPagerState,
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
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = targetState,
                    associate = associate,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                )
            }

            is Screen.Folder -> {
                FolderScreen(
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    folderDataById = targetState.folderDataById,
                    drag = drag,
                    paddingValues = paddingValues,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    homeSettings = homeData.userData.homeSettings,
                    statusBarNotifications = statusBarNotifications,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = targetState,
                    gridItemSource = gridItemSource,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    gridItems = homeData.gridItems,
                    onUpdateScreen = onUpdateScreen,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onLongPressGridItem = { newGridItemSource, imageBitmap ->
                        gridItemSource = newGridItemSource

                        associate = newGridItemSource.gridItem.associate

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onDraggingFolderGridItem = onShowFolderGridCache,
                    onDraggingGridItem = onShowGridCache,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onResetOverlay = onResetOverlay,
                    onEditGridItem = onEditGridItem,
                    onResize = onShowFolderGridCache,
                    onDeleteGridItem = onDeleteGridItem,
                )
            }

            is Screen.FolderDrag -> {
                FolderDragScreen(
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    gridItemCache = gridItemCache,
                    folderDataById = targetState.folderDataById,
                    gridItemSource = gridItemSource,
                    textColor = homeData.textColor,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    moveGridItemResult = movedGridItemResult,
                    statusBarNotifications = statusBarNotifications,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    iconPackFilePaths = iconPackFilePaths,
                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
                    screen = targetState,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onDragEnd = onResetGridCacheAfterMoveFolder,
                    onDragCancel = onCancelFolderGridCache,
                    onMoveGridItemOutsideFolder = { newGridItemSource, folderId, movingGridItem ->
                        gridItemSource = newGridItemSource

                        associate = newGridItemSource.gridItem.associate

                        onMoveGridItemOutsideFolder(
                            folderId,
                            movingGridItem,
                            homeData.gridItems,
                            Screen.Drag,
                        )
                    },
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onUpdateAssociate = { newAssociate ->
                        associate = newAssociate
                    },
                )
            }

            is Screen.FolderResize -> {
                FolderResizeScreen(
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    folderDataById = targetState.folderDataById,
                    drag = drag,
                    paddingValues = paddingValues,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    homeSettings = homeData.userData.homeSettings,
                    statusBarNotifications = statusBarNotifications,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = targetState,
                    gridItem = gridItemSource?.gridItem,
                    moveGridItemResult = movedGridItemResult,
                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
                    gridItemCache = gridItemCache,
                    onUpdateScreen = onUpdateScreen,
                    onResizeGridItem = onResizeFolderGridItem,
                    onResizeEnd = onResetGridCacheAfterResizeFolder,
                    onResizeCancel = onCancelFolderGridCache,
                )
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PostNotificationPermissionEffect()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.OverlayImage(
    modifier: Modifier = Modifier,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    overlayImageBitmap: ImageBitmap?,
    sharedElementKey: SharedElementKey?,
    drag: Drag,
) {
    val density = LocalDensity.current

    val size = with(density) {
        DpSize(width = overlayIntSize.width.toDp(), height = overlayIntSize.height.toDp())
    }

    if (overlayImageBitmap != null && sharedElementKey != null) {
        Image(
            modifier = modifier
                .offset {
                    overlayIntOffset
                }
                .size(size)
                .sharedElementWithCallerManagedVisibility(
                    rememberSharedContentState(key = sharedElementKey),
                    visible = drag == Drag.Start || drag == Drag.Dragging,
                ),
            bitmap = overlayImageBitmap,
            contentDescription = null,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PostNotificationPermissionEffect(modifier: Modifier = Modifier) {
    val notificationsPermissionState =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    var showTextDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = notificationsPermissionState) {
        if (notificationsPermissionState.status.shouldShowRationale) {
            showTextDialog = true
        } else {
            notificationsPermissionState.launchPermissionRequest()
        }
    }

    if (showTextDialog) {
        TextDialog(
            modifier = modifier,
            title = "Notification Permission",
            text = "Allow notification permission so we can inform you about data sync status and important crash reports.",
            onClick = {
                notificationsPermissionState.launchPermissionRequest()

                showTextDialog = false
            },
            onDismissRequest = {
                showTextDialog = false
            },
        )
    }
}

@Composable
private fun LifecycleEffect(
    syncDataEnabled: Boolean,
    userManagerWrapper: AndroidUserManagerWrapper,
    onManagedProfileResultChange: (ManagedProfileResult?) -> Unit,
    onStatusBarNotificationsChange: (Map<String, Int>) -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
) {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    val appWidgetHost = LocalAppWidgetHost.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    DisposableEffect(key1 = lifecycleOwner) {
        val eblanNotificationListenerIntent =
            Intent(context, EblanNotificationListenerService::class.java)

        var shouldUnbindEblanNotificationListenerService = false

        val eblanNotificationListenerServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val listener =
                    (service as EblanNotificationListenerService.LocalBinder).getService()

                lifecycleOwner.lifecycleScope.launch {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        listener.statusBarNotifications.collect {
                            onStatusBarNotificationsChange(it)
                        }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }

        val managedProfileBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val userHandle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        Intent.EXTRA_USER,
                        UserHandle::class.java,
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_USER)
                }

                if (userHandle != null) {
                    onStartSyncData()

                    onManagedProfileResultChange(
                        ManagedProfileResult(
                            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = userHandle),
                            isQuiteModeEnabled = userManagerWrapper.isQuietModeEnabled(userHandle = userHandle),
                        ),
                    )
                }
            }
        }

        val lifecycleEventObserver = LifecycleEventObserver { lifecycleOwner, event ->
            lifecycleOwner.lifecycleScope.launch {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        if (syncDataEnabled && pinItemRequestWrapper.getPinItemRequest() == null) {
                            context.registerReceiver(
                                managedProfileBroadcastReceiver,
                                IntentFilter().apply {
                                    addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                                    addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                                    addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
                                    addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
                                    addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                                        addAction(Intent.ACTION_PROFILE_AVAILABLE)
                                        addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                                    }
                                },
                            )

                            shouldUnbindEblanNotificationListenerService = context.bindService(
                                eblanNotificationListenerIntent,
                                eblanNotificationListenerServiceConnection,
                                Context.BIND_AUTO_CREATE,
                            )

                            onStartSyncData()
                        }

                        appWidgetHost.startListening()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        if (syncDataEnabled && pinItemRequestWrapper.getPinItemRequest() == null) {
                            if (shouldUnbindEblanNotificationListenerService) {
                                context.unregisterReceiver(managedProfileBroadcastReceiver)

                                context.unbindService(eblanNotificationListenerServiceConnection)

                                shouldUnbindEblanNotificationListenerService = false
                            }

                            onStopSyncData()
                        }

                        appWidgetHost.stopListening()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }
}
