package com.eblan.launcher.feature.home.screen.application

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.component.ApplicationInfoMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.screen.pager.GridItemMenu
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    isScrollInProgress: Boolean,
    appDrawerRowsHeight: Int,
    onLongPressApplicationInfo: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDragging: () -> Unit,
    onDragEnd: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }

    var selectedIntSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var selectedGridItemLayoutInfo by remember { mutableStateOf<GridItemLayoutInfo?>(null) }

    val horizontalPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val density = LocalDensity.current

    val appDrawerRowsHeightDp = with(density) {
        appDrawerRowsHeight.toDp()
    }

    LaunchedEffect(key1 = drag) {
        if (!isScrollInProgress) {
            when (drag) {
                Drag.Start -> {
                    showMenu = true
                }

                Drag.End, Drag.Cancel, Drag.None -> {
                    onDragEnd()
                }

                Drag.Dragging -> {
                    showMenu = false

                    onDragging()
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            eblanApplicationInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(count = appDrawerColumns),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        val graphicsLayer = rememberGraphicsLayer()

                        var intSize by remember { mutableStateOf(IntSize.Zero) }

                        var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                        Column(
                            modifier = Modifier
                                .drawWithContent {
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(graphicsLayer)
                                }
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()

                                        val longPress =
                                            awaitLongPressOrCancellation(pointerId = down.id)

                                        if (longPress != null) {
                                            scope.launch {
                                                val gridItemLayoutInfo = getGridItemLayoutInfo(
                                                    page = horizontalPage,
                                                    rows = rows,
                                                    columns = columns,
                                                    x = intOffset.x,
                                                    y = intOffset.y,
                                                    gridWidth = rootWidth,
                                                    gridHeight = rootHeight - dockHeight,
                                                    componentName = eblanApplicationInfo.componentName,
                                                    packageName = eblanApplicationInfo.packageName,
                                                    icon = eblanApplicationInfo.icon,
                                                    label = eblanApplicationInfo.label,
                                                )

                                                selectedIntOffset = intOffset

                                                selectedIntSize = intSize

                                                selectedGridItemLayoutInfo = gridItemLayoutInfo

                                                onLongPressApplicationInfo(
                                                    horizontalPage,
                                                    graphicsLayer.toImageBitmap(),
                                                    intOffset,
                                                    intSize,
                                                    gridItemLayoutInfo,
                                                )
                                            }
                                        }
                                    }
                                }
                                .onSizeChanged {
                                    intSize = it
                                }
                                .onGloballyPositioned {
                                    intOffset = it.positionInParent().round()
                                }
                                .height(appDrawerRowsHeightDp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.height(5.dp))

                            AsyncImage(
                                model = eblanApplicationInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp, 40.dp),
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = eblanApplicationInfo.label.toString(),
                                textAlign = TextAlign.Center,
                                fontSize = TextUnit(
                                    value = 10f,
                                    type = TextUnitType.Sp,
                                ),
                            )

                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }

                if (showMenu) {
                    GridItemMenu(
                        x = selectedIntOffset.x,
                        y = selectedIntOffset.y,
                        width = selectedIntSize.width,
                        height = selectedIntSize.height,
                        onDismissRequest = {
                            showMenu = false
                        },
                        content = {
                            ApplicationInfoMenu(onApplicationInfo = {}, onWidgets = {})
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun getGridItemLayoutInfo(
    page: Int,
    rows: Int,
    columns: Int,
    x: Int,
    y: Int,
    gridWidth: Int,
    gridHeight: Int,
    componentName: String?,
    packageName: String,
    icon: String?,
    label: String?,
): GridItemLayoutInfo {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val startColumn = x / cellWidth

    val startRow = y / cellHeight

    val data = GridItemData.ApplicationInfo(
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
    )

    val gridItem = GridItem(
        id = Uuid.random().toHexString(),
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = 1,
        columnSpan = 1,
        dataId = data.packageName,
        data = data,
        associate = Associate.Grid,
    )

    return GridItemLayoutInfo(
        gridItem = gridItem,
        width = gridItem.columnSpan * cellWidth,
        height = gridItem.rowSpan * cellHeight,
        x = gridItem.startColumn * cellWidth,
        y = gridItem.startRow * cellHeight,
    )
}
