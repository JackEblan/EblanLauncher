package com.eblan.launcher.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.editpage.EditPageScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.resize.ResizeScreen

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val eblanAppWidgetProviderInfosByGroup by viewModel.eblanAppWidgetProviderInfosByGroup.collectAsStateWithLifecycle()

    val shiftedAlgorithm by viewModel.shiftedAlgorithm.collectAsStateWithLifecycle()

    val targetPage by viewModel.targetPage.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
        shiftedAlgorithm = shiftedAlgorithm,
        targetPage = targetPage,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onDeleteGridItem = viewModel::deleteGridItem,
        onShowGridCache = viewModel::showGridCache,
        onResetGridCache = viewModel::resetGridCache,
        onEdit = onEdit,
        onSettings = onSettings,
        onStartMainActivity = viewModel::startMainActivity,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    shiftedAlgorithm: Boolean?,
    targetPage: Int,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onResetGridCache: (Int) -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
) {
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
        ) {
            when (homeUiState) {
                HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is HomeUiState.Success -> {
                    Success(
                        screen = screen,
                        gridItems = homeUiState.gridItemsByPage.gridItems,
                        userData = homeUiState.gridItemsByPage.userData,
                        eblanApplicationInfos = eblanApplicationInfos,
                        eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
                        dockGridItems = homeUiState.gridItemsByPage.dockGridItems,
                        shiftedAlgorithm = shiftedAlgorithm,
                        targetPage = targetPage,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onDeleteGridItem = onDeleteGridItem,
                        onShowGridCache = onShowGridCache,
                        onResetGridCache = onResetGridCache,
                        onEdit = onEdit,
                        onSettings = onSettings,
                        onStartMainActivity = onStartMainActivity,
                    )
                }
            }
        }
    }
}

