package com.eblan.launcher.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.editpage.EditPageScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
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

    val eblanApplicationComponentUiState by viewModel.eblanApplicationComponentUiState.collectAsStateWithLifecycle()

    val movedGridItems by viewModel.movedGridItems.collectAsStateWithLifecycle()

    val targetPage by viewModel.targetPage.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        movedGridItems = movedGridItems,
        targetPage = targetPage,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onDeleteAppWidgetId = viewModel::deleteAppWidgetId,
        onDeleteGridItem = viewModel::deleteGridItem,
        onShowGridCache = viewModel::showGridCache,
        onResetGridCache = viewModel::resetGridCache,
        onEdit = onEdit,
        onSettings = onSettings,
        onEditPage = viewModel::showPageCache,
        onStartMainActivity = viewModel::startMainActivity,
        onSaveEditPage = viewModel::saveEditPage,
        onCancelEditPage = viewModel::cancelEditPage,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    movedGridItems: Boolean?,
    targetPage: Int,
    onMoveGridItem: (
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItems: List<GridItem>,
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
    onEditPage: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onSaveEditPage: (pageItems: List<PageItem>) -> Unit,
    onCancelEditPage: () -> Unit,
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
                        onDragStart = {
                            drag = Drag.Start
                        },
                        onDragEnd = {
                            drag = Drag.End
                        },
                        onDragCancel = {
                            drag = Drag.Cancel
                        },
                        onDrag = { _, dragAmount ->
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
                        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                        dockGridItems = homeUiState.gridItemsByPage.dockGridItems,
                        pageItems = homeUiState.gridItemsByPage.pageItems,
                        movedGridItems = movedGridItems,
                        targetPage = targetPage,
                        rootWidth = constraints.maxWidth,
                        rootHeight = constraints.maxHeight,
                        dragIntOffset = dragIntOffset,
                        overlayIntOffset = overlayIntOffset,
                        drag = drag,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onDeleteAppWidgetId = onDeleteAppWidgetId,
                        onDeleteGridItem = onDeleteGridItem,
                        onShowGridCache = onShowGridCache,
                        onResetGridCache = onResetGridCache,
                        onEdit = onEdit,
                        onSettings = onSettings,
                        onEditPage = onEditPage,
                        onStartMainActivity = onStartMainActivity,
                        onSaveEditPage = onSaveEditPage,
                        onCancelEditPage = onCancelEditPage,
                        onUpdateIntOffset = { newDragIntOffset, newOverlayIntOffset ->
                            dragIntOffset = newDragIntOffset

                            overlayIntOffset = newOverlayIntOffset

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
private fun Success(
    modifier: Modifier = Modifier,
    screen: Screen,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    dockGridItems: List<GridItem>,
    pageItems: List<PageItem>,
    movedGridItems: Boolean?,
    targetPage: Int,
    rootWidth: Int,
    rootHeight: Int,
    dragIntOffset: IntOffset,
    overlayIntOffset: IntOffset,
    drag: Drag,
    onMoveGridItem: (
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        gridItems: List<GridItem>,
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
    onEditPage: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onSaveEditPage: (pageItems: List<PageItem>) -> Unit,
    onCancelEditPage: () -> Unit,
    onUpdateIntOffset: (
        dragIntOffset: IntOffset,
        overlayIntOffset: IntOffset,
    ) -> Unit,
    onUpdateOverlayImageBitmap: (ImageBitmap?) -> Unit,
    onShowOverlay: (Boolean) -> Unit,
) {
    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var currentPage by remember { mutableIntStateOf(0) }

    var addNewPage by remember { mutableStateOf(false) }

    AnimatedContent(
        modifier = modifier,
        targetState = screen,
    ) { targetState ->
        when (targetState) {
            Screen.Pager -> {
                PagerScreen(
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
                    dragIntOffset = dragIntOffset,
                    dockGridItems = dockGridItems,
                    textColor = userData.homeSettings.textColor,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    appDrawerColumns = userData.appDrawerSettings.appDrawerColumns,
                    appDrawerRowsHeight = userData.appDrawerSettings.appDrawerRowsHeight,
                    overlayIntOffset = overlayIntOffset,
                    onLongPressGrid = { newCurrentPage, intOffset ->
                        currentPage = newCurrentPage

                        onUpdateIntOffset(intOffset, IntOffset.Zero)

                        gridItemSource = null
                    },
                    onLongPressedGridItem = { newCurrentPage, imageBitmap, gridItemLayoutInfo, intOffset ->
                        currentPage = newCurrentPage

                        addNewPage = (gridItems[userData.homeSettings.pageCount - 1]?.size ?: 0) > 1

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.Old,
                            imageBitmap = imageBitmap,
                        )

                        onUpdateIntOffset(
                            intOffset,
                            IntOffset(
                                x = gridItemLayoutInfo.x,
                                y = gridItemLayoutInfo.y,
                            ),
                        )

                        onUpdateOverlayImageBitmap(imageBitmap)
                    },
                    onLongPressApplicationInfo = { newCurrentPage, imageBitmap, gridItemLayoutInfo, newDragIntOffset, newOverlayIntOffset ->
                        currentPage = newCurrentPage

                        addNewPage = (gridItems[userData.homeSettings.pageCount - 1]?.size ?: 0) > 1

                        onUpdateIntOffset(
                            newDragIntOffset,
                            newOverlayIntOffset,
                        )

                        onUpdateOverlayImageBitmap(imageBitmap)

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                            imageBitmap = imageBitmap,
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
                    onLongPressWidget = { newCurrentPage, imageBitmap, gridItemLayoutInfo, newDragIntOffset, newOverlayIntOffset ->
                        currentPage = newCurrentPage

                        addNewPage = !gridItems[userData.homeSettings.pageCount - 1].isNullOrEmpty()

                        onUpdateIntOffset(
                            newDragIntOffset,
                            newOverlayIntOffset,
                        )

                        onUpdateOverlayImageBitmap(imageBitmap)

                        gridItemSource = GridItemSource(
                            gridItemLayoutInfo = gridItemLayoutInfo,
                            type = GridItemSource.Type.New,
                            imageBitmap = imageBitmap,
                        )
                    },
                    onDragStartWidget = {
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
                    onEditPage = onEditPage,
                )
            }

            Screen.Drag -> {
                DragScreen(
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

                        onResetGridCache(newTargetPage)

                        onShowOverlay(false)
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    dockRows = userData.homeSettings.dockRows,
                    dockColumns = userData.homeSettings.dockColumns,
                    gridItems = gridItems[currentPage],
                    gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
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
                LoadingScreen()
            }

            Screen.EditPage -> {
                EditPageScreen(
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    pageItems = pageItems,
                    dockHeight = userData.homeSettings.dockHeight,
                    onSaveEditPage = onSaveEditPage,
                    onCancelEditPage = onCancelEditPage,
                )
            }
        }
    }
}