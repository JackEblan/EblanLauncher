package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.ClipData
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalFileManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.local.LocalWallpaperManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    targetPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridItem: GridItem?,
    dockHeight: Int,
    drag: Drag,
    dockGridItems: List<GridItem>,
    textColor: Long,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    rootWidth: Int,
    rootHeight: Int,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    dragIntOffset: IntOffset,
    wallpaperScroll: Boolean,
    gestureSettings: GestureSettings,
    onLongPressGrid: (Int) -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onDragStartPinItemRequest: (GridItemSource) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val gridHorizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + targetPage else targetPage,
        pageCount = {
            if (infiniteScroll) {
                Int.MAX_VALUE
            } else {
                pageCount
            }
        },
    )

    var showDoubleTap by remember { mutableStateOf(false) }

    val swipeUpY = remember { Animatable(rootHeight.toFloat()) }

    val swipeDownY = remember { Animatable(rootHeight.toFloat()) }

    val scope = rememberCoroutineScope()

    val applicationComponentY by remember {
        derivedStateOf {
            if (swipeUpY.value < rootHeight && gestureSettings.swipeUp is GestureAction.OpenAppDrawer) {
                swipeUpY.value
            } else if (swipeDownY.value < rootHeight && gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
                swipeDownY.value
            } else {
                rootHeight.toFloat()
            }
        }
    }

    HorizontalPagerScreen(
        modifier = modifier.pointerInput(Unit) {
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
                        swipeUpY = swipeUpY,
                        rootHeight = rootHeight,
                        swipeDownY = swipeDownY,
                        onStartMainActivity = launcherApps::startMainActivity,
                    )

                    resetSwipeOffset(
                        scope = scope,
                        gestureSettings = gestureSettings,
                        swipeDownY = swipeDownY,
                        rootHeight = rootHeight,
                        swipeUpY = swipeUpY,
                    )
                },
                onDragCancel = {
                    scope.launch {
                        swipeUpY.animateTo(rootHeight.toFloat())

                        swipeDownY.animateTo(rootHeight.toFloat())
                    }
                },
            )
        },
        horizontalPagerState = gridHorizontalPagerState,
        rows = rows,
        columns = columns,
        pageCount = pageCount,
        infiniteScroll = infiniteScroll,
        dockRows = dockRows,
        dockColumns = dockColumns,
        gridItemsByPage = gridItemsByPage,
        gridItem = gridItem,
        dockHeight = dockHeight,
        dockGridItems = dockGridItems,
        textColor = textColor,
        rootWidth = rootWidth,
        rootHeight = rootHeight,
        drag = drag,
        hasShortcutHostPermission = hasShortcutHostPermission,
        wallpaperScroll = wallpaperScroll,
        onLongPressGrid = onLongPressGrid,
        onLongPressGridItem = onLongPressGridItem,
        onDraggingGridItem = onDraggingGridItem,
        onEdit = onEdit,
        onResize = onResize,
        onSettings = onSettings,
        onEditPage = onEditPage,
        onDragStartPinItemRequest = onDragStartPinItemRequest,
        onDoubleTap = {
            showDoubleTap = true
        },
    )

    if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer || gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
        ApplicationComponentScreen(
            modifier = Modifier.offset {
                IntOffset(x = 0, y = applicationComponentY.roundToInt())
            },
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridHorizontalPagerState = gridHorizontalPagerState,
            rows = rows,
            columns = columns,
            appDrawerColumns = appDrawerColumns,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            rootWidth = rootWidth,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            drag = drag,
            appDrawerRowsHeight = appDrawerRowsHeight,
            hasShortcutHostPermission = hasShortcutHostPermission,
            dragIntOffset = dragIntOffset,
            onLongPress = onLongPressGridItem,
            onDragging = onDraggingGridItem,
            onDismiss = {
                scope.launch {
                    swipeUpY.snapTo(rootHeight.toFloat())

                    swipeDownY.snapTo(rootHeight.toFloat())
                }
            },
        )
    }

    if (showDoubleTap) {
        when (val gestureAction = gestureSettings.doubleTap) {
            GestureAction.None -> {

            }

            is GestureAction.OpenApp -> {
                launcherApps.startMainActivity(gestureAction.componentName)
            }

            GestureAction.OpenAppDrawer -> {
                ApplicationComponentScreen(
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    rows = rows,
                    columns = columns,
                    appDrawerColumns = appDrawerColumns,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    dockHeight = dockHeight,
                    drag = drag,
                    appDrawerRowsHeight = appDrawerRowsHeight,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    dragIntOffset = dragIntOffset,
                    onLongPress = onLongPressGridItem,
                    onDragging = onDraggingGridItem,
                    onDismiss = {
                        showDoubleTap = false
                    },
                )

            }

            GestureAction.OpenNotificationPanel -> {

            }
        }
    }
}

