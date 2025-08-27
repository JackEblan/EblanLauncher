package com.eblan.launcher.feature.home

import android.content.ClipDescription
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.editpage.EditPageScreen
import com.eblan.launcher.feature.home.screen.folder.FolderScreen
import com.eblan.launcher.feature.home.screen.folderdrag.FolderDragScreen
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

    val movedGridItemResult by viewModel.movedGridItemResult.collectAsStateWithLifecycle()

    val pageItems by viewModel.pageItems.collectAsStateWithLifecycle()

    val folders by viewModel.foldersDataById.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        movedGridItemResult = movedGridItemResult,
        pageItems = pageItems,
        foldersDataById = folders,
        onMoveGridItem = viewModel::moveGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onShowGridCache = viewModel::showGridCache,
        onResetGridCacheAfterResize = viewModel::resetGridCacheAfterResize,
        onResetGridCacheAfterMove = viewModel::resetGridCacheAfterMove,
        onResetGridCacheAfterMoveFolder = viewModel::resetGridCacheAfterMoveFolder,
        onCancelGridCache = viewModel::cancelGridCache,
        onEdit = onEdit,
        onSettings = onSettings,
        onEditPage = viewModel::showPageCache,
        onSaveEditPage = viewModel::saveEditPage,
        onUpdateScreen = viewModel::updateScreen,
        onDeleteGridItemCache = viewModel::deleteGridItemCache,
        onUpdateGridItemDataCache = viewModel::updateGridItemDataCache,
        onDeleteWidgetGridItemCache = viewModel::deleteWidgetGridItemCache,
        onShowFolder = viewModel::showFolder,
        onRemoveLastFolder = viewModel::removeLastFolder,
        onAddFolder = viewModel::addFolder,
        onMoveOutsideFolder = viewModel::moveGridItemOutsideFolder,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    homeUiState: HomeUiState,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    movedGridItemResult: MoveGridItemResult?,
    pageItems: List<PageItem>,
    foldersDataById: ArrayDeque<FolderDataById>,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onMoveFolderGridItem: (
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
    onShowGridCache: (
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onSaveEditPage: (
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onShowFolder: (String) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onMoveOutsideFolder: () -> Unit,
) {
    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    var dragStartOffset by remember { mutableStateOf(IntOffset.Zero) }

    val density = LocalDensity.current

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

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

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        drag = Drag.Start

                        dragIntOffset = offset.round()
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
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = target,
            )
            .fillMaxSize(),
    ) {
        when (homeUiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is HomeUiState.Success -> {
                val gridWidth = constraints.maxWidth - horizontalPadding

                val gridHeight = constraints.maxHeight - verticalPadding

                Success(
                    screen = screen,
                    gridItems = homeUiState.homeData.gridItems,
                    gridItemsByPage = homeUiState.homeData.gridItemsByPage,
                    userData = homeUiState.homeData.userData,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    dockGridItems = homeUiState.homeData.dockGridItems,
                    pageItems = pageItems,
                    movedGridItemResult = movedGridItemResult,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    paddingValues = paddingValues,
                    dragIntOffset = dragIntOffset,
                    drag = drag,
                    hasShortcutHostPermission = homeUiState.homeData.hasShortcutHostPermission,
                    textColor = homeUiState.homeData.textColor,
                    foldersDataById = foldersDataById,
                    onMoveGridItem = onMoveGridItem,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onResizeGridItem = onResizeGridItem,
                    onShowGridCache = onShowGridCache,
                    onResetGridCacheAfterResize = onResetGridCacheAfterResize,
                    onResetGridCacheAfterMove = onResetGridCacheAfterMove,
                    onCancelGridCache = onCancelGridCache,
                    onEdit = onEdit,
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
                    onShowFolder = onShowFolder,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetGridCacheAfterMoveFolder = onResetGridCacheAfterMoveFolder,
                    onMoveOutsideFolder = onMoveOutsideFolder,
                    onUpdateGridItemOverlay = { intOffset, imageBitmap ->
                        overlayIntOffset = intOffset

                        overlayImageBitmap = imageBitmap
                    },
                )
            }
        }

        OverlayImage(
            drag = drag,
            overlayIntOffset = overlayIntOffset,
            overlayImageBitmap = overlayImageBitmap,
            onUpdateOverlay = {
                overlayImageBitmap = null
            },
        )
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    screen: Screen,
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    userData: UserData,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    dockGridItems: List<GridItem>,
    pageItems: List<PageItem>,
    movedGridItemResult: MoveGridItemResult?,
    gridWidth: Int,
    gridHeight: Int,
    paddingValues: PaddingValues,
    dragIntOffset: IntOffset,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    textColor: Long,
    foldersDataById: ArrayDeque<FolderDataById>,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onMoveFolderGridItem: (
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
    onShowGridCache: (
        gridItems: List<GridItem>,
        screen: Screen,
    ) -> Unit,
    onResetGridCacheAfterResize: (GridItem) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCache: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onEditPage: (List<GridItem>) -> Unit,
    onSaveEditPage: (
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onShowFolder: (String) -> Unit,
    onRemoveLastFolder: () -> Unit,
    onAddFolder: (String) -> Unit,
    onMoveOutsideFolder: () -> Unit,
    onUpdateGridItemOverlay: (
        intOffset: IntOffset,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
) {
    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var targetPage by remember {
        mutableIntStateOf(
            userData.homeSettings.initialPage,
        )
    }

    var folderTargetPage by remember { mutableIntStateOf(0) }

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
                    gridItems = gridItems,
                    gridItemsByPage = gridItemsByPage,
                    dockHeight = userData.homeSettings.dockHeight,
                    drag = drag,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    paddingValues = paddingValues,
                    appDrawerColumns = userData.appDrawerSettings.appDrawerColumns,
                    appDrawerRowsHeight = userData.appDrawerSettings.appDrawerRowsHeight,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    gestureSettings = userData.gestureSettings,
                    wallpaperScroll = userData.homeSettings.wallpaperScroll,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    gridItemSource = gridItemSource,
                    onLongPressGrid = { newCurrentPage ->
                        targetPage = newCurrentPage
                    },
                    onTapFolderGridItem = { newCurrentPage, id ->
                        targetPage = newCurrentPage

                        onShowFolder(id)
                    },
                    onDraggingGridItem = {
                        onShowGridCache(gridItems, Screen.Drag)
                    },
                    onEdit = onEdit,
                    onResize = { newTargetPage ->
                        targetPage = newTargetPage

                        onShowGridCache(gridItems, Screen.Resize)
                    },
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onDragStartPinItemRequest = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        onShowGridCache(gridItems, Screen.Drag)
                    },
                    onLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap, intOffset ->
                        targetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        onUpdateGridItemOverlay(intOffset, imageBitmap)
                    },
                )
            }

            Screen.Drag -> {
                DragScreen(
                    startCurrentPage = targetPage,
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    pageCount = userData.homeSettings.pageCount,
                    infiniteScroll = userData.homeSettings.infiniteScroll,
                    dockRows = userData.homeSettings.dockRows,
                    dockColumns = userData.homeSettings.dockColumns,
                    gridItemsByPage = gridItemsByPage,
                    dragIntOffset = dragIntOffset,
                    gridItemSource = gridItemSource,
                    drag = drag,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    dockHeight = userData.homeSettings.dockHeight,
                    paddingValues = paddingValues,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    moveGridItemResult = movedGridItemResult,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    wallpaperScroll = userData.homeSettings.wallpaperScroll,
                    onMoveGridItem = onMoveGridItem,
                    onDragEndAfterMove = { newTargetPage, movingGridItem, conflictingGridItem ->
                        targetPage = newTargetPage

                        onResetGridCacheAfterMove(movingGridItem, conflictingGridItem)
                    },
                    onMoveGridItemsFailed = { newTargetPage ->
                        targetPage = newTargetPage

                        onCancelGridCache()
                    },
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onDeleteWidgetGridItemCache = { newTargetPage, gridItem, appWidgetId ->
                        targetPage = newTargetPage

                        onDeleteWidgetGridItemCache(gridItem, appWidgetId)
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    dockRows = userData.homeSettings.dockRows,
                    dockColumns = userData.homeSettings.dockColumns,
                    gridItems = gridItemsByPage[targetPage],
                    gridItem = gridItemSource?.gridItem,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    dockHeight = userData.homeSettings.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    paddingValues = paddingValues,
                    onResizeGridItem = onResizeGridItem,
                    onResizeEnd = onResetGridCacheAfterResize,
                    onResizeCancel = onCancelGridCache,
                )
            }

            Screen.Loading -> {
                LoadingScreen()
            }

            Screen.EditPage -> {
                EditPageScreen(
                    rows = userData.homeSettings.rows,
                    columns = userData.homeSettings.columns,
                    gridHeight = gridHeight,
                    pageItems = pageItems,
                    dockHeight = userData.homeSettings.dockHeight,
                    initialPage = userData.homeSettings.initialPage,
                    textColor = textColor,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    paddingValues = paddingValues,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                )
            }

            Screen.Folder -> {
                FolderScreen(
                    startCurrentPage = folderTargetPage,
                    folderRows = userData.homeSettings.folderRows,
                    folderColumns = userData.homeSettings.folderColumns,
                    foldersDataById = foldersDataById,
                    textColor = textColor,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    drag = drag,
                    gridItemSource = gridItemSource,
                    paddingValues = paddingValues,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    onUpdateScreen = onUpdateScreen,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetTargetPage = {
                        folderTargetPage = 0
                    },
                    onLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap, intOffset ->
                        folderTargetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        gridItemSource = newGridItemSource

                        onUpdateGridItemOverlay(intOffset, imageBitmap)
                    },
                    onDraggingGridItem = { folderGridItems ->
                        onShowGridCache(folderGridItems, Screen.FolderDrag)
                    },
                )
            }

            Screen.FolderDrag -> {
                FolderDragScreen(
                    startCurrentPage = folderTargetPage,
                    folderRows = userData.homeSettings.folderRows,
                    folderColumns = userData.homeSettings.folderColumns,
                    gridItemsByPage = gridItemsByPage,
                    gridItemSource = gridItemSource,
                    textColor = textColor,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    folderDataById = foldersDataById.last(),
                    paddingValues = paddingValues,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onDragEnd = { newTargetPage ->
                        folderTargetPage = newTargetPage

                        onResetGridCacheAfterMoveFolder()
                    },
                    onMoveOutsideFolder = { newGridItemSource ->
                        folderTargetPage = 0

                        gridItemSource = newGridItemSource

                        onMoveOutsideFolder()
                    },
                )
            }
        }
    }
}

@Composable
private fun OverlayImage(
    modifier: Modifier = Modifier,
    drag: Drag,
    overlayIntOffset: IntOffset,
    overlayImageBitmap: ImageBitmap?,
    onUpdateOverlay: () -> Unit,
) {
    if (overlayImageBitmap != null) {
        when (drag) {
            Drag.Dragging -> {
                Image(
                    modifier = modifier
                        .offset {
                            overlayIntOffset
                        }
                        .alpha(0.5f),
                    bitmap = overlayImageBitmap,
                    contentDescription = null,
                )
            }

            Drag.End, Drag.Cancel -> {
                onUpdateOverlay()
            }

            else -> Unit
        }
    }
}