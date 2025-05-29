package com.eblan.launcher.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.drag.DragScreen
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

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
        shiftedAlgorithm = shiftedAlgorithm,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onUpdateWidgetGridItem = viewModel::updateWidgetGridItem,
        onDeleteGridItem = viewModel::deleteGridItem,
        onShowGridCache = viewModel::showGridCache,
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
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    shiftedAlgorithm: Boolean?,
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
    onShowGridCache: (Screen) -> Unit,
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is HomeUiState.Success -> {
                    Success(
                        screen = screen,
                        gridItems = homeUiState.gridItemsByPage.gridItems,
                        userData = homeUiState.gridItemsByPage.userData,
                        eblanApplicationInfos = eblanApplicationInfos,
                        eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
                        rootWidth = constraints.maxWidth,
                        rootHeight = constraints.maxHeight,
                        dockGridItems = homeUiState.gridItemsByPage.dockGridItems,
                        shiftedAlgorithm = shiftedAlgorithm,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                        onDeleteGridItem = onDeleteGridItem,
                        onShowGridCache = onShowGridCache,
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
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    rootWidth: Int,
    rootHeight: Int,
    dockGridItems: List<GridItem>,
    shiftedAlgorithm: Boolean?,
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
    onShowGridCache: (Screen) -> Unit,
    onLaunchApplication: (String) -> Unit,
    onResetGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var showOverlay by remember { mutableStateOf(false) }

    var overlayIntSize by remember { mutableStateOf(IntSize.Zero) }

    var drag by remember { mutableStateOf(Drag.None) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var targetPage by remember { mutableIntStateOf(0) }

    var addNewPage by remember { mutableStateOf(false) }

    Box(
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
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    appDrawerColumns = userData.appDrawerColumns,
                    dragIntOffset = dragIntOffset,
                    onLongPressGrid = { currentPage ->
                        targetPage = currentPage

                        gridItemSource = null

                        showBottomSheet = true
                    },
                    onLongPressedGridItem = { currentPage, newPage, imageBitmap, gridItemLayoutInfo ->
                        targetPage = currentPage

                        addNewPage = newPage

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
                    onLaunchApplication = onLaunchApplication,
                    onLongPressApplicationInfo = { currentPage, imageBitmap, intOffset, intSize, gridItemLayoutInfo ->
                        targetPage = currentPage

                        addNewPage = true

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

                        addNewPage = true

                        showOverlay = true

                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Drag -> {
                DragScreen(
                    targetPage = targetPage,
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
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    dockHeight = userData.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = userData.textColor,
                    shiftedAlgorithm = shiftedAlgorithm,
                    addNewPage = addNewPage,
                    onMoveGridItem = onMoveGridItem,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                    onDeleteGridItem = onDeleteGridItem,
                    onDragCancel = {
                        onResetGridCache()

                        showOverlay = false
                    },
                    onDragEnd = { newTargetPage ->
                        targetPage = newTargetPage

                        addNewPage = false

                        onResetGridCache()

                        showOverlay = false
                    },
                    onEdit = {

                    },
                    onResize = { newTargetPage ->
                        targetPage = newTargetPage

                        showOverlay = false

                        onShowGridCache(Screen.Resize)
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    targetPage = targetPage,
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
                    onResizeEnd = onResetGridCache,
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

        if (showBottomSheet) {
            HomeBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onSettings = onSettings,
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

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun HomeBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
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
