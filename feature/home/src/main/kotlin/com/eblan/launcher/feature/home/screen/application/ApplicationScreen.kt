package com.eblan.launcher.feature.home.screen.application

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    drag: Drag,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onLongPressApplicationInfo: (ImageBitmap) -> Unit,
    onDragStart: (
        offset: IntOffset,
        size: IntSize,
        GridItemLayoutInfo,
    ) -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var data by remember { mutableStateOf<GridItemData?>(null) }

    val scope = rememberCoroutineScope()

    val currentOnLongPressApplicationInfo by rememberUpdatedState(onLongPressApplicationInfo)

    LaunchedEffect(key1 = drag) {
        if (drag is Drag.Start && data != null) {
            val addGridItemLayoutInfo = getGridItemLayoutInfo(
                page = page,
                rows = rows,
                columns = columns,
                x = drag.offset.x.roundToInt(),
                y = drag.offset.y.roundToInt(),
                screenSize = drag.size,
                data = data!!,
            )

            val size = IntSize(
                addGridItemLayoutInfo.width,
                addGridItemLayoutInfo.height,
            )

            val offset = IntOffset(
                drag.offset.x.roundToInt() - size.width / 2,
                drag.offset.y.roundToInt() - size.height / 2,
            )

            onDragStart(
                offset,
                size,
                addGridItemLayoutInfo,
            )
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(eblanApplicationInfos) { eblanApplicationInfo ->
            val graphicsLayer = rememberGraphicsLayer()

            Column(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                data = GridItemData.ApplicationInfo(
                                    packageName = eblanApplicationInfo.packageName,
                                    icon = eblanApplicationInfo.icon,
                                    label = eblanApplicationInfo.label,
                                )

                                scope.launch {
                                    currentOnLongPressApplicationInfo(graphicsLayer.toImageBitmap())
                                }
                            },
                        )
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(5.dp))

                AsyncImage(
                    model = eblanApplicationInfo.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = eblanApplicationInfo.label,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(5.dp))
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
    screenSize: IntSize,
    data: GridItemData,
): GridItemLayoutInfo {
    val cellWidth = screenSize.width / columns

    val cellHeight = screenSize.height / rows

    val (startRow, startColumn) = coordinatesToStartPosition(
        x = x,
        y = y,
        rows = rows,
        columns = columns,
        screenWidth = screenSize.width,
        screenHeight = screenSize.height,
    )

    val gridItem = GridItem(
        id = Uuid.random().toHexString(),
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = 1,
        columnSpan = 1,
        data = data,
    )

    return GridItemLayoutInfo(
        gridItem = gridItem,
        width = gridItem.columnSpan * cellWidth,
        height = gridItem.rowSpan * cellHeight,
        x = gridItem.startColumn * cellWidth,
        y = gridItem.startRow * cellHeight,
        screenWidth = screenSize.width,
        screenHeight = screenSize.height,
    )
}
