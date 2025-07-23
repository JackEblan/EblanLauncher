package com.eblan.launcher.feature.home

import android.content.ClipDescription
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.draganddrop.dragAndDropTarget
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
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

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

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        movedGridItems = movedGridItems,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onDeleteGridItem = viewModel::deleteGridItem,
        onShowGridCache = viewModel::showGridCache,
        onResetGridCache = viewModel::resetGridCache,
        onEdit = onEdit,
        onSettings = onSettings,
        onEditPage = viewModel::showPageCache,
        onSaveEditPage = viewModel::saveEditPage,
        onCancelEditPage = viewModel::cancelEditPage,
        onUpdatePinWidget = viewModel::updatePinWidget,
        onDeleteWidgetGridItem = viewModel::deleteWidgetGridItem,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    movedGridItems: Boolean,
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
    onDeleteGridItem: (GridItem) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onResetGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onSaveEditPage: (
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onCancelEditPage: () -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var drag by remember { mutableStateOf(Drag.None) }

    var dragStartOffset by remember { mutableStateOf(IntOffset.Zero) }

    val target = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                val offset = with(event.toAndroidDragEvent()) {
                    IntOffset(x = x.roundToInt(), y = y.roundToInt())
                }

                drag = Drag.Start

                dragIntOffset = offset
            }

            override fun onEnded(event: DragAndDropEvent) {
                drag = Drag.End

                dragStartOffset = IntOffset.Zero
            }

            override fun onMoved(event: DragAndDropEvent) {
                val offset = with(event.toAndroidDragEvent()) {
                    IntOffset(x = x.roundToInt(), y = y.roundToInt())
                }

                if (dragStartOffset == IntOffset.Zero) {
                    dragStartOffset = offset
                    return
                }

                val dx = (offset.x - dragStartOffset.x).absoluteValue
                val dy = (offset.y - dragStartOffset.y).absoluteValue

                if (dx > 100f || dy > 100f) {
                    drag = Drag.Dragging
                }

                dragIntOffset = offset
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                return true
            }
        }
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        BoxWithConstraints(
            modifier = modifier
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    },
                    target = target,
                )
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
                        gridItems = homeUiState.homeData.gridItems,
                        userData = homeUiState.homeData.userData,
                        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                        dockGridItems = homeUiState.homeData.dockGridItems,
                        pageItems = homeUiState.homeData.pageItems,
                        movedGridItems = movedGridItems,
                        rootWidth = constraints.maxWidth,
                        rootHeight = constraints.maxHeight,
                        dragIntOffset = dragIntOffset,
                        drag = drag,
                        hasShortcutHostPermission = homeUiState.homeData.hasShortcutHostPermission,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onDeleteGridItem = onDeleteGridItem,
                        onShowGridCache = onShowGridCache,
                        onResetGridCache = onResetGridCache,
                        onEdit = onEdit,
                        onSettings = onSettings,
                        onEditPage = onEditPage,
                        onSaveEditPage = onSaveEditPage,
                        onCancelEditPage = onCancelEditPage,
                        onUpdatePinWidget = onUpdatePinWidget,
                        onDeleteWidgetGridItem = onDeleteWidgetGridItem,
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
    movedGridItems: Boolean,
    rootWidth: Int,
    rootHeight: Int,
    dragIntOffset: IntOffset,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
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
    onDeleteGridItem: (GridItem) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onResetGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onSaveEditPage: (
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onCancelEditPage: () -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var targetPage by remember(key1 = userData.homeSettings.initialPage) {
        mutableIntStateOf(
            userData.homeSettings.initialPage,
        )
    }

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
                    gridItem = gridItemSource?.gridItem,
                    gridItems = gridItems,
                    dockHeight = userData.homeSettings.dockHeight,
                    drag = drag,
                    dockGridItems = dockGridItems,
                    textColor = userData.homeSettings.textColor,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    appDrawerColumns = userData.appDrawerSettings.appDrawerColumns,
                    appDrawerRowsHeight = userData.appDrawerSettings.appDrawerRowsHeight,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    initialPage = userData.homeSettings.initialPage,
                    onLongPressGrid = { newCurrentPage ->
                        targetPage = newCurrentPage
                    },
                    onLongPressGridItem = { newCurrentPage, newGridItemSource ->
                        targetPage = newCurrentPage

                        gridItemSource = newGridItemSource
                    },
                    onDraggingGridItem = {
                        onShowGridCache(Screen.Drag)
                    },
                    onEdit = {

                    },
                    onResize = { newTargetPage ->
                        targetPage = newTargetPage

                        onShowGridCache(Screen.Resize)
                    },
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onDragStartPinItemRequest = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Drag -> {
                DragScreen(
                    currentPage = targetPage,
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
                    onMoveGridItem = onMoveGridItem,
                    onDeleteGridItem = onDeleteGridItem,
                    onDragCancel = onResetGridCache,
                    onDragEnd = { newTargetPage ->
                        targetPage = newTargetPage

                        onResetGridCache()
                    },
                    onUpdatePinWidget = onUpdatePinWidget,
                    onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    dockRows = userData.homeSettings.dockRows,
                    dockColumns = userData.homeSettings.dockColumns,
                    gridItems = gridItems[targetPage],
                    gridItem = gridItemSource?.gridItem,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    dockHeight = userData.homeSettings.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = userData.homeSettings.textColor,
                    onResizeGridItem = onResizeGridItem,
                    onResizeEnd = onResetGridCache,
                )
            }

            Screen.Loading -> {
                LoadingScreen()
            }

            Screen.EditPage -> {
                EditPageScreen(
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    rootHeight = rootHeight,
                    pageItems = pageItems,
                    dockHeight = userData.homeSettings.dockHeight,
                    initialPage = userData.homeSettings.initialPage,
                    textColor = userData.homeSettings.textColor,
                    onSaveEditPage = onSaveEditPage,
                    onCancelEditPage = onCancelEditPage,
                )
            }
        }
    }
}