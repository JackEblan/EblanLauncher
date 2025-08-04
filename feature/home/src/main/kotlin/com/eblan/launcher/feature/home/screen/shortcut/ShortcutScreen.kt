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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
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
    drag: Drag,
    onLongPress: (
        currentPage: Int,
        gridItemSource: GridItemSource,
    ) -> Unit,
    onDragging: () -> Unit,
    onApplyToScroll: (Float) -> Unit,
    onApplyToFling: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var isLongPress by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            onApplyToScroll = onApplyToScroll,
            onApplyToFling = onApplyToFling,
        )
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isLongPress) {
            onDragging()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            eblanShortcutInfos.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn(overscrollEffect = overscrollEffect) {
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
                                                    isLongPress = true

                                                    val data = GridItemData.ShortcutInfo(
                                                        shortcutId = eblanShortcutInfo.shortcutId,
                                                        packageName = eblanShortcutInfo.packageName,
                                                        shortLabel = eblanShortcutInfo.shortLabel,
                                                        longLabel = eblanShortcutInfo.longLabel,
                                                        icon = eblanShortcutInfo.icon,
                                                    )

                                                    onLongPress(
                                                        page,
                                                        GridItemSource.New(
                                                            gridItem = GridItem(
                                                                id = eblanShortcutInfo.shortcutId,
                                                                page = page,
                                                                startRow = 0,
                                                                startColumn = 0,
                                                                rowSpan = 1,
                                                                columnSpan = 1,
                                                                data = data,
                                                                associate = Associate.Grid,
                                                                zIndex = 0,
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
                                    ),
                                    model = preview,
                                    contentDescription = null,
                                )

                                val infoText = """
                                    ${eblanShortcutInfo.shortcutId}
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