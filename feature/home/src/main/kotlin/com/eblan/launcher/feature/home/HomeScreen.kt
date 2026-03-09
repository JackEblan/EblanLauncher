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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
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
import com.eblan.launcher.feature.home.screen.editpage.EditPageScreen
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
    configureResultCode: Int?,
    viewModel: HomeViewModel = hiltViewModel(),
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onSettings: () -> Unit,
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

    val folderGridItem by viewModel.folderGridItem.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        configureResultCode = configureResultCode,
        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
        eblanApplicationInfoTags = eblanApplicationInfoTags,
        eblanShortcutConfigs = eblanShortcutConfigs,
        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
        folderGridItem = folderGridItem,
        getEblanApplicationInfosByLabel = getEblanApplicationInfos,
        gridItemsCache = gridItemsCache,
        homeUiState = homeUiState,
        iconPackFilePaths = iconPackFilePaths,
        movedGridItemResult = movedGridItemResult,
        pageItems = pageItems,
        pinGridItem = pinGridItem,
        screen = screen,
        onCancelGridCache = viewModel::cancelGridCache,
        onDeleteApplicationInfoGridItem = viewModel::deleteApplicationInfoGridItem,
        onDeleteGridItem = viewModel::deleteGridItem,
        onDeleteGridItemCache = viewModel::deleteGridItemCache,
        onDeleteWidgetGridItemCache = viewModel::deleteWidgetGridItemCache,
        onEditApplicationInfo = onEditApplicationInfo,
        onEditGridItem = onEditGridItem,
        onEditPage = viewModel::showPageCache,
        onGetEblanAppWidgetProviderInfosByLabel = viewModel::getEblanAppWidgetProviderInfosByLabel,
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagIds = viewModel::getEblanApplicationInfosByTagId,
        onGetEblanShortcutConfigsByLabel = viewModel::getEblanShortcutConfigsByLabel,
        onGetPinGridItem = viewModel::getPinGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onMoveFolderGridItemOutsideFolder = viewModel::moveFolderGridItemOutsideFolder,
        onMoveGridItem = viewModel::moveGridItem,
        onResetConfigureResultCode = onResetConfigureResultCode,
        onResetGridCacheAfterMove = viewModel::resetGridCacheAfterMove,
        onResetGridCacheAfterMoveFolder = viewModel::resetGridCacheAfterMoveFolder,
        onResetGridCacheAfterMoveWidgetGridItem = viewModel::resetGridCacheAfterMoveWidgetGridItem,
        onResetGridCacheAfterResize = viewModel::resetGridCacheAfterResize,
        onResetPinGridItem = viewModel::resetPinGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onSaveEditPage = viewModel::saveEditPage,
        onSettings = onSettings,
        onShowFolderWhenDragging = viewModel::showFolderWhenDragging,
        onShowGridCache = viewModel::showGridCache,
        onStartSyncData = viewModel::startSyncData,
        onStopSyncData = viewModel::stopSyncData,
        onUpdateAppDrawerSettings = viewModel::updateAppDrawerSettings,
        onUpdateEblanApplicationInfos = viewModel::updateEblanApplicationInfos,
        onUpdateFolderGridItemId = viewModel::updateFolderGridItemId,
        onUpdateScreen = viewModel::updateScreen,
        onUpdateShortcutConfigGridItemDataCache = viewModel::updateShortcutConfigGridItemDataCache,
        onUpdateShortcutConfigIntoShortcutInfoGridItem = viewModel::updateShortcutConfigIntoShortcutInfoGridItem,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    configureResultCode: Int?,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    folderGridItem: GridItem?,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    gridItemsCache: GridItemCache,
    homeUiState: HomeUiState,
    iconPackFilePaths: Map<String, String>,
    movedGridItemResult: MoveGridItemResult?,
    pageItems: List<PageItem>,
    pinGridItem: GridItem?,
    screen: Screen,
    onCancelGridCache: () -> Unit,
    onDeleteApplicationInfoGridItem: (ApplicationInfoGridItem) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagIds: (List<Long>) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
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
    onResetGridCacheAfterMove: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onResetGridCacheAfterMoveWidgetGridItem: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetPinGridItem: () -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onSettings: () -> Unit,
    onShowFolderWhenDragging: (
        id: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onShowGridCache: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfos: (List<EblanApplicationInfo>) -> Unit,
    onUpdateFolderGridItemId: (String?) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
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
                        context = context,
                        fileManager = fileManager,
                        imageSerializer = imageSerializer,
                        launcherAppsWrapper = launcherApps,
                        pinItemRequest = pinItemRequest,
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
                    configureResultCode = configureResultCode,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    folderGridItem = folderGridItem,
                    getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
                    gridItemCache = gridItemsCache,
                    homeData = homeUiState.homeData,
                    iconPackFilePaths = iconPackFilePaths,
                    movedGridItemResult = movedGridItemResult,
                    paddingValues = paddingValues,
                    pageItems = pageItems,
                    pinGridItem = pinGridItem,
                    screen = screen,
                    screenHeight = screenIntSize.height,
                    screenWidth = screenIntSize.width,
                    onCancelGridCache = onCancelGridCache,
                    onDeleteApplicationInfoGridItem = onDeleteApplicationInfoGridItem,
                    onDeleteGridItem = onDeleteGridItem,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onEditGridItem = onEditGridItem,
                    onEditPage = onEditPage,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
                    onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
                    onMoveGridItem = onMoveGridItem,
                    onResetConfigureResultCode = onResetConfigureResultCode,
                    onResetGridCacheAfterMove = onResetGridCacheAfterMove,
                    onResetGridCacheAfterMoveFolder = onResetGridCacheAfterMoveFolder,
                    onResetGridCacheAfterMoveWidgetGridItem = onResetGridCacheAfterMoveWidgetGridItem,
                    onResetGridCacheAfterResize = onResetGridCacheAfterResize,
                    onResizeGridItem = onResizeGridItem,
                    onSaveEditPage = onSaveEditPage,
                    onSettings = onSettings,
                    onShowFolderWhenDragging = onShowFolderWhenDragging,
                    onShowGridCache = onShowGridCache,
                    onStartSyncData = onStartSyncData,
                    onStopSyncData = onStopSyncData,
                    onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
                    onUpdateEblanApplicationInfos = onUpdateEblanApplicationInfos,
                    onUpdateFolderGridItemId = onUpdateFolderGridItemId,
                    onUpdateImageBitmap = { imageBitmap ->
                        overlayImageBitmap = imageBitmap
                    },
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        overlayIntOffset = intOffset

                        overlayIntSize = intSize
                    },
                    onUpdateScreen = onUpdateScreen,
                    onUpdateSharedElementKey = { newSharedElementKey ->
                        sharedElementKey = newSharedElementKey
                    },
                    onUpdateShortcutConfigGridItemDataCache = onUpdateShortcutConfigGridItemDataCache,
                    onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                )

                OverlayImage(
                    drag = drag,
                    overlayImageBitmap = overlayImageBitmap,
                    overlayIntOffset = overlayIntOffset,
                    overlayIntSize = overlayIntSize,
                    sharedElementKey = sharedElementKey,
                    onResetOverlay = {
                        overlayImageBitmap = null

                        sharedElementKey = null

                        overlayIntOffset = IntOffset.Zero

                        overlayIntSize = IntSize.Zero
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Success(
    modifier: Modifier = Modifier,
    configureResultCode: Int?,
    drag: Drag,
    dragIntOffset: IntOffset,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    folderGridItem: GridItem?,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    gridItemCache: GridItemCache,
    homeData: HomeData,
    iconPackFilePaths: Map<String, String>,
    movedGridItemResult: MoveGridItemResult?,
    paddingValues: PaddingValues,
    pageItems: List<PageItem>,
    pinGridItem: GridItem?,
    screen: Screen,
    screenHeight: Int,
    screenWidth: Int,
    onCancelGridCache: () -> Unit,
    onDeleteApplicationInfoGridItem: (ApplicationInfoGridItem) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagIds: (List<Long>) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
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
    onResetGridCacheAfterMove: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onResetGridCacheAfterMoveWidgetGridItem: (MoveGridItemResult) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onSettings: () -> Unit,
    onShowFolderWhenDragging: (
        id: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onShowGridCache: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfos: (List<EblanApplicationInfo>) -> Unit,
    onUpdateFolderGridItemId: (String?) -> Unit,
    onUpdateImageBitmap: (ImageBitmap?) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
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

    val gridCurrentPage by remember(
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

    val currentPage by remember(
        key1 = gridHorizontalPagerState,
        key2 = dockGridHorizontalPagerState,
        key3 = homeData.userData.homeSettings,
    ) {
        derivedStateOf {
            when (associate) {
                Associate.Grid -> {
                    gridCurrentPage
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

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            when (val data = folderGridItem?.data) {
                is GridItemData.Folder -> {
                    data.gridItemsByPage.size
                }

                else -> 0
            }
        },
    )

    var lastFolderPopupX by rememberSaveable { mutableIntStateOf(0) }

    var lastFolderPopupY by rememberSaveable { mutableIntStateOf(0) }

    var lastFolderPopupWidth by rememberSaveable { mutableIntStateOf(0) }

    var lastFolderPopupHeight by rememberSaveable { mutableIntStateOf(0) }

    var folderPopupIntOffset by remember {
        mutableStateOf(
            IntOffset(
                x = lastFolderPopupX,
                y = lastFolderPopupY,
            ),
        )
    }

    var folderPopupIntSize by remember {
        mutableStateOf(
            IntSize(
                width = lastFolderPopupWidth,
                height = lastFolderPopupHeight,
            ),
        )
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
        onStartSyncData = onStartSyncData,
        onStatusBarNotificationsChange = { newStatusBarNotifications ->
            statusBarNotifications = newStatusBarNotifications
        },
        onStopSyncData = onStopSyncData,
    )

    LaunchedEffect(key1 = Unit) {
        if (homeData.userData.homeSettings.lockScreenOrientation) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }

    LaunchedEffect(key1 = screen) {
        handleKlwpBroadcasts(
            context = context,
            klwpIntegration = homeData.userData.experimentalSettings.klwpIntegration,
            screen = screen,
        )
    }

    AnimatedContent(
        modifier = modifier,
        targetState = screen,
    ) { targetState ->
        when (targetState) {
            Screen.Pager -> {
                PagerScreen(
                    appDrawerSettings = homeData.userData.appDrawerSettings,
                    currentPage = currentPage,
                    dockGridHorizontalPagerState = dockGridHorizontalPagerState,
                    dockGridItemsByPage = homeData.dockGridItemsByPage,
                    drag = drag,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    experimentalSettings = homeData.userData.experimentalSettings,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    folderGridItem = folderGridItem,
                    folderPopupIntOffset = folderPopupIntOffset,
                    folderPopupIntSize = folderPopupIntSize,
                    gestureSettings = homeData.userData.gestureSettings,
                    getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    gridItemSource = gridItemSource,
                    gridItems = homeData.gridItems,
                    gridItemsByPage = homeData.gridItemsByPage,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    hasSystemFeatureAppWidgets = homeData.hasSystemFeatureAppWidgets,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackFilePaths = iconPackFilePaths,
                    managedProfileResult = managedProfileResult,
                    paddingValues = paddingValues,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    statusBarNotifications = statusBarNotifications,
                    textColor = homeData.textColor,
                    onDeleteApplicationInfoGridItem = onDeleteApplicationInfoGridItem,
                    onDeleteGridItem = onDeleteGridItem,
                    onDraggingGridItem = onShowGridCache,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onEditGridItem = onEditGridItem,
                    onEditPage = { gridItems, newAssociate ->
                        associate = newAssociate

                        onEditPage(gridItems, newAssociate)
                    },
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
                    onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                    onResize = onShowGridCache,
                    onSettings = onSettings,
                    onTapFolderGridItem = { id, intOffset, intSize ->
                        onUpdateFolderGridItemId(id)

                        lastFolderPopupX = intOffset.x
                        lastFolderPopupY = intOffset.y

                        lastFolderPopupWidth = intSize.width
                        lastFolderPopupHeight = intSize.height

                        folderPopupIntOffset = intOffset

                        folderPopupIntSize = intSize
                    },
                    onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
                    onUpdateEblanApplicationInfos = onUpdateEblanApplicationInfos,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onUpdateGridItemSource = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        associate = newGridItemSource.gridItem.associate
                    },
                    associate = associate,
                    configureResultCode = configureResultCode,
                    dragIntOffset = dragIntOffset,
                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
                    moveGridItemResult = movedGridItemResult,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onDragCancelAfterMove = onCancelGridCache,
                    onDragEndAfterMove = onResetGridCacheAfterMove,
                    onDragEndAfterMoveFolder = onResetGridCacheAfterMoveFolder,
                    onDragEndAfterMoveWidgetGridItem = onResetGridCacheAfterMoveWidgetGridItem,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
                    onMoveGridItem = onMoveGridItem,
                    onResetConfigureResultCode = onResetConfigureResultCode,
                    onShowFolderWhenDragging = { id, movingGridItem, newGridItemSource, intOffset, intSize ->
                        onShowFolderWhenDragging(
                            id,
                            movingGridItem,
                        )

                        gridItemSource = newGridItemSource

                        lastFolderPopupX = intOffset.x
                        lastFolderPopupY = intOffset.y

                        lastFolderPopupWidth = intSize.width
                        lastFolderPopupHeight = intSize.height

                        folderPopupIntOffset = intOffset

                        folderPopupIntSize = intSize
                    },
                    onUpdateAssociate = { newAssociate ->
                        associate = newAssociate
                    },
                    onUpdateShortcutConfigGridItemDataCache = onUpdateShortcutConfigGridItemDataCache,
                    onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                )
            }

            Screen.Drag -> {
//                DragScreen(
//                    associate = associate,
//                    configureResultCode = configureResultCode,
//                    currentPage = currentPage,
//                    dockGridHorizontalPagerState = dockGridHorizontalPagerState,
//                    drag = drag,
//                    dragIntOffset = dragIntOffset,
//                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
//                    folderGridItem = folderGridItemCache,
//                    folderPopupIntOffset = folderPopupIntOffset,
//                    folderPopupIntSize = folderPopupIntSize,
//                    gridHorizontalPagerState = gridHorizontalPagerState,
//                    gridItemCache = gridItemCache,
//                    gridItemSource = gridItemSource,
//                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
//                    homeSettings = homeData.userData.homeSettings,
//                    iconPackFilePaths = iconPackFilePaths,
//                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
//                    moveGridItemResult = movedGridItemResult,
//                    paddingValues = paddingValues,
//                    screen = targetState,
//                    screenHeight = screenHeight,
//                    screenWidth = screenWidth,
//                    statusBarNotifications = statusBarNotifications,
//                    textColor = homeData.textColor,
//                    onDeleteGridItemCache = onDeleteGridItemCache,
//                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
//                    onDragCancelAfterMove = onCancelGridCache,
//                    onDragEndAfterMove = onResetGridCacheAfterMove,
//                    onDragEndAfterMoveFolder = onResetGridCacheAfterMoveFolder,
//                    onDragEndAfterMoveWidgetGridItem = onResetGridCacheAfterMoveWidgetGridItem,
//                    onMoveFolderGridItem = onMoveFolderGridItem,
//                    onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
//                    onMoveGridItem = onMoveGridItem,
//                    onResetConfigureResultCode = onResetConfigureResultCode,
//                    onShowFolderWhenDragging = { id, movingGridItem, newGridItemSource, intOffset, intSize ->
//                        onShowFolderWhenDragging(
//                            id,
//                            movingGridItem,
//                        )
//
//                        gridItemSource = newGridItemSource
//
//                        lastFolderPopupX = intOffset.x
//                        lastFolderPopupY = intOffset.y
//
//                        lastFolderPopupWidth = intSize.width
//                        lastFolderPopupHeight = intSize.height
//
//                        folderPopupIntOffset = intOffset
//
//                        folderPopupIntSize = intSize
//                    },
//                    onUpdateAssociate = { newAssociate ->
//                        associate = newAssociate
//                    },
//                    onUpdateGridItemSource = { newGridItemSource ->
//                        gridItemSource = newGridItemSource
//                    },
//                    onUpdateSharedElementKey = onUpdateSharedElementKey,
//                    onUpdateShortcutConfigGridItemDataCache = onUpdateShortcutConfigGridItemDataCache,
//                    onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
//                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    dockCurrentPage = dockCurrentPage,
                    gridCurrentPage = gridCurrentPage,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    gridItem = gridItemSource?.gridItem,
                    gridItemCache = gridItemCache,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackFilePaths = iconPackFilePaths,
                    lockMovement = homeData.userData.experimentalSettings.lockMovement,
                    moveGridItemResult = movedGridItemResult,
                    paddingValues = paddingValues,
                    screen = targetState,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    statusBarNotifications = statusBarNotifications,
                    textColor = homeData.textColor,
                    onResizeCancel = onCancelGridCache,
                    onResizeEnd = onResetGridCacheAfterResize,
                    onResizeGridItem = onResizeGridItem,
                )
            }

            Screen.Loading -> {
                LoadingScreen()
            }

            Screen.EditPage -> {
                EditPageScreen(
                    associate = associate,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    homeSettings = homeData.userData.homeSettings,
                    iconPackFilePaths = iconPackFilePaths,
                    paddingValues = paddingValues,
                    pageItems = pageItems,
                    screen = targetState,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
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
    drag: Drag,
    overlayImageBitmap: ImageBitmap?,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    sharedElementKey: SharedElementKey?,
    onResetOverlay: () -> Unit,
) {
    if (overlayImageBitmap == null || sharedElementKey == null) return

    val density = LocalDensity.current

    val size = with(density) {
        DpSize(width = overlayIntSize.width.toDp(), height = overlayIntSize.height.toDp())
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Cancel || drag == Drag.End) {
            onResetOverlay()
        }
    }

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
    onStartSyncData: () -> Unit,
    onStatusBarNotificationsChange: (Map<String, Int>) -> Unit,
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
