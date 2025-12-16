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
package com.eblan.launcher.feature.home.screen.shortcutconfig

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.scroll.OffsetNestedScrollConnection
import com.eblan.launcher.feature.home.component.scroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.pager.handleApplyFling
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun ShortcutConfigScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    gridItemSettings: GridItemSettings,
    eblanShortcutConfigsByLabel: Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>,
    screenHeight: Int,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
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
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                Success(
                    modifier = modifier,
                    currentPage = currentPage,
                    paddingValues = paddingValues,
                    drag = drag,
                    gridItemSettings = gridItemSettings,
                    eblanShortcutConfigsByLabel = eblanShortcutConfigsByLabel,
                    eblanShortcutConfigs = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutConfigs,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                    onDraggingGridItem = onDraggingGridItem,
                    onResetOverlay = onResetOverlay,
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
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Success(
    modifier: Modifier = Modifier,
    currentPage: Int,
    paddingValues: PaddingValues,
    drag: Drag,
    gridItemSettings: GridItemSettings,
    eblanShortcutConfigsByLabel: Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>,
    eblanShortcutConfigs: Map<Long, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val horizontalPagerState = rememberPagerState(
        pageCount = {
            eblanShortcutConfigs.keys.size
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) {
        EblanShortcutConfigDockSearchBar(
            modifier = modifier,
            onQueryChange = onGetEblanShortcutConfigsByLabel,
            eblanShortcutConfigsByLabel = eblanShortcutConfigsByLabel,
            drag = drag,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onLongPressGridItem = onLongPressGridItem,
            currentPage = currentPage,
            gridItemSettings = gridItemSettings,
            onResetOverlay = onResetOverlay,
            onDraggingGridItem = onDraggingGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
        )

        if (eblanShortcutConfigs.keys.size > 1) {
            EblanShortcutConfigTabRow(
                currentPage = horizontalPagerState.currentPage,
                eblanShortcutConfigs = eblanShortcutConfigs,
                onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
            )

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = horizontalPagerState,
            ) { index ->
                EblanShortcutConfigsPage(
                    index = index,
                    currentPage = currentPage,
                    paddingValues = paddingValues,
                    drag = drag,
                    gridItemSettings = gridItemSettings,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    onLongPressGridItem = onLongPressGridItem,
                    onResetOverlay = onResetOverlay,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onDraggingGridItem = onDraggingGridItem,
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        } else {
            EblanShortcutConfigsPage(
                index = 0,
                currentPage = currentPage,
                paddingValues = paddingValues,
                drag = drag,
                gridItemSettings = gridItemSettings,
                eblanShortcutConfigs = eblanShortcutConfigs,
                onLongPressGridItem = onLongPressGridItem,
                onResetOverlay = onResetOverlay,
                onUpdateGridItemOffset = onUpdateGridItemOffset,
                onDraggingGridItem = onDraggingGridItem,
                onVerticalDrag = onVerticalDrag,
                onDragEnd = onDragEnd,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EblanShortcutConfigDockSearchBar(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    eblanShortcutConfigsByLabel: Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>,
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
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var query by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    DockedSearchBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery

                    onQueryChange(newQuery)
                },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Search Shortcuts") },
                leadingIcon = { Icon(EblanLauncherIcons.Search, contentDescription = null) },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(eblanShortcutConfigsByLabel.keys.toList()) { eblanApplicationInfoGroup ->
                EblanApplicationInfoItem(
                    modifier = modifier,
                    eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                    eblanShortcutConfigs = eblanShortcutConfigsByLabel,
                    drag = drag,
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        focusManager.clearFocus()

                        onUpdateGridItemOffset(
                            intOffset,
                            intSize,
                        )
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EblanShortcutConfigTabRow(
    currentPage: Int,
    eblanShortcutConfigs: Map<Long, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SecondaryTabRow(selectedTabIndex = currentPage) {
        eblanShortcutConfigs.keys.forEachIndexed { index, serialNumber ->
            Tab(
                selected = currentPage == index,
                onClick = {
                    scope.launch {
                        onAnimateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = "User $serialNumber",
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EblanShortcutConfigsPage(
    modifier: Modifier = Modifier,
    index: Int,
    currentPage: Int,
    paddingValues: PaddingValues,
    drag: Drag,
    gridItemSettings: GridItemSettings,
    eblanShortcutConfigs: Map<Long, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onResetOverlay: () -> Unit,
    onUpdateGridItemOffset: (IntOffset, IntSize) -> Unit,
    onDraggingGridItem: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val overscrollEffect = remember {
        OffsetOverscrollEffect(
            scope = scope,
            onVerticalDrag = onVerticalDrag,
            onDragEnd = onDragEnd,
        )
    }

    val lazyListState = rememberLazyListState()

    val serialNumber = eblanShortcutConfigs.keys.toList().getOrElse(
        index = index,
        defaultValue = {
            0
        },
    )

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

    Box(
        modifier = modifier
            .run {
                if (!canOverscroll) {
                    nestedScroll(nestedScrollConnection)
                } else {
                    this
                }
            }
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding(),
            ),
            overscrollEffect = if (canOverscroll) {
                overscrollEffect
            } else {
                rememberOverscrollEffect()
            },
        ) {
            items(eblanShortcutConfigs[serialNumber].orEmpty().keys.toList()) { eblanApplicationInfoGroup ->
                EblanApplicationInfoItem(
                    modifier = modifier,
                    eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                    eblanShortcutConfigs = eblanShortcutConfigs[serialNumber].orEmpty(),
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    onResetOverlay = onResetOverlay,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    eblanShortcutConfigs: Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>,
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

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                items(eblanShortcutConfigs[eblanApplicationInfoGroup].orEmpty()) { eblanShortcutConfig ->
                    EblanShortcutConfigItem(
                        eblanShortcutConfig = eblanShortcutConfig,
                        drag = drag,
                        onUpdateGridItemOffset = onUpdateGridItemOffset,
                        onLongPressGridItem = onLongPressGridItem,
                        currentPage = currentPage,
                        gridItemSettings = gridItemSettings,
                        onResetOverlay = onResetOverlay,
                        onDraggingGridItem = onDraggingGridItem,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun EblanShortcutConfigItem(
    modifier: Modifier = Modifier,
    eblanShortcutConfig: EblanShortcutConfig,
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
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Cancel || drag == Drag.End) {
            isLongPress = false

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

                            val data = GridItemData.ShortcutConfig(
                                serialNumber = eblanShortcutConfig.serialNumber,
                                componentName = eblanShortcutConfig.componentName,
                                packageName = eblanShortcutConfig.packageName,
                                activityLabel = eblanShortcutConfig.activityLabel,
                                activityIcon = eblanShortcutConfig.activityIcon,
                                applicationIcon = eblanShortcutConfig.activityIcon,
                                applicationLabel = eblanShortcutConfig.activityLabel,
                                shortcutIntentName = null,
                                shortcutIntentIcon = null,
                                shortcutIntentUri = null,
                                customIcon = null,
                                customLabel = null,
                            )

                            val id = Uuid.random().toHexString()

                            onLongPressGridItem(
                                GridItemSource.New(
                                    gridItem = GridItem(
                                        id = id,
                                        folderId = null,
                                        page = currentPage,
                                        startColumn = -1,
                                        startRow = -1,
                                        columnSpan = 1,
                                        rowSpan = 1,
                                        data = data,
                                        associate = Associate.Grid,
                                        override = false,
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
                                    screen = Screen.Drag,
                                ),
                            )

                            onDraggingGridItem()

                            isLongPress = true
                        }
                    },
                    onPress = {
                        awaitRelease()

                        scale.stop()

                        isLongPress = false

                        onResetOverlay()

                        if (scale.value < 1f) {
                            scale.animateTo(1f)
                        }
                    },
                )
            }
            .size(100.dp)
            .padding(10.dp)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (!isLongPress) {
            Box(
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
                    }
                    .size(gridItemSettings.iconSize.dp),
            ) {
                AsyncImage(
                    model = eblanShortcutConfig.activityIcon,
                    contentDescription = null,
                )

                if (eblanShortcutConfig.serialNumber != 0L) {
                    ElevatedCard(
                        modifier = Modifier
                            .size((gridItemSettings.iconSize * 0.40).dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Work,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = eblanShortcutConfig.activityLabel.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
