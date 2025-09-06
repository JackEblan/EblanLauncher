package com.eblan.launcher.feature.home.screen.widget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
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
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
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

@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    drag: Drag,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    onLongPressGridItem: (
        currentPage: Int,
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
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
                            Column(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(x = 0, y = overscrollOffset.value.roundToInt())
                                    }
                                    .padding(
                                        top = paddingValues.calculateTopPadding(),
                                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                                    )
                                    .matchParentSize(),
                            ) {
                                EblanAppWidgetProviderInfoDockSearchBar(
                                    onQueryChange = onGetEblanAppWidgetProviderInfosByLabel,
                                    eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
                                    drag = drag,
                                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                                    onLongPressGridItem = onLongPressGridItem,
                                    page = page,
                                    gridItemSettings = gridItemSettings,
                                )

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                                    overscrollEffect = overscrollEffect,
                                ) {
                                    items(eblanAppWidgetProviderInfos.keys.toList()) { eblanApplicationInfo ->
                                        EblanApplicationInfoItem(
                                            eblanApplicationInfo = eblanApplicationInfo,
                                            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                                            drag = drag,
                                            onUpdateGridItemOffset = onUpdateGridItemOffset,
                                            onLongPressGridItem = onLongPressGridItem,
                                            page = page,
                                            gridItemSettings = gridItemSettings,
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

@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource, imageBitmap: ImageBitmap?) -> Unit,
    page: Int,
    gridItemSettings: GridItemSettings,
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(text = eblanApplicationInfo.label.toString()) },
        supportingContent = {
            Column {
                Text(text = eblanApplicationInfo.packageName)

                if (expanded) {
                    Spacer(modifier = Modifier.height(10.dp))

                    eblanAppWidgetProviderInfos[eblanApplicationInfo]?.forEach { eblanAppWidgetProviderInfo ->
                        EblanAppWidgetProviderInfoItem(
                            eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                            drag = drag,
                            onUpdateGridItemOffset = onUpdateGridItemOffset,
                            onLongPressGridItem = onLongPressGridItem,
                            page = page,
                            gridItemSettings = gridItemSettings,
                        )
                    }
                }
            }
        },
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
        modifier = modifier
            .clickable {
                expanded = !expanded
            }
            .fillMaxWidth(),
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun EblanAppWidgetProviderInfoItem(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource, imageBitmap: ImageBitmap?) -> Unit,
    page: Int,
    gridItemSettings: GridItemSettings,
) {
    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    val preview = eblanAppWidgetProviderInfo.preview
        ?: eblanAppWidgetProviderInfo.eblanApplicationInfo.icon

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier,
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
                            onUpdateGridItemOffset(intOffset)

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
                                )
                            }
                        },
                    )
                }
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset =
                        layoutCoordinates.positionInRoot()
                            .round()
                }
                .fillMaxWidth(),
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

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = infoText,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EblanAppWidgetProviderInfoDockSearchBar(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    eblanAppWidgetProviderInfosByLabel: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset) -> Unit,
    onLongPressGridItem: (currentPage: Int, gridItemSource: GridItemSource, imageBitmap: ImageBitmap?) -> Unit,
    page: Int,
    gridItemSettings: GridItemSettings,
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
                placeholder = { Text("Search Applications") },
                leadingIcon = { Icon(EblanLauncherIcons.Search, contentDescription = null) },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        LazyColumn {
            items(eblanAppWidgetProviderInfosByLabel.keys.toList()) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    eblanApplicationInfo = eblanApplicationInfo,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosByLabel,
                    drag = drag,
                    onUpdateGridItemOffset = { intOffset ->
                        focusManager.clearFocus()

                        onUpdateGridItemOffset(intOffset)
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    page = page,
                    gridItemSettings = gridItemSettings,
                )
            }
        }
    }
}