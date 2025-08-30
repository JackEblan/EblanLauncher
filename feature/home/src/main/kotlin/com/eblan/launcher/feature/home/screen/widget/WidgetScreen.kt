package com.eblan.launcher.feature.home.screen.widget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridWidth: Int,
    gridHeight: Int,
    dockHeight: Int,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    drag: Drag,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current

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
                val eblanAppWidgetProviderInfos =
                    eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        eblanAppWidgetProviderInfos.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.matchParentSize(),
                                contentPadding = paddingValues,
                                overscrollEffect = overscrollEffect,
                            ) {
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
                                                    gridWidth = gridWidth,
                                                    gridHeight = gridHeight - dockHeight,
                                                    minWidth = eblanAppWidgetProviderInfo.minWidth,
                                                    minHeight = eblanAppWidgetProviderInfo.minHeight,
                                                    targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                                                    targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                                                )

                                                DpSize(width = width.toDp(), height = height.toDp())
                                            }

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
                                                    .pointerInput(key1 = drag) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                scope.launch {
                                                                    scale.animateTo(0.5f)

                                                                    scale.animateTo(1f)

                                                                    onLongPressGridItem(
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
                                                                        graphicsLayer.toImageBitmap(),
                                                                        intOffset,
                                                                    )
                                                                }
                                                            },
                                                        )
                                                    }
                                                    .size(size)
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