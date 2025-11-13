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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.feature.home.component.scroll.OffsetNestedScrollConnection
import com.eblan.launcher.feature.home.component.scroll.OffsetOverscrollEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EblanApplicationInfosPage(
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
    val density = LocalDensity.current

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

    val canOverscroll by remember(key1 = lazyGridState) {
        derivedStateOf {
            val appDrawerRowsHeightPx =
                with(density) {
                    appDrawerSettings.appDrawerRowsHeight.dp.roundToPx()
                }

            val totalRows =
                (lazyGridState.layoutInfo.totalItemsCount + appDrawerSettings.appDrawerColumns - 1) /
                    appDrawerSettings.appDrawerColumns

            (totalRows * appDrawerRowsHeightPx) > lazyGridState.layoutInfo.viewportSize.height
        }
    }

    val nestedScrollConnection = remember(key1 = scope) {
        OffsetNestedScrollConnection(
            scope = scope,
            overscrollAlpha = overscrollAlpha,
            overscrollOffset = overscrollOffset,
            overscrollFactor = appDrawerSettings.overscrollFactor,
            onFling = onFling,
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
            state = lazyGridState,
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
