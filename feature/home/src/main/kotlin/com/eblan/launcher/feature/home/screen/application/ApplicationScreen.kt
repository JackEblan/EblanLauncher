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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    onEditApplicationInfo: (Long, String) -> Unit,
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
        onEditApplicationInfo = onEditApplicationInfo,
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
    onEditApplicationInfo: (Long, String) -> Unit,
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
                                onEditApplicationInfo = onEditApplicationInfo,
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
    onEditApplicationInfo: (Long, String) -> Unit,
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
            onEditApplicationInfo = onEditApplicationInfo,
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
