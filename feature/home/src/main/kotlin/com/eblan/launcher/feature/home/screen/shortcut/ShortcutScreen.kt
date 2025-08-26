package com.eblan.launcher.feature.home.screen.shortcut

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ShortcutScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val animatedSwipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val scope = rememberCoroutineScope()

    val overscrollAlpha = remember { Animatable(0f) }

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            onFling = onDismiss,
            onFastFling = {
                animatedSwipeUpY.animateTo(screenHeight.toFloat())

                onDismiss()
            },
        )
    }

    LaunchedEffect(key1 = animatedSwipeUpY) {
        animatedSwipeUpY.animateTo(0f)
    }

    BackHandler {
        scope.launch {
            animatedSwipeUpY.animateTo(screenHeight.toFloat())

            onDismiss()
        }
    }

    Surface(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = animatedSwipeUpY.value.roundToInt())
            }
            .graphicsLayer(alpha = 1f - (overscrollAlpha.value / 500f))
            .fillMaxSize(),
    ) {
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                val eblanShortcutInfos =
                    eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        eblanShortcutInfos.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.matchParentSize(),
                                contentPadding = paddingValues,
                                overscrollEffect = overscrollEffect,
                            ) {
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

                                            val scale = remember { Animatable(1f) }

                                            Box(
                                                modifier = Modifier
                                                    .drawWithContent {
                                                        graphicsLayer.record {
                                                            drawContext.transform.scale(
                                                                scaleX = scale.value,
                                                                scaleY = scale.value,
                                                            )

                                                            this@drawWithContent.drawContent()
                                                        }

                                                        drawLayer(graphicsLayer)
                                                    }
                                                    .pointerInput(Unit) {
                                                        detectTapGesturesUnConsume(
                                                            onLongPress = {
                                                                scope.launch {
                                                                    scale.animateTo(0.5f)

                                                                    scale.animateTo(1f)

                                                                    val data =
                                                                        GridItemData.ShortcutInfo(
                                                                            shortcutId = eblanShortcutInfo.shortcutId,
                                                                            packageName = eblanShortcutInfo.packageName,
                                                                            shortLabel = eblanShortcutInfo.shortLabel,
                                                                            longLabel = eblanShortcutInfo.longLabel,
                                                                            icon = eblanShortcutInfo.icon,
                                                                        )

                                                                    onLongPressGridItem(
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
                                                                }
                                                            },
                                                        )
                                                    }
                                                    .onGloballyPositioned { layoutCoordinates ->
                                                        intOffset =
                                                            layoutCoordinates.positionInRoot()
                                                                .round()
                                                    },
                                            ) {
                                                AsyncImage(
                                                    modifier = Modifier.matchParentSize(),
                                                    model = preview,
                                                    contentDescription = null,
                                                )
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
        }
    }
}