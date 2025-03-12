package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.screen.pager.component.grid.GridSubcomposeLayout
import com.eblan.launcher.feature.home.screen.pager.component.menu.MenuOverlay
import kotlin.math.roundToInt

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    overlaySize: IntSize,
    screenSize: IntSize,
    dragOffset: Offset,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    showOverlay: Boolean,
    showMenu: Boolean,
    showResize: Boolean,
    onResizeGridItem: (page: Int, id: Int, width: Int, height: Int, cellWidth: Int, cellHeight: Int, anchor: Anchor) -> Unit,
    onDismissRequest: (() -> Unit)?,
    onResizeEnd: () -> Unit,
    onMoveGridItem: (
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        width: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onGetGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onLongPressGridItem: (Offset, IntSize) -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    var gridItemId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(key1 = dragOffset) {
        gridItemId?.let { id ->
            onMoveGridItem(
                pagerState.currentPage,
                id,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                overlaySize.width,
                screenSize.width,
                screenSize.height,
            )
        }
    }

    HorizontalPager(state = pagerState) { page ->
        GridSubcomposeLayout(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)

                            val longPressChange = awaitLongPressOrCancellation(down.id) ?: continue

                            if (!longPressChange.isConsumed) {
                                onGetGridItemByCoordinates(
                                    pagerState.currentPage,
                                    longPressChange.position.x.roundToInt(),
                                    longPressChange.position.y.roundToInt(),
                                    size.width,
                                    size.height,
                                )
                            }
                        }
                    }
                }
                .fillMaxSize(),
            page = page,
            rows = rows,
            columns = columns,
            id = gridItemId,
            gridItems = gridItems,
            onResizeGridItem = onResizeGridItem,
            showMenu = showMenu,
            showResize = showResize,
            onDismissRequest = onDismissRequest,
            onResizeEnd = onResizeEnd,
            gridItemContent = { gridItem, width, height, x, y ->
                EmptyGridItem(
                    modifier = Modifier.pointerInput(key1 = showOverlay) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false)

                                val longPressChange =
                                    awaitLongPressOrCancellation(down.id) ?: continue

                                if (!longPressChange.isConsumed) {
                                    onLongPressGridItem(
                                        dragOffset.copy(
                                            x = x.toFloat(),
                                            y = y.toFloat(),
                                        ),
                                        IntSize(width = width, height = height),
                                    )

                                    gridItemId = gridItem.id
                                }
                            }
                        }
                    },
                )
            },
            menuContent = {
                MenuOverlay(
                    onEdit = onEdit,
                    onResize = onResize,
                )
            },
        )
    }
}

@Composable
fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue),
    ) {
        AsyncImage(model = gridItemData.icon, contentDescription = null)

        Text(text = gridItemData.label)
    }
}

@Composable
fun EmptyGridItem(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Red),
    ) {
        Text(text = "Empty")
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    appWidgetId: Int,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

            appWidgetHost.createView(appWidgetId, appWidgetInfo)
        },
    )
}