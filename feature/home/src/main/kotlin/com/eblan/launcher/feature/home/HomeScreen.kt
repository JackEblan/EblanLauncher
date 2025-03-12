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
import androidx.compose.runtime.rememberUpdatedState
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
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.domain.usecase.AddGridItemResult
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
    onEdit: (Int) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val gridItemBoundary by viewModel.gridItemBoundary.collectAsStateWithLifecycle()

    val gridItemByCoordinates by viewModel.gridItemByCoordinates.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val addGridItemResult by viewModel.addGridItemResult.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        gridItemBoundary = gridItemBoundary,
        homeUiState = homeUiState,
        gridItemByCoordinates = gridItemByCoordinates,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        addGridItemResult = addGridItemResult,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onAddApplicationInfoGridItem = viewModel::addApplicationInfoGridItem,
        onAddAppWidgetProviderInfoGridItem = viewModel::addAppWidgetProviderInfoGridItem,
        onGridItemByCoordinates = viewModel::getGridItemByCoordinates,
        onResetGridItemByCoordinates = viewModel::resetGridItemByCoordinates,
        onResetOverlay = viewModel::resetOverlay,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    gridItemBoundary: GridItemBoundary?,
    homeUiState: HomeUiState,
    gridItemByCoordinates: Boolean?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    addGridItemResult: AddGridItemResult?,
    onMoveGridItem: (
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        width: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
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
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
    onGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResetGridItemByCoordinates: () -> Unit,
    onResetOverlay: () -> Unit,
    onEdit: (Int) -> Unit,
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
                        gridItemBoundary = gridItemBoundary,
                        gridItemByCoordinates = gridItemByCoordinates,
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        addGridItemResult = addGridItemResult,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onAddApplicationInfoGridItem = onAddApplicationInfoGridItem,
                        onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                        onGetGridItemByCoordinates = onGridItemByCoordinates,
                        onResetGridItemByCoordinates = onResetGridItemByCoordinates,
                        onResetOverlay = onResetOverlay,
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
    gridItemBoundary: GridItemBoundary?,
    gridItemByCoordinates: Boolean?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    addGridItemResult: AddGridItemResult?,
    onMoveGridItem: (
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        width: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResizeGridItem: (
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
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
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) -> Unit,
    onGetGridItemByCoordinates: (
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onResetGridItemByCoordinates: () -> Unit,
    onResetOverlay: () -> Unit,
    onEdit: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        pageCount = {
            userData.pageCount
        },
    )

    val sheetState = rememberModalBottomSheetState()

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showOverlay by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    var showResize by remember { mutableStateOf(false) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var homeType by remember { mutableStateOf(HomeType.Pager) }

    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    val currentAddGridItemResult by rememberUpdatedState(addGridItemResult)

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("Widget created")
        } else {
            println("Widget cancelled")
            //Delete the Grid item in the database
        }
    }

    LaunchedEffect(key1 = gridItemBoundary) {
        when (gridItemBoundary) {
            GridItemBoundary.Left -> {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }

            GridItemBoundary.Right -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = addGridItemResult) {
        when (addGridItemResult) {
            AddGridItemResult.Failed -> {

            }

            is AddGridItemResult.Success -> {
                snapshotFlow { dragOffset }.onStart {
                    homeType = HomeType.Pager
                    showOverlay = true
                    showMenu = true
                }.onEach { offset ->
                    onMoveGridItem(
                        pagerState.currentPage,
                        addGridItemResult.gridItem.id,
                        offset.x.roundToInt(),
                        offset.y.roundToInt(),
                        overlaySize.width,
                        screenSize.width,
                        screenSize.height,
                    )
                }.collect()
            }

            null -> Unit
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragEnd = {
                        showOverlay = false
                        showResize = false
                        onResetOverlay()

                        when (val result = currentAddGridItemResult) {
                            AddGridItemResult.Failed -> {

                            }

                            is AddGridItemResult.Success -> {
                                when (val data = result.gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {

                                    }

                                    is GridItemData.Widget -> {
                                        val provider =
                                            ComponentName.unflattenFromString(data.componentName)

                                        if (appWidgetManager.bindAppWidgetIdIfAllowed(
                                                appWidgetId = data.appWidgetId,
                                                provider = provider,
                                            )
                                        ) {
                                            //Grid item data already saved
                                        } else {
                                            val intent =
                                                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                                                    putExtra(
                                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                        data.appWidgetId,
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

                            null -> Unit
                        }
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
                    gridItems = gridItems,
                    showOverlay = showOverlay,
                    showMenu = showMenu,
                    showResize = showResize,
                    onResizeGridItem = onResizeGridItem,
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
                    },
                    onAddAppWidgetProviderInfoGridItem = onAddAppWidgetProviderInfoGridItem,
                )
            }
        }


        if (showOverlay) {
            HomeOverlay(overlaySize = overlaySize, dragOffset = dragOffset)
        }

        if (gridItemByCoordinates != null && gridItemByCoordinates.not()) {
            HomeBottomSheet(
                sheetState = sheetState,
                onResetGridItemByCoordinates = onResetGridItemByCoordinates,
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
    onResetGridItemByCoordinates: () -> Unit,
    onHomeType: (HomeType) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onResetGridItemByCoordinates,
        sheetState = sheetState,
    ) {
        Row {
            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onHomeType(HomeType.Application)

                        onResetGridItemByCoordinates()
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

                        onResetGridItemByCoordinates()
                    },
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                Text(text = "Widgets")
            }
        }
    }
}

@Composable
private fun HomeOverlay(
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
        modifier = Modifier
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
