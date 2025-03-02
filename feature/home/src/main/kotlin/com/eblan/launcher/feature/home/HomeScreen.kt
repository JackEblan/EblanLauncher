package com.eblan.launcher.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
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
import com.eblan.launcher.domain.model.GridItemOverlay
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.MenuOverlay
import com.eblan.launcher.feature.home.component.ResizableOverlay
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (Int) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val gridItemBoundary by viewModel.gridItemBoundary.collectAsStateWithLifecycle()

    val draggedGridItemOverlay by viewModel.draggedGridItemOverlay.collectAsStateWithLifecycle()

    val resizedGridItemOverlay by viewModel.resizedGridItemOverlay.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemBoundary = gridItemBoundary,
        homeUiState = homeUiState,
        draggedGridItemOverlay = draggedGridItemOverlay,
        resizedGridItemOverlay = resizedGridItemOverlay,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onAddGridItem = viewModel::addGridItem,
        onDragGridItemOverlay = viewModel::dragGridItemOverlay,
        onResizeGridItemOverlay = viewModel::resizeGridItemOverlay,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemBoundary: GridItemBoundary?,
    homeUiState: HomeUiState,
    draggedGridItemOverlay: GridItemOverlay?,
    resizedGridItemOverlay: GridItemOverlay?,
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
    onDragGridItemOverlay: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onResizeGridItemOverlay: (
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
                        gridItemOverlay = draggedGridItemOverlay,
                        resizedGridItemOverlay = resizedGridItemOverlay,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onAddGridItem = onAddGridItem,
                        onDragGridItemOverlay = onDragGridItemOverlay,
                        onResizeGridItemOverlay = onResizeGridItemOverlay,
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
    gridItemOverlay: GridItemOverlay?,
    resizedGridItemOverlay: GridItemOverlay?,
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
    onDragGridItemOverlay: (
        page: Int,
        x: Int,
        y: Int,
        screenWidthPixel: Int,
        screenHeightPixel: Int,
    ) -> Unit,
    onResizeGridItemOverlay: (
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

    val gridItemId by rememberUpdatedState(gridItemOverlay?.id)

    var showOverlay by remember { mutableStateOf(false) }

    var showPopupMenu by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var gridItemOverlayId by remember { mutableIntStateOf(-1) }

    var gridItemOverlayOffsetX by remember { mutableIntStateOf(-1) }

    var gridItemOverlayOffsetY by remember { mutableIntStateOf(-1) }

    var gridItemOverlayWidth by remember { mutableIntStateOf(-1) }

    var gridItemOverlayHeight by remember { mutableIntStateOf(-1) }

    var gridItemOverlayScreenWidth by remember { mutableIntStateOf(-1) }

    var gridItemOverlayScreenHeight by remember { mutableIntStateOf(-1) }

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

    LaunchedEffect(key1 = gridItemOverlay) {
        if (gridItemOverlay != null) {
            gridItemOverlayId = gridItemOverlay.id

            dragOffsetX = gridItemOverlay.x
            dragOffsetY = gridItemOverlay.y

            gridItemOverlayOffsetX = gridItemOverlay.x
            gridItemOverlayOffsetY = gridItemOverlay.y

            gridItemOverlayWidth = gridItemOverlay.width
            gridItemOverlayHeight = gridItemOverlay.height

            gridItemOverlayScreenWidth = gridItemOverlay.screenWidth
            gridItemOverlayScreenHeight = gridItemOverlay.screenHeight
            showOverlay = true
            showPopupMenu = true
            showResize = false
        }
    }

    LaunchedEffect(key1 = resizedGridItemOverlay) {
        if (resizedGridItemOverlay != null) {
            gridItemOverlayId = resizedGridItemOverlay.id

            dragOffsetX = resizedGridItemOverlay.x
            dragOffsetY = resizedGridItemOverlay.y

            gridItemOverlayOffsetX = resizedGridItemOverlay.x
            gridItemOverlayOffsetY = resizedGridItemOverlay.y

            gridItemOverlayWidth = resizedGridItemOverlay.width
            gridItemOverlayHeight = resizedGridItemOverlay.height

            gridItemOverlayScreenWidth = resizedGridItemOverlay.screenWidth
            gridItemOverlayScreenHeight = resizedGridItemOverlay.screenHeight
            showOverlay = false
            showPopupMenu = false
            showResize = true
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onDragGridItemOverlay(
                            pagerState.currentPage,
                            offset.x.roundToInt(),
                            offset.y.roundToInt(),
                            size.width,
                            size.height,
                        )
                    },
                    onDrag = { change, dragAmount ->
                        gridItemId?.let { id ->
                            change.consume()
                            dragOffsetX += dragAmount.x.roundToInt()
                            dragOffsetY += dragAmount.y.roundToInt()

                            onMoveGridItem(
                                pagerState.currentPage,
                                id,
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
                rows = userData.rows,
                columns = userData.columns,
                gridItems = gridItems[page],
                content = {
                    EmptyGridItem()
                },
            )
        }

        if (showOverlay) {
            val boundingBoxWidthDp = with(density) {
                gridItemOverlayWidth.toDp()
            }

            val boundingBoxHeightDp = with(density) {
                gridItemOverlayHeight.toDp()
            }

            val widthDp by remember { mutableStateOf(boundingBoxWidthDp) }

            val heightDp by remember { mutableStateOf(boundingBoxHeightDp) }

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
        }

        if (showOverlay) {
            val boundingBoxWidthDp = with(density) {
                gridItemOverlayWidth.toDp()
            }

            val boundingBoxHeightDp = with(density) {
                gridItemOverlayHeight.toDp()
            }

            val widthDp by remember { mutableStateOf(boundingBoxWidthDp) }

            val heightDp by remember { mutableStateOf(boundingBoxHeightDp) }

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
        }

        if (showPopupMenu) {
            MenuOverlay(
                modifier = modifier,
                x = dragOffsetX,
                y = dragOffsetY,
                width = gridItemOverlayWidth,
                height = gridItemOverlayHeight,
                screenWidth = gridItemOverlayScreenWidth,
                screenHeight = gridItemOverlayScreenHeight,
                menuSizeMarginPixel = 100,
                onEdit = {

                },
                onResize = {
                    onResizeGridItemOverlay(
                        pagerState.currentPage,
                        dragOffsetX,
                        dragOffsetY,
                        gridItemOverlayScreenWidth,
                        gridItemOverlayScreenHeight,
                    )
                },
                onClose = {
                    showOverlay = false
                    showPopupMenu = false
                },
            )
        }

        if (showResize) {
            var resizeOffsetX by remember { mutableIntStateOf(gridItemOverlayOffsetX) }

            var resizeOffsetY by remember { mutableIntStateOf(gridItemOverlayOffsetY) }

            var widthPixel by remember { mutableIntStateOf(gridItemOverlayWidth) }

            var heightPixel by remember { mutableIntStateOf(gridItemOverlayHeight) }

            ResizableOverlay(
                x = resizeOffsetX,
                y = resizeOffsetY,
                width = widthPixel,
                height = heightPixel,
                onDragEnd = {
                    showResize = false
                },
                onTopStartDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountX = with(density) {
                        dragAmount.x.toDp().toPx().roundToInt()
                    }

                    val dragAmountY = with(density) {
                        dragAmount.y.toDp().toPx().roundToInt()
                    }

                    widthPixel += -dragAmountX
                    heightPixel += -dragAmountY

                    resizeOffsetX += dragAmount.x.roundToInt()
                    resizeOffsetY += dragAmount.y.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        gridItemOverlayId,
                        widthPixel,
                        heightPixel,
                        gridItemOverlayScreenWidth,
                        gridItemOverlayScreenHeight,
                        Anchor.BOTTOM_END,
                    )
                },
                onTopEndDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountX = with(density) {
                        dragAmount.x.toDp().toPx().roundToInt()
                    }

                    val dragAmountY = with(density) {
                        dragAmount.y.toDp().toPx().roundToInt()
                    }

                    widthPixel += dragAmountX
                    heightPixel += -dragAmountY

                    resizeOffsetY += dragAmount.y.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        gridItemOverlayId,
                        widthPixel,
                        heightPixel,
                        gridItemOverlayScreenWidth,
                        gridItemOverlayScreenHeight,
                        Anchor.BOTTOM_START,
                    )
                },
                onBottomStartDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountX = with(density) {
                        dragAmount.x.toDp().toPx().roundToInt()
                    }

                    val dragAmountY = with(density) {
                        dragAmount.y.toDp().toPx().roundToInt()
                    }

                    widthPixel += -dragAmountX
                    heightPixel += dragAmountY

                    resizeOffsetX += dragAmount.x.roundToInt()

                    onResizeGridItem(
                        pagerState.currentPage,
                        gridItemOverlayId,
                        widthPixel,
                        heightPixel,
                        gridItemOverlayScreenWidth,
                        gridItemOverlayScreenHeight,
                        Anchor.TOP_END,
                    )
                },
                onBottomEndDrag = { change, dragAmount ->
                    change.consume()
                    val dragAmountX = with(density) {
                        dragAmount.x.toDp().toPx().roundToInt()
                    }

                    val dragAmountY = with(density) {
                        dragAmount.y.toDp().toPx().roundToInt()
                    }

                    widthPixel += dragAmountX
                    heightPixel += dragAmountY

                    onResizeGridItem(
                        pagerState.currentPage,
                        gridItemOverlayId,
                        widthPixel,
                        heightPixel,
                        gridItemOverlayScreenWidth,
                        gridItemOverlayScreenHeight,
                        Anchor.TOP_START,
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

