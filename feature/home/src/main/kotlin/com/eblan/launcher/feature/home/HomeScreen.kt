package com.eblan.launcher.feature.home

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
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

    val movedGridItems by viewModel.movedGridItems.collectAsStateWithLifecycle()

    val targetPage by viewModel.targetPage.collectAsStateWithLifecycle()

    val movedPages by viewModel.movedPages.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
        movedGridItems = movedGridItems,
        targetPage = targetPage,
        movedPages = movedPages,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onDeleteAppWidgetId = viewModel::deleteAppWidgetId,
        onDeleteGridItem = viewModel::deleteGridItem,
        onShowGridCache = viewModel::showGridCache,
        onResetGridCache = viewModel::resetGridCache,
        onEdit = onEdit,
        onSettings = onSettings,
        onStartMainActivity = viewModel::startMainActivity,
        onMovePage = viewModel::movePage,
        onResetMovedPages = viewModel::resetMovedPages,
        onCancelEditPage = viewModel::cancelEditPage,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    movedGridItems: Boolean?,
    targetPage: Int,
    movedPages: Boolean,
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
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onResetGridCache: (Int) -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onMovePage: (from: Int, to: Int) -> Unit,
    onResetMovedPages: () -> Unit,
    onCancelEditPage: (Int) -> Unit,
) {
    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    var showOverlay by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        BoxWithConstraints(
            modifier = modifier
                .drawWithContent {
                    drawContent()

                    if (showOverlay) {
                        overlayImageBitmap?.let { image ->
                            drawImage(
                                image = image,
                                topLeft = overlayIntOffset.toOffset(),
                                alpha = 0.5f,
                            )
                        }
                    }
                }
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
                        movedGridItems = movedGridItems,
                        targetPage = targetPage,
                        movedPages = movedPages,
                        rootWidth = constraints.maxWidth,
                        rootHeight = constraints.maxHeight,
                        dragIntOffset = dragIntOffset,
                        drag = drag,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onDeleteAppWidgetId = onDeleteAppWidgetId,
                        onDeleteGridItem = onDeleteGridItem,
                        onShowGridCache = onShowGridCache,
                        onResetGridCache = onResetGridCache,
                        onEdit = onEdit,
                        onSettings = onSettings,
                        onStartMainActivity = onStartMainActivity,
                        onMovePage = onMovePage,
                        onResetMovedPages = onResetMovedPages,
                        onCancelEditPage = onCancelEditPage,
                        onUpdateOverlayIntOffset = { intOffset ->
                            overlayIntOffset = intOffset
                        },
                        onUpdateOverlayImageBitmap = { imageBitmap ->
                            overlayImageBitmap = imageBitmap
                        },
                        onShowOverlay = { newShowOverlay ->
                            showOverlay = newShowOverlay
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.Success(
    modifier: Modifier = Modifier,
    screen: Screen,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    dockGridItems: List<GridItem>,
    movedGridItems: Boolean?,
    targetPage: Int,
    movedPages: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dragIntOffset: IntOffset,
    drag: Drag,
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
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onResetGridCache: (Int) -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onMovePage: (from: Int, to: Int) -> Unit,
    onResetMovedPages: () -> Unit,
    onCancelEditPage: (Int) -> Unit,
    onUpdateOverlayIntOffset: (IntOffset) -> Unit,
    onUpdateOverlayImageBitmap: (ImageBitmap?) -> Unit,
    onShowOverlay: (Boolean) -> Unit,
) {
    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var currentPage by remember { mutableIntStateOf(0) }

    var addNewPage by remember { mutableStateOf(false) }

    when (screen) {
        Screen.Pager -> {
            PagerScreen(
                modifier = modifier,
                targetPage = targetPage,
                rows = userData.homeSettings.rows,
                columns = userData.homeSettings.columns,
                pageCount = userData.homeSettings.pageCount,
                infiniteScroll = userData.homeSettings.infiniteScroll,
                dockRows = userData.homeSettings.dockRows,
                dockColumns = userData.homeSettings.dockColumns,
                gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                gridItems = gridItems,
                dockHeight = userData.homeSettings.dockHeight,
                drag = drag,
                dockGridItems = dockGridItems,
                textColor = userData.homeSettings.textColor,
                eblanApplicationInfos = eblanApplicationInfos,
                eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
                rootWidth = rootWidth,
                rootHeight = rootHeight,
                appDrawerColumns = userData.appDrawerSettings.appDrawerColumns,
                appDrawerRowsHeight = userData.appDrawerSettings.appDrawerRowsHeight,
                onLongPressGrid = { newCurrentPage ->
                    currentPage = newCurrentPage

                    gridItemSource = null
                },
                onLongPressedGridItem = { newCurrentPage, imageBitmap, gridItemLayoutInfo ->
                    currentPage = newCurrentPage

                    addNewPage = (gridItems[userData.homeSettings.pageCount - 1]?.size ?: 0) > 1

                    gridItemSource = GridItemSource(
                        gridItemLayoutInfo = gridItemLayoutInfo,
                        type = GridItemSource.Type.Old,
                    )

                    onUpdateOverlayIntOffset(
                        IntOffset(
                            x = gridItemLayoutInfo.x,
                            y = gridItemLayoutInfo.y,
                        ),
                    )

                    onUpdateOverlayImageBitmap(imageBitmap)
                },
                onLongPressApplicationInfo = { newCurrentPage, imageBitmap, intOffset, gridItemLayoutInfo ->
                    currentPage = newCurrentPage

                    addNewPage = (gridItems[userData.homeSettings.pageCount - 1]?.size ?: 0) > 1

                    onUpdateOverlayIntOffset(intOffset)

                    onUpdateOverlayImageBitmap(imageBitmap)

                    gridItemSource = GridItemSource(
                        gridItemLayoutInfo = gridItemLayoutInfo,
                        type = GridItemSource.Type.New,
                    )
                },
                onDraggingGridItem = {
                    onShowOverlay(true)

                    onShowGridCache(Screen.Drag)
                },
                onDraggingApplicationInfo = {
                    onShowOverlay(true)

                    onShowGridCache(Screen.Drag)
                },
                onDragEndApplicationInfo = {
                    onShowOverlay(false)
                },
                onLongPressWidget = { newCurrentPage, imageBitmap, intOffset, gridItemLayoutInfo ->
                    currentPage = newCurrentPage

                    onUpdateOverlayIntOffset(intOffset)

                    gridItemSource = GridItemSource(
                        gridItemLayoutInfo = gridItemLayoutInfo,
                        type = GridItemSource.Type.New,
                    )

                    onUpdateOverlayImageBitmap(imageBitmap)
                },
                onDragStartWidget = {
                    addNewPage = !gridItems[userData.homeSettings.pageCount - 1].isNullOrEmpty()

                    onShowOverlay(true)

                    onShowGridCache(Screen.Drag)
                },
                onStartMainActivity = onStartMainActivity,
                onEdit = {

                },
                onResize = { newTargetPage ->
                    currentPage = newTargetPage

                    onShowOverlay(false)

                    onShowGridCache(Screen.Resize)
                },
                onSettings = onSettings,
                onShowGridCache = onShowGridCache,
            )
        }

        Screen.Drag -> {
            DragScreen(
                modifier = modifier,
                currentPage = currentPage,
                rows = userData.homeSettings.rows,
                columns = userData.homeSettings.columns,
                pageCount = userData.homeSettings.pageCount,
                infiniteScroll = userData.homeSettings.infiniteScroll,
                dockRows = userData.homeSettings.dockRows,
                dockColumns = userData.homeSettings.dockColumns,
                gridItems = gridItems,
                dragIntOffset = dragIntOffset,
                gridItemSource = gridItemSource,
                drag = drag,
                rootWidth = rootWidth,
                rootHeight = rootHeight,
                dockHeight = userData.homeSettings.dockHeight,
                dockGridItems = dockGridItems,
                textColor = userData.homeSettings.textColor,
                movedGridItems = movedGridItems,
                addNewPage = addNewPage,
                onMoveGridItem = onMoveGridItem,
                onDeleteAppWidgetId = onDeleteAppWidgetId,
                onDeleteGridItem = onDeleteGridItem,
                onDragCancel = {
                    onResetGridCache(currentPage)

                    onShowOverlay(false)
                },
                onDragEnd = { newTargetPage ->
                    addNewPage = false

                    gridItemSource = null

                    onResetGridCache(newTargetPage)

                    onShowOverlay(false)
                },
            )
        }

        Screen.Resize -> {
            ResizeScreen(
                modifier = modifier,
                currentPage = currentPage,
                rows = userData.homeSettings.rows,
                columns = userData.homeSettings.columns,
                dockRows = userData.homeSettings.dockRows,
                dockColumns = userData.homeSettings.dockColumns,
                gridItems = gridItems,
                gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                dockHeight = userData.homeSettings.dockHeight,
                dockGridItems = dockGridItems,
                textColor = userData.homeSettings.textColor,
                onResizeGridItem = onResizeGridItem,
                onResizeEnd = {
                    gridItemSource = null

                    onResetGridCache(currentPage)
                },
            )
        }

        Screen.Loading -> {
            CircularProgressIndicator(modifier = modifier.align(Alignment.Center))
        }

        Screen.EditPage -> {
            EditPageScreen(
                modifier = modifier,
                currentPage = currentPage,
                rows = userData.homeSettings.rows,
                columns = userData.homeSettings.columns,
                pageCount = userData.homeSettings.pageCount,
                textColor = userData.homeSettings.textColor,
                gridItems = gridItems,
                dragIntOffset = dragIntOffset,
                drag = drag,
                rootWidth = rootWidth,
                dockHeight = userData.homeSettings.dockHeight,
                movedPages = movedPages,
                onSaveEditPage = onResetGridCache,
                onCancelEditPage = onCancelEditPage,
                onLongPress = { imageBitmap, intOffset ->
                    onUpdateOverlayIntOffset(intOffset)

                    onUpdateOverlayImageBitmap(imageBitmap)

                    onShowOverlay(true)
                },
                onMovePage = onMovePage,
                onDragEnd = {
                    onShowOverlay(false)

                    onUpdateOverlayIntOffset(IntOffset.Zero)
                },
                onResetMovedPages = onResetMovedPages,
            )
        }
    }
}