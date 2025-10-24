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

import android.os.Process
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfoApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.ui.local.LocalUserManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    drag: Drag,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    appDrawerSettings: AppDrawerSettings,
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
    onResetOverlay: () -> Unit,
) {
    val animatedSwipeUpY = remember { Animatable(screenHeight.toFloat()) }

    val overscrollAlpha = remember { Animatable(0f) }

    val overscrollOffset = remember { Animatable(0f) }

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
                            Success(
                                currentPage = currentPage,
                                isApplicationComponentVisible = isApplicationComponentVisible,
                                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                                gridItemSettings = gridItemSettings,
                                paddingValues = paddingValues,
                                screenHeight = screenHeight,
                                drag = drag,
                                eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
                                appDrawerSettings = appDrawerSettings,
                                animatedSwipeUpY = animatedSwipeUpY,
                                overscrollOffset = overscrollOffset,
                                overscrollAlpha = overscrollAlpha,
                                onLongPressGridItem = onLongPressGridItem,
                                onUpdateGridItemOffset = onUpdateGridItemOffset,
                                onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                                onDismiss = onDismiss,
                                onDraggingGridItem = onDraggingGridItem,
                                onResetOverlay = onResetOverlay,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    eblanAppWidgetProviderInfos: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    drag: Drag,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    appDrawerSettings: AppDrawerSettings,
    animatedSwipeUpY: Animatable<Float, AnimationVector1D>,
    overscrollOffset: Animatable<Float, AnimationVector1D>,
    overscrollAlpha: Animatable<Float, AnimationVector1D>,
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
    onResetOverlay: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            overscrollOffset = overscrollOffset,
            overscrollFactor = appDrawerSettings.overscrollFactor,
            onFling = onDismiss,
            onFastFling = onDismiss,
        )
    }

    LaunchedEffect(key1 = animatedSwipeUpY) {
        animatedSwipeUpY.animateTo(0f)
    }

    LaunchedEffect(key1 = drag) {
        if (isApplicationComponentVisible) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()
                }

                Drag.Cancel, Drag.End -> {
                    onResetOverlay()
                }

                else -> Unit
            }
        }
    }

    BackHandler {
        scope.launch {
            animatedSwipeUpY.animateTo(screenHeight.toFloat())

            onDismiss()
        }
    }

    Column(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = overscrollOffset.value.roundToInt())
            }
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) {
        EblanAppWidgetProviderInfoDockSearchBar(
            onQueryChange = onGetEblanAppWidgetProviderInfosByLabel,
            eblanAppWidgetProviderInfosByLabel = eblanAppWidgetProviderInfosByLabel,
            drag = drag,
            onUpdateGridItemOffset = onUpdateGridItemOffset,
            onLongPressGridItem = onLongPressGridItem,
            currentPage = currentPage,
            gridItemSettings = gridItemSettings,
            onResetOverlay = onResetOverlay,
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                overscrollEffect = overscrollEffect,
            ) {
                items(eblanAppWidgetProviderInfos.keys.toList()) { eblanApplicationInfo ->
                    EblanApplicationInfoItem(
                        eblanAppWidgetProviderInfoApplicationInfo = eblanApplicationInfo,
                        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                        drag = drag,
                        onUpdateGridItemOffset = onUpdateGridItemOffset,
                        onLongPressGridItem = onLongPressGridItem,
                        currentPage = currentPage,
                        gridItemSettings = gridItemSettings,
                        onResetOverlay = onResetOverlay,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EblanAppWidgetProviderInfoDockSearchBar(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
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
                placeholder = { Text("Search Widgets") },
                leadingIcon = { Icon(EblanLauncherIcons.Search, contentDescription = null) },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(eblanAppWidgetProviderInfosByLabel.keys.toList()) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    eblanAppWidgetProviderInfoApplicationInfo = eblanApplicationInfo,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosByLabel,
                    drag = drag,
                    onUpdateGridItemOffset = { intOffset, intSize ->
                        focusManager.clearFocus()

                        onUpdateGridItemOffset(intOffset, intSize)
                    },
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    onResetOverlay = onResetOverlay,
                )
            }
        }
    }
}

@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfoApplicationInfo: EblanAppWidgetProviderInfoApplicationInfo,
    eblanAppWidgetProviderInfos: Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>,
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
            headlineContent = { Text(text = eblanAppWidgetProviderInfoApplicationInfo.label.toString()) },
            leadingContent = {
                AsyncImage(
                    model = eblanAppWidgetProviderInfoApplicationInfo.icon,
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

            eblanAppWidgetProviderInfos[eblanAppWidgetProviderInfoApplicationInfo]?.forEach { eblanAppWidgetProviderInfo ->
                EblanAppWidgetProviderInfoItem(
                    eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    onResetOverlay = onResetOverlay,
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
) {
    val userManager = LocalUserManager.current

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val preview =
        eblanAppWidgetProviderInfo.preview ?: eblanAppWidgetProviderInfo.icon

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
                                        className = eblanAppWidgetProviderInfo.className,
                                        componentName = eblanAppWidgetProviderInfo.componentName,
                                        configure = eblanAppWidgetProviderInfo.configure,
                                        packageName = eblanAppWidgetProviderInfo.packageName,
                                        serialNumber = userManager.getSerialNumberForUser(userHandle = Process.myUserHandle()),
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
            .fillMaxWidth()
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun getWidgetGridItem(
    id: String,
    page: Int,
    componentName: String,
    configure: String?,
    className: String,
    packageName: String,
    serialNumber: Long,
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
    label: String?,
    icon: String?,
    gridItemSettings: GridItemSettings,
): GridItem {
    val data = GridItemData.Widget(
        appWidgetId = 0,
        className = className,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
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
        label = label,
        icon = icon,
    )

    return GridItem(
        id = id,
        folderId = null,
        page = page,
        startColumn = -1,
        startRow = -1,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = gridItemSettings,
    )
}
