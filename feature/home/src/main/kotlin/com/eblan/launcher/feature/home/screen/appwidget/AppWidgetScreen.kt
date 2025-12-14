package com.eblan.launcher.feature.home.screen.appwidget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.pager.handleApplyFling
import com.eblan.launcher.feature.home.screen.widget.getWidgetGridItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun AppWidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup?,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    drag: Drag,
    screenHeight: Int,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(screenHeight.toFloat()) }

    LaunchedEffect(key1 = Unit) {
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                easing = FastOutSlowInEasing,
            ),
        )
    }

    BackHandler {
        scope.launch {
            offsetY.animateTo(
                targetValue = screenHeight.toFloat(),
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )

            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = offsetY.value.roundToInt())
            }
            .pointerInput(key1 = Unit) {
                detectTapGestures(onTap = {
                    scope.launch {
                        offsetY.animateTo(
                            targetValue = screenHeight.toFloat(),
                            animationSpec = tween(
                                easing = FastOutSlowInEasing,
                            ),
                        )

                        onDismiss()
                    }
                })
            }
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
        ) {
            if (eblanApplicationInfoGroup != null) {
                Success(
                    modifier = Modifier
                        .pointerInput(key1 = Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    scope.launch {
                                        offsetY.snapTo(
                                            (offsetY.value + dragAmount).coerceAtLeast(0f)
                                        )
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        handleApplyFling(
                                            offsetY = offsetY,
                                            remaining = 0f,
                                            screenHeight = screenHeight,
                                            onDismiss = onDismiss,
                                        )
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        handleApplyFling(
                                            offsetY = offsetY,
                                            remaining = 0f,
                                            screenHeight = screenHeight,
                                            onDismiss = onDismiss,
                                        )
                                    }
                                },
                            )
                        }
                        .fillMaxWidth()
                        .padding(paddingValues),
                    eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos[eblanApplicationInfoGroup.packageName].orEmpty(),
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = onDraggingGridItem,
                )
            }
        }
    }

}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset, IntSize) -> Unit,
    onLongPressGridItem: (GridItemSource, ImageBitmap?) -> Unit,
    currentPage: Int,
    gridItemSettings: GridItemSettings,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = eblanApplicationInfoGroup.icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(text = eblanApplicationInfoGroup.label.toString())

        Spacer(modifier = Modifier.height(5.dp))

        LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            items(eblanAppWidgetProviderInfos) { eblanAppWidgetProviderInfo ->
                EblanAppWidgetProviderInfoItem(
                    eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = onDraggingGridItem,
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun EblanAppWidgetProviderInfoItem(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    drag: Drag,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    currentPage: Int,
    gridItemSettings: GridItemSettings,
    onResetOverlay: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val preview = eblanAppWidgetProviderInfo.preview ?: eblanAppWidgetProviderInfo.icon

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Cancel || drag == Drag.End) {
            alpha = 1f

            scale.stop()

            if (scale.value < 1f) {
                scale.animateTo(1f)
            }
        }
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = drag) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            onLongPressGridItem(
                                GridItemSource.New(
                                    gridItem = getWidgetGridItem(
                                        id = Uuid.random().toHexString(),
                                        page = currentPage,
                                        componentName = eblanAppWidgetProviderInfo.componentName,
                                        configure = eblanAppWidgetProviderInfo.configure,
                                        packageName = eblanAppWidgetProviderInfo.packageName,
                                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
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
                                        label = eblanAppWidgetProviderInfo.label,
                                        icon = eblanAppWidgetProviderInfo.icon,
                                        gridItemSettings = gridItemSettings,
                                    ),
                                ),
                                graphicsLayer.toImageBitmap(),
                            )

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onDraggingGridItem()

                            alpha = 0f
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        alpha = 1f

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .sizeIn(
                maxWidth = 200.dp,
                maxHeight = 200.dp,
            )
            .padding(20.dp)
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            modifier = Modifier
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset = layoutCoordinates.positionInRoot().round()

                    intSize = layoutCoordinates.size
                },
            model = preview,
            contentDescription = null,
        )
    }
}