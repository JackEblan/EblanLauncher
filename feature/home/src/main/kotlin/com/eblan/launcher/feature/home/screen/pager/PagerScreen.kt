package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
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
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.gestures.detectVerticalDragGestures
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.InteractiveGridItemContent
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
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
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.handleWallpaperScroll
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    targetPage: Int,
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
    gestureSettings: GestureSettings,
    appDrawerSettings: AppDrawerSettings,
    gridItemSource: GridItemSource?,
    homeSettings: HomeSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosByLabel: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
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
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanShortcutInfosByLabel: (String) -> Unit,
) {
    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val gridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeSettings.infiniteScroll) (Int.MAX_VALUE / 2) + targetPage else targetPage,
        pageCount = {
            if (homeSettings.infiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeSettings.pageCount
            }
        },
    )

    var showDoubleTap by remember { mutableStateOf(false) }

    var showWidgets by remember { mutableStateOf(false) }

    var showShortcuts by remember { mutableStateOf(false) }

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
        gridItems = gridItems,
        gridItemsByPage = gridItemsByPage,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        paddingValues = paddingValues,
        dockGridItems = dockGridItems,
        textColor = textColor,
        drag = drag,
        hasShortcutHostPermission = hasShortcutHostPermission,
        gridItemSource = gridItemSource,
        homeSettings = homeSettings,
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
            pageCount = homeSettings.pageCount,
            infiniteScroll = homeSettings.infiniteScroll,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            paddingValues = paddingValues,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
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
                DoubleTapApplicationScreen(
                    currentPage = gridHorizontalPagerState.currentPage,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    pageCount = homeSettings.pageCount,
                    infiniteScroll = homeSettings.infiniteScroll,
                    paddingValues = paddingValues,
                    drag = drag,
                    screenHeight = screenHeight,
                    appDrawerSettings = appDrawerSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    onDismiss = {
                        showDoubleTap = false
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                )
            }

            GestureAction.OpenNotificationPanel -> {

            }
        }
    }

    if (showWidgets) {
        WidgetScreen(
            currentPage = gridHorizontalPagerState.currentPage,
            pageCount = homeSettings.pageCount,
            infiniteScroll = homeSettings.infiniteScroll,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            drag = drag,
            eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
            onDismiss = {
                showWidgets = false
            },
        )
    }

    if (showShortcuts) {
        ShortcutScreen(
            currentPage = gridHorizontalPagerState.currentPage,
            pageCount = homeSettings.pageCount,
            infiniteScroll = homeSettings.infiniteScroll,
            eblanApplicationComponentUiState = eblanApplicationComponentUiState,
            gridItemSettings = homeSettings.gridItemSettings,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            drag = drag,
            eblanShortcutInfosByLabel = eblanShortcutInfosByLabel,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanShortcutInfosByLabel = onGetEblanShortcutInfosByLabel,
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
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridWidth: Int,
    gridHeight: Int,
    paddingValues: PaddingValues,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    gridItemSource: GridItemSource?,
    onLongPressGrid: (Int) -> Unit,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    homeSettings: HomeSettings,
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
        homeSettings.dockHeight.toDp()
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

    var popupGridItemMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val currentPage by remember {
        derivedStateOf {
            calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )
        }
    }

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
        if (drag == Drag.Dragging) {
            showPopupGridItemMenu = false
        }

        handlePinItemRequest(
            currentPage = currentPage,
            drag = drag,
            pinItemRequestWrapper = pinItemRequestWrapper,
            context = context,
            fileManager = fileManager,
            gridItemSettings = homeSettings.gridItemSettings,
            onDragStart = onDragStartPinItemRequest,
        )
    }

    LaunchedEffect(key1 = horizontalPagerState) {
        handleWallpaperScroll(
            horizontalPagerState = horizontalPagerState,
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

                        onLongPressGrid(currentPage)
                    },
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    requireUnconsumed = true,
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                )
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
    ) {
        HorizontalPager(
            state = horizontalPagerState,
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
                rows = homeSettings.rows,
                columns = homeSettings.columns,
            ) {
                gridItemsByPage[page]?.forEach { gridItem ->
                    val cellWidth = gridWidth / homeSettings.columns

                    val cellHeight =
                        (gridHeight - pageIndicatorHeightPx - homeSettings.dockHeight) / homeSettings.rows

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
                        onTapApplicationInfo = launcherApps::startMainActivity,
                        onTapShortcutInfo = launcherApps::startShortcut,
                        onTapFolderGridItem = {
                            onTapFolderGridItem(currentPage, gridItem.id)
                        },
                        onLongPress = {
                            val intOffset = IntOffset(x = x + leftPadding, y = y + topPadding)

                            popupMenuIntOffset = IntOffset(x = x, y = y)

                            popupGridItemMenuIntSize = IntSize(width = width, height = height)

                            onUpdateGridItemOffset(intOffset)
                        },
                        onUpdateImageBitmap = { imageBitmap ->
                            onLongPressGridItem(
                                currentPage,
                                GridItemSource.Existing(gridItem = gridItem),
                                imageBitmap,
                            )

                            showPopupGridItemMenu = true
                        },
                    )
                }
            }
        }

        PageIndicator(
            modifier = Modifier
                .height(pageIndicatorHeight)
                .fillMaxWidth(),
            pageCount = homeSettings.pageCount,
            currentPage = currentPage,
        )

        GridLayout(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                )
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = homeSettings.dockRows,
            columns = homeSettings.dockColumns,
        ) {
            dockGridItems.forEach { gridItem ->
                val cellWidth = gridWidth / homeSettings.dockColumns

                val cellHeight = homeSettings.dockHeight / homeSettings.dockRows

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
                    onTapApplicationInfo = launcherApps::startMainActivity,
                    onTapShortcutInfo = launcherApps::startShortcut,
                    onTapFolderGridItem = {
                        onTapFolderGridItem(currentPage, gridItem.id)
                    },
                    onLongPress = {
                        val dockY =
                            y + (gridHeight - pageIndicatorHeightPx - homeSettings.dockHeight)

                        val intOffset = IntOffset(x = x + leftPadding, y = dockY + topPadding)

                        popupMenuIntOffset = intOffset

                        popupGridItemMenuIntSize = IntSize(width = width, height = height)

                        onUpdateGridItemOffset(intOffset)
                    },
                    onUpdateImageBitmap = { imageBitmap ->
                        onLongPressGridItem(
                            currentPage,
                            GridItemSource.Existing(gridItem = gridItem),
                            imageBitmap,
                        )

                        showPopupGridItemMenu = true
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
            width = popupGridItemMenuIntSize.width,
            height = popupGridItemMenuIntSize.height,
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
