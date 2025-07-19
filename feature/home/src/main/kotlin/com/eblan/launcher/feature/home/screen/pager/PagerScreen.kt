package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.ClipData
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage

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
    gridItemLayoutInfo: GridItemLayoutInfo?,
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
    onLongPressGrid: (Int) -> Unit,
    onLongPressedGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
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
                    gridItemLayoutInfo = gridItemLayoutInfo,
                    dockHeight = dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    drag = drag,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    onLongPressGrid = onLongPressGrid,
                    onLongPressedGridItem = onLongPressedGridItem,
                    onDraggingGridItem = onDraggingGridItem,
                    onEdit = onEdit,
                    onResize = onResize,
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                )
            }

            1 -> {
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
                    gridItemLayoutInfo = gridItemLayoutInfo,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    onLongPress = onLongPressedGridItem,
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
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    appDrawerRowsHeight: Int,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    hasShortcutHostPermission: Boolean,
    onLongPress: (
        currentPage: Int,
        gridItemSource: GridItemSource,
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
                                rows = rows,
                                columns = columns,
                                appDrawerColumns = appDrawerColumns,
                                pageCount = pageCount,
                                infiniteScroll = infiniteScroll,
                                eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                drag = drag,
                                appDrawerRowsHeight = appDrawerRowsHeight,
                                gridItemLayoutInfo = gridItemLayoutInfo,
                                onLongPress = onLongPress,
                                onDragging = onDragging,
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
                                drag = drag,
                                gridItemLayoutInfo = gridItemLayoutInfo,
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
                                rows = rows,
                                columns = columns,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                drag = drag,
                                gridItemLayoutInfo = gridItemLayoutInfo,
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
    gridItemLayoutInfo: GridItemLayoutInfo?,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onLongPressGrid: (Int) -> Unit,
    rootWidth: Int,
    rootHeight: Int,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    onLongPressedGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var showPopupGridItemMenu by remember { mutableStateOf(false) }

    var showPopupSettingsMenu by remember { mutableStateOf(false) }

    var popupSettingsMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    val launcherApps = LocalLauncherApps.current

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemLayoutInfo != null) {
            showPopupGridItemMenu = false

            onDraggingGridItem()
        }
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
                                    gridItemData = data,
                                    onTap = {
                                        launcherApps.startMainActivity(data.componentName)
                                    },
                                    onLongPress = {
                                        showPopupGridItemMenu = true

                                        onLongPressedGridItem(
                                            page,
                                            GridItemSource(
                                                gridItemLayoutInfo = GridItemLayoutInfo(
                                                    gridItem = gridItem,
                                                    width = width,
                                                    height = height,
                                                    x = x,
                                                    y = y,
                                                ),
                                                type = GridItemSource.Type.Old,
                                            ),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItem(
                                    gridItem = gridItem,
                                    gridItemData = data,
                                    onLongPress = {
                                        showPopupGridItemMenu = true

                                        onLongPressedGridItem(
                                            page,
                                            GridItemSource(
                                                gridItemLayoutInfo = GridItemLayoutInfo(
                                                    gridItem = gridItem,
                                                    width = width,
                                                    height = height,
                                                    x = x,
                                                    y = y,
                                                ),
                                                type = GridItemSource.Type.Old,
                                            ),
                                        )
                                    },
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                ShortcutInfoGridItem(
                                    textColor = textColor,
                                    gridItem = gridItem,
                                    gridItemData = data,
                                    onTap = {
                                        if (hasShortcutHostPermission) {
                                            launcherApps.startShortcut(
                                                packageName = data.packageName,
                                                id = data.id,
                                            )
                                        }
                                    },
                                    onLongPress = {
                                        showPopupGridItemMenu = true

                                        onLongPressedGridItem(
                                            page,
                                            GridItemSource(
                                                gridItemLayoutInfo = GridItemLayoutInfo(
                                                    gridItem = gridItem,
                                                    width = width,
                                                    height = height,
                                                    x = x,
                                                    y = y,
                                                ),
                                                type = GridItemSource.Type.Old,
                                            ),
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
            dockGridItems.forEach { dockGridItem ->
                key(dockGridItem.id) {
                    val cellWidth = rootWidth / dockColumns

                    val cellHeight = dockHeight / dockRows

                    val x = dockGridItem.startColumn * cellWidth

                    val y = dockGridItem.startRow * cellHeight

                    val width = dockGridItem.columnSpan * cellWidth

                    val height = dockGridItem.rowSpan * cellHeight

                    when (val data = dockGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                textColor = textColor,
                                gridItem = dockGridItem,
                                gridItemData = data,
                                onTap = {
                                    launcherApps.startMainActivity(data.componentName)
                                },
                                onLongPress = {
                                    showPopupGridItemMenu = true

                                    onLongPressedGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        GridItemSource(
                                            gridItemLayoutInfo = GridItemLayoutInfo(
                                                gridItem = dockGridItem,
                                                width = width,
                                                height = height,
                                                x = x,
                                                y = y,
                                            ),
                                            type = GridItemSource.Type.Old,
                                        ),
                                    )
                                },
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                gridItem = dockGridItem,
                                gridItemData = data,
                                onLongPress = {
                                    showPopupGridItemMenu = true

                                    onLongPressedGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        GridItemSource(
                                            gridItemLayoutInfo = GridItemLayoutInfo(
                                                gridItem = dockGridItem,
                                                width = width,
                                                height = height,
                                                x = x,
                                                y = y,
                                            ),
                                            type = GridItemSource.Type.Old,
                                        ),
                                    )
                                },
                            )
                        }

                        is GridItemData.ShortcutInfo -> {
                            ShortcutInfoGridItem(
                                textColor = textColor,
                                gridItem = dockGridItem,
                                gridItemData = data,
                                onTap = {
                                    launcherApps.startShortcut(
                                        packageName = data.packageName,
                                        id = data.id,
                                    )
                                },
                                onLongPress = {
                                    showPopupGridItemMenu = true

                                    onLongPressedGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        GridItemSource(
                                            gridItemLayoutInfo = GridItemLayoutInfo(
                                                gridItem = dockGridItem,
                                                width = width,
                                                height = height,
                                                x = x,
                                                y = y,
                                            ),
                                            type = GridItemSource.Type.Old,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPopupGridItemMenu && gridItemLayoutInfo?.gridItem != null) {
        when (gridItemLayoutInfo.gridItem.associate) {
            Associate.Grid -> {
                Popup(
                    popupPositionProvider = MenuPositionProvider(
                        x = gridItemLayoutInfo.x,
                        y = gridItemLayoutInfo.y,
                        width = gridItemLayoutInfo.width,
                        height = gridItemLayoutInfo.height,
                    ),
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                    content = {
                        when (val data = gridItemLayoutInfo.gridItem.data) {
                            is GridItemData.ApplicationInfo, is GridItemData.ShortcutInfo -> {
                                ApplicationInfoGridItemMenu(
                                    showResize = gridItemLayoutInfo.gridItem.associate == Associate.Grid,
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
                                    gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

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
                        x = gridItemLayoutInfo.x,
                        y = rootHeight - dockHeight,
                        width = gridItemLayoutInfo.width,
                        height = gridItemLayoutInfo.height,
                    ),
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                    content = {
                        when (val data = gridItemLayoutInfo.gridItem.data) {
                            is GridItemData.ApplicationInfo, is GridItemData.ShortcutInfo -> {
                                ApplicationInfoGridItemMenu(
                                    showResize = gridItemLayoutInfo.gridItem.associate == Associate.Grid,
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
                                    gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

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
    gridItemData: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    Column(
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = gridItemData.icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp, 40.dp)
                .weight(1f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = gridItemData.label.toString(),
            modifier = Modifier.weight(1f),
            color = color,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(
                value = 10f,
                type = TextUnitType.Sp,
            ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: TextColor,
    gridItem: GridItem,
    gridItemData: GridItemData.ShortcutInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    Column(
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = gridItemData.icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp, 40.dp)
                .weight(1f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = gridItemData.shortLabel,
            modifier = Modifier.weight(1f),
            color = color,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(
                value = 10f,
                type = TextUnitType.Sp,
            ),
        )
    }
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

