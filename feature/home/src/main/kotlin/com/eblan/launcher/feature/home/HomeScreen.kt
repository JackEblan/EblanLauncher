package com.eblan.launcher.feature.home

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemMovement
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.HomeType
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val gridItemMovement by viewModel.gridItemMovement.collectAsStateWithLifecycle()

    val gridItemIdByCoordinates by viewModel.gridItemIdByCoordinates.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val addGridItem by viewModel.addGridItem.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemMovement = gridItemMovement,
        homeUiState = homeUiState,
        gridItemIdByCoordinates = gridItemIdByCoordinates,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        addGridItem = addGridItem,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onResizeWidgetGridItem = viewModel::resizeWidgetGridItem,
        onAddApplicationInfoGridItem = viewModel::addApplicationInfoGridItem,
        onAddAppWidgetProviderInfoGridItem = viewModel::addAppWidgetProviderInfoGridItem,
        onGridItemByCoordinates = viewModel::getGridItemByCoordinates,
        onUpdateWidget = viewModel::updateWidget,
        onResetGridItemIdByCoordinates = viewModel::resetGridItemIdByCoordinates,
        onResetAddGridItem = viewModel::resetAddGridItem,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemMovement: GridItemMovement?,
    homeUiState: HomeUiState,
    gridItemIdByCoordinates: String?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    addGridItem: GridItem?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        width: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        page: Int,
        gridItem: GridItem,
        widthPixel: Int,
        heightPixel: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onAddApplicationInfoGridItem: (
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
    onAddAppWidgetProviderInfoGridItem: (
        page: Int,
        componentName: String,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdateWidget: (id: String, appWidgetId: Int) -> Unit,
    onResetGridItemIdByCoordinates: () -> Unit,
    onResetAddGridItem: () -> Unit,
    onEdit: (String) -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
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
                        gridItems = homeUiState.gridItemsByPage.gridItems,
                        userData = homeUiState.gridItemsByPage.userData,
                        gridItemMovement = gridItemMovement,
                        gridItemIdByCoordinates = gridItemIdByCoordinates,
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        addGridItem = addGridItem,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onResizeWidgetGridItem = onResizeWidgetGridItem,
                        onAddApplicationInfoGridItem = onAddApplicationInfoGridItem,
                        onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                        onGetGridItemByCoordinates = onGridItemByCoordinates,
                        onUpdateWidget = onUpdateWidget,
                        onResetGridItemIdByCoordinates = onResetGridItemIdByCoordinates,
                        onResetAddGridItem = onResetAddGridItem,
                        onEdit = onEdit,
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
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    gridItemMovement: GridItemMovement?,
    gridItemIdByCoordinates: String?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    addGridItem: GridItem?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        width: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        page: Int,
        gridItem: GridItem,
        widthPixel: Int,
        heightPixel: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onAddApplicationInfoGridItem: (
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
    onAddAppWidgetProviderInfoGridItem: (
        page: Int,
        componentName: String,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onGetGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdateWidget: (id: String, appWidgetId: Int) -> Unit,
    onResetGridItemIdByCoordinates: () -> Unit,
    onResetAddGridItem: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val pagerState = rememberPagerState(
        pageCount = {
            userData.pageCount
        },
    )

    val sheetState = rememberModalBottomSheetState()

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var showOverlay by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var homeType by remember { mutableStateOf(HomeType.Pager) }

    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    var appWidgetId by remember { mutableStateOf<Int?>(null) }

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        appWidgetId = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        } else {
            null
        }
    }

    LaunchedEffect(key1 = gridItemMovement) {
        when (gridItemMovement) {
            GridItemMovement.Left -> {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }

            GridItemMovement.Right -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            GridItemMovement.Inside, null -> Unit
        }
    }

    LaunchedEffect(key1 = addGridItem) {
        if (addGridItem != null) {
            snapshotFlow { dragOffset }.onStart {
                homeType = HomeType.Pager
                showOverlay = true
                showMenu = true
            }.onEach { offset ->
                onMoveGridItem(
                    pagerState.currentPage,
                    addGridItem,
                    offset.x.roundToInt(),
                    offset.y.roundToInt(),
                    overlaySize.width,
                    screenSize.width,
                    screenSize.height,
                )
            }.collect()
        }
    }

    LaunchedEffect(key1 = addGridItem, key2 = showOverlay, key3 = gridItemMovement) {
        if (addGridItem != null && showOverlay.not() && gridItemMovement != null) {
            when (val data = addGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    onResetAddGridItem()
                }

                is GridItemData.Widget -> {
                    val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

                    val provider = ComponentName.unflattenFromString(data.componentName)

                    if (appWidgetManager.bindAppWidgetIdIfAllowed(
                            appWidgetId = allocateAppWidgetId,
                            provider = provider,
                        )
                    ) {
                        //Widget created already
                    } else {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                            putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                allocateAppWidgetId,
                            )
                            putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                                provider,
                            )
                        }

                        appWidgetLauncher.launch(intent)
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = addGridItem, key2 = appWidgetId) {
        if (addGridItem != null && appWidgetId != null) {
            appWidgetId?.let { id ->
                onUpdateWidget(addGridItem.id, id)
            }

            appWidgetId = null

            onResetAddGridItem()
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        showBottomSheet = true
                    },
                    onDragEnd = {
                        showOverlay = false
                        showResize = false
                    },
                    onDragCancel = {
                        showOverlay = false
                        showResize = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount

                        showMenu = false
                        showResize = false
                    },
                )
            }
            .fillMaxSize()
            .onSizeChanged { intSize ->
                screenSize = intSize
            },
    ) {
        when (homeType) {
            HomeType.Pager -> {
                PagerScreen(
                    pagerState = pagerState,
                    overlaySize = overlaySize,
                    screenSize = screenSize,
                    dragOffset = dragOffset,
                    rows = userData.rows,
                    columns = userData.columns,
                    gridItemIdByCoordinates = gridItemIdByCoordinates,
                    gridItems = gridItems,
                    showOverlay = showOverlay,
                    showMenu = showMenu,
                    showResize = showResize,
                    onResizeGridItem = onResizeGridItem,
                    onResizeWidgetGridItem = onResizeWidgetGridItem,
                    onDismissRequest = {
                        showMenu = false
                    },
                    onResizeEnd = {
                        showResize = false
                    },
                    onMoveGridItem = onMoveGridItem,
                    onGetGridItemByCoordinates = onGetGridItemByCoordinates,
                    onLongPressGridItem = { offset, size ->
                        dragOffset = offset
                        overlaySize = size
                        showOverlay = true
                        showMenu = true
                    },
                    onEdit = {

                    },
                    onResize = {
                        showOverlay = false
                        showResize = true
                    },
                )
            }

            HomeType.Application -> {
                ApplicationScreen(
                    pagerState = pagerState,
                    screenSize = screenSize,
                    eblanApplicationInfos = eblanApplicationInfos,
                    onLongPressApplicationInfo = { offset, size ->
                        dragOffset = offset
                        overlaySize = size
                        onResetGridItemIdByCoordinates()
                        showBottomSheet = false
                    },
                    onAddApplicationInfoGridItem = onAddApplicationInfoGridItem,
                )
            }

            HomeType.Widget -> {
                WidgetScreen(
                    pagerState = pagerState,
                    rows = userData.rows,
                    columns = userData.columns,
                    screenSize = screenSize,
                    appWidgetProviderInfos = appWidgetProviderInfos,
                    onLongPressAppWidgetProviderInfo = { offset, size ->
                        dragOffset = offset
                        overlaySize = size
                        onResetGridItemIdByCoordinates()
                        showBottomSheet = false
                    },
                    onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                )
            }
        }


        if (showOverlay) {
            GridItemOverlay(overlaySize = overlaySize, dragOffset = dragOffset)
        }

        if (gridItemIdByCoordinates == null && showBottomSheet) {
            HomeBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    onResetGridItemIdByCoordinates()
                    showBottomSheet = false
                },
                onHomeType = { type ->
                    homeType = type
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onHomeType: (HomeType) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Row {
            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onHomeType(HomeType.Application)

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null)

                Text(text = "Application")
            }

            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onHomeType(HomeType.Widget)

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                Text(text = "Widgets")
            }
        }
    }
}


@Composable
private fun GridItemOverlay(
    modifier: Modifier = Modifier,
    overlaySize: IntSize,
    dragOffset: Offset,
) {
    val density = LocalDensity.current

    val boundingBoxWidthDp = with(density) {
        overlaySize.width.toDp()
    }

    val boundingBoxHeightDp = with(density) {
        overlaySize.height.toDp()
    }

    val widthDp by remember { mutableStateOf(boundingBoxWidthDp) }

    val heightDp by remember { mutableStateOf(boundingBoxHeightDp) }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = dragOffset.x.roundToInt(),
                    y = dragOffset.y.roundToInt(),
                )
            }
            .size(width = widthDp, height = heightDp)
            .background(Color.Green),
    ) {
        Text(text = "Drag")
    }
}
