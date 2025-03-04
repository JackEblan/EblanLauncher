package com.eblan.launcher.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
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
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.grid.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.menu.MenuOverlay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (Int) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val gridItemBoundary by viewModel.gridItemBoundary.collectAsStateWithLifecycle()

    val gridItemOverlayUiState by viewModel.gridItemOverlayUiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemBoundary = gridItemBoundary,
        homeUiState = homeUiState,
        gridItemOverlayUiState = gridItemOverlayUiState,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onAddGridItem = viewModel::addGridItem,
        onGridItemByCoordinates = viewModel::getGridItemByCoordinates,
        onResetGridItemOverlay = viewModel::resetGridItemOverlay,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemBoundary: GridItemBoundary?,
    homeUiState: HomeUiState,
    gridItemOverlayUiState: GridItemOverlayUiState,
    onMoveGridItem: (
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onAddGridItem: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResetGridItemOverlay: () -> Unit,
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
                        gridItemOverlayUiState = gridItemOverlayUiState,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onAddGridItem = onAddGridItem,
                        onGridItemByCoordinates = onGridItemByCoordinates,
                        onResetGridItemOverlay = onResetGridItemOverlay,
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
    gridItemOverlayUiState: GridItemOverlayUiState,
    onMoveGridItem: (
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onAddGridItem: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResetGridItemOverlay: () -> Unit,
    onEdit: (Int) -> Unit,
) {
    val density = LocalDensity.current

    val pagerState = rememberPagerState(
        pageCount = {
            userData.pageCount
        },
    )

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showOverlay by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var gridItemOverlayId by remember { mutableStateOf<Int?>(null) }

    var gridItemOverlayWidth by remember { mutableIntStateOf(-1) }

    var gridItemOverlayHeight by remember { mutableIntStateOf(-1) }

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

    LaunchedEffect(key1 = gridItemOverlayUiState) {
        when (gridItemOverlayUiState) {
            GridItemOverlayUiState.Loading -> {

            }

            is GridItemOverlayUiState.Success -> {
                if (gridItemOverlayUiState.gridItemOverlay != null) {
                    gridItemOverlayId = gridItemOverlayUiState.gridItemOverlay.gridItem.id

                    dragOffset = dragOffset.copy(
                        x = gridItemOverlayUiState.gridItemOverlay.x.toFloat(),
                        y = gridItemOverlayUiState.gridItemOverlay.y.toFloat(),
                    )

                    gridItemOverlayWidth = gridItemOverlayUiState.gridItemOverlay.width
                    gridItemOverlayHeight = gridItemOverlayUiState.gridItemOverlay.height

                    showOverlay = true
                    showMenu = true

                    snapshotFlow { dragOffset }.onEach { offset ->
                        onMoveGridItem(
                            pagerState.currentPage,
                            gridItemOverlayUiState.gridItemOverlay.gridItem.id,
                            offset.x.roundToInt(),
                            offset.y.roundToInt(),
                            gridItemOverlayUiState.gridItemOverlay.screenWidth,
                            gridItemOverlayUiState.gridItemOverlay.screenHeight,
                        )
                    }.collect()
                } else {
                    showBottomSheet = true
                }
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // Wait for the first down event (even if partially consumed).
                        val down = awaitFirstDown(requireUnconsumed = false)
                        // Wait for a long press (or cancellation) starting from that down.
                        val longPressChange = awaitLongPressOrCancellation(down.id)
                        if (longPressChange == null) {
                            onResetGridItemOverlay()
                            showOverlay = false
                            continue
                        }
                        // Only trigger onDragStart if the long press wasn’t already consumed.
                        if (!longPressChange.isConsumed) {
                            onGridItemByCoordinates(
                                pagerState.currentPage,
                                longPressChange.position.x.roundToInt(),
                                longPressChange.position.y.roundToInt(),
                                size.width,
                                size.height,
                            )
                        }
                        var dragging: Boolean

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val dragAmount = change.position - change.previousPosition
                                    // Only trigger drag if the change isn’t already consumed.
                                    if (!change.isConsumed) {
                                        change.consume()
                                        showMenu = false
                                        dragOffset += dragAmount
                                    }
                                }
                            }
                            dragging = event.changes.any { it.pressed }
                        } while (dragging)

                        onResetGridItemOverlay()
                        showOverlay = false
                    }
                }
            }
            .fillMaxSize(),
    ) {
        HorizontalPager(state = pagerState) { page ->
            GridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                page = page,
                rows = userData.rows,
                columns = userData.columns,
                id = gridItemOverlayId,
                gridItems = gridItems,
                onResizeGridItem = onResizeGridItem,
                showMenu = showMenu,
                showResize = showResize,
                onDismissRequest = {
                    showMenu = false
                },
                onResizeEnd = {
                    showResize = false
                },
                gridItemContent = {
                    EmptyGridItem()
                },
                menuContent = {
                    MenuOverlay(
                        onEdit = {

                        },
                        onResize = {
                            showOverlay = false
                            showResize = true
                        },
                    )
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
                            x = dragOffset.x.roundToInt(),
                            y = dragOffset.y.roundToInt(),
                        )
                    }
                    .size(width = widthDp, height = heightDp)
                    .background(Color.Green),
            ) {
                Text(text = "Drag")
            }
        }

        if (showBottomSheet) {
            // Maybe show a bottom sheet here
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
