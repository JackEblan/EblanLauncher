package com.eblan.launcher.feature.home.screen.application

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    appDrawerColumns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    appDrawerRowsHeight: Int,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    paddingValues: PaddingValues,
    overscrollAlpha: Animatable<Float, AnimationVector1D>,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
    ) -> Unit,
    onFling: suspend () -> Unit,
    onFastFling: suspend () -> Unit,
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

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            onFling = onFling,
            onFastFling = onFastFling,
        )
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
                    modifier = Modifier.matchParentSize(),
                    contentPadding = paddingValues,
                    overscrollEffect = overscrollEffect,
                ) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        var intOffset by remember { mutableStateOf(IntOffset.Zero) }

                        var intSize by remember { mutableStateOf(IntSize.Zero) }

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

                        if (show) {
                            Column(
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
                                                    showPopupApplicationMenu = true

                                                    popupMenuIntOffset = intOffset

                                                    popupMenuIntSize = intSize

                                                    val data = GridItemData.ApplicationInfo(
                                                        componentName = eblanApplicationInfo.componentName,
                                                        packageName = eblanApplicationInfo.packageName,
                                                        icon = eblanApplicationInfo.icon,
                                                        label = eblanApplicationInfo.label,
                                                    )

                                                    onLongPressGridItem(
                                                        page,
                                                        GridItemSource.New(
                                                            gridItem = GridItem(
                                                                id = Uuid.random().toHexString(),
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

                                        intSize = layoutCoordinates.size
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
                }

                if (showPopupApplicationMenu) {
                    val leftPadding = with(density) {
                        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
                    }

                    val topPadding = with(density) {
                        paddingValues.calculateTopPadding().roundToPx()
                    }

                    val x = popupMenuIntOffset.x - leftPadding

                    val y = popupMenuIntOffset.y - topPadding

                    Popup(
                        popupPositionProvider = MenuPositionProvider(
                            x = x,
                            y = y,
                            width = popupMenuIntSize.width,
                            height = popupMenuIntSize.height,
                        ),
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