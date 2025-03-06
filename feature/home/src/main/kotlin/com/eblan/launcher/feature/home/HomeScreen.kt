package com.eblan.launcher.feature.home

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemOverlay
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.grid.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.menu.MenuOverlay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (Int) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val gridItemBoundary by viewModel.gridItemBoundary.collectAsStateWithLifecycle()

    val gridItemByCoordinates by viewModel.gridItemByCoordinates.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemBoundary = gridItemBoundary,
        homeUiState = homeUiState,
        gridItemByCoordinates = gridItemByCoordinates,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onAddGridItem = viewModel::addGridItem,
        onGridItemByCoordinates = viewModel::getGridItemByCoordinates,
        onResetGridItemByCoordinates = viewModel::resetGridItemByCoordinates,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemBoundary: GridItemBoundary?,
    homeUiState: HomeUiState,
    gridItemByCoordinates: Boolean?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
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
    onResetGridItemByCoordinates: () -> Unit,
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
                        gridItemByCoordinates = gridItemByCoordinates,
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onAddGridItem = onAddGridItem,
                        onGetGridItemByCoordinates = onGridItemByCoordinates,
                        onResetGridItemByCoordinates = onResetGridItemByCoordinates,
                        onEdit = onEdit,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Success(
    modifier: Modifier = Modifier,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    gridItemBoundary: GridItemBoundary?,
    gridItemByCoordinates: Boolean?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
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
    onGetGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResetGridItemByCoordinates: () -> Unit,
    onEdit: (Int) -> Unit,
) {
    val density = LocalDensity.current

    val context = LocalContext.current

    val pagerState = rememberPagerState(
        pageCount = {
            userData.pageCount
        },
    )

    val sheetState = rememberModalBottomSheetState()

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showOverlay by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var gridItemOverlayId by remember { mutableStateOf<Int?>(null) }

    var gridItemOverlayWidth by remember { mutableIntStateOf(-1) }

    var gridItemOverlayHeight by remember { mutableIntStateOf(-1) }

    var gridItemOverlay by remember { mutableStateOf<GridItemOverlay?>(null) }

    var successUiState by remember { mutableStateOf(SuccessUiState.Pager) }

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
        combine(snapshotFlow { gridItemOverlay }, snapshotFlow { dragOffset }) { overlay, offset ->
            if (overlay != null) {
                gridItemOverlayId = overlay.gridItem.id
                gridItemOverlayWidth = overlay.width
                gridItemOverlayHeight = overlay.height

                onMoveGridItem(
                    pagerState.currentPage,
                    overlay.gridItem.id,
                    offset.x.roundToInt(),
                    offset.y.roundToInt(),
                    overlay.screenWidth,
                    overlay.screenHeight,
                )
            }
        }.collect()
    }

    LaunchedEffect(key1 = gridItemByCoordinates) {
        if (gridItemByCoordinates == null) return@LaunchedEffect

        if (gridItemByCoordinates) {
            showOverlay = true
            showMenu = true
        } else {
            showBottomSheet = true
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onGetGridItemByCoordinates(
                            pagerState.currentPage,
                            offset.x.roundToInt(),
                            offset.y.roundToInt(),
                            size.width,
                            size.height,
                        )
                    },
                    onDragEnd = {
                        showOverlay = false
                        showResize = false
                        gridItemOverlay = null
                        dragOffset = Offset.Zero
                        onResetGridItemByCoordinates()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        showMenu = false
                        showResize = false
                    },
                )
            }
            .fillMaxSize(),
    ) {
        when (successUiState) {
            SuccessUiState.Pager -> {
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
                        gridItemContent = { overlay ->
                            EmptyGridItem(
                                modifier = Modifier.pointerInput(key1 = overlay) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val down = awaitFirstDown(requireUnconsumed = false)

                                            val longPressChange =
                                                awaitLongPressOrCancellation(down.id) ?: continue

                                            if (!longPressChange.isConsumed) {
                                                dragOffset = dragOffset.copy(
                                                    x = overlay.x.toFloat(),
                                                    y = overlay.y.toFloat(),
                                                )

                                                gridItemOverlay = overlay
                                            }
                                        }
                                    }
                                },
                            )
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
            }

            SuccessUiState.Applications -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        Column(
                            modifier = Modifier.pointerInput(key1 = eblanApplicationInfo) {

                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = eblanApplicationInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )

                            Text(
                                text = eblanApplicationInfo.label,
                            )
                        }
                    }
                }
            }

            SuccessUiState.Widgets -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(appWidgetProviderInfos) { (eblanApplicationInfo, appWidgetProviderInfos) ->
                        Column(
                            modifier = Modifier.pointerInput(Unit) {

                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = eblanApplicationInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )

                            Text(
                                text = eblanApplicationInfo.label,
                            )

                            appWidgetProviderInfos.forEach { appWidgetProviderInfo ->
                                AsyncImage(
                                    model = appWidgetProviderInfo.loadPreviewImage(context, 0),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
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
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
            ) {
                Row {
                    Column(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                successUiState = SuccessUiState.Applications
                            },
                    ) {
                        Icon(imageVector = Icons.Default.Android, contentDescription = null)

                        Text(text = "Application")
                    }

                    Column(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                successUiState = SuccessUiState.Widgets
                            },
                    ) {
                        Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                        Text(text = "Widgets")
                    }
                }
            }
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
