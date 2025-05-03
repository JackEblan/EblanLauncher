package com.eblan.launcher.feature.home

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
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

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
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
    homeUiState: HomeUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: Map<EblanApplicationInfo, List<AppWidgetProviderInfo>>,
    onMoveGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
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
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
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
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        constraintsMaxWidth = constraints.maxWidth,
                        constraintsMaxHeight = constraints.maxHeight,
                        dockGridItems = homeUiState.gridItemsByPage.dockGridItems,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
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
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: Map<EblanApplicationInfo, List<AppWidgetProviderInfo>>,
    constraintsMaxWidth: Int,
    constraintsMaxHeight: Int,
    dockGridItems: List<GridItem>,
    onMoveGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
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

    var gridItemOffset by remember { mutableStateOf(IntOffset.Zero) }

    var showMenu by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var drag by remember { mutableStateOf(Drag.None) }

    val applicationScreenY = remember { Animatable(constraintsMaxHeight.toFloat()) }

    var preview by remember { mutableStateOf<ImageBitmap?>(null) }

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val configuration  = LocalConfiguration.current

    val screenHeight = with(density){
        configuration.screenHeightDp.dp.toPx()
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        onShowGridCache(Screen.Application)
                    },
                    onDragEnd = {
                        scope.launch {
                            if (applicationScreenY.value < constraintsMaxHeight / 2) {
                                applicationScreenY.animateTo(0f)
                            } else {
                                applicationScreenY.animateTo(screenHeight)

                                onShowGridCache(Screen.Pager)
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()

                        scope.launch {
                            val newY = applicationScreenY.value + dragAmount

                            applicationScreenY.snapTo(newY)
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        drag = Drag.Start

                        gridItemOffset = offset.round()
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

                        gridItemOffset += dragAmount.round()
                    },
                )
            }
            .fillMaxSize(),
    ) {
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
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    dockGridItems = dockGridItems,
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
                    onDragStart = {
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
                    gridItemOffset = gridItemOffset,
                    eblanApplicationInfos = eblanApplicationInfos,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    applicationScreenY = applicationScreenY.value,
                    onLongPressApplicationInfo = { imageBitmap ->
                        preview = imageBitmap
                    },
                    onDragStart = { size, gridItemLayoutInfo ->
                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                        )

                        overlaySize = size

                        onShowGridCache(Screen.Drag)
                    },
                    onClose = { y ->
                        scope.launch {
                            applicationScreenY.snapTo(y)

                            applicationScreenY.animateTo(constraintsMaxHeight.toFloat())

                            onShowGridCache(Screen.Pager)
                        }
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
                    gridItemOffset = gridItemOffset,
                    appWidgetProviderInfos = appWidgetProviderInfos,
                    constraintsMaxWidth = constraintsMaxWidth,
                    constraintsMaxHeight = constraintsMaxHeight,
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    onLongPressWidget = { imageBitmap ->
                        preview = imageBitmap
                    },
                    onDragStart = { size, gridItemLayoutInfo ->
                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                        )

                        overlaySize = size

                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Drag -> {
                DragScreen(
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
                    onDragEnd = { targetPage, gridItemLayoutInfo ->
                        gridItemSource =
                            gridItemSource?.copy(gridItemLayoutInfo = gridItemLayoutInfo)

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
