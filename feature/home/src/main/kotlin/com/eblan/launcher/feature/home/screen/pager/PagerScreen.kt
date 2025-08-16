package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.local.LocalFileManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.local.LocalWallpaperManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveFolderGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.InteractiveWidgetGridItem
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
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
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
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
    wallpaperScroll: Boolean,
    gestureSettings: GestureSettings,
    gridItemSettings: GridItemSettings,
    gridItemSource: GridItemSource?,
    onLongPressGrid: (Int) -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onTapFolderGridItem: (
        currentPage: Int,
        id: String,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: (String) -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
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
                        swipeUpY = swipeUpY.value,
                        swipeDownY = swipeDownY.value,
                        rootHeight = rootHeight,
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
        gridItems = gridItems,
        gridItemsByPage = gridItemsByPage,
        dockHeight = dockHeight,
        dockGridItems = dockGridItems,
        textColor = textColor,
        rootWidth = rootWidth,
        rootHeight = rootHeight,
        drag = drag,
        hasShortcutHostPermission = hasShortcutHostPermission,
        wallpaperScroll = wallpaperScroll,
        gridItemSettings = gridItemSettings,
        gridItemSource = gridItemSource,
        onLongPressGrid = onLongPressGrid,
        onLongPressGridItem = onLongPressGridItem,
        onTapFolderGridItem = onTapFolderGridItem,
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

    if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer ||
        gestureSettings.swipeDown is GestureAction.OpenAppDrawer
    ) {
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
            gridItemSource = gridItemSource,
            appDrawerRowsHeight = appDrawerRowsHeight,
            hasShortcutHostPermission = hasShortcutHostPermission,
            gridItemSettings = gridItemSettings,
            onLongPress = onLongPressGridItem,
            onDragging = onDraggingGridItem,
            onDismiss = {
                scope.launch {
                    swipeUpY.snapTo(rootHeight.toFloat())

                    swipeDownY.snapTo(rootHeight.toFloat())
                }
            },
            onAnimateDismiss = {
                scope.launch {
                    swipeUpY.animateTo(
                        targetValue = rootHeight.toFloat(),
                        animationSpec = tween(500),
                    )

                    swipeDownY.animateTo(
                        targetValue = rootHeight.toFloat(),
                        animationSpec = tween(500),
                    )
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
                val animatedSwipeUpY = remember { Animatable(rootHeight.toFloat()) }

                LaunchedEffect(key1 = animatedSwipeUpY) {
                    animatedSwipeUpY.animateTo(0f)
                }

                ApplicationComponentScreen(
                    modifier = Modifier.offset {
                        IntOffset(x = 0, y = animatedSwipeUpY.value.roundToInt())
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
                    gridItemSource = gridItemSource,
                    appDrawerRowsHeight = appDrawerRowsHeight,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    gridItemSettings = gridItemSettings,
                    onLongPress = onLongPressGridItem,
                    onDragging = onDraggingGridItem,
                    onDismiss = {
                        showDoubleTap = false
                    },
                    onAnimateDismiss = {
                        scope.launch {
                            animatedSwipeUpY.animateTo(
                                targetValue = rootHeight.toFloat(),
                                animationSpec = tween(500),
                            )

                            showDoubleTap = false
                        }
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
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItemSource: GridItemSource?,
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
    onTapFolderGridItem: (
        currentPage: Int,
        id: String,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: (String) -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
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

    val fileManager = LocalFileManager.current

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val context = LocalContext.current

    val view = LocalView.current

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val currentPage by remember {
        derivedStateOf {
            calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemSource != null) {
            onDraggingGridItem()
        }

        handlePinItemRequest(
            currentPage = currentPage,
            drag = drag,
            pinItemRequestWrapper = pinItemRequestWrapper,
            context = context,
            fileManager = fileManager,
            gridItemSettings = gridItemSettings,
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

                        onLongPressGrid(currentPage)
                    },
                )
            }
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            PageGridLayout(
                modifier = Modifier.fillMaxSize(),
                currentPage = currentPage,
                gridItems = gridItemsByPage[page],
                rows = rows,
                columns = columns,
                gridWidth = rootWidth,
                gridHeight = rootHeight - dockHeight,
                gridItemSettings = gridItemSettings,
                textColor = textColor,
                hasShortcutHostPermission = hasShortcutHostPermission,
                onLongPressGridItem = onLongPressGridItem,
                onTapFolderGridItem = onTapFolderGridItem,
                onUpdatePopupMenu = { newPopupMenuIntOffset, newPopupMenuIntSize ->
                    popupMenuIntOffset = newPopupMenuIntOffset

                    popupMenuIntSize = newPopupMenuIntSize

                    showPopupGridItemMenu = true
                },
                onTapApplicationInfo = launcherApps::startMainActivity,
                onTapShortcutInfo = launcherApps::startShortcut,
            )
        }

        PageGridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            currentPage = currentPage,
            gridItems = dockGridItems,
            rows = dockRows,
            columns = dockColumns,
            gridWidth = rootWidth,
            gridHeight = dockHeight,
            gridItemSettings = gridItemSettings,
            textColor = textColor,
            hasShortcutHostPermission = hasShortcutHostPermission,
            onLongPressGridItem = onLongPressGridItem,
            onTapFolderGridItem = onTapFolderGridItem,
            onUpdatePopupMenu = { newPopupMenuIntOffset, newPopupMenuIntSize ->
                popupMenuIntOffset = newPopupMenuIntOffset

                popupMenuIntSize = newPopupMenuIntSize

                showPopupGridItemMenu = true
            },
            onTapApplicationInfo = launcherApps::startMainActivity,
            onTapShortcutInfo = launcherApps::startShortcut,
        )
    }

    if (showPopupGridItemMenu) {
        val gridItem = gridItemSource?.gridItem

        when (gridItem?.associate) {
            Associate.Grid -> {
                PopupGridItemMenu(
                    currentPage = currentPage,
                    gridItem = gridItem,
                    x = popupMenuIntOffset.x,
                    y = popupMenuIntOffset.y,
                    width = popupMenuIntSize.width,
                    height = popupMenuIntSize.height,
                    onEdit = onEdit,
                    onResize = onResize,
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                )
            }

            Associate.Dock -> {
                PopupGridItemMenu(
                    currentPage = currentPage,
                    gridItem = gridItem,
                    x = popupMenuIntOffset.x,
                    y = (rootHeight - dockHeight) + popupMenuIntOffset.y,
                    width = popupMenuIntSize.width,
                    height = popupMenuIntSize.height,
                    onEdit = onEdit,
                    onResize = onResize,
                    onDismissRequest = {
                        showPopupGridItemMenu = false
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

                    onEditPage(gridItems)
                },
            )
        }
    }
}

@Composable
private fun PageGridLayout(
    modifier: Modifier = Modifier,
    currentPage: Int,
    gridItems: List<GridItem>?,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    gridItemSettings: GridItemSettings,
    textColor: Long,
    hasShortcutHostPermission: Boolean,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onTapFolderGridItem: (
        currentPage: Int,
        shortcutId: String,
    ) -> Unit,
    onUpdatePopupMenu: (
        popupMenuIntOffset: IntOffset,
        popupMenuIntSize: IntSize,
    ) -> Unit,
    onTapApplicationInfo: (String?) -> Unit,
    onTapShortcutInfo: (
        packageName: String,
        shortcutId: String,
    ) -> Unit,
) {
    GridLayout(
        modifier = modifier,
        rows = rows,
        columns = columns,
    ) {
        gridItems?.forEach { gridItem ->
            key(gridItem.id) {
                val cellWidth = gridWidth / columns

                val cellHeight = gridHeight / rows

                val x = gridItem.startColumn * cellWidth

                val y = gridItem.startRow * cellHeight

                val width = gridItem.columnSpan * cellWidth

                val height = gridItem.rowSpan * cellHeight

                val currentGridItemSettings by remember(key1 = gridItem) {
                    val currentGridItemSettings = if (gridItem.override) {
                        gridItem.gridItemSettings
                    } else {
                        gridItemSettings
                    }

                    mutableStateOf(currentGridItemSettings)
                }

                val currentTextColor by remember(key1 = gridItem) {
                    val currentTextColor = if (gridItem.override) {
                        when (gridItem.gridItemSettings.textColor) {
                            TextColor.System -> {
                                textColor
                            }

                            TextColor.Light -> {
                                0xFFFFFFFF
                            }

                            TextColor.Dark -> {
                                0xFF000000
                            }
                        }
                    } else {
                        textColor
                    }

                    mutableLongStateOf(currentTextColor)
                }

                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        InteractiveApplicationInfoGridItem(
                            textColor = currentTextColor,
                            gridItemSettings = currentGridItemSettings,
                            gridItem = gridItem,
                            data = data,
                            onTap = {
                                onTapApplicationInfo(data.componentName)
                            },
                            onLongPress = {
                                onUpdatePopupMenu(
                                    IntOffset(x = x, y = y),
                                    IntSize(width = width, height = height),
                                )

                                onLongPressGridItem(
                                    currentPage,
                                    GridItemSource.Existing(gridItem = gridItem),
                                )
                            },
                        )
                    }

                    is GridItemData.Widget -> {
                        InteractiveWidgetGridItem(
                            gridItem = gridItem,
                            gridItemData = data,
                            onLongPress = {
                                onUpdatePopupMenu(
                                    IntOffset(x = x, y = y),
                                    IntSize(width = width, height = height),
                                )

                                onLongPressGridItem(
                                    currentPage,
                                    GridItemSource.Existing(gridItem = gridItem),
                                )
                            },
                        )
                    }

                    is GridItemData.ShortcutInfo -> {
                        InteractiveShortcutInfoGridItem(
                            gridItemSettings = currentGridItemSettings,
                            textColor = currentTextColor,
                            gridItem = gridItem,
                            data = data,
                            onTap = {
                                if (hasShortcutHostPermission) {
                                    onTapShortcutInfo(
                                        data.packageName,
                                        data.shortcutId,
                                    )
                                }
                            },
                            onLongPress = {
                                onUpdatePopupMenu(
                                    IntOffset(x = x, y = y),
                                    IntSize(width = width, height = height),
                                )

                                onLongPressGridItem(
                                    currentPage,
                                    GridItemSource.Existing(gridItem = gridItem),
                                )
                            },
                        )
                    }

                    is GridItemData.Folder -> {
                        InteractiveFolderGridItem(
                            gridItemSettings = currentGridItemSettings,
                            textColor = currentTextColor,
                            gridItem = gridItem,
                            data = data,
                            onTap = {
                                onTapFolderGridItem(
                                    currentPage,
                                    gridItem.id,
                                )
                            },
                            onLongPress = {
                                onUpdatePopupMenu(
                                    IntOffset(x = x, y = y),
                                    IntSize(width = width, height = height),
                                )

                                onLongPressGridItem(
                                    currentPage,
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

@Composable
private fun PopupGridItemMenu(
    currentPage: Int,
    gridItem: GridItem,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    onEdit: (String) -> Unit,
    onResize: (Int) -> Unit,
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
                is GridItemData.ApplicationInfo,
                is GridItemData.ShortcutInfo,
                is GridItemData.Folder,
                    -> {
                    ApplicationInfoGridItemMenu(
                        onEdit = {
                            onDismissRequest()

                            onEdit(gridItem.id)
                        },
                        onResize = {
                            onResize(currentPage)
                        },
                    )
                }

                is GridItemData.Widget -> {
                    val showResize =
                        data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                    WidgetGridItemMenu(
                        showResize = showResize,
                        onEdit = {
                            onEdit(gridItem.id)
                        },
                        onResize = {
                            onResize(currentPage)
                        },
                    )
                }
            }
        },
    )
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
    gridItemSource: GridItemSource?,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    onLongPress: (
        currentPage: Int,
        newGridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
    onDismiss: () -> Unit,
    onAnimateDismiss: () -> Unit,
) {
    var alpha by remember { mutableFloatStateOf(1f) }

    BackHandler {
        onAnimateDismiss()
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
                                appDrawerColumns = appDrawerColumns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                                appDrawerRowsHeight = appDrawerRowsHeight,
                                gridItemSettings = gridItemSettings,
                                drag = drag,
                                gridItemSource = gridItemSource,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onUpdateAlpha = { newAlpha ->
                                    alpha = newAlpha
                                },
                                onFling = {
                                    if (alpha < 0.2f) {
                                        onDismiss()
                                    }
                                },
                                onFastFling = onAnimateDismiss,
                            )
                        }

                        1 -> {
                            WidgetScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                rows = rows,
                                columns = columns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanAppWidgetProviderInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                gridItemSettings = gridItemSettings,
                                drag = drag,
                                gridItemSource = gridItemSource,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onUpdateAlpha = { newAlpha ->
                                    alpha = newAlpha
                                },
                                onFling = {
                                    if (alpha < 0.2f) {
                                        onDismiss()
                                    }
                                },
                                onFastFling = onAnimateDismiss,
                            )
                        }

                        2 -> {
                            ShortcutScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanShortcutInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos,
                                gridItemSettings = gridItemSettings,
                                drag = drag,
                                gridItemSource = gridItemSource,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                                onUpdateAlpha = { newAlpha ->
                                    alpha = newAlpha
                                },
                                onFling = {
                                    if (alpha < 0.2f) {
                                        onDismiss()
                                    }
                                },
                                onFastFling = onAnimateDismiss,
                            )
                        }
                    }
                }
            }
        }
    }
}
