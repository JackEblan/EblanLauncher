package com.eblan.launcher.feature.home.screen.pager

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.feature.home.model.HomeType
import com.eblan.launcher.feature.home.screen.grid.GridSubcomposeLayout
import com.eblan.launcher.feature.home.screen.pager.component.menu.MenuOverlay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    userData: UserData,
    gridItems: Map<Int, List<GridItem>>,
    lastGridItemDimensions: GridItemDimensions?,
    showMenu: Boolean,
    showResize: Boolean,
    showBottomSheet: Boolean,
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
    onShowBottomSheet: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onLongPressedGridItem: (
        gridItemDimensions: GridItemDimensions,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) -> Unit,
    onHomeType: (HomeType) -> Unit,
    onResetLastGridItemByCoordinates: () -> Unit,
    onResetShowBottomSheet: () -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    HorizontalPager(state = pagerState) { index ->
        val offset = index - (Int.MAX_VALUE / 2)

        val page = offset - offset.floorDiv(userData.pageCount) * userData.pageCount

        GridSubcomposeLayout(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()

                        val longPress = awaitLongPressOrCancellation(down.id)

                        if (longPress != null) {
                            onResetLastGridItemByCoordinates()

                            onShowBottomSheet(
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

                                onLongPressedGridItem(
                                    gridItemDimensions,
                                    x,
                                    y,
                                    width,
                                    height,
                                )
                            },
                        )
                    }

                    is GridItemData.Widget -> {
                        WidgetGridItem(
                            gridItemData = gridItemData,
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

                                onLongPressedGridItem(
                                    gridItemDimensions,
                                    x,
                                    y,
                                    width,
                                    height,
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

    if (showBottomSheet) {
        HomeBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onResetShowBottomSheet,
            onHomeType = onHomeType,
        )
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onHomeType: (HomeType) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Row {
            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onHomeType(HomeType.Application)

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null)

                Text(text = "Application")
            }

            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onHomeType(HomeType.Widget)

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                Text(text = "Widgets")
            }
        }
    }
}