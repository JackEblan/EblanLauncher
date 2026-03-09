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
import android.content.Intent
import android.content.Intent.ACTION_SET_WALLPAPER
import android.content.Intent.createChooser
import android.content.Intent.parseUri
import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N_MR1
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.draw.alpha
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
import androidx.core.util.Consumer
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
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.drag.handleAnimateScrollToPage
import com.eblan.launcher.feature.home.screen.drag.handleAppWidgetLauncherResult
import com.eblan.launcher.feature.home.screen.drag.handleBoundWidget
import com.eblan.launcher.feature.home.screen.drag.handleConfigureLauncherResult
import com.eblan.launcher.feature.home.screen.drag.handleConflictingGridItem
import com.eblan.launcher.feature.home.screen.drag.handleDeleteAppWidgetId
import com.eblan.launcher.feature.home.screen.drag.handleDragGridItem
import com.eblan.launcher.feature.home.screen.drag.handleDropGridItem
import com.eblan.launcher.feature.home.screen.drag.handlePageDirection
import com.eblan.launcher.feature.home.screen.drag.handleShortcutConfigIntentSenderLauncherResult
import com.eblan.launcher.feature.home.screen.drag.handleShortcutConfigLauncherResult
import com.eblan.launcher.feature.home.screen.folder.FolderScreen
import com.eblan.launcher.feature.home.screen.shortcutconfig.ShortcutConfigScreen
import com.eblan.launcher.feature.home.screen.widget.AppWidgetScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun SharedTransitionScope.PagerScreen(
    modifier: Modifier = Modifier,
    appDrawerSettings: AppDrawerSettings,
    currentPage: Int,
    dockGridHorizontalPagerState: PagerState,
    dockGridItemsByPage: Map<Int, List<GridItem>>,
    drag: Drag,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    experimentalSettings: ExperimentalSettings,
    folderGridHorizontalPagerState: PagerState,
    folderGridItem: GridItem?,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    gestureSettings: GestureSettings,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    gridHorizontalPagerState: PagerState,
    gridItemSource: GridItemSource?,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    homeSettings: HomeSettings,
    iconPackFilePaths: Map<String, String>,
    managedProfileResult: ManagedProfileResult?,
    paddingValues: PaddingValues,
    screen: Screen,
    screenHeight: Int,
    screenWidth: Int,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    associate: Associate?,
    configureResultCode: Int?,
    dragIntOffset: IntOffset,
    lockMovement: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    onDeleteApplicationInfoGridItem: (ApplicationInfoGridItem) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDraggingGridItem: (
        screen: Screen,
        gridItems: List<GridItem>,
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
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onResize: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onSettings: () -> Unit,
    onTapFolderGridItem: (
        id: String?,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfos: (List<EblanApplicationInfo>) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
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

    //Drag
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

    var isLongPress by remember { mutableStateOf(false) }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            appWidgetManager = appWidgetManager,
            gridItemSource = gridItemSource,
            result = result,
            isLongPress = isLongPress,
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
                isLongPress = isLongPress,
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
                isLongPress = isLongPress,
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
            isLongPress = isLongPress,
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
                    isLongPress = isLongPress,
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
                    onUpdateIsLongPress = { newIsLongPress ->
                        isLongPress = newIsLongPress
                    },
                )

                onResetConfigureResultCode()
            }

            Drag.Cancel -> {
                isLongPress = false

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
            isLongPress = isLongPress,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onUpdateIsLongPress = { newIsLongPress ->
                isLongPress = newIsLongPress
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
            isLongPress = isLongPress,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
            onUpdateIsLongPress = { newIsLongPress ->
                isLongPress = newIsLongPress
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
            isLongPress = isLongPress,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMoveWidgetGridItem = onDragEndAfterMoveWidgetGridItem,
            onResetConfigureResultCode = onResetConfigureResultCode,
            onUpdateIsLongPress = { newIsLongPress ->
                isLongPress = newIsLongPress
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
    //Drag

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
                        screen = screen,
                        statusBarNotifications = statusBarNotifications,
                        textColor = textColor,
                        gridItemSource = gridItemSource,
                        isLongPress = isLongPress,
                        onDraggingGridItem = {
                            showGridItemPopup = false

                            onDraggingGridItem(
                                Screen.Drag,
                                gridItems,
                            )
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
                            onTapFolderGridItem(
                                gridItem.id,
                                IntOffset(
                                    x = x,
                                    y = y,
                                ),
                                IntSize(
                                    width = width,
                                    height = height,
                                ),
                            )
                        },
                        onTapShortcutConfig = { uri ->
                            context.startActivity(parseUri(uri, 0))
                        },
                        onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                            val sourceBoundsX = x + leftPadding

                            val sourceBoundsY = y + topPadding

                            if (SDK_INT >= N_MR1) {
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

                            onUpdateGridItemOffset(intOffset, intSize)

                            showGridItemPopup = true
                        },
                        onUpdateImageBitmap = onUpdateImageBitmap,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsLongPress = { newIsLongPress ->
                            isLongPress = newIsLongPress
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
                    screen = screen,
                    statusBarNotifications = statusBarNotifications,
                    textColor = textColor,
                    gridItemSource = gridItemSource,
                    isLongPress = isLongPress,
                    onDraggingGridItem = {
                        showGridItemPopup = false

                        onDraggingGridItem(
                            Screen.Drag,
                            gridItems,
                        )
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
                        onTapFolderGridItem(
                            gridItem.id,
                            IntOffset(
                                x = x,
                                y = y + dockTopLeft,
                            ),
                            IntSize(
                                width = width,
                                height = height,
                            ),
                        )
                    },
                    onTapShortcutConfig = { uri ->
                        context.startActivity(parseUri(uri, 0))
                    },
                    onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                        val sourceBoundsX = x + leftPadding

                        val sourceBoundsY = y + dockTopLeft

                        if (SDK_INT >= N_MR1) {
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

                        onUpdateGridItemOffset(intOffset, intSize)

                        showGridItemPopup = true
                    },
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onUpdateGridItemSource = onUpdateGridItemSource,
                    onUpdateIsLongPress = { newIsLongPress ->
                        isLongPress = newIsLongPress
                    },
                )
            }
        }
    }

    if (showGridItemPopup && gridItemSource?.gridItem != null) {
        GridItemPopup(
            currentPage = currentPage,
            drag = drag,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            gridItem = gridItemSource.gridItem,
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
                onDraggingGridItem(
                    Screen.Drag,
                    gridItems,
                )
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

                if (SDK_INT >= N_MR1) {
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
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onWidgets = { newEblanApplicationInfoGroup: EblanApplicationInfoGroup ->
                eblanApplicationInfoGroup = newEblanApplicationInfoGroup
            },
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
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
            onDismissRequest = {
                onTapFolderGridItem(
                    null,
                    IntOffset.Zero,
                    IntSize.Zero,
                )
            },
            onDraggingGridItem = {
                onDraggingGridItem(
                    Screen.Drag,
                    gridItems,
                )
            },
            onOpenAppDrawer = {
                showAppDrawer = true
            },
            onUpdateGridItemOffset = { intOffset, intSize ->
                popupIntOffset = intOffset

                popupIntSize = intSize

                onUpdateGridItemOffset(intOffset, intSize)

                showFolderGridItemPopup = true
            },
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
        )
    }

    if (showFolderGridItemPopup && gridItemSource != null) {
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
            screen = screen,
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
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onVerticalDrag = { dragAmount ->
                scope.launch {
                    swipeY.snapTo(swipeY.value + dragAmount)
                }
            },
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
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
            screen = screen,
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
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onVerticalDrag = { dragAmount ->
                scope.launch {
                    swipeY.snapTo(swipeY.value + dragAmount)
                }
            },
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
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
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
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
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
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
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateGridItemSource = onUpdateGridItemSource,
        )
    }
}
