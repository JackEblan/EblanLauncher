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

import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.window.Popup
import androidx.core.util.Consumer
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GlobalAction
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.GridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu
import com.eblan.launcher.feature.home.component.pageindicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.application.DoubleTapApplicationScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.launch

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    drag: Drag,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    gestureSettings: GestureSettings,
    appDrawerSettings: AppDrawerSettings,
    gridItemSource: GridItemSource?,
    homeSettings: HomeSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    iconPackInfoPackageName: String,
    gridHorizontalPagerState: PagerState,
    currentPage: Int,
    onTapFolderGridItem: (String) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetOverlay: () -> Unit,
) {
    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    var showDoubleTap by remember { mutableStateOf(false) }

    var showWidgets by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val swipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val swipeDownY = remember { Animatable(screenHeight.toFloat()) }

    val offsetY by remember {
        derivedStateOf {
            if (swipeUpY.value < screenHeight && gestureSettings.swipeUp is GestureAction.OpenAppDrawer) {
                swipeUpY.value
            } else if (swipeDownY.value < screenHeight && gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
                swipeDownY.value
            } else {
                screenHeight.toFloat()
            }
        }
    }

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val view = LocalView.current

    val activity = LocalActivity.current as ComponentActivity

    val isApplicationComponentVisible by remember {
        derivedStateOf {
            offsetY == 0f
        }
    }

    DisposableEffect(key1 = scope) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleOnNewIntent(
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    intent = intent,
                    initialPage = homeSettings.initialPage,
                    wallpaperScroll = homeSettings.wallpaperScroll,
                    wallpaperManagerWrapper = wallpaperManagerWrapper,
                    pageCount = homeSettings.pageCount,
                    infiniteScroll = homeSettings.infiniteScroll,
                    windowToken = view.windowToken,
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    HorizontalPagerScreen(
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
                        doGestureActions(
                            gestureSettings = gestureSettings,
                            swipeUpY = swipeUpY.value,
                            swipeDownY = swipeDownY.value,
                            screenHeight = screenHeight,
                            onStartMainActivity = { componentName ->
                                launcherApps.startMainActivity(
                                    componentName = componentName,
                                    sourceBounds = Rect(),
                                )
                            },
                            onPerformGlobalAction = { globalAction ->
                                val intent = Intent(GlobalAction.NAME)
                                    .putExtra(
                                        GlobalAction.GLOBAL_ACTION_TYPE,
                                        globalAction.name,
                                    )

                                context.sendBroadcast(intent)
                            },
                        )

                        resetSwipeOffset(
                            scope = scope,
                            gestureSettings = gestureSettings,
                            swipeDownY = swipeDownY,
                            screenHeight = screenHeight,
                            swipeUpY = swipeUpY,
                        )
                    },
                    onDragCancel = {
                        scope.launch {
                            swipeUpY.animateTo(screenHeight.toFloat())

                            swipeDownY.animateTo(screenHeight.toFloat())
                        }
                    },
                )
            },
        gridHorizontalPagerState = gridHorizontalPagerState,
        currentPage = currentPage,
        isApplicationComponentVisible = isApplicationComponentVisible,
        gridItems = gridItems,
        gridItemsByPage = gridItemsByPage,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        paddingValues = paddingValues,
        dockGridItems = dockGridItems,
        textColor = textColor,
        drag = drag,
        hasShortcutHostPermission = hasShortcutHostPermission,
        hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
        gridItemSource = gridItemSource,
        homeSettings = homeSettings,
        iconPackInfoPackageName = iconPackInfoPackageName,
        onTapFolderGridItem = onTapFolderGridItem,
        onEdit = onEdit,
        onResize = onResize,
        onSettings = onSettings,
        onEditPage = onEditPage,
        onWidgets = {
            showWidgets = true
        },
        onDoubleTap = {
            showDoubleTap = true
        },
        onLongPressGridItem = onLongPressGridItem,
        onUpdateGridItemOffset = onUpdateGridItemOffset,
        onDraggingGridItem = onDraggingGridItem,
        onDeleteGridItem = onDeleteGridItem,
        onResetOverlay = onResetOverlay,
    )

    if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer ||
        gestureSettings.swipeDown is GestureAction.OpenAppDrawer
    ) {
        ApplicationScreen(
            currentPage = currentPage,
            offsetY = offsetY,
            isApplicationComponentVisible = isApplicationComponentVisible,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            paddingValues = paddingValues,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
            iconPackInfoPackageName = iconPackInfoPackageName,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
            gridItemSource = gridItemSource,
            onDismiss = {
                scope.launch {
                    swipeUpY.snapTo(screenHeight.toFloat())

                    swipeDownY.snapTo(screenHeight.toFloat())
                }
            },
            onAnimateDismiss = {
                scope.launch {
                    swipeUpY.animateTo(screenHeight.toFloat())

                    swipeDownY.animateTo(screenHeight.toFloat())
                }
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
        )
    }

    if (showDoubleTap) {
        when (val gestureAction = gestureSettings.doubleTap) {
            GestureAction.None -> {
            }

            is GestureAction.OpenApp -> {
                SideEffect {
                    launcherApps.startMainActivity(
                        componentName = gestureAction.componentName,
                        sourceBounds = Rect(),
                    )

                    showDoubleTap = false
                }
            }

            GestureAction.OpenAppDrawer -> {
                DoubleTapApplicationScreen(
                    currentPage = currentPage,
                    isApplicationComponentVisible = isApplicationComponentVisible,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    paddingValues = paddingValues,
                    drag = drag,
                    screenHeight = screenHeight,
                    appDrawerSettings = appDrawerSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    gridItemSource = gridItemSource,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    onDismiss = {
                        showDoubleTap = false
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onDraggingGridItem = onDraggingGridItem,
                    onResetOverlay = onResetOverlay,
                )
            }

            GestureAction.OpenNotificationPanel -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME)
                        .putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.Notifications.name,
                        )

                    context.sendBroadcast(intent)

                    showDoubleTap = false
                }
            }

            GestureAction.LockScreen -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME)
                        .putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.LockScreen.name,
                        )

                    context.sendBroadcast(intent)

                    showDoubleTap = false
                }
            }

            GestureAction.OpenQuickSettings -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME)
                        .putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.QuickSettings.name,
                        )

                    context.sendBroadcast(intent)

                    showDoubleTap = false
                }
            }

            GestureAction.OpenRecents -> {
                SideEffect {
                    val intent = Intent(GlobalAction.NAME)
                        .putExtra(
                            GlobalAction.GLOBAL_ACTION_TYPE,
                            GlobalAction.Recents.name,
                        )

                    context.sendBroadcast(intent)

                    showDoubleTap = false
                }
            }
        }
    }

    if (showWidgets) {
        WidgetScreen(
            currentPage = currentPage,
            isApplicationComponentVisible = isApplicationComponentVisible,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            drag = drag,
            eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
            appDrawerSettings = appDrawerSettings,
            onDismiss = {
                showWidgets = false
            },
            onDraggingGridItem = onDraggingGridItem,
            onResetOverlay = onResetOverlay,
        )
    }
}

