package com.eblan.launcher.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.Menu
import com.eblan.launcher.feature.home.component.ResizableBox
import com.eblan.launcher.feature.home.component.gridItem
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (Int) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val gridItemBoundary by viewModel.gridItemBoundary.collectAsStateWithLifecycle()

    val selectedGridItem by viewModel.selectedGridItem.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemBoundary = gridItemBoundary,
        homeUiState = homeUiState,
        selectedGridItem = selectedGridItem,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onAddGridItem = viewModel::addGridItem,
        onLongPress = viewModel::getGridItemByCoordinates,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemBoundary: GridItemBoundary?,
    homeUiState: HomeUiState,
    selectedGridItem: GridItem?,
    onMoveGridItem: (
        page: Int,
        id: Int,
        width: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        id: Int,
        widthPixel: Int,
        heightPixel: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
        anchor: Anchor,
    ) -> Unit,
    onAddGridItem: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onLongPress: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onEdit: (Int) -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (homeUiState) {
                HomeUiState.Loading -> {

                }

                is HomeUiState.Success -> {
                    Success(
                        gridItems = homeUiState.gridItems,
                        userData = homeUiState.userData,
                        gridItemBoundary = gridItemBoundary,
                        selectedGridItem = selectedGridItem,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onAddGridItem = onAddGridItem,
                        onLongPress = onLongPress,
                        onEdit = onEdit,
                    )
                }
            }
        }
    }
}

@Composable
fun Success(
    modifier: Modifier = Modifier,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    gridItemBoundary: GridItemBoundary?,
    selectedGridItem: GridItem?,
    onMoveGridItem: (
        page: Int,
        id: Int,
        width: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        id: Int,
        widthPixel: Int,
        heightPixel: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
        anchor: Anchor,
    ) -> Unit,
    onAddGridItem: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onLongPress: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onEdit: (Int) -> Unit,
) {
    val density = LocalDensity.current

    val pagerState = rememberPagerState(
        pageCount = {
            userData.pageCount
        },
    )

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    LaunchedEffect(key1 = gridItemBoundary) {
        when (gridItemBoundary) {
            GridItemBoundary.Left -> {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }

            GridItemBoundary.Right -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            null -> Unit
        }
    }

    val gridItem by rememberUpdatedState(selectedGridItem)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onLongPress(
                            pagerState.currentPage,
                            offset.x.roundToInt(),
                            offset.y.roundToInt(),
                            size.width,
                            size.height,
                        )
                    },
                    onDrag = { change, dragAmount ->
                        gridItem?.let { gridItem ->
                            change.consume()
                            dragOffsetX += dragAmount.x.roundToInt()
                            dragOffsetY += dragAmount.y.roundToInt()

                            onMoveGridItem(
                                pagerState.currentPage,
                                gridItem.id,
                                gridItem.columnSpan * size.width,
                                dragOffsetX,
                                dragOffsetY,
                                size.width,
                                size.height,
                            )
                        }
                    },
                )
            }
            .fillMaxSize(),
    ) {
        HorizontalPager(state = pagerState) { page ->
            GridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                page = page,
                rows = userData.rows,
                columns = userData.columns,
                gridItems = gridItems[page],
                gridItem = gridItem,
                content = {
                    EmptyGridItem()
                },
                resizableContent = { id, width, height, x, y, gridWidth, gridHeight ->
                    var widthPixel by remember { mutableIntStateOf(width) }

                    var heightPixel by remember { mutableIntStateOf(height) }

                    dragOffsetX = x

                    dragOffsetY = y

                    ResizableBox(
                        modifier = Modifier.gridItem(
                            width = widthPixel,
                            height = heightPixel,
                            x = dragOffsetX,
                            y = dragOffsetY,
                        ),
                        onTopStartDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx()
                            }.roundToInt()

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx()
                            }.roundToInt()

                            widthPixel += -dragAmountX
                            heightPixel += -dragAmountY

                            dragOffsetX += dragAmount.x.roundToInt()
                            dragOffsetY += dragAmount.y.roundToInt()

                            onResizeGridItem(
                                pagerState.currentPage,
                                id,
                                widthPixel,
                                heightPixel,
                                gridWidth,
                                gridHeight,
                                Anchor.BOTTOM_END,
                            )
                        },
                        onTopEndDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx()
                            }.roundToInt()

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx()
                            }.roundToInt()

                            widthPixel += dragAmountX
                            heightPixel += -dragAmountY

                            dragOffsetY += dragAmount.y.roundToInt()

                            onResizeGridItem(
                                pagerState.currentPage,
                                id,
                                widthPixel,
                                heightPixel,
                                gridWidth,
                                gridHeight,
                                Anchor.BOTTOM_START,
                            )
                        },
                        onBottomStartDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx()
                            }.roundToInt()

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx()
                            }.roundToInt()

                            widthPixel += -dragAmountX
                            heightPixel += dragAmountY

                            dragOffsetX += dragAmount.x.roundToInt()

                            onResizeGridItem(
                                pagerState.currentPage,
                                id,
                                widthPixel,
                                heightPixel,
                                gridWidth,
                                gridHeight,
                                Anchor.TOP_END,
                            )
                        },
                        onBottomEndDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx()
                            }.roundToInt()

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx()
                            }.roundToInt()

                            widthPixel += dragAmountX
                            heightPixel += dragAmountY

                            onResizeGridItem(
                                pagerState.currentPage,
                                id,
                                widthPixel,
                                heightPixel,
                                gridWidth,
                                gridHeight,
                                Anchor.TOP_START,
                            )
                        },
                    )
                },
                menuContent = {
                    Menu(
                        onEdit = {

                        },
                    )
                },
            )
        }
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

