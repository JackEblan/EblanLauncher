package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.ClipData
import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.content.pm.ShortcutInfo
import android.os.Build
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalFileManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
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
import com.eblan.launcher.feature.home.screen.shortcut.getShortcutGridItem
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.screen.widget.getWidgetGridItem
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import java.io.File

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
    gridItems: Map<Int, List<GridItem>>,
    gridItem: GridItem?,
    dockHeight: Int,
    drag: Drag,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    rootWidth: Int,
    rootHeight: Int,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    initialPage: Int,
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

    val verticalPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            2
        },
    )

    VerticalPager(
        state = verticalPagerState,
        modifier = modifier,
        flingBehavior = PagerDefaults.flingBehavior(
            state = verticalPagerState,
            snapPositionalThreshold = 0.2f,
        ),
    ) { verticalPage ->
        when (verticalPage) {
            0 -> {
                HorizontalPagerScreen(
                    horizontalPagerState = gridHorizontalPagerState,
                    rows = rows,
                    columns = columns,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                    dockRows = dockRows,
                    dockColumns = dockColumns,
                    gridItems = gridItems,
                    gridItem = gridItem,
                    dockHeight = dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    drag = drag,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    initialPage = initialPage,
                    onLongPressGrid = onLongPressGrid,
                    onLongPressGridItem = onLongPressGridItem,
                    onDraggingGridItem = onDraggingGridItem,
                    onEdit = onEdit,
                    onResize = onResize,
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onDragStartPinItemRequest = onDragStartPinItemRequest,
                )
            }

            1 -> {
                ApplicationComponentScreen(
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    rows = rows,
                    columns = columns,
                    dockRows = dockRows,
                    dockColumns = dockColumns,
                    appDrawerColumns = appDrawerColumns,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    dockHeight = dockHeight,
                    drag = drag,
                    appDrawerRowsHeight = appDrawerRowsHeight,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    onLongPress = onLongPressGridItem,
                    onDragging = onDraggingGridItem,
                )
            }
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
    dockRows: Int,
    dockColumns: Int,
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    appDrawerRowsHeight: Int,
    hasShortcutHostPermission: Boolean,
    onLongPress: (
        currentPage: Int,
        newGridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize()) {
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
                                drag = drag,
                                appDrawerRowsHeight = appDrawerRowsHeight,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                            )
                        }

                        1 -> {
                            WidgetScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                rows = rows,
                                columns = columns,
                                dockRows = dockRows,
                                dockColumns = dockColumns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanAppWidgetProviderInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                drag = drag,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                            )
                        }

                        2 -> {
                            ShortcutScreen(
                                currentPage = gridHorizontalPagerState.currentPage,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanShortcutInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos,
                                drag = drag,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
                            )
                        }
                    }
                }
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
    gridItems: Map<Int, List<GridItem>>,
    gridItem: GridItem?,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onLongPressGrid: (Int) -> Unit,
    rootWidth: Int,
    rootHeight: Int,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    initialPage: Int,
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

    val context = LocalContext.current

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && showPopupGridItemMenu) {
            showPopupGridItemMenu = false

            onDraggingGridItem()
        }

        handlePinItemRequest(
            rows = rows,
            columns = columns,
            gridWidth = rootWidth,
            gridHeight = rootHeight - dockHeight,
            drag = drag,
            pinItemRequestWrapper = pinItemRequestWrapper,
            launcherAppsWrapper = launcherAppsWrapper,
            context = context,
            initialPage = initialPage,
            fileManager = fileManager,
            onDragStart = onDragStartPinItemRequest,
        )
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
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
                gridItems[page]?.forEach { gridItem ->
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
                                                id = data.id,
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
                                        id = data.id,
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
        val onDismissRequest1 = {
            showPopupSettingsMenu = false
        }
        Popup(
            popupPositionProvider = SettingsMenuPositionProvider(
                x = popupSettingsMenuIntOffset.x,
                y = popupSettingsMenuIntOffset.y,
            ),
            onDismissRequest = onDismissRequest1,
        ) {
            SettingsMenu(
                onSettings = {
                    onDismissRequest1()

                    onSettings()
                },
                onEditPage = {
                    onDismissRequest1()

                    onEditPage()
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: TextColor,
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

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
        color = color,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: TextColor,
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

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
        color = color,
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

private suspend fun handlePinItemRequest(
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    drag: Drag,
    pinItemRequestWrapper: PinItemRequestWrapper,
    launcherAppsWrapper: LauncherAppsWrapper,
    context: Context,
    initialPage: Int,
    fileManager: FileManager,
    onDragStart: (GridItemSource) -> Unit,
) {
    val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

    suspend fun getWidgetGridItemSource(
        pinItemRequest: PinItemRequest,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): GridItemSource {
        val byteArray = appWidgetProviderInfo.loadPreviewImage(context, 0)?.toByteArray()

        val previewInferred =
            File(
                fileManager.widgetsDirectory,
                appWidgetProviderInfo.provider.className,
            ).absolutePath

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GridItemSource.Pin(
                gridItem = getWidgetGridItem(
                    page = initialPage,
                    rows = rows,
                    columns = columns,
                    componentName = appWidgetProviderInfo.provider.flattenToString(),
                    configure = appWidgetProviderInfo.configure.flattenToString(),
                    packageName = appWidgetProviderInfo.provider.packageName,
                    targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                    targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                    minWidth = appWidgetProviderInfo.minWidth,
                    minHeight = appWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    preview = previewInferred,
                ),
                pinItemRequest = pinItemRequest,
                byteArray = byteArray,
            )
        } else {
            GridItemSource.Pin(
                gridItem = getWidgetGridItem(
                    page = initialPage,
                    rows = rows,
                    columns = columns,
                    componentName = appWidgetProviderInfo.provider.flattenToString(),
                    configure = appWidgetProviderInfo.configure.flattenToString(),
                    packageName = appWidgetProviderInfo.provider.packageName,
                    targetCellHeight = 0,
                    targetCellWidth = 0,
                    minWidth = appWidgetProviderInfo.minWidth,
                    minHeight = appWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = 0,
                    maxResizeHeight = 0,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    preview = previewInferred,
                ),
                pinItemRequest = pinItemRequest,
                byteArray = byteArray,
            )
        }
    }

    suspend fun getShortcutGridItemSource(
        pinItemRequest: PinItemRequest,
        shortcutInfo: ShortcutInfo,
    ): GridItemSource? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val byteArray = launcherAppsWrapper.getShortcutIconDrawable(
                shortcutInfo = shortcutInfo,
                density = 0,
            ).toByteArray()

            val iconInferred =
                File(fileManager.shortcutsDirectory, shortcutInfo.id).absolutePath

            GridItemSource.Pin(
                gridItem = getShortcutGridItem(
                    page = initialPage,
                    id = shortcutInfo.id,
                    packageName = shortcutInfo.`package`,
                    shortLabel = shortcutInfo.shortLabel.toString(),
                    longLabel = shortcutInfo.longLabel.toString(),
                    icon = iconInferred,
                ),
                pinItemRequest = pinItemRequest,
                byteArray = byteArray,
            )
        } else {
            null
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null) {
        when (drag) {
            Drag.Start -> {
                when (pinItemRequest.requestType) {
                    PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                        val appWidgetProviderInfo = pinItemRequest.getAppWidgetProviderInfo(context)

                        if (appWidgetProviderInfo != null) {
                            onDragStart(
                                getWidgetGridItemSource(
                                    pinItemRequest = pinItemRequest,
                                    appWidgetProviderInfo = appWidgetProviderInfo,
                                ),
                            )
                        }
                    }

                    PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                        val shortcutInfo = pinItemRequest.shortcutInfo

                        if (shortcutInfo != null) {
                            getShortcutGridItemSource(
                                pinItemRequest = pinItemRequest,
                                shortcutInfo = shortcutInfo,
                            )?.let { gridItemSource ->
                                onDragStart(gridItemSource)
                            }
                        }
                    }
                }
            }

            Drag.End -> {
                pinItemRequestWrapper.updatePinItemRequest(null)
            }

            else -> Unit
        }
    }
}

