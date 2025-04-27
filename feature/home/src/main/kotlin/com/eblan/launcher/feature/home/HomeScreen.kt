package com.eblan.launcher.feature.home

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.resize.ResizeScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val pageDirection by viewModel.pageDirection.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        pageDirection = pageDirection,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        wallpaper = wallpaper,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onResizeWidgetGridItem = viewModel::resizeWidgetGridItem,
        onUpdateWidgetGridItem = viewModel::updateWidgetGridItem,
        onDeleteGridItem = viewModel::deleteGridItem,
        onUpdatePageCount = viewModel::updatePageCount,
        onDeletePage = viewModel::deletePage,
        onShowGridCache = viewModel::showGridCache,
        onUpdateScreen = viewModel::updateScreen,
        onLaunchApplication = viewModel::launchApplication,
        onResetGridCache = viewModel::resetGridCache,
        onEdit = onEdit,
        onSettings = onSettings,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    pageDirection: PageDirection?,
    homeUiState: HomeUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: Map<EblanApplicationInfo, List<AppWidgetProviderInfo>>,
    wallpaper: ByteArray?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onLaunchApplication: (String) -> Unit,
    onResetGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold { paddingValues ->
        BoxWithConstraints(
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
                        screen = screen,
                        gridItems = homeUiState.gridItemsByPage.gridItems,
                        userData = homeUiState.gridItemsByPage.userData,
                        pageDirection = pageDirection,
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        wallpaper = wallpaper,
                        constraintsMaxWidth = constraints.maxWidth,
                        constraintsMaxHeight = constraints.maxHeight,
                        dockGridItems = homeUiState.gridItemsByPage.dockGridItems,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onResizeWidgetGridItem = onResizeWidgetGridItem,
                        onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                        onDeleteGridItem = onDeleteGridItem,
                        onUpdatePageCount = onUpdatePageCount,
                        onDeletePage = onDeletePage,
                        onShowGridCache = onShowGridCache,
                        onUpdateScreen = onUpdateScreen,
                        onLaunchApplication = onLaunchApplication,
                        onResetGridCache = onResetGridCache,
                        onEdit = onEdit,
                        onSettings = onSettings,
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
    screen: Screen,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    pageDirection: PageDirection?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: Map<EblanApplicationInfo, List<AppWidgetProviderInfo>>,
    wallpaper: ByteArray?,
    constraintsMaxWidth: Int,
    constraintsMaxHeight: Int,
    dockGridItems: List<GridItem>,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onLaunchApplication: (String) -> Unit,
    onResetGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
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

    var gridItemOffset by remember { mutableStateOf(Offset.Zero) }

    var showMenu by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var userScrollEnabled by remember { mutableStateOf(true) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var drag by remember { mutableStateOf(Drag.None) }

    var preview by remember { mutableStateOf<ImageBitmap?>(null) }

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        userScrollEnabled = true
                    },
                    onDrag = { change, dragAmount ->
                        // TODO: Implement gestures here in the future, when vertical gesture is detected, we disable the pager scroll

                        val angle = atan2(
                            dragAmount.y,
                            dragAmount.x,
                        ) * (180 / Math.PI)  // Calculate angle in degrees
                        println("Swipe Angle: $angle")

                        // Check for a horizontal swipe (right condition)
                        if (abs(dragAmount.x) > abs(dragAmount.y)) {
                            // Right swipe (angle close to 0°)
                            if (dragAmount.x > 0) {
                                println("Horizontal swipe right (Angle: $angle)")
                            }
                            // Left swipe (angle close to ±180°)
                            else {
                                println("Horizontal swipe left (Angle: $angle)")
                            }
                        } else {
                            userScrollEnabled = false

                            // Vertical swipe (angle close to ±90°)
                            if (dragAmount.y > 0) {
                                println("Vertical swipe down (Angle: $angle)")
                            } else {
                                println("Vertical swipe up (Angle: $angle)")
                            }
                        }

                        change.consume() // Consume the gesture to prevent further handling
                    },
                )
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        drag = Drag.Start

                        gridItemOffset = offset
                    },
                    onDragEnd = {
                        drag = Drag.End
                    },
                    onDragCancel = {
                        drag = Drag.Cancel
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        drag = Drag.Dragging

                        gridItemOffset += dragAmount
                    },
                )
            }
            .fillMaxSize(),
    ) {
        AsyncImage(
            model = wallpaper,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        when (screen) {
            Screen.Pager -> {
                PagerScreen(
                    pagerState = pagerState,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                    gridItems = gridItems,
                    showMenu = showMenu,
                    userScrollEnabled = userScrollEnabled,
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    gridItemOffset = gridItemOffset.round(),
                    dockGridItems = dockGridItems,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    onDismissRequest = {
                        showMenu = false
                    },
                    onLongPressGrid = {
                        gridItemSource = null

                        showBottomSheet = true
                    },
                    onLongPressedGridItem = { imageBitmap, gridItemLayoutInfo ->
                        preview = imageBitmap

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.Old,
                        )

                        overlaySize = IntSize(
                            width = gridItemLayoutInfo.width,
                            height = gridItemLayoutInfo.height,
                        )

                    },
                    onDragStart = { intOffset ->
                        gridItemOffset = intOffset.toOffset()

                        onShowGridCache(Screen.Drag)
                    },
                    onLaunchApplication = onLaunchApplication,
                    onEdit = {

                    },
                    onResize = {
                        onShowGridCache(Screen.Resize)
                    },
                )
            }

            Screen.Application -> {
                ApplicationScreen(
                    currentPage = pagerState.currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    gridItemOffset = gridItemOffset.round(),
                    eblanApplicationInfos = eblanApplicationInfos,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    onLongPressApplicationInfo = { imageBitmap ->
                        preview = imageBitmap
                    },
                    onDragStart = { offset, size, gridItemLayoutInfo ->
                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                        )

                        gridItemOffset = offset.toOffset()

                        overlaySize = size

                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Widget -> {
                WidgetScreen(
                    currentPage = pagerState.currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    gridItemOffset = gridItemOffset.round(),
                    appWidgetProviderInfos = appWidgetProviderInfos,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    onLongPressWidget = { imageBitmap ->
                        preview = imageBitmap
                    },
                    onDragStart = { offset, size, gridItemLayoutInfo ->
                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                        )

                        gridItemOffset = offset.toOffset()

                        overlaySize = size

                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Drag -> {
                DragScreen(
                    pageDirection = pageDirection,
                    currentPage = pagerState.currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    gridItems = gridItems,
                    gridItemOffset = gridItemOffset,
                    gridItemSource = gridItemSource,
                    drag = drag,
                    preview = preview,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    dockHeight = userData.dockHeight,
                    dockGridItems = dockGridItems,
                    onMoveGridItem = onMoveGridItem,
                    onUpdatePageCount = onUpdatePageCount,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                    onDeleteGridItem = onDeleteGridItem,
                    onDragCancel = {
                        onResetGridCache()
                    },
                    onDragEnd = { targetPage ->
                        showMenu = true

                        onResetGridCache()

                        scope.launch {
                            pagerState.scrollToPage(targetPage)
                        }
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    currentPage = pagerState.currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                    dockHeight = userData.dockHeight,
                    dockGridItems = dockGridItems,
                    gridItems = gridItems,
                    onResizeGridItem = onResizeGridItem,
                    onResizeWidgetGridItem = onResizeWidgetGridItem,
                    onResizeEnd = {
                        onResetGridCache()
                    },
                )
            }
        }

        if (showBottomSheet) {
            HomeBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onUpdateScreen = onUpdateScreen,
                onDeletePage = {
                    val page = calculatePage(
                        index = pagerState.currentPage,
                        infiniteScroll = userData.infiniteScroll,
                        pageCount = userData.pageCount,
                    )

                    onDeletePage(page)
                },
                onSettings = onSettings,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun HomeBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeletePage: () -> Unit,
    onSettings: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
    ) {
        FlowRow(modifier = Modifier.fillMaxWidth(), maxLines = 4) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onUpdateScreen(Screen.Application)

                        onDismissRequest()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null)

                Text(text = "Application")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onUpdateScreen(Screen.Widget)

                        onDismissRequest()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                Text(text = "Widgets")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onDeletePage()

                        onDismissRequest()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)

                Text(text = "Delete page")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSettings()

                        onDismissRequest()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)

                Text(text = "Settings")
            }
        }
    }
}
