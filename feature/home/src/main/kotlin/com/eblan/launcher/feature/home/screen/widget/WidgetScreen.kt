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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfoByGroup
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.component.overscroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun WidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    isApplicationComponentVisible: Boolean,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    drag: Drag,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoByGroup, List<EblanAppWidgetProviderInfo>>,
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
    eblanAppWidgetProviderInfos: Map<EblanAppWidgetProviderInfoByGroup, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenHeight: Int,
    drag: Drag,
    eblanAppWidgetProviderInfosByLabel: Map<EblanAppWidgetProviderInfoByGroup, List<EblanAppWidgetProviderInfo>>,
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
                        eblanAppWidgetProviderInfoByGroup = eblanApplicationInfo,
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