@Composable
private fun HorizontalPagerScreen(
    modifier: Modifier = Modifier,
    horizontalPagerState: PagerState,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridItem: GridItem?,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: Long,
    onLongPressGrid: (Int) -> Unit,
    rootWidth: Int,
    rootHeight: Int,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    wallpaperScroll: Boolean,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onDragStartPinItemRequest: (GridItemSource) -> Unit,
    onDoubleTap: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var showPopupGridItemMenu by remember { mutableStateOf(false) }

    var showPopupSettingsMenu by remember { mutableStateOf(false) }

    var popupSettingsMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    val launcherApps = LocalLauncherApps.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    val launcherAppsWrapper = LocalLauncherApps.current

    val fileManager = LocalFileManager.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val context = LocalContext.current

    val view = LocalView.current

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && showPopupGridItemMenu) {
            showPopupGridItemMenu = false

            onDraggingGridItem()
        }

        handlePinItemRequest(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            rows = rows,
            columns = columns,
            gridWidth = rootWidth,
            gridHeight = rootHeight - dockHeight,
            drag = drag,
            pinItemRequestWrapper = pinItemRequestWrapper,
            launcherAppsWrapper = launcherAppsWrapper,
            context = context,
            fileManager = fileManager,
            onDragStart = onDragStartPinItemRequest,
        )
    }

    LaunchedEffect(key1 = horizontalPagerState) {
        handleWallpaperScroll(
            horizontalPagerState = horizontalPagerState,
            wallpaperScroll = wallpaperScroll,
            wallpaperManagerWrapper = wallpaperManagerWrapper,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
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

                        onLongPressGrid(
                            calculatePage(
                                index = horizontalPagerState.currentPage,
                                infiniteScroll = infiniteScroll,
                                pageCount = pageCount,
                            ),
                        )
                    },
                )
            }
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )
            GridLayout(
                modifier = Modifier.fillMaxSize(),
                rows = rows,
                columns = columns,
            ) {
                gridItemsByPage[page]?.forEach { gridItem ->
                    key(gridItem.id) {
                        val cellWidth = rootWidth / columns

                        val cellHeight = (rootHeight - dockHeight) / rows

                        val x = gridItem.startColumn * cellWidth

                        val y = gridItem.startRow * cellHeight

                        val width = gridItem.columnSpan * cellWidth

                        val height = gridItem.rowSpan * cellHeight

                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoGridItem(
                                    textColor = textColor,
                                    gridItem = gridItem,
                                    data = data,
                                    onTap = {
                                        launcherApps.startMainActivity(data.componentName)
                                    },
                                    onLongPress = {
                                        popupMenuIntOffset = IntOffset(x = x, y = y)

                                        popupMenuIntSize = IntSize(width = width, height = height)

                                        showPopupGridItemMenu = true

                                        onLongPressGridItem(
                                            page,
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItem(
                                    gridItem = gridItem,
                                    gridItemData = data,
                                    onLongPress = {
                                        popupMenuIntOffset = IntOffset(x = x, y = y)

                                        popupMenuIntSize = IntSize(width = width, height = height)

                                        showPopupGridItemMenu = true

                                        onLongPressGridItem(
                                            page,
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                ShortcutInfoGridItem(
                                    textColor = textColor,
                                    gridItem = gridItem,
                                    data = data,
                                    onTap = {
                                        if (hasShortcutHostPermission) {
                                            launcherApps.startShortcut(
                                                packageName = data.packageName,
                                                id = data.shortcutId,
                                            )
                                        }
                                    },
                                    onLongPress = {
                                        popupMenuIntOffset = IntOffset(x = x, y = y)

                                        popupMenuIntSize = IntSize(width = width, height = height)

                                        showPopupGridItemMenu = true

                                        onLongPressGridItem(
                                            page,
                                            GridItemSource.Existing(gridItem = gridItem),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
        ) {
            dockGridItems.forEach { gridItem ->
                key(gridItem.id) {
                    val cellWidth = rootWidth / dockColumns

                    val cellHeight = dockHeight / dockRows

                    val x = gridItem.startColumn * cellWidth

                    val y = gridItem.startRow * cellHeight

                    val width = gridItem.columnSpan * cellWidth

                    val height = gridItem.rowSpan * cellHeight

                    when (val data = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                textColor = textColor,
                                gridItem = gridItem,
                                data = data,
                                onTap = {
                                    launcherApps.startMainActivity(data.componentName)
                                },
                                onLongPress = {
                                    popupMenuIntOffset = IntOffset(x = x, y = y)

                                    popupMenuIntSize = IntSize(width = width, height = height)

                                    showPopupGridItemMenu = true

                                    onLongPressGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        GridItemSource.Existing(gridItem = gridItem),
                                    )
                                },
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                gridItem = gridItem,
                                gridItemData = data,
                                onLongPress = {
                                    popupMenuIntOffset = IntOffset(x = x, y = y)

                                    popupMenuIntSize = IntSize(width = width, height = height)

                                    showPopupGridItemMenu = true

                                    onLongPressGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        GridItemSource.Existing(gridItem = gridItem),
                                    )
                                },
                            )
                        }

                        is GridItemData.ShortcutInfo -> {
                            ShortcutInfoGridItem(
                                textColor = textColor,
                                gridItem = gridItem,
                                data = data,
                                onTap = {
                                    launcherApps.startShortcut(
                                        packageName = data.packageName,
                                        id = data.shortcutId,
                                    )
                                },
                                onLongPress = {
                                    popupMenuIntOffset = IntOffset(x = x, y = y)

                                    popupMenuIntSize = IntSize(width = width, height = height)

                                    showPopupGridItemMenu = true

                                    onLongPressGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        GridItemSource.Existing(gridItem = gridItem),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPopupGridItemMenu) {
        when (gridItem?.associate) {
            Associate.Grid -> {
                Popup(
                    popupPositionProvider = MenuPositionProvider(
                        x = popupMenuIntOffset.x,
                        y = popupMenuIntOffset.y,
                        width = popupMenuIntSize.width,
                        height = popupMenuIntSize.height,
                    ),
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                    content = {
                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo, is GridItemData.ShortcutInfo -> {
                                ApplicationInfoGridItemMenu(
                                    showResize = true,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                val showResize =
                                    data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                WidgetGridItemMenu(
                                    showResize = showResize,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            }

            Associate.Dock -> {
                Popup(
                    popupPositionProvider = MenuPositionProvider(
                        x = popupMenuIntOffset.x,
                        y = rootHeight - dockHeight,
                        width = popupMenuIntSize.width,
                        height = popupMenuIntSize.height,
                    ),
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                    content = {
                        when (gridItem.data) {
                            is GridItemData.ApplicationInfo, is GridItemData.ShortcutInfo -> {
                                ApplicationInfoGridItemMenu(
                                    showResize = false,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItemMenu(
                                    showResize = false,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            }

            null -> Unit
        }
    }

    if (showPopupSettingsMenu) {
        Popup(
            popupPositionProvider = SettingsMenuPositionProvider(
                x = popupSettingsMenuIntOffset.x,
                y = popupSettingsMenuIntOffset.y,
            ),
            onDismissRequest = {
                showPopupSettingsMenu = false
            },
        ) {
            SettingsMenu(
                onSettings = {
                    showPopupSettingsMenu = false

                    onSettings()
                },
                onEditPage = {
                    showPopupSettingsMenu = false

                    onEditPage()
                },
            )
        }
    }
}

@Composable
private fun ApplicationComponentScreen(
    modifier: Modifier = Modifier,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridHorizontalPagerState: PagerState,
    rows: Int,
    columns: Int,
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    dragIntOffset: IntOffset,
    onLongPress: (
        currentPage: Int,
        newGridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
    onDismiss: suspend () -> Unit,
) {
    val overscrollAlpha = remember { Animatable(0f) }

    val alpha by remember {
        derivedStateOf {
            1f - (abs(overscrollAlpha.value) / 100f)
        }
    }

    Surface(
        modifier = modifier
            .graphicsLayer(alpha = alpha)
            .fillMaxSize(),
    ) {
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                val applicationHorizontalPagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = {
                        if (hasShortcutHostPermission) {
                            3
                        } else {
                            2
                        }
                    },
                )

                HorizontalPager(state = applicationHorizontalPagerState) { page ->
                    when (page) {
                        0 -> {
                            ApplicationScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                overscrollAlpha = overscrollAlpha,
                                appDrawerColumns = appDrawerColumns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                                drag = drag,
                                appDrawerRowsHeight = appDrawerRowsHeight,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onDismiss = onDismiss,
                            )
                        }

                        1 -> {
                            WidgetScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                overscrollAlpha = overscrollAlpha,
                                rows = rows,
                                columns = columns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanAppWidgetProviderInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                drag = drag,
                                dragIntOffset = dragIntOffset,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onDismiss = onDismiss,
                            )
                        }

                        2 -> {
                            ShortcutScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                overscrollAlpha = overscrollAlpha,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanShortcutInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos,
                                drag = drag,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onDismiss = onDismiss,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    ApplicationInfoGridItem(
        modifier = modifier
            .gridItem(gridItem)
            .dragAndDropSource(
                block = {
                    detectTapGestures(
                        onTap = {
                            onTap()
                        },
                        onLongPress = {
                            onLongPress()

                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("Screen", Screen.Drag.name),
                                ),
                            )
                        },
                    )
                },
            ),
        data = data,
        color = Color(textColor),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    ShortcutInfoGridItem(
        modifier = modifier
            .gridItem(gridItem)
            .dragAndDropSource(
                block = {
                    detectTapGestures(
                        onTap = {
                            onTap()
                        },
                        onLongPress = {
                            onLongPress()

                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("Screen", Screen.Drag.name),
                                ),
                            )
                        },
                    )
                },
            ),
        data = data,
        color = Color(textColor),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridItemData: GridItemData.Widget,
    onLongPress: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    Box(modifier = modifier.gridItem(gridItem)) {
        if (appWidgetInfo != null) {
            Box(
                modifier = modifier
                    .dragAndDropSource(
                        drawDragDecoration = {
                            drawRoundRect(
                                color = Color.White,
                                alpha = 0.2f,
                                cornerRadius = CornerRadius(
                                    x = 25f,
                                    y = 25f,
                                ),
                            )
                        },
                        block = {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)

                                down.consume()

                                val longPress = awaitLongPressOrCancellation(pointerId = down.id)

                                if (longPress != null) {
                                    onLongPress()

                                    startTransfer(
                                        DragAndDropTransferData(
                                            clipData = ClipData.newPlainText(
                                                "Screen",
                                                Screen.Drag.name,
                                            ),
                                        ),
                                    )
                                }
                            }
                        },
                    )
                    .matchParentSize(),
            )

            AndroidView(
                factory = {
                    appWidgetHost.createView(
                        appWidgetId = gridItemData.appWidgetId,
                        appWidgetProviderInfo = appWidgetInfo,
                    ).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                        )

                        setAppWidget(appWidgetId, appWidgetInfo)
                    }
                },
            )
        }
    }
}
