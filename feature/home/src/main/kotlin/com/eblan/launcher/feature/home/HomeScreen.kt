package com.eblan.launcher.feature.home

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.eblan.launcher.feature.home.component.ResizableBoxWithMenu
import com.eblan.launcher.feature.home.component.gridItem
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (Int) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    viewModel.updatedGridItem.collectAsStateWithLifecycle()

    val gridItemBoundary by viewModel.gridItemBoundary.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemBoundary = gridItemBoundary,
        homeUiState = homeUiState,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onAddGridItem = viewModel::addGridItem,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemBoundary: GridItemBoundary?,
    homeUiState: HomeUiState,
    onMoveGridItem: (
        page: Int,
        id: Int,
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
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onAddGridItem = onAddGridItem,
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
    onMoveGridItem: (
        page: Int,
        id: Int,
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
    onEdit: (Int) -> Unit,
) {
    val density = LocalDensity.current

    var isEditing by remember { mutableStateOf(false) }

    var dragOffsetX by remember { mutableIntStateOf(-1) }

    var dragOffsetY by remember { mutableIntStateOf(-1) }

    var selectedGridItemPixelIntSize by remember { mutableStateOf(IntSize.Zero) }

    val pagerState = rememberPagerState(
        pageCount = {
            userData.pageCount
        },
    )

    var selectedGridItemId by remember { mutableIntStateOf(-1) }

    var screenIntSize by remember { mutableStateOf(IntSize.Zero) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                screenIntSize = intSize
            },
    ) {
        HorizontalPager(state = pagerState) { page ->
            GridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                page = page,
                rows = userData.rows,
                columns = userData.columns,
                gridItems = gridItems,
                content = { id, width, height, x, y ->
                    EmptyGridItem(
                        modifier = Modifier.pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isEditing = true
                                    selectedGridItemId = id
                                    selectedGridItemPixelIntSize = IntSize(
                                        width = width,
                                        height = height,
                                    )
                                    dragOffsetX = x
                                    dragOffsetY = y
                                },
                                onDragEnd = {
                                    isEditing = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetX += dragAmount.x.roundToInt()
                                    dragOffsetY += dragAmount.y.roundToInt()

                                    onMoveGridItem(
                                        pagerState.currentPage,
                                        selectedGridItemId,
                                        dragOffsetX,
                                        dragOffsetY,
                                        screenIntSize.width,
                                        screenIntSize.height,
                                    )
                                },
                            )
                        },
                    )
                },
            )
        }

        if (isEditing) {
            val boundingBoxWidthDp = with(density) {
                selectedGridItemPixelIntSize.width.toDp()
            }

            val boundingBoxHeightDp = with(density) {
                selectedGridItemPixelIntSize.height.toDp()
            }

            var widthDp by remember { mutableStateOf(boundingBoxWidthDp) }

            var heightDp by remember { mutableStateOf(boundingBoxHeightDp) }

            var widthPixel by remember { mutableIntStateOf(selectedGridItemPixelIntSize.width) }

            var heightPixel by remember { mutableIntStateOf(selectedGridItemPixelIntSize.height) }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = dragOffsetX, y = dragOffsetY,
                        )
                    }
                    .size(width = widthDp, height = heightDp)
                    .background(Color.Green),
            ) {
                Text(text = "Drag")
            }

            ResizableBoxWithMenu(
                x = dragOffsetX,
                y = dragOffsetY,
                width = widthPixel,
                height = heightPixel,
                screenWidth = screenIntSize.width,
                screenHeight = screenIntSize.height,
                onDragEnd = {
                    isEditing = false
                },
                onTopStartDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += -dragAmountXDp
                    heightDp += -dragAmountYDp

                    dragOffsetX += dragAmount.x.roundToInt()
                    dragOffsetY += dragAmount.y.roundToInt()

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        selectedGridItemId,
                        widthPixel,
                        heightPixel,
                        screenIntSize.width,
                        screenIntSize.height,
                        Anchor.BOTTOM_END,
                    )
                },
                onTopEndDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += dragAmountXDp
                    heightDp += -dragAmountYDp

                    dragOffsetY += dragAmount.y.roundToInt()

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        selectedGridItemId,
                        widthPixel,
                        heightPixel,
                        screenIntSize.width,
                        screenIntSize.height,
                        Anchor.BOTTOM_START,
                    )
                },
                onBottomStartDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += -dragAmountXDp
                    heightDp += dragAmountYDp

                    dragOffsetX += dragAmount.x.roundToInt()

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        selectedGridItemId,
                        widthPixel,
                        heightPixel,
                        screenIntSize.width,
                        screenIntSize.height,
                        Anchor.TOP_END,
                    )
                },
                onBottomEndDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountXDp = with(density) {
                        dragAmount.x.toDp()
                    }

                    val dragAmountYDp = with(density) {
                        dragAmount.y.toDp()
                    }

                    widthDp += dragAmountXDp
                    heightDp += dragAmountYDp

                    widthPixel = with(density) {
                        widthDp.toPx()
                    }.roundToInt()

                    heightPixel = with(density) {
                        heightDp.toPx()
                    }.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        selectedGridItemId,
                        widthPixel,
                        heightPixel,
                        screenIntSize.width,
                        screenIntSize.height,
                        Anchor.TOP_START,
                    )
                },
                onEdit = {
                    onEdit(selectedGridItemId)
                },
            )
        }
    }
}

@Composable
fun AnimatedGridItem(
    modifier: Modifier = Modifier,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onLongPress: ((Offset) -> Unit)? = null,
    content: @Composable (BoxScope.() -> Unit),
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Box(
        modifier = modifier
            .pointerInput(key1 = Unit) {
                detectTapGestures(
                    onLongPress = onLongPress,
                )
            }
            .gridItem(
                width = width, height = height, x = x, y = y,
            ),
        content = content,
    )
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