@Composable
fun Success(
    modifier: Modifier = Modifier,
    screen: Screen,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    dockGridItems: List<GridItem>,
    shiftedAlgorithm: Boolean?,
    targetPage: Int,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onResetGridCache: (Int) -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
) {
    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var showOverlay by remember { mutableStateOf(false) }

    var overlayIntSize by remember { mutableStateOf(IntSize.Zero) }

    var drag by remember { mutableStateOf(Drag.None) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var currentPage by remember { mutableIntStateOf(0) }

    var addNewPage by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragIntOffset = offset.round()

                        drag = Drag.Start
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

                        dragIntOffset += dragAmount.round()

                        overlayIntOffset += dragAmount.round()
                    },
                )
            }
            .fillMaxSize(),
    ) {
        when (screen) {
            Screen.Pager -> {
                PagerScreen(
                    targetPage = targetPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                    gridItems = gridItems,
                    dockHeight = userData.dockHeight,
                    drag = drag,
                    dockGridItems = dockGridItems,
                    textColor = userData.textColor,
                    eblanApplicationInfos = eblanApplicationInfos,
                    eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
                    rootWidth = constraints.maxWidth,
                    rootHeight = constraints.maxHeight,
                    appDrawerColumns = userData.appDrawerColumns,
                    dragIntOffset = dragIntOffset,
                    onLongPressGrid = { newCurrentPage ->
                        currentPage = newCurrentPage

                        gridItemSource = null

                        onShowGridCache(Screen.EditPage)
                    },
                    onLongPressedGridItem = { newCurrentPage, imageBitmap, gridItemLayoutInfo ->
                        currentPage = newCurrentPage

                        addNewPage =
                            newCurrentPage != userData.pageCount - 1 || (gridItems[userData.pageCount - 1]?.size
                                ?: 0) > 1

                        overlayImageBitmap = imageBitmap

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.Old,
                        )

                        overlayIntOffset =
                            IntOffset(x = gridItemLayoutInfo.x, y = gridItemLayoutInfo.y)

                        overlayIntSize = IntSize(
                            width = gridItemLayoutInfo.width,
                            height = gridItemLayoutInfo.height,
                        )
                    },
                    onLongPressApplicationInfo = { newCurrentPage, imageBitmap, intOffset, intSize, gridItemLayoutInfo ->
                        currentPage = newCurrentPage

                        addNewPage = (gridItems[userData.pageCount - 1]?.size ?: 0) > 1

                        overlayImageBitmap = imageBitmap

                        overlayIntOffset = intOffset

                        overlayIntSize = intSize

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                        )

                        showOverlay = true
                    },
                    onDragStart = {
                        showOverlay = true

                        onShowGridCache(Screen.Drag)
                    },
                    onDraggingApplicationInfo = {
                        onShowGridCache(Screen.Drag)
                    },
                    onDragEndApplicationInfo = {
                        showOverlay = false
                    },
                    onLongPressWidget = { imageBitmap ->
                        overlayImageBitmap = imageBitmap
                    },
                    onDragStartWidget = { intOffset, intSize, gridItemLayoutInfo ->
                        overlayIntOffset = intOffset

                        overlayIntSize = intSize

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                        )

                        addNewPage = !gridItems[userData.pageCount - 1].isNullOrEmpty()

                        showOverlay = true

                        onShowGridCache(Screen.Drag)
                    },
                    onStartMainActivity = onStartMainActivity,
                )
            }

            Screen.Drag -> {
                DragScreen(
                    currentPage = currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    gridItems = gridItems,
                    dragIntOffset = dragIntOffset,
                    gridItemSource = gridItemSource,
                    drag = drag,
                    rootWidth = constraints.maxWidth,
                    rootHeight = constraints.maxHeight,
                    dockHeight = userData.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = userData.textColor,
                    shiftedAlgorithm = shiftedAlgorithm,
                    addNewPage = addNewPage,
                    onMoveGridItem = onMoveGridItem,
                    onDeleteGridItem = onDeleteGridItem,
                    onDragCancel = {
                        onResetGridCache(currentPage)

                        showOverlay = false
                    },
                    onDragEnd = { newTargetPage ->
                        addNewPage = false

                        onResetGridCache(newTargetPage)

                        showOverlay = false
                    },
                    onEdit = {

                    },
                    onResize = { newTargetPage ->
                        currentPage = newTargetPage

                        showOverlay = false

                        onShowGridCache(Screen.Resize)
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    currentPage = currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    gridItems = gridItems,
                    gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                    dockHeight = userData.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = userData.textColor,
                    onResizeGridItem = onResizeGridItem,
                    onResizeEnd = {
                        onResetGridCache(currentPage)
                    },
                )
            }

            Screen.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Screen.EditPage -> {
                EditPageScreen(
                    currentPage = currentPage,
                    rows = userData.rows,
                    columns = userData.columns,
                    pageCount = userData.pageCount,
                    infiniteScroll = userData.infiniteScroll,
                    dockRows = userData.dockRows,
                    dockColumns = userData.dockColumns,
                    dragIntOffset = dragIntOffset,
                    gridItems = gridItems,
                    dockHeight = userData.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = userData.textColor,
                    onSettings = onSettings,
                )
            }
        }

        if (showOverlay) {
            GridItemOverlay(
                preview = overlayImageBitmap,
                overlayIntOffset = overlayIntOffset,
                overlayIntSize = overlayIntSize,
            )
        }
    }
}

@Composable
private fun GridItemOverlay(
    modifier: Modifier = Modifier,
    preview: ImageBitmap?,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
) {
    val density = LocalDensity.current

    val size = remember {
        with(density) {
            DpSize(
                width = overlayIntSize.width.toDp(),
                height = overlayIntSize.height.toDp(),
            )
        }
    }

    if (preview != null) {
        Image(
            bitmap = preview,
            contentDescription = null,
            modifier = modifier
                .offset {
                    overlayIntOffset
                }
                .size(size)
                .zIndex(1f)
                .fillMaxSize()
                .border(width = 2.dp, color = Color.White),
        )
    }
}