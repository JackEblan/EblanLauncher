/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home.screen.widget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.scroll.OffsetNestedScrollConnection
import com.eblan.launcher.feature.home.component.scroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.pager.handleApplyFling
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    drag: Drag,
    isPressHome: Boolean,
    screen: Screen,
    screenWidth: Int,
    screenHeight: Int,
    columns: Int,
    rows: Int,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(screenHeight.toFloat()) }

    val alpha by remember {
        derivedStateOf {
            ((screenHeight - offsetY.value) / (screenHeight / 2)).coerceIn(0f, 1f)
        }
    }

    val cornerSize by remember {
        derivedStateOf {
            val progress = offsetY.value.coerceAtLeast(0f) / screenHeight

            (20 * progress).dp
        }
    }

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

    Surface(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = offsetY.value.roundToInt())
            }
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerSize))
            .alpha(alpha),
    ) {
        Success(
            currentPage = currentPage,
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
            gridItemSettings = gridItemSettings,
            paddingValues = paddingValues,
            drag = drag,
            screen = screen,
            isPressHome = isPressHome,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            columns = columns,
            rows = rows,
            onLongPressGridItem = onLongPressGridItem,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
            onDraggingGridItem = onDraggingGridItem,
            onVerticalDrag = { dragAmount ->
                scope.launch {
                    offsetY.snapTo(offsetY.value + dragAmount)
                }
            },
            onDragEnd = { remaining ->
                scope.launch {
                    handleApplyFling(
                        offsetY = offsetY,
                        remaining = remaining,
                        screenHeight = screenHeight,
                        onDismiss = onDismiss,
                    )
                }
            },
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onDismiss = {
                scope.launch {
                    offsetY.animateTo(
                        targetValue = screenHeight.toFloat(),
                        animationSpec = tween(
                            easing = FastOutSlowInEasing,
                        ),
                    )

                    onDismiss()
                }
            },
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun SharedTransitionScope.Success(
    modifier: Modifier = Modifier,
    currentPage: Int,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    drag: Drag,
    screen: Screen,
    isPressHome: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    columns: Int,
    rows: Int,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDraggingGridItem: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            onVerticalDrag = onVerticalDrag,
            onDragEnd = onDragEnd,
        )
    }

    val canOverscroll by remember(key1 = lazyListState) {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo

            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            val total = layoutInfo.totalItemsCount

            lastVisible < total - 1
        }
    }

    val nestedScrollConnection = remember {
        OffsetNestedScrollConnection(
            onVerticalDrag = onVerticalDrag,
            onDragEnd = onDragEnd,
        )
    }

    val searchBarState = rememberSearchBarState()

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(key1 = isPressHome) {
        if (isPressHome) {
            onDismiss()
        }

        if (isPressHome && searchBarState.currentValue == SearchBarValue.Expanded) {
            searchBarState.animateToCollapsed()
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start && searchBarState.currentValue == SearchBarValue.Expanded) {
            searchBarState.animateToCollapsed()
        }
    }

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text }
            .debounce(500L)
            .onEach { text ->
                onGetEblanAppWidgetProviderInfosByLabel(text.toString())
            }.collect()
    }

    Column(
        modifier = modifier
            .run {
                if (!canOverscroll) {
                    nestedScroll(nestedScrollConnection)
                } else {
                    this
                }
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) {
        SearchBar(
            state = searchBarState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            inputField = {
                SearchBarDefaults.InputField(
                    searchBarState = searchBarState,
                    textFieldState = textFieldState,
                    leadingIcon = {
                        Icon(
                            imageVector = EblanLauncherIcons.Search,
                            contentDescription = null,
                        )
                    },
                    onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                    placeholder = { Text(text = "Search Widgets") },
                )
            },
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
            overscrollEffect = overscrollEffect,
        ) {
            items(eblanAppWidgetProviderInfos.keys.toList()) { eblanApplicationInfoGroup ->
                key(eblanApplicationInfoGroup.packageName) {
                    EblanApplicationInfoItem(
                        eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                        drag = drag,
                        onUpdateGridItemOffset = onUpdateGridItemOffset,
                        onLongPressGridItem = onLongPressGridItem,
                        currentPage = currentPage,
                        gridItemSettings = gridItemSettings,
                        screen = screen,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        columns = columns,
                        rows = rows,
                        onDraggingGridItem = onDraggingGridItem,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    drag: Drag,
    screenWidth: Int,
    screenHeight: Int,
    columns: Int,
    rows: Int,
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
    screen: Screen,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        expanded = !expanded
                    },
                    onLongPress = {
                        expanded = !expanded
                    },
                )
            }
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        ListItem(
            headlineContent = { Text(text = eblanApplicationInfoGroup.label.toString()) },
            leadingContent = {
                AsyncImage(
                    model = eblanApplicationInfoGroup.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) {
                        EblanLauncherIcons.ArrowDropUp
                    } else {
                        EblanLauncherIcons.ArrowDropDown
                    },
                    contentDescription = null,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
        )

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))

            eblanAppWidgetProviderInfos[eblanApplicationInfoGroup]?.forEach { eblanAppWidgetProviderInfo ->
                EblanAppWidgetProviderInfoItem(
                    eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    screen = screen,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    columns = columns,
                    rows = rows,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.EblanAppWidgetProviderInfoItem(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    drag: Drag,
    screen: Screen,
    screenWidth: Int,
    screenHeight: Int,
    columns: Int,
    rows: Int,
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
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val preview = eblanAppWidgetProviderInfo.preview ?: eblanAppWidgetProviderInfo.applicationIcon

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    val isDragging by remember(key1 = drag) {
        derivedStateOf {
            isLongPress && (drag == Drag.Start || drag == Drag.Dragging)
        }
    }

    val id = remember { Uuid.random().toHexString() }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Dragging if isLongPress -> {
                onDraggingGridItem()
            }

            Drag.End, Drag.Cancel -> {
                isLongPress = false

                scale.stop()

                if (scale.value < 1f) {
                    scale.animateTo(1f)
                }
            }

            else -> Unit
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
                                        id = id,
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
                                        label = eblanAppWidgetProviderInfo.applicationLabel,
                                        icon = eblanAppWidgetProviderInfo.applicationIcon,
                                        gridItemSettings = gridItemSettings,
                                    ),
                                ),
                                graphicsLayer.toImageBitmap(),
                            )

                            onUpdateGridItemOffset(
                                intOffset,
                                intSize,
                            )

                            onUpdateSharedElementKey(
                                SharedElementKey(
                                    id = id,
                                    screen = screen,
                                ),
                            )

                            isLongPress = true
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        isLongPress = false

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .fillMaxWidth()
            .padding(20.dp)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
        ) {
            if (!isDragging) {
                AsyncImage(
                    modifier = Modifier
                        .sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = id,
                                    screen = screen,
                                ),
                            ),
                            visible = drag == Drag.Cancel || drag == Drag.End,
                        )
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }

                            drawLayer(graphicsLayer)
                        }
                        .onGloballyPositioned { layoutCoordinates ->
                            intOffset = layoutCoordinates.positionInRoot().round()

                            intSize = layoutCoordinates.size
                        }
                        .matchParentSize(),
                    model = preview,
                    contentDescription = null,
                )
            }
        }

        val text =
            if (eblanAppWidgetProviderInfo.targetCellWidth > 0 && eblanAppWidgetProviderInfo.targetCellHeight > 0) {
                "${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}"
            } else if (eblanAppWidgetProviderInfo.minWidth > 0 && eblanAppWidgetProviderInfo.minHeight > 0) {
                val cellWidth = screenWidth / columns

                val cellHeight = screenHeight / rows

                val spanX = (eblanAppWidgetProviderInfo.minWidth + cellWidth - 1) / cellWidth

                val spanY = (eblanAppWidgetProviderInfo.minHeight + cellHeight - 1) / cellHeight

                "${spanX}x$spanY"
            } else {
                null
            }

        val textModifier = Modifier.alpha(
            if (isDragging) {
                0f
            } else {
                1f
            },
        )

        eblanAppWidgetProviderInfo.label?.let { label ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = textModifier,
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (text != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = textModifier,
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        eblanAppWidgetProviderInfo.description?.let { description ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = textModifier,
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
