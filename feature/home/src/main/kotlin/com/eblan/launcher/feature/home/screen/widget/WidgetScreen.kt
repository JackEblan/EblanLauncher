package com.eblan.launcher.feature.home.screen.widget

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalFoundationApi::class, ExperimentalUuidApi::class)
@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    gridItemSource: GridItemSource?,
    onLongPress: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
    onApplyToScroll: (Float) -> Unit,
    onApplyToFling: () -> Unit,
) {
    val density = LocalDensity.current

    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            onApplyToScroll = onApplyToScroll,
            onApplyToFling = onApplyToFling,
        )
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemSource != null) {
            onDragging()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            eblanAppWidgetProviderInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn(overscrollEffect = overscrollEffect) {
                    items(eblanAppWidgetProviderInfos.keys.toList()) { eblanApplicationInfo ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = eblanApplicationInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )

                            Text(
                                text = eblanApplicationInfo.label.toString(),
                            )

                            eblanAppWidgetProviderInfos[eblanApplicationInfo]?.forEach { eblanAppWidgetProviderInfo ->
                                var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                                val preview = eblanAppWidgetProviderInfo.preview
                                    ?: eblanAppWidgetProviderInfo.eblanApplicationInfo.icon

                                val size = with(density) {
                                    val (width, height) = getWidgetGridItemSize(
                                        rows = rows,
                                        columns = columns,
                                        gridWidth = rootWidth,
                                        gridHeight = rootHeight - dockHeight,
                                        minWidth = eblanAppWidgetProviderInfo.minWidth,
                                        minHeight = eblanAppWidgetProviderInfo.minHeight,
                                        targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                                        targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                                    )

                                    DpSize(width = width.toDp(), height = height.toDp())
                                }

                                AsyncImage(
                                    modifier = Modifier
                                        .dragAndDropSource(
                                            block = {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        onLongPress(
                                                            page,
                                                            GridItemSource.New(
                                                                gridItem = getWidgetGridItem(
                                                                    id = Uuid.random()
                                                                        .toHexString(),
                                                                    page = page,
                                                                    componentName = eblanAppWidgetProviderInfo.componentName,
                                                                    configure = eblanAppWidgetProviderInfo.configure,
                                                                    packageName = eblanAppWidgetProviderInfo.packageName,
                                                                    targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                                                                    targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                                                                    minWidth = eblanAppWidgetProviderInfo.minWidth,
                                                                    minHeight = eblanAppWidgetProviderInfo.minHeight,
                                                                    resizeMode = eblanAppWidgetProviderInfo.resizeMode,
                                                                    minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
                                                                    minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
                                                                    maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
                                                                    maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
                                                                    preview = eblanAppWidgetProviderInfo.preview,
                                                                    gridItemSettings = gridItemSettings,
                                                                ),
                                                            ),
                                                        )

                                                        startTransfer(
                                                            DragAndDropTransferData(
                                                                clipData = ClipData.newPlainText(
                                                                    "Drag",
                                                                    "Drag",
                                                                ),
                                                            ),
                                                        )
                                                    },
                                                )
                                            },
                                        )
                                        .size(size)
                                        .onGloballyPositioned { layoutCoordinates ->
                                            intOffset = layoutCoordinates.positionInRoot().round()
                                        },
                                    model = preview,
                                    contentDescription = null,
                                )

                                val infoText = """
    ${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}
    MinWidth = ${eblanAppWidgetProviderInfo.minWidth} MinHeight = ${eblanAppWidgetProviderInfo.minHeight}
    ResizeMode = ${eblanAppWidgetProviderInfo.resizeMode}
    MinResizeWidth = ${eblanAppWidgetProviderInfo.minResizeWidth} MinResizeHeight = ${eblanAppWidgetProviderInfo.minResizeHeight}
    MaxResizeWidth = ${eblanAppWidgetProviderInfo.maxResizeWidth} MaxResizeHeight = ${eblanAppWidgetProviderInfo.maxResizeHeight}
    """.trimIndent()

                                Text(
                                    text = infoText,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getWidgetGridItem(
    id: String,
    page: Int,
    componentName: String,
    configure: String?,
    packageName: String,
    targetCellHeight: Int,
    targetCellWidth: Int,
    minWidth: Int,
    minHeight: Int,
    resizeMode: Int,
    minResizeWidth: Int,
    minResizeHeight: Int,
    maxResizeWidth: Int,
    maxResizeHeight: Int,
    preview: String?,
    gridItemSettings: GridItemSettings,
): GridItem {
    val data = GridItemData.Widget(
        appWidgetId = 0,
        componentName = componentName,
        packageName = packageName,
        configure = configure,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
    )

    return GridItem(
        id = id,
        folderId = null,
        page = page,
        startRow = 0,
        startColumn = 0,
        rowSpan = 1,
        columnSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = gridItemSettings,
    )
}