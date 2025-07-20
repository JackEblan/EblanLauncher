package com.eblan.launcher.feature.home.screen.shortcut

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortcutScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanShortcutInfos: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    rows: Int,
    columns: Int,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    drag: Drag,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    onLongPress: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemLayoutInfo != null) {
            onDragging()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            eblanShortcutInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn {
                    items(eblanShortcutInfos.keys.toList()) { eblanApplicationInfo ->
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

                            eblanShortcutInfos[eblanApplicationInfo]?.forEach { eblanShortcutInfo ->
                                val preview = eblanShortcutInfo.icon
                                    ?: eblanShortcutInfo.eblanApplicationInfo.icon

                                AsyncImage(
                                    modifier = Modifier.dragAndDropSource(
                                        block = {
                                            detectTapGestures(
                                                onLongPress = {
                                                    onLongPress(
                                                        page,
                                                        GridItemSource(
                                                            gridItemLayoutInfo = getGridItemLayoutInfo(
                                                                page = page,
                                                                rows = rows,
                                                                columns = columns,
                                                                gridWidth = rootWidth,
                                                                gridHeight = rootHeight - dockHeight,
                                                                id = eblanShortcutInfo.id,
                                                                packageName = eblanShortcutInfo.packageName,
                                                                shortLabel = eblanShortcutInfo.shortLabel,
                                                                longLabel = eblanShortcutInfo.longLabel,
                                                                icon = eblanShortcutInfo.icon,
                                                            ),
                                                            type = GridItemSource.Type.New,
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
                                    ),
                                    model = preview,
                                    contentDescription = null,
                                )

                                val infoText = """
                                    ${eblanShortcutInfo.id}
                                    ${eblanShortcutInfo.shortLabel}
                                    ${eblanShortcutInfo.longLabel}
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

private fun getGridItemLayoutInfo(
    page: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    id: String,
    packageName: String,
    shortLabel: String,
    longLabel: String,
    icon: String?,
): GridItemLayoutInfo {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val data = GridItemData.ShortcutInfo(
        id = id,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
    )

    val gridItem = GridItem(
        id = 0,
        page = page,
        startRow = 0,
        startColumn = 0,
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