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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.pager.handleApplyFling
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.AppWidgetScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup?,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    drag: Drag,
    screenHeight: Int,
    isPressHome: Boolean,
    screen: Screen,
    onLongPressGridItem: (
        gridItemSource: GridItemSource,
        imageBitmap: ImageBitmap?,
    ) -> Unit,
    onUpdateGridItemOffset: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onDismiss: () -> Unit,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var isDismiss by remember { mutableStateOf(false) }

    var swipeYTarget by remember { mutableFloatStateOf(screenHeight.toFloat()) }

    val swipeY by animateFloatAsState(targetValue = swipeYTarget)

    LaunchedEffect(key1 = swipeY) {
        if (swipeY == screenHeight.toFloat()) {
            if (isDismiss) {
                onDismiss()
            } else {
                swipeYTarget = 0f
            }
        }
    }

    LaunchedEffect(key1 = isPressHome) {
        if (isPressHome) {
            swipeYTarget = screenHeight.toFloat()

            isDismiss = true
        }
    }

    BackHandler {
        swipeYTarget = screenHeight.toFloat()

        isDismiss = true
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = swipeY.roundToInt())
            }
            .pointerInput(key1 = Unit) {
                detectTapGestures(onTap = {
                    scope.launch {
                        swipeYTarget = screenHeight.toFloat()

                        onDismiss()
                    }
                })
            }
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
        ) {
            if (eblanApplicationInfoGroup != null) {
                Success(
                    modifier = Modifier
                        .pointerInput(key1 = Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    swipeYTarget += dragAmount
                                },
                                onDragEnd = {
                                    scope.launch {
                                        handleApplyFling(
                                            swipeY = swipeY,
                                            remaining = 0f,
                                            screenHeight = screenHeight,
                                            onDismiss = {
                                                isDismiss = true
                                            },
                                            onChangeTargetValue = { swipeY ->
                                                swipeYTarget = swipeY
                                            },
                                        )
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        handleApplyFling(
                                            swipeY = swipeY,
                                            remaining = 0f,
                                            screenHeight = screenHeight,
                                            onChangeTargetValue = { swipeY ->
                                                swipeYTarget = swipeY
                                            },
                                        )
                                    }
                                },
                            )
                        }
                        .fillMaxWidth()
                        .padding(paddingValues),
                    eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos[eblanApplicationInfoGroup.packageName].orEmpty(),
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    screen = screen,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    drag: Drag,
    onUpdateGridItemOffset: (IntOffset, IntSize) -> Unit,
    onLongPressGridItem: (GridItemSource, ImageBitmap?) -> Unit,
    currentPage: Int,
    gridItemSettings: GridItemSettings,
    screen: Screen,
    onDraggingGridItem: () -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = eblanApplicationInfoGroup.icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(text = eblanApplicationInfoGroup.label.toString())

        Spacer(modifier = Modifier.height(5.dp))

        LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            items(eblanAppWidgetProviderInfos) { eblanAppWidgetProviderInfo ->
                EblanAppWidgetProviderInfoItem(
                    eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                    drag = drag,
                    onUpdateGridItemOffset = onUpdateGridItemOffset,
                    onLongPressGridItem = onLongPressGridItem,
                    currentPage = currentPage,
                    gridItemSettings = gridItemSettings,
                    screen = screen,
                    onDraggingGridItem = onDraggingGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }
    }
}