@Composable
private fun HorizontalPagerScreen(
    modifier: Modifier = Modifier,
    gridHorizontalPagerState: PagerState,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridWidth: Int,
    gridHeight: Int,
    paddingValues: PaddingValues,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    gridItemSource: GridItemSource?,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    hasSystemFeatureAppWidgets: Boolean,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    onTapFolderGridItem: (String) -> Unit,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetOverlay: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeight = homeSettings.dockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    var showPopupGridItemMenu by remember { mutableStateOf(false) }

    var showPopupSettingsMenu by remember { mutableStateOf(false) }

    var popupSettingsMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    val launcherApps = LocalLauncherApps.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val context = LocalContext.current

    val view = LocalView.current

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupGridItemMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val pageIndicatorHeight = 30.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    LaunchedEffect(key1 = drag) {
        if (!isApplicationComponentVisible && drag == Drag.Dragging) {
            onDraggingGridItem()

            showPopupGridItemMenu = false
        }
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onLongPress = { offset ->
                        popupSettingsMenuIntOffset = offset.round()

                        showPopupSettingsMenu = true
                    },
                )
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
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
                    val cellWidth = gridWidth / homeSettings.columns

                    val cellHeight =
                        (gridHeight - pageIndicatorHeightPx - dockHeightPx) / homeSettings.rows

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
                        iconPackInfoPackageName = iconPackInfoPackageName,
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
                        onTapFolderGridItem = {
                            onTapFolderGridItem(gridItem.id)
                        },
                        onLongPress = {
                            val intOffset = IntOffset(x = x + leftPadding, y = y + topPadding)

                            val intSize = IntSize(width = width, height = height)

                            popupMenuIntOffset = IntOffset(x = x, y = y)

                            popupGridItemMenuIntSize = IntSize(width = width, height = height)

                            onUpdateGridItemOffset(intOffset, intSize)
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                GridItemSource.Existing(gridItem = gridItem),
                                imageBitmap,
                            )

                            showPopupGridItemMenu = true
                        },
                        onResetOverlay = onResetOverlay,
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(pageIndicatorHeight),
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
            gridItems = dockGridItems,
            columns = homeSettings.dockColumns,
            rows = homeSettings.dockRows,
            { gridItem ->
                val cellWidth = gridWidth / homeSettings.dockColumns

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
                    iconPackInfoPackageName = iconPackInfoPackageName,
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
                    onTapFolderGridItem = {
                        onTapFolderGridItem(gridItem.id)
                    },
                    onLongPress = {
                        val dockY =
                            y + (gridHeight - dockHeightPx)

                        val intOffset = IntOffset(x = x + leftPadding, y = dockY + topPadding)

                        val intSize = IntSize(width = width, height = height)

                        popupMenuIntOffset = intOffset

                        popupGridItemMenuIntSize = IntSize(width = width, height = height)

                        onUpdateGridItemOffset(intOffset, intSize)
                    },
                    onUpdateImageBitmap = { imageBitmap ->
                        onLongPressGridItem(
                            GridItemSource.Existing(gridItem = gridItem),
                            imageBitmap,
                        )

                        showPopupGridItemMenu = true
                    },
                    onResetOverlay = onResetOverlay,
                )
            },
        )
    }

    if (showPopupGridItemMenu && gridItemSource?.gridItem != null) {
        PopupGridItemMenu(
            gridItem = gridItemSource.gridItem,
            x = popupMenuIntOffset.x,
            y = popupMenuIntOffset.y,
            width = popupGridItemMenuIntSize.width,
            height = popupGridItemMenuIntSize.height,
            onEdit = onEdit,
            onResize = onResize,
            onDeleteGridItem = onDeleteGridItem,
            onInfo = { serialNumber, componentName ->
                launcherApps.startAppDetailsActivity(
                    serialNumber = serialNumber,
                    componentName = componentName,
                    sourceBounds = Rect(
                        popupMenuIntOffset.x,
                        popupMenuIntOffset.y,
                        popupMenuIntOffset.x + popupGridItemMenuIntSize.width,
                        popupMenuIntOffset.y + popupGridItemMenuIntSize.height,
                    ),
                )
            },
            onDismissRequest = {
                showPopupGridItemMenu = false
            },
        )
    }

    if (showPopupSettingsMenu) {
        PopupSettingsMenu(
            popupSettingsMenuIntOffset = popupSettingsMenuIntOffset,
            gridItems = gridItems,
            hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
            onSettings = onSettings,
            onEditPage = onEditPage,
            onWidgets = onWidgets,
            onWallpaper = {
                val intent = Intent(Intent.ACTION_SET_WALLPAPER)

                val chooser = Intent.createChooser(intent, "Set Wallpaper")

                context.startActivity(chooser)
            },
            onDismissRequest = {
                showPopupSettingsMenu = false
            },
        )
    }
}

