package com.eblan.launcher.feature.home.screen.shortcut

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
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
    drag: Drag,
    eblanShortcutInfosByLabel: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onGetEblanShortcutInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val animatedSwipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val scope = rememberCoroutineScope()

    val overscrollAlpha = remember { Animatable(0f) }

    val overscrollOffset = remember { Animatable(0f) }

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            overscrollOffset = overscrollOffset,
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
                IntOffset(
                    x = 0,
                    y = animatedSwipeUpY.value.roundToInt(),
                )
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
                            Column(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = 0,
                                            y = overscrollOffset.value.roundToInt(),
                                        )
                                    }
                                    .matchParentSize()
                                    .padding(
                                        top = paddingValues.calculateTopPadding(),
                                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                                    ),
                            ) {
                                EblanShortcutInfoDockSearchBar(
                                    onQueryChange = onGetEblanShortcutInfosByLabel,
                                    eblanShortcutInfosByLabel = eblanShortcutInfosByLabel,
                                    drag = drag,
                                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                                    onLongPressGridItem = onLongPressGridItem,
                                    page = page,
                                    gridItemSettings = gridItemSettings,
                                    onDraggingGridItem = onDraggingGridItem,
                                )

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                                    overscrollEffect = overscrollEffect,
                                ) {
                                    items(eblanShortcutInfos.keys.toList()) { eblanApplicationInfo ->
                                        EblanApplicationInfoItem(
                                            eblanApplicationInfo = eblanApplicationInfo,
                                            eblanShortcutInfos = eblanShortcutInfos,
                                            drag = drag,
                                            onUpdateGridItemOffset = onUpdateGridItemOffset,
                                            onLongPressGridItem = onLongPressGridItem,
                                            page = page,
                                            gridItemSettings = gridItemSettings,
                                            onDraggingGridItem = onDraggingGridItem,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EblanShortcutInfoDockSearchBar(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    eblanShortcutInfosByLabel: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource, imageBitmap: ImageBitmap?) -> Unit,
    page: Int,
    gridItemSettings: GridItemSettings,
    onDraggingGridItem: () -> Unit,
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
                leadingIcon = {
                    Icon(
                        EblanLauncherIcons.Search,
                        contentDescription = null,
                    )
                },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(eblanShortcutInfosByLabel.keys.toList()) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    eblanApplicationInfo = eblanApplicationInfo,
                    eblanShortcutInfos = eblanShortcutInfosByLabel,
                    drag = drag,
                    onUpdateGridItemOffset = { intOffset ->
                        focusManager.clearFocus()

                        onUpdateGridItemOffset(intOffset)
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    page = page,
                    gridItemSettings = gridItemSettings,
                    onDraggingGridItem = onDraggingGridItem,
                )
            }
        }
    }
}

@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    eblanShortcutInfos: Map<EblanApplicationInfo, List<EblanShortcutInfo>>,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource, imageBitmap: ImageBitmap?) -> Unit,
    page: Int,
    gridItemSettings: GridItemSettings,
    onDraggingGridItem: () -> Unit,
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
                    })
            }
            .fillMaxWidth()
            .animateContentSize()
    ) {
        ListItem(
            headlineContent = { Text(text = eblanApplicationInfo.label.toString()) },
            supportingContent = { Text(text = eblanApplicationInfo.packageName) },
            leadingContent = {
                AsyncImage(
                    model = eblanApplicationInfo.icon,
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

            eblanShortcutInfos[eblanApplicationInfo]?.forEach { eblanShortcutInfo ->
                EblanShortcutInfoItem(
                    eblanShortcutInfo = eblanShortcutInfo,
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    page = page,
                    gridItemSettings = gridItemSettings,
                    onDraggingGridItem = onDraggingGridItem,
                )
            }
        }
    }
}

@Composable
private fun EblanShortcutInfoItem(
    modifier: Modifier = Modifier,
    eblanShortcutInfo: EblanShortcutInfo,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource, imageBitmap: ImageBitmap?) -> Unit,
    page: Int,
    gridItemSettings: GridItemSettings,
    onDraggingGridItem: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    val preview = eblanShortcutInfo.icon ?: eblanShortcutInfo.eblanApplicationInfo.icon

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    var isLongPressed by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && isLongPressed) {
            onDraggingGridItem()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
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
                            isLongPressed = true

                            onUpdateGridItemOffset(intOffset)

                            scope.launch {
                                scale.animateTo(0.5f)

                                scale.animateTo(1f)

                                val data = GridItemData.ShortcutInfo(
                                    shortcutId = eblanShortcutInfo.shortcutId,
                                    packageName = eblanShortcutInfo.packageName,
                                    shortLabel = eblanShortcutInfo.shortLabel,
                                    longLabel = eblanShortcutInfo.longLabel,
                                    icon = eblanShortcutInfo.icon,
                                    eblanApplicationInfo = eblanShortcutInfo.eblanApplicationInfo,
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
                                )
                            }
                        },
                        onPress = {
                            awaitRelease()

                            isLongPressed = false
                        }
                    )
                }
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset = layoutCoordinates.positionInRoot().round()
                },
            model = preview,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = eblanShortcutInfo.shortLabel,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}