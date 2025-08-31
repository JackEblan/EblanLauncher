package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalFileManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.local.LocalWallpaperManager
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
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
    gridWidth: Int,
    gridHeight: Int,
    paddingValues: PaddingValues,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    wallpaperScroll: Boolean,
    gestureSettings: GestureSettings,
    gridItemSettings: GridItemSettings,
    gridItemSource: GridItemSource?,
    onLongPressGrid: (Int) -> Unit,
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
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
) {
    val density = LocalDensity.current

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

    var showWidgets by remember { mutableStateOf(false) }

    var showShortcuts by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val verticalPadding = topPadding + bottomPadding

    val screenHeight = gridHeight + verticalPadding

    val swipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val swipeDownY = remember { Animatable(screenHeight.toFloat()) }

    val applicationComponentY by remember {
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

    LaunchedEffect(key1 = drag, key2 = gridItemSource) {
        if (drag == Drag.Dragging && gridItemSource != null) {
            onDraggingGridItem()
        }
    }

    HorizontalPagerScreen(
        modifier = modifier,
        horizontalPagerState = gridHorizontalPagerState,
        rows = rows,
        columns = columns,
        pageCount = pageCount,
        infiniteScroll = infiniteScroll,
        dockRows = dockRows,
        dockColumns = dockColumns,
        gridItems = gridItems,
        gridItemsByPage = gridItemsByPage,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        dockHeight = dockHeight,
        paddingValues = paddingValues,
        dockGridItems = dockGridItems,
        textColor = textColor,
        drag = drag,
        hasShortcutHostPermission = hasShortcutHostPermission,
        wallpaperScroll = wallpaperScroll,
        gridItemSettings = gridItemSettings,
        gridItemSource = gridItemSource,
        onLongPressGrid = onLongPressGrid,
        onTapFolderGridItem = onTapFolderGridItem,
        onEdit = onEdit,
        onResize = onResize,
        onSettings = onSettings,
        onEditPage = onEditPage,
        onWidgets = {
            showWidgets = true
        },
        onShortcuts = {
            showShortcuts = true
        },
        onDragStartPinItemRequest = onDragStartPinItemRequest,
        onDoubleTap = {
            showDoubleTap = true
        },
        onLongPressGridItem = onLongPressGridItem,
        onUpdateGridItemOffset = onUpdateGridItemOffset,
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
                onStartMainActivity = launcherApps::startMainActivity,
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

    if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer ||
        gestureSettings.swipeDown is GestureAction.OpenAppDrawer
    ) {
        ApplicationScreen(
            modifier = Modifier.offset {
                IntOffset(x = 0, y = applicationComponentY.roundToInt())
            },
            currentPage = gridHorizontalPagerState.currentPage,
            appDrawerColumns = appDrawerColumns,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            appDrawerRowsHeight = appDrawerRowsHeight,
            gridItemSettings = gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
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
                val animatedSwipeUpY = remember { Animatable(screenHeight.toFloat()) }

                LaunchedEffect(key1 = animatedSwipeUpY) {
                    animatedSwipeUpY.animateTo(0f)
                }

                ApplicationScreen(
                    modifier = Modifier.offset {
                        IntOffset(x = 0, y = animatedSwipeUpY.value.roundToInt())
                    },
                    currentPage = gridHorizontalPagerState.currentPage,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    appDrawerColumns = appDrawerColumns,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                    paddingValues = paddingValues,
                    appDrawerRowsHeight = appDrawerRowsHeight,
                    gridItemSettings = gridItemSettings,
                    drag = drag,
                    onDismiss = {
                        showDoubleTap = false
                    },
                    onAnimateDismiss = {
                        scope.launch {
                            animatedSwipeUpY.animateTo(screenHeight.toFloat())

                            showDoubleTap = false
                        }
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                )
            }

            GestureAction.OpenNotificationPanel -> {

            }
        }
    }

    if (showWidgets) {
        WidgetScreen(
            currentPage = gridHorizontalPagerState.currentPage,
            rows = rows,
            columns = columns,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            dockHeight = dockHeight,
            gridItemSettings = gridItemSettings,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            drag = drag,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDismiss = {
                showWidgets = false
            },
        )
    }

    if (showShortcuts) {
        ShortcutScreen(
            currentPage = gridHorizontalPagerState.currentPage,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridItemSettings = gridItemSettings,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            drag = drag,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onDismiss = {
                showShortcuts = false
            },
        )
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
    gridWidth: Int,
    gridHeight: Int,
    dockHeight: Int,
    paddingValues: PaddingValues,
    dockGridItems: List<GridItem>,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItemSource: GridItemSource?,
    onLongPressGrid: (Int) -> Unit,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    wallpaperScroll: Boolean,
    onTapFolderGridItem: (
        currentPage: Int,
        id: String,
    ) -> Unit,
    onEdit: (String) -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onShortcuts: () -> Unit,
    onDragStartPinItemRequest: (GridItemSource) -> Unit,
    onDoubleTap: () -> Unit,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onVerticalDrag: (
        change: PointerInputChange,
        dragAmount: Float,
    ) -> Unit,
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

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val pageIndicator = 5.dp

    val pageIndicatorPx = with(density) {
        pageIndicator.roundToPx()
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging) {
            showPopupGridItemMenu = false
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
                detectVerticalDragGestures(
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onLongPress = { offset ->
                        popupSettingsMenuIntOffset = IntOffset(
                            x = offset.round().x + leftPadding,
                            y = offset.round().y + topPadding,
                        )

                        showPopupSettingsMenu = true

                        onLongPressGrid(currentPage)
                    },
                )
            }
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            )
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

            GridLayout(
                modifier = Modifier
                    .padding(
                        start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                    )
                    .fillMaxSize(),
                rows = rows,
                columns = columns,
            ) {
                gridItemsByPage[page]?.forEach { gridItem ->
                    val cellWidth = gridWidth / columns

                    val cellHeight = (gridHeight - pageIndicatorPx - dockHeight) / rows

                    val x = gridItem.startColumn * cellWidth

                    val y = gridItem.startRow * cellHeight

                    val width = gridItem.columnSpan * cellWidth

                    val height = gridItem.rowSpan * cellHeight

                    GridItemContent(
                        gridItem = gridItem,
                        gridItemSettings = gridItemSettings,
                        textColor = textColor,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        drag = drag,
                        onTapApplicationInfo = launcherApps::startMainActivity,
                        onTapShortcutInfo = launcherApps::startShortcut,
                        onTapFolderGridItem = {
                            onTapFolderGridItem(currentPage, gridItem.id)
                        },
                        onLongPress = {
                            val intOffset = IntOffset(x = x + leftPadding, y = y + topPadding)

                            popupMenuIntOffset = intOffset

                            popupMenuIntSize = IntSize(width = width, height = height)

                            showPopupGridItemMenu = true

                            onUpdateGridItemOffset(intOffset)
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                currentPage,
                                GridItemSource.Existing(gridItem = gridItem),
                                imageBitmap,
                            )
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pageCount) { index ->
                val color = if (currentPage == index) Color.LightGray else Color.DarkGray

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(pageIndicator),
                )
            }
        }

        GridLayout(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                )
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
        ) {
            dockGridItems.forEach { gridItem ->
                val cellWidth = gridWidth / dockColumns

                val cellHeight = dockHeight / dockRows

                val x = gridItem.startColumn * cellWidth

                val y = gridItem.startRow * cellHeight

                val width = gridItem.columnSpan * cellWidth

                val height = gridItem.rowSpan * cellHeight

                GridItemContent(
                    gridItem = gridItem,
                    gridItemSettings = gridItemSettings,
                    textColor = textColor,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    drag = drag,
                    onTapApplicationInfo = launcherApps::startMainActivity,
                    onTapShortcutInfo = launcherApps::startShortcut,
                    onTapFolderGridItem = {
                        onTapFolderGridItem(currentPage, gridItem.id)
                    },
                    onLongPress = {
                        val dockY = y + (gridHeight - pageIndicatorPx - dockHeight)

                        val intOffset = IntOffset(x = x + leftPadding, y = dockY + topPadding)

                        popupMenuIntOffset = intOffset

                        popupMenuIntSize = IntSize(width = width, height = height)

                        showPopupGridItemMenu = true

                        onUpdateGridItemOffset(intOffset)
                    },
                    onUpdateImageBitmap = { imageBitmap ->
                        onLongPressGridItem(
                            currentPage,
                            GridItemSource.Existing(gridItem = gridItem),
                            imageBitmap,
                        )
                    },
                )
            }
        }
    }

    if (showPopupGridItemMenu && gridItemSource?.gridItem != null) {
        PopupGridItemMenu(
            currentPage = currentPage,
            gridItem = gridItemSource.gridItem,
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

    if (showPopupSettingsMenu) {
        PopupSettingsMenu(
            popupSettingsMenuIntOffset = popupSettingsMenuIntOffset,
            gridItems = gridItems,
            hasShortcutHostPermission = hasShortcutHostPermission,
            onSettings = onSettings,
            onEditPage = onEditPage,
            onWidgets = onWidgets,
            onShortcuts = onShortcuts,
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
    hasShortcutHostPermission: Boolean,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onWidgets: () -> Unit,
    onShortcuts: () -> Unit,
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
            hasShortcutHostPermission = hasShortcutHostPermission,
            onSettings = {
                onDismissRequest()

                onSettings()
            },
            onEditPage = {
                onDismissRequest()

                onEditPage(gridItems)
            },

            onWidgets = {
                onDismissRequest()

                onWidgets()
            },
            onShortcuts = {
                onDismissRequest()

                onShortcuts()
            },
            onWallpaper = {
                onDismissRequest()

                onWallpaper()
            },
        )
    }
}

@Composable
private fun GridItemContent(
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    textColor: Long,
    hasShortcutHostPermission: Boolean,
    drag: Drag,
    onTapApplicationInfo: (String?) -> Unit,
    onTapShortcutInfo: (
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onTapFolderGridItem: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap?) -> Unit,
) {
    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

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

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            ApplicationInfoGridItem(
                textColor = currentTextColor,
                gridItemSettings = currentGridItemSettings,
                gridItem = gridItem,
                data = data,
                drag = drag,
                onTap = {
                    onTapApplicationInfo(data.componentName)
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItem(
                gridItem = gridItem,
                data = data,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }

        is GridItemData.ShortcutInfo -> {
            ShortcutInfoGridItem(
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                gridItem = gridItem,
                data = data,
                drag = drag,
                onTap = {
                    if (hasShortcutHostPermission) {
                        onTapShortcutInfo(
                            data.packageName,
                            data.shortcutId,
                        )
                    }
                },
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
        }

        is GridItemData.Folder -> {
            FolderGridItem(
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                gridItem = gridItem,
                data = data,
                drag = drag,
                onTap = onTapFolderGridItem,
                onLongPress = onLongPress,
                onUpdateImageBitmap = onUpdateImageBitmap,
            )
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
                    val showResize = data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

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
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    val scale = remember { Animatable(1f) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.label.toString(),
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.Widget,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = data.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                ).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )

                    setAppWidget(appWidgetId, appWidgetInfo)
                }
            },
            modifier = modifier
                .gridItem(gridItem)
                .drawWithContent {
                    graphicsLayer.record {
                        drawContext.transform.scale(
                            scaleX = scale.value,
                            scaleY = scale.value,
                        )

                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .pointerInput(Unit) {
                    detectTapGesturesUnConsume(
                        requireUnconsumed = false,
                        onLongPress = {
                            onLongPress()

                            scope.launch {
                                scale.animateTo(0.5f)

                                scale.animateTo(1f)

                                onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                            }
                        },
                    )
                },
        )
    }
}

@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val iconSizeDp = with(density) {
        gridItemSettings.iconSize.toDp()
    }

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    val scale = remember { Animatable(1f) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp),
            )
        }

        if (gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.shortLabel,
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}

