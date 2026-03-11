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

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SET_WALLPAPER
import android.content.Intent.createChooser
import android.content.Intent.parseUri
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.os.UserHandle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.EblanUser
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GetEblanApplicationInfosByLabel
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.ManagedProfileResult
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.handlePinItemRequest
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.folder.FolderScreen
import com.eblan.launcher.feature.home.screen.shortcutconfig.ShortcutConfigScreen
import com.eblan.launcher.feature.home.screen.widget.AppWidgetScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.service.EblanNotificationListenerService
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun PagerScreen(
    modifier: Modifier = Modifier,
    appDrawerSettings: AppDrawerSettings,
    dockGridItemsByPage: Map<Int, List<GridItem>>,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    experimentalSettings: ExperimentalSettings,
    folderGridItem: GridItem?,
    gestureSettings: GestureSettings,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    homeSettings: HomeSettings,
    iconPackFilePaths: Map<String, String>,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    textColor: TextColor,
    configureResultCode: Int?,
    lockMovement: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    pinGridItem: GridItem?,
    onDeleteApplicationInfoGridItem: (ApplicationInfoGridItem) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDraggingGridItem: (List<GridItem>) -> Unit,
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
    onResize: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onSettings: () -> Unit,
    onUpdateFolderGridItemId: (String?) -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfos: (List<EblanApplicationInfo>) -> Unit,
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
    onResetPinGridItem: () -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
) {
    val context = LocalContext.current

    val launcherApps = LocalLauncherApps.current

    var hasDoubleTap by remember { mutableStateOf(false) }

    var showAppDrawer by remember { mutableStateOf(false) }

    var showWidgets by remember { mutableStateOf(false) }

    var showShortcutConfigActivities by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var lastSwipeUpY by rememberSaveable { mutableFloatStateOf(screenHeight.toFloat()) }

    var lastSwipeDownY by rememberSaveable { mutableFloatStateOf(screenHeight.toFloat()) }

    val swipeUpY = remember { Animatable(lastSwipeUpY) }

    val swipeDownY = remember { Animatable(lastSwipeDownY) }

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val view = LocalView.current

    val activity = LocalActivity.current as ComponentActivity

    val swipeY by remember {
        derivedStateOf {
            if (swipeUpY.value < screenHeight.toFloat() && gestureSettings.swipeUp.eblanActionType == EblanActionType.OpenAppDrawer) {
                swipeUpY
            } else if (swipeDownY.value < screenHeight.toFloat() && gestureSettings.swipeDown.eblanActionType == EblanActionType.OpenAppDrawer) {
                swipeDownY
            } else {
                Animatable(screenHeight.toFloat())
            }
        }
    }

    val pagerScreenAlpha by remember {
        derivedStateOf {
            val threshold = screenHeight / 2

            ((swipeY.value - threshold) / threshold).coerceIn(0f, 1f)
        }
    }

    var isPressHome by remember { mutableStateOf(false) }

    var eblanApplicationInfoGroup by remember { mutableStateOf<EblanApplicationInfoGroup?>(null) }

    val isApplicationScreenVisible by remember {
        derivedStateOf {
            swipeY.value < screenHeight.toFloat()
        }
    }

    val applicationScreenAlpha by remember {
        derivedStateOf {
            if (experimentalSettings.klwpIntegration) {
                1f
            } else {
                ((screenHeight - swipeY.value) / (screenHeight / 2)).coerceIn(0f, 1f)
            }
        }
    }

    val cornerSize by remember {
        derivedStateOf {
            val progress = swipeY.value.coerceAtLeast(0f) / screenHeight

            (20 * progress).dp
        }
    }

    val density = LocalDensity.current

    val dockHeight = homeSettings.dockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }
    var showGridItemPopup by remember { mutableStateOf(false) }

    var showSettingsPopup by remember { mutableStateOf(false) }

    var showFolderGridItemPopup by remember { mutableStateOf(false) }

    var isLongPress by remember { mutableStateOf(false) }

    var isDragging by remember { mutableStateOf(false) }

    var settingsPopupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupIntSize by remember { mutableStateOf(IntSize.Zero) }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val dockTopLeft = safeDrawingHeight - dockHeightPx

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val appWidgetManager = LocalAppWidgetManager.current

    val userManager = LocalUserManager.current

    val imageSerializer = LocalImageSerializer.current

    val fileManager = LocalFileManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    var lastAppWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    var updatedWidgetGridItem by remember { mutableStateOf<GridItem?>(null) }

    var gridPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var dockPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var folderPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var folderTitleHeightPx by remember { mutableIntStateOf(0) }

    val pinItemRequestWrapper = LocalPinItemRequest.current

    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntSize by remember { mutableStateOf(IntSize.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    val touchSlop = with(density) {
        50.dp.toPx()
    }

    var accumulatedDragOffset by remember { mutableStateOf(Offset.Zero) }

    var sharedElementKey by remember { mutableStateOf<SharedElementKey?>(null) }

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

    val gridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeSettings.infiniteScroll) {
            (Int.MAX_VALUE / 2) + homeSettings.initialPage
        } else {
            homeSettings.initialPage
        },
        pageCount = {
            if (homeSettings.infiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeSettings.pageCount
            }
        },
    )

    val dockGridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeSettings.dockInfiniteScroll) {
            (Int.MAX_VALUE / 2) + homeSettings.dockInitialPage
        } else {
            homeSettings.dockInitialPage
        },
        pageCount = {
            if (homeSettings.dockInfiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeSettings.dockPageCount
            }
        },
    )

    val gridCurrentPage by remember(
        key1 = gridHorizontalPagerState,
        key2 = homeSettings,
    ) {
        derivedStateOf {
            calculatePage(
                index = gridHorizontalPagerState.currentPage,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )
        }
    }

    val dockCurrentPage by remember(
        key1 = dockGridHorizontalPagerState,
        key2 = homeSettings,
    ) {
        derivedStateOf {
            calculatePage(
                index = dockGridHorizontalPagerState.currentPage,
                infiniteScroll = homeSettings.dockInfiniteScroll,
                pageCount = homeSettings.dockPageCount,
            )
        }
    }

    var managedProfileResult by remember { mutableStateOf<ManagedProfileResult?>(null) }

    var statusBarNotifications by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var associate by remember { mutableStateOf<Associate?>(null) }

    val currentPage by remember(
        key1 = gridHorizontalPagerState,
        key2 = dockGridHorizontalPagerState,
        key3 = homeSettings,
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

            onDraggingGridItem(gridItems)
        }
    }

    LifecycleEffect(
        syncDataEnabled = experimentalSettings.syncData,
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

//    LaunchedEffect(key1 = screen) {
//        handleKlwpBroadcasts(
//            context = context,
//            klwpIntegration = experimentalSettings.klwpIntegration,
//            screen = screen,
//        )
//    }

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
            isDragging = isDragging,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
            onMoveGridItem = onMoveGridItem,
            onUpdateAssociate = { newAssociate ->
                associate = newAssociate
            },
            onUpdateGridItemSource = { newGridItemSource ->
                gridItemSource = newGridItemSource

                associate = newGridItemSource.gridItem.associate
            },
            onUpdateSharedElementKey = { newSharedElementKey ->
                sharedElementKey = newSharedElementKey
            },
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
                    isDragging = isDragging,
                    isApplicationScreenVisible = isApplicationScreenVisible,
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
                    onResetIsLongPressAndIsDragging = {
                        isLongPress = false

                        isDragging = false
                    },
                )
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
            isDragging = isDragging,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onResetIsLongPressAndIsDragging = {
                isLongPress = false

                isDragging = false
            },
        )
    }

    LaunchedEffect(key1 = updatedWidgetGridItem) {
        handleBoundWidget(
            activity = activity,
            androidAppWidgetHostWrapper = appWidgetHost,
            gridItemSource = gridItemSource,
            moveGridItemResult = moveGridItemResult,
            updatedWidgetGridItem = updatedWidgetGridItem,
            isDragging = isDragging,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
            onResetIsLongPressAndIsDragging = {
                isLongPress = false

                isDragging = false
            },
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
            isDragging = isDragging,
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
            onUpdateSharedElementKey = { newSharedElementKey ->
                sharedElementKey = newSharedElementKey
            },
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
            isDragging = isDragging,
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
            onUpdateIsDragging = { newIsDragging ->
                isLongPress = newIsDragging

                isDragging = newIsDragging
            },
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

    LaunchedEffect(key1 = hasDoubleTap) {
        handleHasDoubleTap(
            context = context,
            gestureSettings = gestureSettings,
            hasDoubleTap = hasDoubleTap,
            launcherApps = launcherApps,
            onOpenAppDrawer = {
                showAppDrawer = true
            },
        )

        hasDoubleTap = false
    }

    DisposableEffect(key1 = activity) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleActionMainIntent(
                    eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    infiniteScroll = homeSettings.infiniteScroll,
                    initialPage = homeSettings.initialPage,
                    intent = intent,
                    pageCount = homeSettings.pageCount,
                    screenHeight = screenHeight,
                    showShortcutConfigActivities = showShortcutConfigActivities,
                    showWidgets = showWidgets,
                    swipeY = swipeY,
                    wallpaperManagerWrapper = wallpaperManagerWrapper,
                    wallpaperScroll = homeSettings.wallpaperScroll,
                    windowToken = view.windowToken,
                    onHome = {
                        isPressHome = true
                    },
                )

                handleEblanActionIntent(
                    context = context,
                    intent = intent,
                    launcherApps = launcherApps,
                    onOpenAppDrawer = {
                        showAppDrawer = true
                    },
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    LaunchedEffect(key1 = isApplicationScreenVisible) {
        handleKlwpBroadcasts(
            context = context,
            isApplicationScreenVisible = isApplicationScreenVisible,
            klwpIntegration = experimentalSettings.klwpIntegration,
        )
    }

    LaunchedEffect(key1 = swipeUpY) {
        snapshotFlow { swipeUpY.value }.onEach { y ->
            lastSwipeUpY = y
        }.collect()
    }

    LaunchedEffect(key1 = swipeDownY) {
        snapshotFlow { swipeDownY.value }.onEach { y ->
            lastSwipeDownY = y
        }.collect()
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

    LaunchedEffect(key1 = isPressHome) {
        if (isPressHome) {
            showGridItemPopup = false

            showSettingsPopup = false
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
            .fillMaxSize(),
    ) {
        Column(
            modifier = modifier
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            scope.launch {
                                swipeUpY.snapTo(swipeUpY.value + dragAmount)

                                swipeDownY.snapTo(swipeDownY.value - dragAmount)
                            }
                        },
                        onDragEnd = {
                            swipeEblanAction(
                                context = context,
                                gestureSettings = gestureSettings,
                                launcherApps = launcherApps,
                                screenHeight = screenHeight,
                                swipeDownY = swipeDownY.value,
                                swipeUpY = swipeUpY.value,
                            )

                            scope.launch {
                                resetSwipeOffset(
                                    gestureSettings = gestureSettings,
                                    screenHeight = screenHeight,
                                    swipeDownY = swipeDownY,
                                    swipeUpY = swipeUpY,
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                swipeUpY.animateTo(screenHeight.toFloat())

                                swipeDownY.animateTo(screenHeight.toFloat())
                            }
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            hasDoubleTap = true
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
                )
                .alpha(pagerScreenAlpha),
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
                        val gridHeight = safeDrawingHeight - pageIndicatorHeightPx - dockHeightPx

                        val cellWidth = safeDrawingWidth / homeSettings.columns

                        val cellHeight =
                            gridHeight / homeSettings.rows

                        val x = gridItem.startColumn * cellWidth

                        val y = gridItem.startRow * cellHeight

                        val width = gridItem.columnSpan * cellWidth

                        val height = gridItem.rowSpan * cellHeight

                        InteractiveGridItemContent(
                            drag = drag,
                            gridItem = gridItem,
                            gridItemSettings = homeSettings.gridItemSettings,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            iconPackFilePaths = iconPackFilePaths,
                            isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
                            statusBarNotifications = statusBarNotifications,
                            textColor = textColor,
                            gridItemSource = gridItemSource,
                            isLongPress = isLongPress,
                            onDraggingGridItem = {
                                showGridItemPopup = false

                                onDraggingGridItem(gridItems)
                            },
                            onOpenAppDrawer = {
                                showAppDrawer = true
                            },
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
                            onTapFolderGridItem = {
                                onUpdateFolderGridItemId(gridItem.id)

                                lastFolderPopupX = x
                                lastFolderPopupY = y

                                lastFolderPopupWidth = width
                                lastFolderPopupHeight = height

                                folderPopupIntOffset = IntOffset(
                                    x = x,
                                    y = y,
                                )

                                folderPopupIntSize = IntSize(
                                    width = width,
                                    height = height,
                                )
                            },
                            onTapShortcutConfig = { uri ->
                                context.startActivity(parseUri(uri, 0))
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
                            onUpdateGridItemOffset = { intOffset, intSize ->
                                popupIntOffset = intOffset

                                popupIntSize = IntSize(
                                    width = intSize.width,
                                    height = height,
                                )

                                overlayIntOffset = intOffset

                                overlayIntSize = intSize

                                showGridItemPopup = true
                            },
                            onUpdateImageBitmap = { newImageBitmap ->
                                overlayImageBitmap = newImageBitmap
                            },
                            onUpdateSharedElementKey = { newSharedElementKey ->
                                sharedElementKey = newSharedElementKey
                            },
                            onUpdateGridItemSource = { newGridItemSource ->
                                gridItemSource = newGridItemSource

                                associate = newGridItemSource.gridItem.associate
                            },
                            onUpdateIsLongPress = { newIsLongPress ->
                                isLongPress = newIsLongPress
                            },
                            onUpdateIsDragging = { newIsDragging ->
                                isDragging = newIsDragging
                            },
                        )
                    },
                )
            }

            PageIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PAGE_INDICATOR_HEIGHT),
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
                    .height(dockHeight)
                    .padding(
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
                    modifier = Modifier.fillMaxSize(),
                    gridItems = dockGridItemsByPage[page],
                    columns = homeSettings.dockColumns,
                    rows = homeSettings.dockRows,
                ) { gridItem ->
                    val cellWidth = safeDrawingWidth / homeSettings.dockColumns

                    val cellHeight = dockHeightPx / homeSettings.dockRows

                    val x = gridItem.startColumn * cellWidth

                    val y = gridItem.startRow * cellHeight

                    val width = gridItem.columnSpan * cellWidth

                    val height = gridItem.rowSpan * cellHeight

                    InteractiveGridItemContent(
                        drag = drag,
                        gridItem = gridItem,
                        gridItemSettings = homeSettings.gridItemSettings,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        iconPackFilePaths = iconPackFilePaths,
                        isScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
                        statusBarNotifications = statusBarNotifications,
                        textColor = textColor,
                        gridItemSource = gridItemSource,
                        isLongPress = isLongPress,
                        onDraggingGridItem = {
                            showGridItemPopup = false

                            onDraggingGridItem(gridItems)
                        },
                        onOpenAppDrawer = {
                            showAppDrawer = true
                        },
                        onTapApplicationInfo = { serialNumber, componentName ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + dockTopLeft

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
                        onTapFolderGridItem = {
                            onUpdateFolderGridItemId(gridItem.id)

                            lastFolderPopupX = x
                            lastFolderPopupY = y + dockTopLeft

                            lastFolderPopupWidth = width
                            lastFolderPopupHeight = height

                            folderPopupIntOffset = IntOffset(
                                x = x,
                                y = y + dockTopLeft,
                            )

                            folderPopupIntSize = IntSize(
                                width = width,
                                height = height,
                            )
                        },
                        onTapShortcutConfig = { uri ->
                            context.startActivity(parseUri(uri, 0))
                        },
                        onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + dockTopLeft

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
                        onUpdateGridItemOffset = { intOffset, intSize ->
                            popupIntOffset = intOffset

                            popupIntSize = IntSize(
                                width = intSize.width,
                                height = height,
                            )

                            overlayIntOffset = intOffset

                            overlayIntSize = intSize

                            showGridItemPopup = true
                        },
                        onUpdateImageBitmap = { newImageBitmap ->
                            overlayImageBitmap = newImageBitmap
                        },
                        onUpdateSharedElementKey = { newSharedElementKey ->
                            sharedElementKey = newSharedElementKey
                        },
                        onUpdateGridItemSource = { newGridItemSource ->
                            gridItemSource = newGridItemSource

                            associate = newGridItemSource.gridItem.associate
                        },
                        onUpdateIsLongPress = { newIsLongPress ->
                            isLongPress = newIsLongPress
                        },
                        onUpdateIsDragging = { newIsDragging ->
                            isDragging = newIsDragging
                        },
                    )
                }
            }
        }

        if (showGridItemPopup) {
            GridItemPopup(
                currentPage = currentPage,
                drag = drag,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                gridItem = gridItemSource?.gridItem,
                gridItemSettings = homeSettings.gridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                paddingValues = paddingValues,
                popupIntOffset = popupIntOffset,
                popupIntSize = popupIntSize,
                onDeleteGridItem = onDeleteGridItem,
                onDismissRequest = {
                    showGridItemPopup = false
                },
                onDraggingGridItem = {
                    onDraggingGridItem(gridItems)
                },
                onEdit = onEditGridItem,
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
                onResize = {
                    onResize(
                        Screen.Resize,
                        gridItems,
                    )
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
                onUpdateGridItemOffset = { intOffset, intSize ->
                    overlayIntOffset = intOffset

                    overlayIntSize = intSize
                },
                onUpdateSharedElementKey = { newSharedElementKey ->
                    sharedElementKey = newSharedElementKey
                },
                onWidgets = { newEblanApplicationInfoGroup: EblanApplicationInfoGroup ->
                    eblanApplicationInfoGroup = newEblanApplicationInfoGroup
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
            )
        }

        if (showSettingsPopup) {
            SettingsPopup(
                gridItems = gridItems,
                hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
                popupSettingsIntOffset = settingsPopupIntOffset,
                onDismissRequest = {
                    showSettingsPopup = false
                },
                onEditPage = onEditPage,
                onSettings = onSettings,
                onShortcutConfigActivities = {
                    showShortcutConfigActivities = true
                },
                onWallpaper = {
                    val intent = Intent(ACTION_SET_WALLPAPER)

                    val chooser = createChooser(intent, "Set Wallpaper")

                    context.startActivity(chooser)
                },
                onWidgets = {
                    showWidgets = true
                },
            )
        }

        if (folderGridItem != null) {
            FolderScreen(
                drag = drag,
                folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                folderGridItem = folderGridItem,
                folderPopupIntOffset = folderPopupIntOffset,
                folderPopupIntSize = folderPopupIntSize,
                gridItemSettings = homeSettings.gridItemSettings,
                homeSettings = homeSettings,
                iconPackFilePaths = iconPackFilePaths,
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                screenWidth = screenWidth,
                statusBarNotifications = statusBarNotifications,
                textColor = textColor,
                gridItemSource = gridItemSource,
                isLongPress = isLongPress,
                onDismissRequest = {
                    onUpdateFolderGridItemId(null)

                    lastFolderPopupX = 0
                    lastFolderPopupY = 0

                    lastFolderPopupWidth = 0
                    lastFolderPopupHeight = 0

                    folderPopupIntOffset = IntOffset.Zero

                    folderPopupIntSize = IntSize.Zero
                },
                onDraggingGridItem = {
                    showFolderGridItemPopup = false

                    onDraggingGridItem(gridItems)
                },
                onOpenAppDrawer = {
                    showAppDrawer = true
                },
                onUpdateGridItemOffset = { intOffset, intSize ->
                    popupIntOffset = intOffset

                    popupIntSize = intSize

                    overlayIntOffset = intOffset

                    overlayIntSize = intSize

                    showFolderGridItemPopup = true
                },
                onUpdateSharedElementKey = { newSharedElementKey ->
                    sharedElementKey = newSharedElementKey
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
                onUpdateIsDragging = { newIsDragging ->
                    isDragging = newIsDragging
                },
                onUpdateIsLongPress = { newIsLongPress ->
                    isLongPress = newIsLongPress
                },
            )
        }

        if (showFolderGridItemPopup) {
            FolderGridItemPopup(
                gridItemSource = gridItemSource,
                paddingValues = paddingValues,
                popupIntOffset = popupIntOffset,
                popupIntSize = popupIntSize,
                onDeleteApplicationInfoGridItem = onDeleteApplicationInfoGridItem,
                onDismissRequest = {
                    showFolderGridItemPopup = false
                },
                onEdit = onEditGridItem,
            )
        }

        if (gestureSettings.swipeUp.eblanActionType == EblanActionType.OpenAppDrawer || gestureSettings.swipeDown.eblanActionType == EblanActionType.OpenAppDrawer) {
            ApplicationScreen(
                alpha = applicationScreenAlpha,
                appDrawerSettings = appDrawerSettings,
                columns = homeSettings.columns,
                cornerSize = cornerSize,
                currentPage = currentPage,
                drag = drag,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanApplicationInfoTags = eblanApplicationInfoTags,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
                gridItemSource = gridItemSource,
                gridItems = gridItems,
                hasShortcutHostPermission = hasShortcutHostPermission,
                iconPackFilePaths = iconPackFilePaths,
                isPressHome = isPressHome,
                klwpIntegration = experimentalSettings.klwpIntegration,
                managedProfileResult = managedProfileResult,
                paddingValues = paddingValues,
                rows = homeSettings.rows,
                screenHeight = screenHeight,
                screenWidth = screenWidth,
                swipeY = swipeY.value,
                onDismiss = {
                    scope.launch {
                        swipeY.animateTo(
                            targetValue = screenHeight.toFloat(),
                            animationSpec = tween(
                                easing = FastOutSlowInEasing,
                            ),
                        )

                        isPressHome = false
                    }
                },
                onDragEnd = { remaining ->
                    scope.launch {
                        handleApplyFling(
                            offsetY = swipeY,
                            remaining = remaining,
                            screenHeight = screenHeight,
                        )
                    }
                },
                onDraggingGridItem = onDraggingGridItem,
                onEditApplicationInfo = onEditApplicationInfo,
                onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
                onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
                onUpdateEblanApplicationInfos = onUpdateEblanApplicationInfos,
                onUpdateGridItemOffset = { intOffset, intSize ->
                    overlayIntOffset = intOffset

                    overlayIntSize = intSize
                },
                onUpdateSharedElementKey = { newSharedElementKey ->
                    sharedElementKey = newSharedElementKey
                },
                onVerticalDrag = { dragAmount ->
                    scope.launch {
                        swipeY.snapTo(swipeY.value + dragAmount)
                    }
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
                onUpdateIsLongPressAndIsDragging = {
                    isLongPress = true

                    isDragging = true
                },
            )
        }

        if (showAppDrawer) {
            LaunchedEffect(key1 = Unit) {
                swipeY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }

            ApplicationScreen(
                alpha = applicationScreenAlpha,
                appDrawerSettings = appDrawerSettings,
                columns = homeSettings.columns,
                cornerSize = cornerSize,
                currentPage = currentPage,
                drag = drag,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanApplicationInfoTags = eblanApplicationInfoTags,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
                gridItemSource = gridItemSource,
                gridItems = gridItems,
                hasShortcutHostPermission = hasShortcutHostPermission,
                iconPackFilePaths = iconPackFilePaths,
                isPressHome = isPressHome,
                klwpIntegration = experimentalSettings.klwpIntegration,
                managedProfileResult = managedProfileResult,
                paddingValues = paddingValues,
                rows = homeSettings.rows,
                screenHeight = screenHeight,
                screenWidth = screenWidth,
                swipeY = swipeY.value,
                onDismiss = {
                    scope.launch {
                        swipeY.animateTo(
                            targetValue = screenHeight.toFloat(),
                            animationSpec = tween(
                                easing = FastOutSlowInEasing,
                            ),
                        )

                        showAppDrawer = false

                        isPressHome = false
                    }
                },
                onDragEnd = { remaining ->
                    scope.launch {
                        handleApplyFling(
                            offsetY = swipeY,
                            remaining = remaining,
                            screenHeight = screenHeight,
                            onDismiss = {
                                showAppDrawer = false
                            },
                        )
                    }
                },
                onDraggingGridItem = onDraggingGridItem,
                onEditApplicationInfo = onEditApplicationInfo,
                onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
                onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
                onUpdateEblanApplicationInfos = onUpdateEblanApplicationInfos,
                onUpdateGridItemOffset = { intOffset, intSize ->
                    overlayIntOffset = intOffset

                    overlayIntSize = intSize
                },
                onUpdateSharedElementKey = { newSharedElementKey ->
                    sharedElementKey = newSharedElementKey
                },
                onVerticalDrag = { dragAmount ->
                    scope.launch {
                        swipeY.snapTo(swipeY.value + dragAmount)
                    }
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
                onUpdateIsLongPressAndIsDragging = {
                    isLongPress = false

                    isDragging = false
                },
            )
        }

        if (showWidgets) {
            WidgetScreen(
                columns = homeSettings.columns,
                currentPage = currentPage,
                drag = drag,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                gridItemSettings = homeSettings.gridItemSettings,
                gridItems = gridItems,
                isPressHome = isPressHome,
                paddingValues = paddingValues,
                rows = homeSettings.rows,
                screenHeight = screenHeight,
                screenWidth = screenWidth,
                onDismiss = {
                    showWidgets = false

                    isPressHome = false
                },
                onDraggingGridItem = onDraggingGridItem,
                onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                onUpdateGridItemOffset = { intOffset, intSize ->
                    overlayIntOffset = intOffset

                    overlayIntSize = intSize
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
                onUpdateSharedElementKey = { newSharedElementKey ->
                    sharedElementKey = newSharedElementKey
                },
                onUpdateIsDragging = { newIsDragging ->
                    isDragging = newIsDragging
                },
            )
        }

        if (showShortcutConfigActivities) {
            ShortcutConfigScreen(
                currentPage = currentPage,
                drag = drag,
                eblanShortcutConfigs = eblanShortcutConfigs,
                gridItemSettings = homeSettings.gridItemSettings,
                gridItems = gridItems,
                isPressHome = isPressHome,
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                onDismiss = {
                    showShortcutConfigActivities = false

                    isPressHome = false
                },
                onDraggingGridItem = onDraggingGridItem,
                onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                onUpdateGridItemOffset = { intOffset, intSize ->
                    overlayIntOffset = intOffset

                    overlayIntSize = intSize
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
            )
        }

        if (eblanApplicationInfoGroup != null) {
            AppWidgetScreen(
                columns = homeSettings.columns,
                currentPage = currentPage,
                drag = drag,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                gridItemSettings = homeSettings.gridItemSettings,
                gridItems = gridItems,
                isPressHome = isPressHome,
                paddingValues = paddingValues,
                rows = homeSettings.rows,
                screenHeight = screenHeight,
                screenWidth = screenWidth,
                onDismiss = {
                    eblanApplicationInfoGroup = null

                    isPressHome = false
                },
                onDraggingGridItem = onDraggingGridItem,
                onUpdateGridItemOffset = { intOffset, intSize ->
                    overlayIntOffset = intOffset

                    overlayIntSize = intSize
                },
                onUpdateImageBitmap = { newImageBitmap ->
                    overlayImageBitmap = newImageBitmap
                },
                onUpdateGridItemSource = { newGridItemSource ->
                    gridItemSource = newGridItemSource

                    associate = newGridItemSource.gridItem.associate
                },
            )
        }

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

                drag = Drag.None
            },
        )
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