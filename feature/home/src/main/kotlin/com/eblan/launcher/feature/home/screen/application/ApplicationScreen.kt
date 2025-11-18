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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import kotlin.math.roundToInt

@Composable
internal fun ApplicationScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    offsetY: Animatable<Float, AnimationVector1D>,
    isApplicationComponentVisible: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    paddingValues: PaddingValues,
    drag: Drag,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfosByLabel: List<EblanApplicationInfo>,
    gridItemSource: GridItemSource?,
    iconPackInfoPackageName: String,
    screenHeight: Int,
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
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
) {
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
                                offsetY = offsetY,
                                onLongPressGridItem = onLongPressGridItem,
                                onUpdateGridItemOffset = onUpdateGridItemOffset,
                                onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                                onDismiss = onDismiss,
                                onDraggingGridItem = onDraggingGridItem,
                                onResetOverlay = onResetOverlay,
                                onVerticalDrag = onVerticalDrag,
                                onDragEnd = onDragEnd,
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
    offsetY: Animatable<Float, AnimationVector1D>,
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
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
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

        onDismiss()
    }

    LaunchedEffect(key1 = drag) {
        if (isApplicationComponentVisible && showPopupApplicationMenu) {
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
                    offsetY = offsetY,
                    onLongPressGridItem = onLongPressGridItem,
                    onResetOverlay = onResetOverlay,
                    onLongPress = { intOffset, intSize ->
                        onUpdateGridItemOffset(intOffset, intSize)

                        popupMenuIntOffset = intOffset

                        popupMenuIntSize = intSize
                    },
                    onUpdatePopupMenu = {
                        showPopupApplicationMenu = true
                    },
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
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
                offsetY = offsetY,
                onLongPressGridItem = onLongPressGridItem,
                onResetOverlay = onResetOverlay,
                onLongPress = { intOffset, intSize ->
                    onUpdateGridItemOffset(intOffset, intSize)

                    popupMenuIntOffset = intOffset

                    popupMenuIntSize = intSize
                },
                onUpdatePopupMenu = {
                    showPopupApplicationMenu = true
                },
                onVerticalDrag = onVerticalDrag,
                onDragEnd = onDragEnd,
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
