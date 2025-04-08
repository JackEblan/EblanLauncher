package com.eblan.launcher.feature.home

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.DragType
import com.eblan.launcher.feature.home.model.GridItemByCoordinates
import com.eblan.launcher.feature.home.model.HomeType
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.placeholder.PlaceholderScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val pageDirection by viewModel.pageDirection.collectAsStateWithLifecycle()

    val showBottomSheet by viewModel.showBottomSheet.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val addGridItem by viewModel.addGridItem.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        pageDirection = pageDirection,
        homeUiState = homeUiState,
        showBottomSheet = showBottomSheet,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        addGridItem = addGridItem,
        onMoveGridItem = viewModel::moveGridItem,
        onMoveAddGridItem = viewModel::moveAddGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onResizeWidgetGridItem = viewModel::resizeWidgetGridItem,
        onAddApplicationInfoGridItem = viewModel::addApplicationInfoGridItem,
        onAddAppWidgetProviderInfoGridItem = viewModel::addAppWidgetProviderInfoGridItem,
        onShowBottomSheet = viewModel::showBottomSheet,
        onUpdateWidget = viewModel::updateWidget,
        onUpdatePageCount = viewModel::updatePageCount,
        onResetShowBottomSheet = viewModel::resetShowBottomSheet,
        onResetGridItemMovement = viewModel::resetGridItemMovement,
        onResetAddGridItem = viewModel::resetAddGridItem,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    homeUiState: HomeUiState,
    showBottomSheet: Boolean,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    addGridItem: GridItem?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onMoveAddGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
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
    onAddApplicationInfoGridItem: (
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
    onAddAppWidgetProviderInfoGridItem: (
        page: Int,
        componentName: String,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onShowBottomSheet: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdateWidget: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onResetShowBottomSheet: () -> Unit,
    onResetGridItemMovement: () -> Unit,
    onResetAddGridItem: () -> Unit,
    onEdit: (String) -> Unit,
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
                        gridItems = homeUiState.gridItemsByPage.gridItems,
                        userData = homeUiState.gridItemsByPage.userData,
                        pageDirection = pageDirection,
                        showBottomSheet = showBottomSheet,
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        addGridItem = addGridItem,
                        onMoveGridItem = onMoveGridItem,
                        onMoveAddGridItem = onMoveAddGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onResizeWidgetGridItem = onResizeWidgetGridItem,
                        onAddApplicationInfoGridItem = onAddApplicationInfoGridItem,
                        onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                        onShowBottomSheet = onShowBottomSheet,
                        onUpdateWidget = onUpdateWidget,
                        onUpdatePageCount = onUpdatePageCount,
                        onResetShowBottomSheet = onResetShowBottomSheet,
                        onResetGridItemMovement = onResetGridItemMovement,
                        onResetAddGridItem = onResetAddGridItem,
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
    pageDirection: PageDirection?,
    showBottomSheet: Boolean,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    addGridItem: GridItem?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onMoveAddGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
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
    onAddApplicationInfoGridItem: (
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
    onAddAppWidgetProviderInfoGridItem: (
        page: Int,
        componentName: String,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onShowBottomSheet: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdateWidget: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onResetShowBottomSheet: () -> Unit,
    onResetGridItemMovement: () -> Unit,
    onResetAddGridItem: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2,
        pageCount = {
            Int.MAX_VALUE
        },
    )

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showOverlay by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var homeType by remember { mutableStateOf(HomeType.Pager) }

    var dragType by remember { mutableStateOf(DragType.None) }

    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    var appWidgetId by remember { mutableStateOf<Int?>(null) }

    var lastGridItemByCoordinates by remember { mutableStateOf<GridItemByCoordinates?>(null) }

    val scope = rememberCoroutineScope()

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        appWidgetId = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        } else {
            -1
        }
    }

    LaunchedEffect(key1 = addGridItem, key2 = dragType) {
        if (addGridItem != null) {
            snapshotFlow { dragOffset }.onStart {
                homeType = HomeType.Placeholder
                showOverlay = true
                showMenu = true
            }.onEach { offset ->
                val z = pagerState.currentPage - (Int.MAX_VALUE / 2)

                val page = z - z.floorDiv(userData.pageCount) * userData.pageCount

                onMoveAddGridItem(
                    page,
                    addGridItem,
                    offset.x.roundToInt(),
                    offset.y.roundToInt(),
                    screenSize.width,
                    screenSize.height,
                )

                if (dragType == DragType.End) {
                    when (val data = addGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            homeType = HomeType.Pager
                            onResetAddGridItem()
                        }

                        is GridItemData.Widget -> {
                            val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

                            val provider = ComponentName.unflattenFromString(data.componentName)

                            if (appWidgetManager.bindAppWidgetIdIfAllowed(
                                    appWidgetId = allocateAppWidgetId,
                                    provider = provider,
                                )
                            ) {
                                onUpdateWidget(addGridItem, allocateAppWidgetId)
                            } else {
                                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                                    putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        allocateAppWidgetId,
                                    )
                                    putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                                        provider,
                                    )
                                }

                                appWidgetLauncher.launch(intent)
                            }
                        }
                    }
                }
            }.collect()
        }
    }

    LaunchedEffect(key1 = addGridItem, key2 = appWidgetId) {
        if (addGridItem != null && appWidgetId != null) {
            onUpdateWidget(addGridItem, appWidgetId!!)

            appWidgetId = null

            homeType = HomeType.Pager

            onResetAddGridItem()
        }
    }

    LaunchedEffect(key1 = dragType) {
        when (dragType) {
            DragType.Start -> {
                showResize = false
            }

            DragType.End, DragType.Cancel -> {
                showOverlay = false
                showResize = false
                homeType = HomeType.Pager
            }

            DragType.Drag -> {
                showMenu = false
                showResize = false
            }

            DragType.None -> Unit
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragType = DragType.Start
                    },
                    onDragEnd = {
                        dragType = DragType.End
                    },
                    onDragCancel = {
                        dragType = DragType.Cancel
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount

                        dragType = DragType.Drag
                    },
                )
            }
            .fillMaxSize()
            .onSizeChanged { intSize ->
                screenSize = intSize
            },
    ) {
        when (homeType) {
            HomeType.Pager -> {
                PagerScreen(
                    pagerState = pagerState,
                    lastGridItemByCoordinates = lastGridItemByCoordinates,
                    userData = userData,
                    gridItems = gridItems,
                    showMenu = showMenu,
                    showResize = showResize,
                    showBottomSheet = showBottomSheet,
                    onResizeGridItem = onResizeGridItem,
                    onResizeWidgetGridItem = onResizeWidgetGridItem,
                    onDismissRequest = {
                        showMenu = false
                    },
                    onResizeEnd = {
                        showResize = false
                    },
                    onShowBottomSheet = onShowBottomSheet,
                    onLongPressedGridItem = { gridItemByCoordinates, x, y, width, height ->
                        lastGridItemByCoordinates = gridItemByCoordinates

                        dragOffset = IntOffset(
                            x = x,
                            y = y,
                        ).toOffset()

                        overlaySize = IntSize(
                            width = width,
                            height = height,
                        )
                        showOverlay = true
                        showMenu = true
                        homeType = HomeType.Placeholder
                    },
                    onHomeType = {
                        homeType = it
                    },
                    onResetLastGridItemByCoordinates = {
                        lastGridItemByCoordinates = null
                    },
                    onResetShowBottomSheet = onResetShowBottomSheet,
                    onEdit = {

                    },
                    onResize = {
                        showOverlay = false
                        showResize = true
                    },
                )
            }

            HomeType.Application -> {
                ApplicationScreen(
                    currentPage = pagerState.currentPage,
                    pageCount = userData.pageCount,
                    screenSize = screenSize,
                    eblanApplicationInfos = eblanApplicationInfos,
                    onLongPressApplicationInfo = { offset, size ->
                        dragOffset = offset
                        overlaySize = size
                        onResetShowBottomSheet()
                    },
                    onAddApplicationInfoGridItem = onAddApplicationInfoGridItem,
                )
            }

            HomeType.Widget -> {
                WidgetScreen(
                    currentPage = pagerState.currentPage,
                    pageCount = userData.pageCount,
                    rows = userData.rows,
                    columns = userData.columns,
                    screenSize = screenSize,
                    appWidgetProviderInfos = appWidgetProviderInfos,
                    onLongPressAppWidgetProviderInfo = { offset, size ->
                        dragOffset = offset
                        overlaySize = size
                        onResetShowBottomSheet()
                    },
                    onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                )
            }

            HomeType.Placeholder -> {
                PlaceholderScreen(
                    pageDirection = pageDirection,
                    currentPage = pagerState.currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    gridItems = gridItems,
                    dragOffset = dragOffset,
                    lastGridItemByCoordinates = lastGridItemByCoordinates,
                    dragType = dragType,
                    onMoveGridItem = onMoveGridItem,
                    onUpdatePageCount = onUpdatePageCount,
                    onDragEnd = { index ->
                        val targetPage = run {
                            val currentPage = pagerState.currentPage
                            val offset = currentPage - (Int.MAX_VALUE / 2)
                            val currentReal = offset - Math.floorDiv(
                                offset,
                                userData.pageCount,
                            ) * userData.pageCount
                            val delta = index - currentReal
                            currentPage + delta
                        }

                        scope.launch {
                            pagerState.scrollToPage(targetPage)
                        }
                    },
                )
            }
        }

        if (showOverlay) {
            GridItemOverlay(overlaySize = overlaySize, dragOffset = dragOffset)
        }
    }
}

@Composable
private fun GridItemOverlay(
    modifier: Modifier = Modifier,
    overlaySize: IntSize,
    dragOffset: Offset,
) {
    val density = LocalDensity.current

    val boundingBoxWidthDp = with(density) {
        overlaySize.width.toDp()
    }

    val boundingBoxHeightDp = with(density) {
        overlaySize.height.toDp()
    }

    val widthDp by remember { mutableStateOf(boundingBoxWidthDp) }

    val heightDp by remember { mutableStateOf(boundingBoxHeightDp) }

    Box(
        modifier = modifier
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