@Composable
private fun PopupSettingsMenu(
    popupSettingsMenuIntOffset: IntOffset,
    gridItems: List<GridItem>,
    hasSystemFeatureAppWidgets: Boolean,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onWallpaper: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Popup(
        popupPositionProvider = SettingsMenuPositionProvider(
            x = popupSettingsMenuIntOffset.x,
            y = popupSettingsMenuIntOffset.y,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        SettingsMenu(
            hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
            onSettings = {
                onSettings()

                onDismissRequest()
            },
            onEditPage = {
                onEditPage(gridItems)

                onDismissRequest()
            },

            onWidgets = {
                onWidgets()

                onDismissRequest()
            },
            onWallpaper = {
                onWallpaper()

                onDismissRequest()
            },
        )
    }
}

@Composable
private fun PopupGridItemMenu(
    gridItem: GridItem,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    onEdit: (String) -> Unit,
    onResize: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onInfo: (
        serialNumber: Long,
        componentName: String?,
    ) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = width,
            height = height,
        ),
        onDismissRequest = onDismissRequest,
        content = {
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItemMenu(
                        onEdit = {
                            onEdit(gridItem.id)

                            onDismissRequest()
                        },
                        onResize = {
                            onResize()

                            onDismissRequest()
                        },
                        onInfo = {
                            onInfo(
                                data.serialNumber,
                                data.componentName,
                            )

                            onDismissRequest()
                        },
                        onDelete = {
                            onDeleteGridItem(gridItem)

                            onDismissRequest()
                        },
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    GridItemMenu(
                        onEdit = {
                            onEdit(gridItem.id)

                            onDismissRequest()
                        },
                        onResize = {
                            onResize()

                            onDismissRequest()
                        },
                        onDelete = {
                            onDeleteGridItem(gridItem)

                            onDismissRequest()
                        },
                    )
                }

                is GridItemData.Folder -> {
                    GridItemMenu(
                        onEdit = {
                            onEdit(gridItem.id)

                            onDismissRequest()
                        },
                        onResize = {
                            onResize()

                            onDismissRequest()
                        },
                        onDelete = {
                            onDeleteGridItem(gridItem)

                            onDismissRequest()
                        },
                    )
                }

                is GridItemData.Widget -> {
                    val showResize = data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                    WidgetGridItemMenu(
                        showResize = showResize,
                        onResize = {
                            onResize()

                            onDismissRequest()
                        },
                        onDelete = {
                            onDeleteGridItem(gridItem)

                            onDismissRequest()
                        },
                    )
                }
            }
        },
    )
}