@Composable
private fun FolderGridItem(
    modifier: Modifier = Modifier,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    gridItem: GridItem,
    data: GridItemData.Folder,
    drag: Drag,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val color = Color(color = textColor)

    val textSizeSp = with(density) {
        gridItemSettings.textSize.toSp()
    }

    val scale = remember { Animatable(1f) }

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    Column(
        modifier = modifier
            .gridItem(gridItem)
            .drawWithContent {
                graphicsLayer.record {
                    drawContext.transform.scale(
                        scaleX = scale.value,
                        scaleY = scale.value,
                    )

                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()

                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onUpdateImageBitmap(graphicsLayer.toImageBitmap())
                        }
                    },
                    onTap = {
                        onTap()
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            maxItemsInEachRow = 2,
            maxLines = 2,
        ) {
            data.gridItems.take(4).sortedBy { it.startRow + it.startColumn }.forEach { gridItem ->
                Column {
                    val gridItemModifier = Modifier
                        .padding(2.dp)
                        .size(20.dp)

                    when (val currentData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            AsyncImage(
                                model = currentData.icon,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }

                        is GridItemData.ShortcutInfo -> {
                            AsyncImage(
                                model = currentData.icon,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }

                        is GridItemData.Widget -> {
                            AsyncImage(
                                model = currentData.preview,
                                contentDescription = null,
                                modifier = gridItemModifier,
                            )
                        }

                        is GridItemData.Folder -> {
                            Icon(
                                imageVector = EblanLauncherIcons.Folder,
                                contentDescription = null,
                                modifier = gridItemModifier,
                                tint = Color(textColor),
                            )
                        }
                    }
                }
            }
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.label,
                color = color,
                textAlign = TextAlign.Center,
                fontSize = textSizeSp,
                maxLines = maxLines,
            )
        }
    }
}
