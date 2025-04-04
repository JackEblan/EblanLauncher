package com.eblan.launcher.feature.home.screen.pager

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemByCoordinates
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.feature.home.screen.pager.component.grid.GridSubcomposeLayout
import com.eblan.launcher.feature.home.screen.pager.component.menu.MenuOverlay
import kotlin.math.roundToInt

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    dragOffset: Offset,
    rows: Int,
    columns: Int,
    lastGridItemByCoordinates: GridItemByCoordinates?,
    gridItems: Map<Int, List<GridItem>>,
    showMenu: Boolean,
    showResize: Boolean,
    onResizeGridItem: (
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        page: Int,
        gridItem: GridItem,
        widthPixel: Int,
        heightPixel: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onDismissRequest: (() -> Unit)?,
    onResizeEnd: () -> Unit,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
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
    onResetLastGridItem: () -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    LaunchedEffect(key1 = dragOffset, key2 = lastGridItemByCoordinates) {
        if (lastGridItemByCoordinates != null) {
            onMoveGridItem(
                pagerState.currentPage,
                lastGridItemByCoordinates.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                lastGridItemByCoordinates.screenWidth,
                lastGridItemByCoordinates.screenHeight,
            )
        }
    }

    HorizontalPager(state = pagerState) { page ->
        GridSubcomposeLayout(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()

                        val longPress = awaitLongPressOrCancellation(down.id)

                        if (longPress != null) {
                            onResetLastGridItem()

                            onGetGridItemByCoordinates(
                                pagerState.currentPage,
                                longPress.position.x.roundToInt(),
                                longPress.position.y.roundToInt(),
                                size.width,
                                size.height,
                            )
                        }
                    }
                }
                .fillMaxSize(),
            page = page,
            rows = rows,
            columns = columns,
            lastGridItemByCoordinates = lastGridItemByCoordinates,
            gridItems = gridItems,
            onResizeGridItem = onResizeGridItem,
            onResizeWidgetGridItem = onResizeWidgetGridItem,
            showMenu = showMenu,
            showResize = showResize,
            onDismissRequest = onDismissRequest,
            onResizeEnd = onResizeEnd,
            gridItemContent = { gridItem, x, y, screenWidth, screenHeight ->
                when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        ApplicationInfoGridItem(
                            gridItemData = gridItemData,
                        )
                    }

                    is GridItemData.Widget -> {
                        WidgetGridItem(
                            modifier = modifier,
                            gridItemData = gridItemData,
                            onLongPress = {
                                onGetGridItemByCoordinates(
                                    pagerState.currentPage,
                                    x,
                                    y,
                                    screenWidth,
                                    screenHeight,
                                )
                            },
                        )
                    }
                }
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
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
    onLongPress: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isLongPress, key2 = gridItemData) {
        if (isLongPress) {
            onLongPress()

            isLongPress = false
        }
    }

    if (appWidgetInfo != null) {
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

                    setOnLongClickListener {
                        isLongPress = true
                        true
                    }

                    setAppWidget(appWidgetId, appWidgetInfo)
                }
            },
            modifier = modifier,
        )
    }
}