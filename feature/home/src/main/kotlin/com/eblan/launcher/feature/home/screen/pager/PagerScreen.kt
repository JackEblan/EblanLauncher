package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItemBody
import com.eblan.launcher.feature.home.component.ApplicationInfoMenuOverlay
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.MenuPositionProvider
import com.eblan.launcher.feature.home.component.WidgetGridItemBody
import com.eblan.launcher.feature.home.component.WidgetMenuOverlay
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    showMenu: Boolean,
    userScrollEnabled: Boolean,
    dockHeight: Int,
    drag: Drag,
    dockGridItems: List<GridItem>,
    constraintsMaxHeight: Int,
    onDismissRequest: () -> Unit,
    onLongPressGrid: () -> Unit,
    onLongPressedGridItem: (
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onLaunchApplication: (String) -> Unit,
    onDragStart: () -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start && gridItemLayoutInfo != null && !pagerState.isScrollInProgress) {
            onDragStart()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPressGrid()
                    },
                )
            }
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userScrollEnabled = userScrollEnabled,
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            GridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                page = page,
                rows = rows,
                columns = columns,
                gridItems = gridItems,
                gridItemContent = { gridItem, x, y, width, height ->
                    when (val data = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                gridItemData = data,
                                onTap = {
                                    onLaunchApplication(data.packageName)
                                },
                                onLongPress = { preview ->
                                    onLongPressedGridItem(
                                        preview,
                                        GridItemLayoutInfo(
                                            gridItem = gridItem,
                                            width = width,
                                            height = height,
                                            x = x,
                                            y = y,
                                        ),
                                    )
                                },
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                gridItemData = data,
                                onLongPress = { preview ->
                                    onLongPressedGridItem(
                                        preview,
                                        GridItemLayoutInfo(
                                            gridItem = gridItem,
                                            width = width,
                                            height = height,
                                            x = x,
                                            y = y,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                },
            )
        }

        DockGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
            dockGridItems = dockGridItems,
        ) { dockGridItem, x, y, width, height ->
            when (val data = dockGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        gridItemData = data,
                        onTap = {
                            onLaunchApplication(data.packageName)
                        },
                        onLongPress = { preview ->
                            onLongPressedGridItem(
                                preview,
                                GridItemLayoutInfo(
                                    gridItem = dockGridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y,
                                ),
                            )
                        },
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(
                        gridItemData = data,
                        onLongPress = { preview ->
                            onLongPressedGridItem(
                                preview,
                                GridItemLayoutInfo(
                                    gridItem = dockGridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }

    if (showMenu && gridItemLayoutInfo?.gridItem != null) {
        when (gridItemLayoutInfo.gridItem.associate) {
            Associate.Grid -> {
                GridItemMenu(
                    x = gridItemLayoutInfo.x,
                    y = gridItemLayoutInfo.y,
                    width = gridItemLayoutInfo.width,
                    height = gridItemLayoutInfo.height,
                    onDismissRequest = onDismissRequest,
                    content = {
                        when (val data = gridItemLayoutInfo.gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoMenuOverlay(
                                    showResize = gridItemLayoutInfo.gridItem.associate == Associate.Grid,
                                    onEdit = onEdit,
                                    onResize = onResize,
                                )
                            }

                            is GridItemData.Widget -> {
                                val showResize =
                                    gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                WidgetMenuOverlay(
                                    showResize = showResize,
                                    onEdit = onEdit,
                                    onResize = onResize,
                                )
                            }
                        }
                    },
                )
            }

            Associate.Dock -> {
                GridItemMenu(
                    x = gridItemLayoutInfo.x,
                    y = constraintsMaxHeight - dockHeight,
                    width = gridItemLayoutInfo.width,
                    height = gridItemLayoutInfo.height,
                    onDismissRequest = onDismissRequest,
                    content = {
                        when (val data = gridItemLayoutInfo.gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoMenuOverlay(
                                    showResize = gridItemLayoutInfo.gridItem.associate == Associate.Grid,
                                    onEdit = onEdit,
                                    onResize = onResize,
                                )
                            }

                            is GridItemData.Widget -> {
                                val showResize =
                                    gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                WidgetMenuOverlay(
                                    showResize = showResize,
                                    onEdit = onEdit,
                                    onResize = onResize,
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    ApplicationInfoGridItemBody(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onTap()
                    },
                    onLongPress = {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
                        }
                    },
                )
            }
            .fillMaxSize(),
        gridItemData = gridItemData,
    )
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    if (appWidgetInfo != null) {
        WidgetGridItemBody(
            modifier = modifier.drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            },
            appWidgetHostView = appWidgetHost.createView(
                appWidgetId = gridItemData.appWidgetId,
                appWidgetProviderInfo = appWidgetInfo,
            ).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )

                setOnLongClickListener {
                    scope.launch {
                        onLongPress(graphicsLayer.toImageBitmap())
                    }

                    true
                }

                setAppWidget(appWidgetId, appWidgetInfo)
            },
        )
    }
}

@Composable
fun GridItemMenu(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = width,
            height = height,
        ),
        onDismissRequest = onDismissRequest,
        content = content,
    )
}