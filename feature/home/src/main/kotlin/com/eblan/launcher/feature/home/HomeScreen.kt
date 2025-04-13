package com.eblan.launcher.feature.home

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.DragType
import com.eblan.launcher.feature.home.model.HomeType
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.grid.GridScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val pageDirection by viewModel.pageDirection.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val addGridItemDimensions by viewModel.addGridItemDimensions.collectAsStateWithLifecycle()

    val gridCacheItems by viewModel.gridCacheItems.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        pageDirection = pageDirection,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        gridCacheItems = gridCacheItems,
        addGridItemDimensions = addGridItemDimensions,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onResizeWidgetGridItem = viewModel::resizeWidgetGridItem,
        onAddApplicationInfoGridItem = viewModel::addApplicationInfoGridItem,
        onAddAppWidgetProviderInfoGridItem = viewModel::addAppWidgetProviderInfoGridItem,
        onUpdateWidget = viewModel::updateWidget,
        onUpdatePageCount = viewModel::updatePageCount,
        onDeletePage = viewModel::deletePage,
        onResetAddGridItem = viewModel::resetAddGridItemDimensions,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    homeUiState: HomeUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    gridCacheItems: Map<Int, List<GridItem>>,
    addGridItemDimensions: GridItemDimensions?,
    onMoveGridItem: (
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
    onUpdateWidget: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
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
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        gridCacheItems = gridCacheItems,
                        addGridItemDimensions = addGridItemDimensions,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onResizeWidgetGridItem = onResizeWidgetGridItem,
                        onAddApplicationInfoGridItem = onAddApplicationInfoGridItem,
                        onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                        onUpdateWidget = onUpdateWidget,
                        onUpdatePageCount = onUpdatePageCount,
                        onDeletePage = onDeletePage,
                        onResetAddGridItem = onResetAddGridItem,
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
    pageDirection: PageDirection?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    gridCacheItems: Map<Int, List<GridItem>>,
    addGridItemDimensions: GridItemDimensions?,
    onMoveGridItem: (
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
    onUpdateWidget: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onResetAddGridItem: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = if (userData.infiniteScroll) Int.MAX_VALUE / 2 else 0,
        pageCount = {
            if (userData.infiniteScroll) {
                Int.MAX_VALUE
            } else {
                userData.pageCount
            }
        },
    )

    val sheetState = rememberModalBottomSheetState()

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showOverlay by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var homeType by remember { mutableStateOf(HomeType.Pager) }

    var dragType by remember { mutableStateOf(DragType.None) }

    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    var appWidgetId by remember { mutableStateOf<Int?>(null) }

    var lastGridItemDimensions by remember { mutableStateOf<GridItemDimensions?>(null) }

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

    LaunchedEffect(key1 = dragType) {
        if (addGridItemDimensions != null) {
            homeType = HomeType.Grid
            showOverlay = true
            showMenu = true

            if (dragType == DragType.End) {
                when (val data = addGridItemDimensions.gridItem.data) {
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
                            onUpdateWidget(addGridItemDimensions.gridItem, allocateAppWidgetId)
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
        }
    }

    LaunchedEffect(key1 = appWidgetId) {
        if (addGridItemDimensions != null && appWidgetId != null) {
            onUpdateWidget(addGridItemDimensions.gridItem, appWidgetId!!)

            appWidgetId = null

            homeType = HomeType.Pager

            onResetAddGridItem()
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
                    lastGridItemDimensions = lastGridItemDimensions,
                    userData = userData,
                    gridItems = gridItems,
                    showMenu = showMenu,
                    showResize = showResize,
                    dragType = dragType,
                    onResizeGridItem = onResizeGridItem,
                    onResizeWidgetGridItem = onResizeWidgetGridItem,
                    onDismissRequest = {
                        showMenu = false
                    },
                    onResizeEnd = {
                        showResize = false
                    },
                    onShowBottomSheet = {
                        lastGridItemDimensions = null
                        showBottomSheet = true
                    },
                    onLongPressedGridItem = { gridItemDimensions ->
                        lastGridItemDimensions = gridItemDimensions

                        dragOffset = IntOffset(
                            x = gridItemDimensions.x,
                            y = gridItemDimensions.y,
                        ).toOffset()

                        overlaySize = IntSize(
                            width = gridItemDimensions.width,
                            height = gridItemDimensions.height,
                        )
                    },
                    onDragStart = {
                        showOverlay = true
                        showMenu = true
                        homeType = HomeType.Grid
                    },
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
                        showBottomSheet = false
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
                        showBottomSheet = false
                    },
                    onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                )
            }

            HomeType.Grid -> {
                GridScreen(
                    pageDirection = pageDirection,
                    currentPage = pagerState.currentPage,
                    userData = userData,
                    gridCacheItems = gridCacheItems,
                    dragOffset = dragOffset,
                    lastGridItemDimensions = lastGridItemDimensions,
                    addGridItemDimensions = addGridItemDimensions,
                    dragType = dragType,
                    onMoveGridItem = onMoveGridItem,
                    onUpdatePageCount = onUpdatePageCount,
                    onDragEnd = { targetPage ->
                        showOverlay = false
                        homeType = HomeType.Pager

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
        if (showBottomSheet) {
            HomeBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onChangeHomeType = {
                    homeType = it
                },
                onDeletePage = {
                    val page = calculatePage(
                        index = pagerState.currentPage,
                        infiniteScroll = userData.infiniteScroll,
                        pageCount = userData.pageCount,
                    )

                    onDeletePage(page)
                },
            )
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onChangeHomeType: (HomeType) -> Unit,
    onDeletePage: () -> Unit,
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
                        onChangeHomeType(HomeType.Application)

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
                        onChangeHomeType(HomeType.Widget)

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                Text(text = "Widgets")
            }

            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onDeletePage()

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)

                Text(text = "Delete page")
            }
        }
    }
}
