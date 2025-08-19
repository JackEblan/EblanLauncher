package com.eblan.launcher.feature.home.screen.shortcut

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortcutScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanShortcutInfos: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    onTestLongPressApplicationComponent: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
    onUpdateAlpha: (Float) -> Unit,
    onFling: () -> Unit,
    onFastFling: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            onFling = onFling,
            onFastFling = onFastFling,
        )
    }

    LaunchedEffect(key1 = overscrollEffect) {
        snapshotFlow { overscrollEffect.overscrollAlpha.value }.collect { overscrollAlpha ->
            onUpdateAlpha(1f - (abs(overscrollAlpha) / 500f))
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
                                var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                                val preview = eblanShortcutInfo.icon
                                    ?: eblanShortcutInfo.eblanApplicationInfo.icon

                                val graphicsLayer = rememberGraphicsLayer()

                                var show by remember { mutableStateOf(true) }

                                val scale = remember { Animatable(1f) }

                                LaunchedEffect(key1 = drag) {
                                    if (scale.value == 1.1f) {
                                        when (drag) {
                                            Drag.Dragging -> {
                                                show = false
                                            }

                                            Drag.Cancel, Drag.End -> {
                                                scale.animateTo(targetValue = 1f)

                                                show = true
                                            }

                                            else -> Unit
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .drawWithContent {
                                            graphicsLayer.record {
                                                this@drawWithContent.drawContent()
                                            }

                                            drawLayer(
                                                graphicsLayer.apply {
                                                    scaleX = scale.value
                                                    scaleY = scale.value
                                                },
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectTapGesturesUnConsume(
                                                onLongPress = {
                                                    scope.launch {
                                                        val data = GridItemData.ShortcutInfo(
                                                            shortcutId = eblanShortcutInfo.shortcutId,
                                                            packageName = eblanShortcutInfo.packageName,
                                                            shortLabel = eblanShortcutInfo.shortLabel,
                                                            longLabel = eblanShortcutInfo.longLabel,
                                                            icon = eblanShortcutInfo.icon,
                                                        )

                                                        onTestLongPressApplicationComponent(
                                                            page,
                                                            GridItemSource.New(
                                                                gridItem = GridItem(
                                                                    id = eblanShortcutInfo.shortcutId,
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
                                                                ),
                                                            ),
                                                            graphicsLayer.toImageBitmap(),
                                                            intOffset,
                                                        )

                                                        scale.animateTo(targetValue = 0.5f)

                                                        scale.animateTo(targetValue = 1.1f)
                                                    }
                                                },
                                            )
                                        }
                                        .onGloballyPositioned { layoutCoordinates ->
                                            intOffset = layoutCoordinates.positionInRoot().round()
                                        },
                                ) {
                                    if (show) {
                                        AsyncImage(
                                            modifier = Modifier.matchParentSize(),
                                            model = preview,
                                            contentDescription = null,
                                        )
                                    }
                                }

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