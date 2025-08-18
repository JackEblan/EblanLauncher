package com.eblan.launcher.feature.home

import android.content.ClipDescription
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.draganddrop.dragAndDropTarget
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
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
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
        onCancelGridCacheAfterMove = viewModel::cancelGridCacheAfterMove,
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
    onResetGridCacheAfterResize: (Int) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCacheAfterMove: () -> Unit,
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

    var showOverlay by remember { mutableStateOf(false) }

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
                .drawWithContent {
                    drawContent()

                    if (showOverlay) {
                        overlayImageBitmap?.let { image ->
                            drawImage(
                                image = image,
                                topLeft = overlayIntOffset.toOffset(),
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
                        gridItemsByPage = homeUiState.homeData.gridItemsByPage,
                        userData = homeUiState.homeData.userData,
                        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                        dockGridItems = homeUiState.homeData.dockGridItems,
                        pageItems = pageItems,
                        movedGridItemResult = movedGridItemResult,
                        rootWidth = constraints.maxWidth,
                        rootHeight = constraints.maxHeight,
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
                        onCancelGridCacheAfterMove = onCancelGridCacheAfterMove,
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
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    userData: UserData,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    dockGridItems: List<GridItem>,
    pageItems: List<PageItem>,
    movedGridItemResult: MoveGridItemResult?,
    rootWidth: Int,
    rootHeight: Int,
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
    onResetGridCacheAfterResize: (Int) -> Unit,
    onResetGridCacheAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onResetGridCacheAfterMoveFolder: () -> Unit,
    onCancelGridCacheAfterMove: () -> Unit,
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
    onUpdateIntOffset: (
        dragIntOffset: IntOffset,
        overlayIntOffset: IntOffset,
    ) -> Unit,
    onUpdateOverlayImageBitmap: (ImageBitmap?) -> Unit,
    onShowOverlay: (Boolean) -> Unit,
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
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
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
                    onLongPressGridItem = { newCurrentPage, newGridItemSource ->
                        targetPage = newCurrentPage

                        gridItemSource = newGridItemSource
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

                        onShowOverlay(false)
                    },
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onDragStartPinItemRequest = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        onShowGridCache(gridItems, Screen.Drag)
                    },
                    onTestLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap, newDragIntOffset, newOverlayIntOffset ->
                        targetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        onUpdateIntOffset(
                            newDragIntOffset,
                            newOverlayIntOffset,
                        )

                        onUpdateOverlayImageBitmap(imageBitmap)

                        onShowOverlay(true)
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
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    dockHeight = userData.homeSettings.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    moveGridItemResult = movedGridItemResult,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    onMoveGridItem = onMoveGridItem,
                    onDragCancel = { newTargetPage ->
                        onResetGridCacheAfterResize(newTargetPage)

                        onShowOverlay(false)
                    },
                    onDragEndAfterMove = { newTargetPage, movingGridItem, conflictingGridItem ->
                        targetPage = newTargetPage

                        onResetGridCacheAfterMove(movingGridItem, conflictingGridItem)

                        onShowOverlay(false)
                    },
                    onMoveGridItemsFailed = { newTargetPage ->
                        targetPage = newTargetPage

                        onCancelGridCacheAfterMove()

                        onShowOverlay(false)
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
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    dockHeight = userData.homeSettings.dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    onResizeGridItem = onResizeGridItem,
                    onResizeEnd = {
                        onResetGridCacheAfterResize(targetPage)
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
                    rootHeight = rootHeight,
                    pageItems = pageItems,
                    dockHeight = userData.homeSettings.dockHeight,
                    initialPage = userData.homeSettings.initialPage,
                    textColor = textColor,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
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
                    onUpdateScreen = onUpdateScreen,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetTargetPage = {
                        folderTargetPage = 0
                    },
                    onLongPressGridItem = { newTargetPage, newGridItemSource ->
                        folderTargetPage = newTargetPage

                        gridItemSource = newGridItemSource
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
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    moveGridItemResult = movedGridItemResult,
                    gridItemSettings = userData.homeSettings.gridItemSettings,
                    folderDataById = foldersDataById.last(),
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