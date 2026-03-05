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
import android.content.Intent.ACTION_SET_WALLPAPER
import android.content.Intent.createChooser
import android.content.Intent.parseUri
import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N_MR1
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.ManagedProfileResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.component.popup.GridItemPopup
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.GridItemSource.Existing
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
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun SharedTransitionScope.PagerScreen(
    modifier: Modifier = Modifier,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    drag: Drag,
    dockGridItemsByPage: Map<Int, List<GridItem>>,
    textColor: TextColor,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    gestureSettings: GestureSettings,
    appDrawerSettings: AppDrawerSettings,
    gridItemSource: GridItemSource?,
    homeSettings: HomeSettings,
    gridHorizontalPagerState: PagerState,
    dockGridHorizontalPagerState: PagerState,
    currentPage: Int,
    statusBarNotifications: Map<String, Int>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    iconPackFilePaths: Map<String, String>,
    managedProfileResult: ManagedProfileResult?,
    screen: Screen,
    experimentalSettings: ExperimentalSettings,
    getEblanApplicationInfosByLabel: GetEblanApplicationInfosByLabel,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    folderGridHorizontalPagerState: PagerState,
    folderGridItem: GridItem?,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    onDraggingGridItem: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onResize: (
        screen: Screen,
        gridItems: List<GridItem>,
    ) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDeleteApplicationInfoGridItem: (ApplicationInfoGridItem) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onGetEblanApplicationInfosByTagIds: (List<Long>) -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfos: (List<EblanApplicationInfo>) -> Unit,
    onTapFolderGridItem: (
        id: String?,
        intOffset: IntOffset,
        intSize: IntSize,
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

    LaunchedEffect(key1 = hasDoubleTap) {
        handleHasDoubleTap(
            hasDoubleTap = hasDoubleTap,
            gestureSettings = gestureSettings,
            launcherApps = launcherApps,
            context = context,
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
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    intent = intent,
                    initialPage = homeSettings.initialPage,
                    wallpaperScroll = homeSettings.wallpaperScroll,
                    wallpaperManagerWrapper = wallpaperManagerWrapper,
                    pageCount = homeSettings.pageCount,
                    infiniteScroll = homeSettings.infiniteScroll,
                    windowToken = view.windowToken,
                    swipeY = swipeY,
                    screenHeight = screenHeight,
                    showWidgets = showWidgets,
                    showShortcutConfigActivities = showShortcutConfigActivities,
                    eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                    onHome = {
                        isPressHome = true
                    },
                )

                handleEblanActionIntent(
                    intent = intent,
                    launcherApps = launcherApps,
                    context = context,
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
            klwpIntegration = experimentalSettings.klwpIntegration,
            isApplicationScreenVisible = isApplicationScreenVisible,
            context = context,
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
            wallpaperScroll = homeSettings.wallpaperScroll,
            wallpaperManagerWrapper = wallpaperManagerWrapper,
            pageCount = homeSettings.pageCount,
            infiniteScroll = homeSettings.infiniteScroll,
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
                            gestureSettings = gestureSettings,
                            swipeUpY = swipeUpY.value,
                            swipeDownY = swipeDownY.value,
                            screenHeight = screenHeight,
                            launcherApps = launcherApps,
                            context = context,
                        )

                        scope.launch {
                            resetSwipeOffset(
                                gestureSettings = gestureSettings,
                                swipeDownY = swipeDownY,
                                screenHeight = screenHeight,
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
                        gridItem = gridItem,
                        gridItemSettings = homeSettings.gridItemSettings,
                        textColor = textColor,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        drag = drag,
                        statusBarNotifications = statusBarNotifications,
                        isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
                        iconPackFilePaths = iconPackFilePaths,
                        screen = screen,
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
                        onTapShortcutConfig = { uri ->
                            context.startActivity(parseUri(uri, 0))
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
                        onUpdateGridItemOffset = { intOffset, intSize ->
                            popupIntOffset = intOffset

                            popupIntSize = IntSize(
                                width = intSize.width,
                                height = height,
                            )

                            onUpdateGridItemOffset(intOffset, intSize)

                            showGridItemPopup = true
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                Existing(gridItem = gridItem),
                                imageBitmap,
                            )
                        },
                        onDraggingGridItem = {
                            showGridItemPopup = false

                            onDraggingGridItem(
                                Screen.Drag,
                                gridItems,
                            )
                        },
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onOpenAppDrawer = {
                            showAppDrawer = true
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
                systemTextColor = textColor,
                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
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
                    gridItem = gridItem,
                    gridItemSettings = homeSettings.gridItemSettings,
                    textColor = textColor,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    drag = drag,
                    statusBarNotifications = statusBarNotifications,
                    isScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = screen,
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
                    onTapShortcutConfig = { uri ->
                        context.startActivity(parseUri(uri, 0))
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
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        popupIntOffset = intOffset

                        popupIntSize = IntSize(
                            width = intSize.width,
                            height = height,
                        )

                        onUpdateGridItemOffset(intOffset, intSize)

                        showGridItemPopup = true
                    },
                    onUpdateImageBitmap = { imageBitmap ->
                        onLongPressGridItem(
                            Existing(gridItem = gridItem),
                            imageBitmap,
                        )
                    },
                    onDraggingGridItem = {
                        showGridItemPopup = false

                        onDraggingGridItem(
                            Screen.Drag,
                            gridItems,
                        )
                    },
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onOpenAppDrawer = {
                        showAppDrawer = true
                    },
                )
            }
        }
    }

    if (showGridItemPopup && gridItemSource?.gridItem != null) {
        GridItemPopup(
            gridItem = gridItemSource.gridItem,
            popupIntOffset = popupIntOffset,
            popupIntSize = popupIntSize,
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            hasShortcutHostPermission = hasShortcutHostPermission,
            currentPage = currentPage,
            drag = drag,
            gridItemSettings = homeSettings.gridItemSettings,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            paddingValues = paddingValues,
            onEdit = onEditGridItem,
            onResize = {
                onResize(
                    Screen.Resize,
                    gridItems,
                )
            },
            onWidgets = { newEblanApplicationInfoGroup: EblanApplicationInfoGroup ->
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
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDraggingGridItem = {
                onDraggingGridItem(
                    Screen.Drag,
                    gridItems,
                )
            },
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }

    if (showSettingsPopup) {
        SettingsPopup(
            popupSettingsIntOffset = settingsPopupIntOffset,
            gridItems = gridItems,
            hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
            onSettings = onSettings,
            onEditPage = onEditPage,
            onWidgets = {
                showWidgets = true
            },
            onShortcutConfigActivities = {
                showShortcutConfigActivities = true
            },
            onWallpaper = {
                val intent = Intent(ACTION_SET_WALLPAPER)

                val chooser = createChooser(intent, "Set Wallpaper")

                context.startActivity(chooser)
            },
            onDismissRequest = {
                showSettingsPopup = false
            },
        )
    }

    if (folderGridItem != null) {
        FolderScreen(
            folderGridItem = folderGridItem,
            folderPopupIntOffset = folderPopupIntOffset,
            folderPopupIntSize = folderPopupIntSize,
            paddingValues = paddingValues,
            folderGridHorizontalPagerState = folderGridHorizontalPagerState,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            homeSettings = homeSettings,
            textColor = textColor,
            gridItemSettings = homeSettings.gridItemSettings,
            statusBarNotifications = statusBarNotifications,
            iconPackFilePaths = iconPackFilePaths,
            drag = drag,
            onDismissRequest = {
                onTapFolderGridItem(
                    null,
                    IntOffset.Zero,
                    IntSize.Zero,
                )
            },
            onUpdateGridItemOffset = { intOffset, intSize ->
                popupIntOffset = intOffset

                popupIntSize = intSize

                onUpdateGridItemOffset(intOffset, intSize)

                showFolderGridItemPopup = true
            },
            onDraggingGridItem = {
                onDraggingGridItem(
                    Screen.Drag,
                    gridItems,
                )
            },
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onLongPressGridItem = onLongPressGridItem,
            onOpenAppDrawer = {
                showAppDrawer = true
            },
        )
    }

    if (showFolderGridItemPopup && gridItemSource != null) {
        FolderGridItemPopup(
            gridItemSource = gridItemSource,
            popupIntOffset = popupIntOffset,
            popupIntSize = popupIntSize,
            paddingValues = paddingValues,
            onEdit = onEditGridItem,
            onDeleteApplicationInfoGridItem = onDeleteApplicationInfoGridItem,
            onDismissRequest = {
                showFolderGridItemPopup = false
            },
        )
    }

    if (gestureSettings.swipeUp.eblanActionType == EblanActionType.OpenAppDrawer || gestureSettings.swipeDown.eblanActionType == EblanActionType.OpenAppDrawer) {
        ApplicationScreen(
            currentPage = currentPage,
            swipeY = swipeY.value,
            getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
            paddingValues = paddingValues,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            hasShortcutHostPermission = hasShortcutHostPermission,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            iconPackFilePaths = iconPackFilePaths,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
            gridItemSource = gridItemSource,
            isPressHome = isPressHome,
            managedProfileResult = managedProfileResult,
            screen = screen,
            gridItems = gridItems,
            klwpIntegration = experimentalSettings.klwpIntegration,
            alpha = applicationScreenAlpha,
            cornerSize = cornerSize,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            eblanApplicationInfoTags = eblanApplicationInfoTags,
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
            onDraggingGridItem = onDraggingGridItem,
            onVerticalDrag = { dragAmount ->
                scope.launch {
                    swipeY.snapTo(swipeY.value + dragAmount)
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
            onEditApplicationInfo = onEditApplicationInfo,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
            onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
            onUpdateEblanApplicationInfos = onUpdateEblanApplicationInfos,
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
            currentPage = currentPage,
            swipeY = swipeY.value,
            getEblanApplicationInfosByLabel = getEblanApplicationInfosByLabel,
            paddingValues = paddingValues,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            hasShortcutHostPermission = hasShortcutHostPermission,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            iconPackFilePaths = iconPackFilePaths,
            managedProfileResult = managedProfileResult,
            screen = screen,
            gridItems = gridItems,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
            gridItemSource = gridItemSource,
            isPressHome = isPressHome,
            klwpIntegration = experimentalSettings.klwpIntegration,
            alpha = applicationScreenAlpha,
            cornerSize = cornerSize,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            eblanApplicationInfoTags = eblanApplicationInfoTags,
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
            onDraggingGridItem = onDraggingGridItem,
            onVerticalDrag = { dragAmount ->
                scope.launch {
                    swipeY.snapTo(swipeY.value + dragAmount)
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
            onEditApplicationInfo = onEditApplicationInfo,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onGetEblanApplicationInfosByTagIds = onGetEblanApplicationInfosByTagIds,
            onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
            onUpdateEblanApplicationInfos = onUpdateEblanApplicationInfos,
        )
    }

    if (showWidgets) {
        WidgetScreen(
            currentPage = currentPage,
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            isPressHome = isPressHome,
            screen = screen,
            gridItems = gridItems,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
            onDismiss = {
                showWidgets = false

                isPressHome = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }

    if (showShortcutConfigActivities) {
        ShortcutConfigScreen(
            currentPage = currentPage,
            eblanShortcutConfigs = eblanShortcutConfigs,
            paddingValues = paddingValues,
            drag = drag,
            gridItemSettings = homeSettings.gridItemSettings,
            screenHeight = screenHeight,
            isPressHome = isPressHome,
            screen = screen,
            gridItems = gridItems,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
            onDismiss = {
                showShortcutConfigActivities = false

                isPressHome = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }

    if (eblanApplicationInfoGroup != null) {
        AppWidgetScreen(
            currentPage = currentPage,
            eblanApplicationInfoGroup = eblanApplicationInfoGroup,
            eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            isPressHome = isPressHome,
            screen = screen,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            gridItems = gridItems,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDismiss = {
                eblanApplicationInfoGroup = null

                isPressHome = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )
    }
}
