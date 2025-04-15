package com.eblan.launcher.feature.home

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.application.rememberApplicationState
import com.eblan.launcher.feature.home.screen.drag.DragScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.feature.home.screen.resize.ResizeScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.screen.widget.rememberWidgetState
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()

    val pageDirection by viewModel.pageDirection.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    val appWidgetProviderInfos by viewModel.appWidgetProviderInfos.collectAsStateWithLifecycle()

    val gridCacheItems by viewModel.gridCacheItems.collectAsStateWithLifecycle()

    val screen by viewModel.screen.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        screen = screen,
        pageDirection = pageDirection,
        homeUiState = homeUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        appWidgetProviderInfos = appWidgetProviderInfos,
        gridCacheItems = gridCacheItems,
        onMoveGridItem = viewModel::moveGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onResizeWidgetGridItem = viewModel::resizeWidgetGridItem,
        onUpdateWidget = viewModel::updateWidget,
        onUpdatePageCount = viewModel::updatePageCount,
        onDeletePage = viewModel::deletePage,
        onShowGridCache = viewModel::showGridCache,
        onUpdateScreen = viewModel::updateScreen,
        onResetGridCache = viewModel::resetGridCache,
        onEdit = onEdit,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    screen: Screen,
    pageDirection: PageDirection?,
    homeUiState: HomeUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    gridCacheItems: Map<Int, List<GridItem>>,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
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
    onUpdateWidget: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onResetGridCache: () -> Unit,
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
                        screen = screen,
                        gridItems = homeUiState.gridItemsByPage.gridItems,
                        userData = homeUiState.gridItemsByPage.userData,
                        pageDirection = pageDirection,
                        eblanApplicationInfos = eblanApplicationInfos,
                        appWidgetProviderInfos = appWidgetProviderInfos,
                        gridCacheItems = gridCacheItems,
                        onMoveGridItem = onMoveGridItem,
                        onResizeGridItem = onResizeGridItem,
                        onResizeWidgetGridItem = onResizeWidgetGridItem,
                        onUpdateWidget = onUpdateWidget,
                        onUpdatePageCount = onUpdatePageCount,
                        onDeletePage = onDeletePage,
                        onShowGridCache = onShowGridCache,
                        onUpdateScreen = onUpdateScreen,
                        onResetGridCache = onResetGridCache,
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
    screen: Screen,
    gridItems: Map<Int, List<GridItem>>,
    userData: UserData,
    pageDirection: PageDirection?,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appWidgetProviderInfos: List<Pair<EblanApplicationInfo, List<AppWidgetProviderInfo>>>,
    gridCacheItems: Map<Int, List<GridItem>>,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
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
    onUpdateWidget: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onShowGridCache: (Screen) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onResetGridCache: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = if (userData.infiniteScroll) Int.MAX_VALUE / 2 else 0,
        pageCount = {
            if (userData.infiniteScroll) {
                Int.MAX_VALUE
            } else {
                userData.pageCount
            }
        },
    )

    val sheetState = rememberModalBottomSheetState()

    var dragOffset by remember { mutableStateOf(Offset(x = -1f, y = -1f)) }

    var showMenu by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }

    var overlaySize by remember { mutableStateOf(IntSize.Zero) }

    var drag by remember { mutableStateOf(Drag.None) }

    var screenSize by remember { mutableStateOf(IntSize.Zero) }

    var appWidgetId by remember { mutableStateOf<Int?>(null) }

    var lastGridItemLayoutInfo by remember { mutableStateOf<GridItemLayoutInfo?>(null) }

    var addGridItemLayoutInfo by remember { mutableStateOf<GridItemLayoutInfo?>(null) }

    val scope = rememberCoroutineScope()

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        appWidgetId = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        } else {
            -1
        }
    }

    val applicationState = rememberApplicationState()

    val widgetState = rememberWidgetState()

    LaunchedEffect(key1 = drag) {
        if (addGridItemLayoutInfo != null) {
            if (drag == Drag.End) {
                when (val data = addGridItemLayoutInfo!!.gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        addGridItemLayoutInfo = null
                    }

                    is GridItemData.Widget -> {
                        val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

                        val provider = ComponentName.unflattenFromString(data.componentName)

                        if (appWidgetManager.bindAppWidgetIdIfAllowed(
                                appWidgetId = allocateAppWidgetId,
                                provider = provider,
                            )
                        ) {
                            onUpdateWidget(
                                addGridItemLayoutInfo!!.gridItem,
                                allocateAppWidgetId,
                            )
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
    }

    LaunchedEffect(key1 = appWidgetId) {
        if (addGridItemLayoutInfo != null && appWidgetId != null) {
            onUpdateWidget(addGridItemLayoutInfo!!.gridItem, appWidgetId!!)

            appWidgetId = null

            addGridItemLayoutInfo = null
        }
    }

    Box(
        modifier = modifier
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
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount

                        drag = Drag.Drag
                    },
                )
            }
            .fillMaxSize()
            .onSizeChanged { intSize ->
                screenSize = intSize
            },
    ) {
        when (screen) {
            Screen.Pager -> {
                PagerScreen(
                    pagerState = pagerState,
                    lastGridItemLayoutInfo = lastGridItemLayoutInfo,
                    userData = userData,
                    gridItems = gridItems,
                    showMenu = showMenu,
                    drag = drag,
                    onDismissRequest = {
                        showMenu = false
                    },
                    onShowBottomSheet = {
                        lastGridItemLayoutInfo = null
                        showBottomSheet = true
                    },
                    onLongPressedGridItem = { gridItemLayoutInfo ->
                        lastGridItemLayoutInfo = gridItemLayoutInfo

                        dragOffset = IntOffset(
                            x = gridItemLayoutInfo.x,
                            y = gridItemLayoutInfo.y,
                        ).toOffset()

                        overlaySize = IntSize(
                            width = gridItemLayoutInfo.width,
                            height = gridItemLayoutInfo.height,
                        )
                    },
                    onDragStart = {
                        onShowGridCache(Screen.Drag)
                    },
                    onEdit = {

                    },
                    onResize = {
                        onShowGridCache(Screen.Resize)
                    },
                )
            }

            Screen.Application -> {
                ApplicationScreen(
                    applicationState = applicationState,
                    currentPage = pagerState.currentPage,
                    userData = userData,
                    screenSize = screenSize,
                    drag = drag,
                    eblanApplicationInfos = eblanApplicationInfos,
                    onLongPressedApplicationInfo = { gridItemLayoutInfo ->
                        addGridItemLayoutInfo = gridItemLayoutInfo

                        dragOffset = IntOffset(
                            x = gridItemLayoutInfo.x,
                            y = gridItemLayoutInfo.y,
                        ).toOffset()

                        overlaySize = IntSize(
                            width = gridItemLayoutInfo.width,
                            height = gridItemLayoutInfo.height,
                        )
                    },
                    onDragStart = {
                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Widget -> {
                WidgetScreen(
                    widgetState = widgetState,
                    currentPage = pagerState.currentPage,
                    userData = userData,
                    screenSize = screenSize,
                    drag = drag,
                    appWidgetProviderInfos = appWidgetProviderInfos,
                    onLongPressAppWidgetProviderInfo = { gridItemLayoutInfo ->
                        addGridItemLayoutInfo = gridItemLayoutInfo

                        dragOffset = IntOffset(
                            x = gridItemLayoutInfo.x,
                            y = gridItemLayoutInfo.y,
                        ).toOffset()

                        overlaySize = IntSize(
                            width = gridItemLayoutInfo.width,
                            height = gridItemLayoutInfo.height,
                        )
                    },
                    onDragStart = {
                        onShowGridCache(Screen.Drag)
                    },
                )
            }

            Screen.Drag -> {
                DragScreen(
                    pageDirection = pageDirection,
                    currentPage = pagerState.currentPage,
                    userData = userData,
                    gridItems = gridCacheItems,
                    dragOffset = dragOffset,
                    lastGridItemLayoutInfo = lastGridItemLayoutInfo,
                    addGridItemLayoutInfo = addGridItemLayoutInfo,
                    drag = drag,
                    overlaySize = overlaySize,
                    onMoveGridItem = onMoveGridItem,
                    onUpdatePageCount = onUpdatePageCount,
                    onDragEnd = { targetPage ->
                        showMenu = true

                        onResetGridCache()

                        scope.launch {
                            pagerState.scrollToPage(targetPage)
                        }
                    },
                )
            }

            Screen.Resize -> {
                ResizeScreen(
                    currentPage = pagerState.currentPage,
                    lastGridItemLayoutInfo = lastGridItemLayoutInfo,
                    userData = userData,
                    gridItems = gridCacheItems,
                    onResizeGridItem = onResizeGridItem,
                    onResizeWidgetGridItem = onResizeWidgetGridItem,
                    onResizeEnd = {
                        onResetGridCache()
                    },
                )
            }
        }

        if (showBottomSheet) {
            HomeBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onUpdateScreen = onUpdateScreen,
                onDeletePage = {
                    val page = calculatePage(
                        index = pagerState.currentPage,
                        infiniteScroll = userData.infiniteScroll,
                        pageCount = userData.pageCount,
                    )

                    onDeletePage(page)
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
    onUpdateScreen: (Screen) -> Unit,
    onDeletePage: () -> Unit,
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
                        onUpdateScreen(Screen.Application)

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
                        onUpdateScreen(Screen.Widget)

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Widgets, contentDescription = null)

                Text(text = "Widgets")
            }

            Column(
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onDeletePage()

                        onDismissRequest()
                    },
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)

                Text(text = "Delete page")
            }
        }
    }
}
