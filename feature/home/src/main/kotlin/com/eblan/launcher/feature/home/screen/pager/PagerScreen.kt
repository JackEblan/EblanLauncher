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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.screen.grid.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.screen.pager.component.MenuOverlay

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    userData: UserData,
    gridItems: Map<Int, List<GridItem>>,
    lastGridItemDimensions: GridItemDimensions?,
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
    onDismissRequest: () -> Unit,
    onResizeEnd: () -> Unit,
    onShowBottomSheet: () -> Unit,
    onResetLastGridDimensions: () -> Unit,
    onLongPressedGridItem: (gridItemDimensions: GridItemDimensions) -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    var hit by remember { mutableIntStateOf(0) }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isLongPress) {
        if (isLongPress) {
            if (hit == 1) {
                onShowBottomSheet()
            }

            isLongPress = false
        }
    }

    HorizontalPager(state = pagerState) { index ->
        val offset = index - (Int.MAX_VALUE / 2)

        val page = offset - offset.floorDiv(userData.pageCount) * userData.pageCount

        GridSubcomposeLayout(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)

                        hit = 1

                        val longPress = awaitLongPressOrCancellation(down.id)

                        if (longPress != null) {
                            onResetLastGridDimensions()
                            isLongPress = true
                        }
                    }
                }
                .fillMaxSize(),
            page = page,
            rows = userData.rows,
            columns = userData.columns,
            lastGridItemDimensions = lastGridItemDimensions,
            gridItems = gridItems,
            onResizeGridItem = onResizeGridItem,
            onResizeWidgetGridItem = onResizeWidgetGridItem,
            showMenu = showMenu,
            showResize = showResize,
            onDismissRequest = onDismissRequest,
            onResizeEnd = onResizeEnd,
            gridItemContent = { gridItem, x, y, width, height, screenWidth, screenHeight ->
                when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        ApplicationInfoGridItem(
                            gridItemData = gridItemData,
                            onTap = {
                                hit += 1
                            },
                            onLongPress = {
                                val gridItemDimensions = GridItemDimensions(
                                    gridItem = gridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                )

                                onLongPressedGridItem(gridItemDimensions)
                            },
                        )
                    }

                    is GridItemData.Widget -> {
                        WidgetGridItem(
                            gridItemData = gridItemData,
                            onTap = {
                                hit += 1
                            },
                            onLongPress = {
                                val gridItemDimensions = GridItemDimensions(
                                    gridItem = gridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y,
                                    screenWidth = screenWidth,
                                    screenHeight = screenHeight,
                                )

                                onLongPressedGridItem(gridItemDimensions)
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
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isLongPress) {
        if (isLongPress) {
            onLongPress()

            isLongPress = false
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()

                    onTap()

                    val longPress = awaitLongPressOrCancellation(down.id)

                    if (longPress != null) {
                        isLongPress = true
                    }
                }
            }

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
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isLongPress) {
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

                    setOnClickListener {
                        onTap()
                    }

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