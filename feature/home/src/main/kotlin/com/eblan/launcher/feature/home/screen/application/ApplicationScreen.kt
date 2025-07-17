package com.eblan.launcher.feature.home.screen.application

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.pager.PopupGridItemMenu
import com.eblan.launcher.feature.home.util.calculatePage

@OptIn(ExperimentalFoundationApi::class)
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
    appDrawerRowsHeight: Int,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    onLongPressApplicationInfo: (
        currentPage: Int,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDragging: () -> Unit,
) {
    var showPopupApplicationMenu by remember { mutableStateOf(false) }

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val density = LocalDensity.current

    val appDrawerRowsHeightDp = with(density) {
        appDrawerRowsHeight.toDp()
    }

    var overlayIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemLayoutInfo != null) {
            showPopupApplicationMenu = false

            onDragging()
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
                LazyVerticalGrid(columns = GridCells.Fixed(count = appDrawerColumns)) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                        Column(
                            modifier = Modifier
                                .dragAndDropSource(
                                    block = {
                                        detectTapGestures(
                                            onLongPress = {
                                                showPopupApplicationMenu = true

                                                overlayIntOffset = intOffset

                                                onLongPressApplicationInfo(
                                                    page,
                                                    getGridItemLayoutInfo(
                                                        page = page,
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
                                                    ),
                                                )

                                                startTransfer(
                                                    DragAndDropTransferData(
                                                        clipData = ClipData.newPlainText(
                                                            "Screen",
                                                            Screen.Drag.name,
                                                        ),
                                                    ),
                                                )
                                            },
                                        )
                                    },
                                )
                                .onGloballyPositioned { layoutCoordinates ->
                                    intOffset = layoutCoordinates.positionInRoot().round()
                                }
                                .height(appDrawerRowsHeightDp),
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

                if (showPopupApplicationMenu && gridItemLayoutInfo?.gridItem != null) {
                    PopupGridItemMenu(
                        x = overlayIntOffset.x,
                        y = overlayIntOffset.y,
                        width = rootWidth / appDrawerColumns,
                        height = appDrawerRowsHeight,
                        onDismissRequest = {
                            showPopupApplicationMenu = false
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
        id = 0,
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
