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
package com.eblan.launcher.feature.home.screen.application

import android.graphics.Rect
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.VerticalArrangement
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.util.getSystemTextColor
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun DoubleTapApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    screenHeight: Int,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
) {
    val offsetY = remember { Animatable(screenHeight.toFloat()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = offsetY) {
        offsetY.animateTo(0f)
    }

    ApplicationScreen(
        modifier = modifier,
        currentPage = currentPage,
        offsetY = offsetY.value,
        isApplicationComponentVisible = isApplicationComponentVisible,
        eblanApplicationComponentUiState = eblanApplicationComponentUiState,
        paddingValues = paddingValues,
        drag = drag,
        appDrawerSettings = appDrawerSettings,
        eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
        gridItemSource = gridItemSource,
        iconPackInfoPackageName = iconPackInfoPackageName,
        onLongPressGridItem = onLongPressGridItem,
        onUpdateGridItemOffset = onUpdateGridItemOffset,
        onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
        onDismiss = onDismiss,
        onAnimateDismiss = {
            scope.launch {
                offsetY.animateTo(screenHeight.toFloat())

                onDismiss()
            }
        },
        onDraggingGridItem = onDraggingGridItem,
        onResetOverlay = onResetOverlay,
    )
}

@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    offsetY: Float,
    isApplicationComponentVisible: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    onAnimateDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
) {
    val overscrollAlpha = remember { Animatable(0f) }

    val overscrollOffset = remember { Animatable(0f) }

    Surface(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = offsetY.roundToInt())
            }
            .graphicsLayer(alpha = 1f - (overscrollAlpha.value / 500f))
            .fillMaxSize(),
    ) {
        when (eblanApplicationComponentUiState) {
            EblanApplicationComponentUiState.Loading -> {
                LoadingScreen()
            }

            is EblanApplicationComponentUiState.Success -> {
                val eblanApplicationInfos =
                    eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when {
                        eblanApplicationInfos.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        else -> {
                            Success(
                                currentPage = currentPage,
                                isApplicationComponentVisible = isApplicationComponentVisible,
                                paddingValues = paddingValues,
                                drag = drag,
                                appDrawerSettings = appDrawerSettings,
                                eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
                                gridItemSource = gridItemSource,
                                iconPackInfoPackageName = iconPackInfoPackageName,
                                eblanApplicationInfos = eblanApplicationInfos,
                                overscrollOffset = overscrollOffset,
                                overscrollAlpha = overscrollAlpha,
                                onLongPressGridItem = onLongPressGridItem,
                                onUpdateGridItemOffset = onUpdateGridItemOffset,
                                onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                                onAnimateDismiss = onAnimateDismiss,
                                onDraggingGridItem = onDraggingGridItem,
                                onResetOverlay = onResetOverlay,
                                onFling = onDismiss,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Success(
    modifier: Modifier = Modifier,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    eblanApplicationInfos: Map<Long, List<EblanApplicationInfo>>,
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
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onAnimateDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onResetOverlay: () -> Unit,
    onFling: suspend () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var showPopupApplicationMenu by remember { mutableStateOf(false) }

    var popupMenuIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupMenuIntSize by remember { mutableStateOf(IntSize.Zero) }

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            eblanApplicationInfos.keys.size
        },
    )

    BackHandler {
        showPopupApplicationMenu = false

        onAnimateDismiss()
    }

    LaunchedEffect(key1 = drag) {
        if (isApplicationComponentVisible) {
            when (drag) {
                Drag.Dragging -> {
                    onDraggingGridItem()

                    showPopupApplicationMenu = false
                }

                Drag.Cancel -> {
                    onResetOverlay()

                    showPopupApplicationMenu = false
                }

                Drag.End -> {
                    onResetOverlay()
                }

                else -> Unit
            }
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
        EblanApplicationInfoDockSearchBar(
            currentPage = currentPage,
            onQueryChange = onGetEblanApplicationInfosByLabel,
            eblanApplicationInfosByLabel = eblanApplicationInfosByLabel,
            drag = drag,
            appDrawerSettings = appDrawerSettings,
            iconPackInfoPackageName = iconPackInfoPackageName,
            paddingValues = paddingValues,
            onLongPress = { intOffset, intSize ->
                onUpdateGridItemOffset(intOffset, intSize)

                popupMenuIntOffset = intOffset

                popupMenuIntSize = intSize

                focusManager.clearFocus()
            },
            onLongPressGridItem = onLongPressGridItem,
            onUpdatePopupMenu = {
                showPopupApplicationMenu = true
            },
            onResetOverlay = onResetOverlay,
        )

        if (eblanApplicationInfos.keys.size > 1) {
            EblanApplicationInfoTabRow(
                currentPage = horizontalPagerState.currentPage,
                eblanApplicationInfos = eblanApplicationInfos,
                onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
            )

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = horizontalPagerState,
            ) { index ->
                EblanApplicationInfosPage(
                    index = index,
                    currentPage = currentPage,
                    paddingValues = paddingValues,
                    drag = drag,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    eblanApplicationInfos = eblanApplicationInfos,
                    overscrollOffset = overscrollOffset,
                    overscrollAlpha = overscrollAlpha,
                    onLongPressGridItem = onLongPressGridItem,
                    onAnimateDismiss = onAnimateDismiss,
                    onResetOverlay = onResetOverlay,
                    onFling = onFling,
                    onLongPress = { intOffset, intSize ->
                        onUpdateGridItemOffset(intOffset, intSize)

                        popupMenuIntOffset = intOffset

                        popupMenuIntSize = intSize
                    },
                    onUpdatePopupMenu = {
                        showPopupApplicationMenu = true
                    },
                )
            }
        } else {
            EblanApplicationInfosPage(
                index = 0,
                currentPage = currentPage,
                paddingValues = paddingValues,
                drag = drag,
                appDrawerSettings = appDrawerSettings,
                iconPackInfoPackageName = iconPackInfoPackageName,
                eblanApplicationInfos = eblanApplicationInfos,
                overscrollOffset = overscrollOffset,
                overscrollAlpha = overscrollAlpha,
                onLongPressGridItem = onLongPressGridItem,
                onAnimateDismiss = onAnimateDismiss,
                onResetOverlay = onResetOverlay,
                onFling = onFling,
                onLongPress = { intOffset, intSize ->
                    onUpdateGridItemOffset(intOffset, intSize)

                    popupMenuIntOffset = intOffset

                    popupMenuIntSize = intSize
                },
                onUpdatePopupMenu = {
                    showPopupApplicationMenu = true
                },
            )
        }
    }

    if (showPopupApplicationMenu && gridItemSource?.gridItem != null) {
        PopupApplicationInfoMenu(
            paddingValues = paddingValues,
            popupMenuIntOffset = popupMenuIntOffset,
            gridItem = gridItemSource.gridItem,
            popupMenuIntSize = popupMenuIntSize,
            onDismissRequest = {
                showPopupApplicationMenu = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EblanApplicationInfoDockSearchBar(
    modifier: Modifier = Modifier,
    currentPage: Int,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    onQueryChange: (String) -> Unit,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    iconPackInfoPackageName: String,
    paddingValues: PaddingValues,
    onLongPress: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdatePopupMenu: () -> Unit,
    onResetOverlay: () -> Unit,
) {
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(eblanApplicationInfosByLabel) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    currentPage = currentPage,
                    drag = drag,
                    eblanApplicationInfo = eblanApplicationInfo,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    paddingValues = paddingValues,
                    onLongPress = onLongPress,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdatePopupMenu = onUpdatePopupMenu,
                    onResetOverlay = onResetOverlay,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EblanApplicationInfoTabRow(
    currentPage: Int,
    eblanApplicationInfos: Map<Long, List<EblanApplicationInfo>>,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SecondaryTabRow(selectedTabIndex = currentPage) {
        eblanApplicationInfos.keys.forEachIndexed { index, serialNumber ->
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
private fun EblanApplicationInfosPage(
    modifier: Modifier = Modifier,
    index: Int,
    currentPage: Int,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    iconPackInfoPackageName: String,
    eblanApplicationInfos: Map<Long, List<EblanApplicationInfo>>,
    overscrollOffset: Animatable<Float, AnimationVector1D>,
    overscrollAlpha: Animatable<Float, AnimationVector1D>,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onAnimateDismiss: () -> Unit,
    onResetOverlay: () -> Unit,
    onFling: suspend () -> Unit,
    onLongPress: (IntOffset, IntSize) -> Unit,
    onUpdatePopupMenu: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val overscrollEffect = remember(key1 = scope) {
        OffsetOverscrollEffect(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            overscrollOffset = overscrollOffset,
            overscrollFactor = appDrawerSettings.overscrollFactor,
            onFling = onFling,
            onFastFling = onAnimateDismiss,
        )
    }

    val lazyGridState = rememberLazyGridState()

    val serialNumber = eblanApplicationInfos.keys.toList()[index]

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
            state = lazyGridState,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding(),
            ),
            overscrollEffect = overscrollEffect,
        ) {
            items(eblanApplicationInfos[serialNumber].orEmpty()) { eblanApplicationInfo ->
                EblanApplicationInfoItem(
                    currentPage = currentPage,
                    drag = drag,
                    eblanApplicationInfo = eblanApplicationInfo,
                    appDrawerSettings = appDrawerSettings,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    paddingValues = paddingValues,
                    onLongPress = onLongPress,
                    onLongPressGridItem = onLongPressGridItem,
                    onUpdatePopupMenu = onUpdatePopupMenu,
                    onResetOverlay = onResetOverlay,
                )
            }
        }

        if (!WindowInsets.isImeVisible) {
            ScrollBarThumb(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight(),
                lazyGridState = lazyGridState,
                appDrawerSettings = appDrawerSettings,
                paddingValues = paddingValues,
                eblanApplicationInfos = eblanApplicationInfos[serialNumber].orEmpty(),
                onScrollToItem = lazyGridState::scrollToItem,
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    currentPage: Int,
    drag: Drag,
    eblanApplicationInfo: EblanApplicationInfo,
    appDrawerSettings: AppDrawerSettings,
    iconPackInfoPackageName: String,
    paddingValues: PaddingValues,
    onLongPress: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdatePopupMenu: () -> Unit,
    onResetOverlay: () -> Unit,
) {
    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val textColor = getSystemTextColor(textColor = appDrawerSettings.gridItemSettings.textColor)

    val appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight.dp

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val iconPacksDirectory = File(context.filesDir, FileManager.ICON_PACKS_DIR)

    val iconPackDirectory = File(iconPacksDirectory, iconPackInfoPackageName)

    val iconFile = File(iconPackDirectory, eblanApplicationInfo.packageName)

    val icon = if (iconPackInfoPackageName.isNotEmpty() && iconFile.exists()) {
        iconFile.absolutePath
    } else {
        eblanApplicationInfo.icon
    }

    val horizontalAlignment = when (appDrawerSettings.gridItemSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Alignment.Start
        HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        HorizontalAlignment.End -> Alignment.End
    }

    val verticalArrangement = when (appDrawerSettings.gridItemSettings.verticalArrangement) {
        VerticalArrangement.Top -> Arrangement.Top
        VerticalArrangement.Center -> Arrangement.Center
        VerticalArrangement.Bottom -> Arrangement.Bottom
    }

    var alpha by remember { mutableFloatStateOf(1f) }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

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
                    onTap = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            val sourceBoundsX = intOffset.x + leftPadding

                            val sourceBoundsY = intOffset.y + topPadding

                            launcherApps.startMainActivity(
                                serialNumber = eblanApplicationInfo.serialNumber,
                                componentName = eblanApplicationInfo.componentName,
                                sourceBounds = Rect(
                                    sourceBoundsX,
                                    sourceBoundsY,
                                    sourceBoundsX + intSize.width,
                                    sourceBoundsY + intSize.height,
                                ),
                            )
                        }
                    },
                    onLongPress = {
                        scope.launch {
                            scale.animateTo(0.5f)

                            scale.animateTo(1f)

                            val data =
                                GridItemData.ApplicationInfo(
                                    serialNumber = eblanApplicationInfo.serialNumber,
                                    componentName = eblanApplicationInfo.componentName,
                                    packageName = eblanApplicationInfo.packageName,
                                    icon = eblanApplicationInfo.icon,
                                    label = eblanApplicationInfo.label,
                                )

                            onLongPressGridItem(
                                GridItemSource.New(
                                    gridItem = GridItem(
                                        id = Uuid.random()
                                            .toHexString(),
                                        folderId = null,
                                        page = currentPage,
                                        startColumn = -1,
                                        startRow = -1,
                                        columnSpan = 1,
                                        rowSpan = 1,
                                        data = data,
                                        associate = Associate.Grid,
                                        override = false,
                                        gridItemSettings = appDrawerSettings.gridItemSettings,
                                    ),
                                ),
                                graphicsLayer.toImageBitmap(),
                            )

                            onLongPress(
                                intOffset,
                                intSize,
                            )

                            onUpdatePopupMenu()

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
            .height(appDrawerRowsHeight)
            .alpha(alpha)
            .scale(
                scaleX = scale.value,
                scaleY = scale.value,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .onGloballyPositioned { layoutCoordinates ->
                    intOffset =
                        layoutCoordinates.positionInRoot().round()

                    intSize = layoutCoordinates.size
                }
                .size(appDrawerSettings.gridItemSettings.iconSize.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(icon)
                    .addLastModifiedToFileCacheKey(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
            )

            if (eblanApplicationInfo.serialNumber != 0L) {
                ElevatedCard(
                    modifier = Modifier
                        .size((appDrawerSettings.gridItemSettings.iconSize * 0.40).dp)
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

        if (appDrawerSettings.gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = eblanApplicationInfo.label.toString(),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PopupApplicationInfoMenu(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    popupMenuIntOffset: IntOffset,
    gridItem: GridItem?,
    popupMenuIntSize: IntSize,
    onDismissRequest: () -> Unit,
) {
    val applicationInfo = gridItem?.data as? GridItemData.ApplicationInfo ?: return

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
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
        onDismissRequest = onDismissRequest,
        content = {
            ApplicationInfoMenu(
                modifier = modifier,
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = applicationInfo.serialNumber,
                        componentName = applicationInfo.componentName,
                        sourceBounds = Rect(
                            x,
                            y,
                            x + popupMenuIntSize.width,
                            y + popupMenuIntSize.height,
                        ),
                    )

                    onDismissRequest()
                },
            )
        },
    )
}

@Composable
private fun ScrollBarThumb(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    appDrawerSettings: AppDrawerSettings,
    paddingValues: PaddingValues,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onScrollToItem: suspend (Int) -> Unit,
) {
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val appDrawerRowsHeightPx =
        with(density) {
            appDrawerSettings.appDrawerRowsHeight.dp.roundToPx()
        }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val thumbHeight by remember(lazyGridState) {
        derivedStateOf {
            with(density) {
                (lazyGridState.layoutInfo.viewportSize.height / 4).toDp()
            }
        }
    }

    val viewPortThumbY by remember(key1 = lazyGridState, key2 = appDrawerSettings) {
        derivedStateOf {
            val totalRows =
                (lazyGridState.layoutInfo.totalItemsCount + appDrawerSettings.appDrawerColumns - 1) /
                    appDrawerSettings.appDrawerColumns

            val visibleRows =
                ceil(lazyGridState.layoutInfo.viewportSize.height / appDrawerRowsHeightPx.toFloat()).toInt()

            val scrollableRows = (totalRows - visibleRows).coerceAtLeast(0)

            val availableScroll = scrollableRows * appDrawerRowsHeightPx

            val row = lazyGridState.firstVisibleItemIndex / appDrawerSettings.appDrawerColumns

            val totalScrollY =
                (row * appDrawerRowsHeightPx) + lazyGridState.firstVisibleItemScrollOffset

            val thumbHeightPx = with(density) {
                thumbHeight.toPx()
            }

            val availableHeight =
                (lazyGridState.layoutInfo.viewportSize.height - thumbHeightPx - bottomPadding)
                    .coerceAtLeast(0f)

            if (availableScroll <= 0) {
                0f
            } else {
                (totalScrollY.toFloat() / availableScroll.toFloat() * availableHeight).coerceIn(
                    0f,
                    availableHeight,
                )
            }
        }
    }

    var isDraggingThumb by remember { mutableStateOf(false) }

    var thumbY by remember { mutableFloatStateOf(0f) }

    val thumbAlpha by animateFloatAsState(
        targetValue = if (lazyGridState.isScrollInProgress || isDraggingThumb) 1f else 0.2f,
    )

    val firstVisibleItem by remember(key1 = lazyGridState, key2 = eblanApplicationInfos) {
        derivedStateOf {
            if (isDraggingThumb && lazyGridState.firstVisibleItemIndex in eblanApplicationInfos.indices) {
                eblanApplicationInfos[lazyGridState.firstVisibleItemIndex].label
            } else {
                null
            }
        }
    }

    Row(modifier = modifier) {
        if (isDraggingThumb) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(x = 0, y = thumbY.roundToInt())
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = firstVisibleItem.orEmpty(),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .offset {
                    val y = if (isDraggingThumb) {
                        thumbY
                    } else {
                        viewPortThumbY
                    }

                    IntOffset(0, y.roundToInt())
                }
                .pointerInput(lazyGridState, appDrawerSettings) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            thumbY = viewPortThumbY

                            isDraggingThumb = true
                        },
                        onVerticalDrag = { change, deltaY ->
                            val totalRows =
                                (lazyGridState.layoutInfo.totalItemsCount + appDrawerSettings.appDrawerColumns - 1) /
                                    appDrawerSettings.appDrawerColumns

                            val visibleRows =
                                ceil(lazyGridState.layoutInfo.viewportSize.height / appDrawerRowsHeightPx.toFloat()).toInt()

                            val scrollableRows = (totalRows - visibleRows).coerceAtLeast(0)

                            val availableScroll = scrollableRows * appDrawerRowsHeightPx

                            val thumbHeightPx = with(density) { thumbHeight.toPx() }

                            val availableHeight =
                                lazyGridState.layoutInfo.viewportSize.height - thumbHeightPx - bottomPadding

                            thumbY = (thumbY + deltaY).coerceIn(0f, availableHeight)

                            val progress = thumbY / availableHeight

                            val targetScrollY = progress * availableScroll

                            val targetRow = targetScrollY / appDrawerRowsHeightPx

                            val targetIndex = (targetRow * appDrawerSettings.appDrawerColumns)
                                .roundToInt()
                                .coerceIn(0, eblanApplicationInfos.lastIndex)

                            scope.launch {
                                onScrollToItem(targetIndex)
                            }
                        },
                        onDragEnd = {
                            isDraggingThumb = false
                        },
                        onDragCancel = {
                            isDraggingThumb = false
                        },
                    )
                }
                .alpha(thumbAlpha)
                .size(width = 8.dp, height = thumbHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp),
                ),
        )
    }
}
