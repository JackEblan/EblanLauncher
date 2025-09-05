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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
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

    val eblanApplicationInfosByLabel by viewModel.eblanApplicationInfosByLabel.collectAsStateWithLifecycle()

    val eblanAppWidgetProviderInfosByLabel by viewModel.eblanAppWidgetProviderInfosByLabel.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        homeUiState = homeUiState,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        movedGridItemResult = movedGridItemResult,
        pageItems = pageItems,
        foldersDataById = folders,
        eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
        eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
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
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
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
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
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
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
) {
    var dragIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var overlayImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var drag by remember { mutableStateOf(Drag.None) }

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

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
            }

            override fun onMoved(event: DragAndDropEvent) {
                val offset = with(event.toAndroidDragEvent()) {
                    IntOffset(x = x.roundToInt(), y = y.roundToInt())
                }

                drag = Drag.Dragging

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
                Success(
                    screen = screen,
                    homeData = homeUiState.homeData,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    pageItems = pageItems,
                    movedGridItemResult = movedGridItemResult,
                    screenWidth = constraints.maxWidth,
                    screenHeight = constraints.maxHeight,
                    paddingValues = paddingValues,
                    dragIntOffset = dragIntOffset,
                    drag = drag,
                    foldersDataById = foldersDataById,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
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
                    onUpdateGridItemImageBitmap = { imageBitmap ->
                        overlayImageBitmap = imageBitmap
                    },
                    onUpdateGridItemOffset = { intOffset ->
                        overlayIntOffset = intOffset
                    },
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
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
    homeData: HomeData,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    pageItems: List<PageItem>,
    movedGridItemResult: MoveGridItemResult?,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    dragIntOffset: IntOffset,
    drag: Drag,
    foldersDataById: ArrayDeque<FolderDataById>,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
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
    onUpdateGridItemImageBitmap: (ImageBitmap?) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
) {
    var gridItemSource by remember { mutableStateOf<GridItemSource?>(null) }

    var targetPage by remember {
        mutableIntStateOf(
            homeData.userData.homeSettings.initialPage,
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
                    gridItems = homeData.gridItems,
                    gridItemsByPage = homeData.gridItemsByPage,
                    drag = drag,
                    dockGridItems = homeData.dockGridItems,
                    textColor = homeData.textColor,
                    eblanApplicationComponentUiState = eblanApplicationComponentUiState,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    appDrawerSettings = homeData.userData.appDrawerSettings,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    gestureSettings = homeData.userData.gestureSettings,
                    gridItemSource = gridItemSource,
                    homeSettings = homeData.userData.homeSettings,
                    eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                    eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
                    onLongPressGrid = { newCurrentPage ->
                        targetPage = newCurrentPage
                    },
                    onTapFolderGridItem = { newCurrentPage, id ->
                        targetPage = newCurrentPage

                        onShowFolder(id)
                    },
                    onDraggingGridItem = {
                        onShowGridCache(homeData.gridItems, Screen.Drag)
                    },
                    onEdit = onEdit,
                    onResize = { newTargetPage ->
                        targetPage = newTargetPage

                        onShowGridCache(homeData.gridItems, Screen.Resize)
                    },
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onDragStartPinItemRequest = { newGridItemSource ->
                        gridItemSource = newGridItemSource

                        onShowGridCache(homeData.gridItems, Screen.Drag)
                    },
                    onLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap ->
                        targetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                )
            }

            Screen.Drag -> {
                DragScreen(
                    startCurrentPage = targetPage,
                    gridItemsByPage = homeData.gridItemsByPage,
                    dragIntOffset = dragIntOffset,
                    gridItemSource = gridItemSource,
                    drag = drag,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    dockGridItems = homeData.dockGridItems,
                    textColor = homeData.textColor,
                    moveGridItemResult = movedGridItemResult,
                    homeSettings = homeData.userData.homeSettings,
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
                    gridItems = homeData.gridItemsByPage[targetPage],
                    gridItem = gridItemSource?.gridItem,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    dockGridItems = homeData.dockGridItems,
                    textColor = homeData.textColor,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
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
                    screenHeight = screenHeight,
                    pageItems = pageItems,
                    textColor = homeData.textColor,
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                )
            }

            Screen.Folder -> {
                FolderScreen(
                    startCurrentPage = folderTargetPage,
                    foldersDataById = foldersDataById,
                    drag = drag,
                    gridItemSource = gridItemSource,
                    paddingValues = paddingValues,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = homeData.textColor,
                    homeSettings = homeData.userData.homeSettings,
                    onUpdateScreen = onUpdateScreen,
                    onRemoveLastFolder = onRemoveLastFolder,
                    onAddFolder = onAddFolder,
                    onResetTargetPage = {
                        folderTargetPage = 0
                    },
                    onLongPressGridItem = { newCurrentPage, newGridItemSource, imageBitmap ->
                        folderTargetPage = newCurrentPage

                        gridItemSource = newGridItemSource

                        onUpdateGridItemImageBitmap(imageBitmap)
                    },
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onDraggingGridItem = { folderGridItems ->
                        onShowGridCache(folderGridItems, Screen.FolderDrag)
                    },
                )
            }

            Screen.FolderDrag -> {
                FolderDragScreen(
                    startCurrentPage = folderTargetPage,
                    gridItemsByPage = homeData.gridItemsByPage,
                    gridItemSource = gridItemSource,
                    textColor = homeData.textColor,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    folderDataById = foldersDataById.last(),
                    paddingValues = paddingValues,
                    homeSettings = homeData.userData.homeSettings,
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