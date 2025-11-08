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
package com.eblan.launcher.feature.home.screen.drag

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun AnimatedDropGridItem(
    modifier: Modifier = Modifier,
    currentPage: Int,
    gridPadding: Int,
    screenWidth: Int,
    screenHeight: Int,
    dockHeight: Dp,
    pageIndicatorHeight: Int,
    paddingValues: PaddingValues,
    columns: Int,
    rows: Int,
    dockColumns: Int,
    dockRows: Int,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    textColor: TextColor,
    iconPackInfoPackageName: String,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    moveGridItemResult: MoveGridItemResult?,
    gridItemSource: GridItemSource,
    statusBarNotifications: Map<String, Int>,
) {
    if (drag != Drag.End ||
        moveGridItemResult?.isSuccess != true ||
        moveGridItemResult.movingGridItem.page != currentPage ||
        gridItemSource is GridItemSource.Pin
    ) {
        return
    }

    val density = LocalDensity.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val gridLeft = leftPadding + gridPadding

    val gridTop = topPadding + gridPadding

    var targetX: Int

    var targetY: Int

    var targetWidth: Int

    var targetHeight: Int

    val currentGridItemSettings = if (moveGridItemResult.movingGridItem.override) {
        moveGridItemResult.movingGridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val textColor = if (moveGridItemResult.movingGridItem.override) {
        getGridItemTextColor(
            systemTextColor = textColor,
            gridItemTextColor = moveGridItemResult.movingGridItem.gridItemSettings.textColor,
        )
    } else {
        getSystemTextColor(textColor = textColor)
    }

    when (moveGridItemResult.movingGridItem.associate) {
        Associate.Grid -> {
            val gridWidthWithPadding = gridWidth - (gridPadding * 2)

            val gridHeightWithPadding =
                (gridHeight - pageIndicatorHeight - dockHeightPx) - (gridPadding * 2)

            val cellWidth = gridWidthWithPadding / columns

            val cellHeight = gridHeightWithPadding / rows

            targetX = (moveGridItemResult.movingGridItem.startColumn * cellWidth) + gridLeft

            targetY = (moveGridItemResult.movingGridItem.startRow * cellHeight) + gridTop

            targetWidth = moveGridItemResult.movingGridItem.columnSpan * cellWidth

            targetHeight = moveGridItemResult.movingGridItem.rowSpan * cellHeight
        }

        Associate.Dock -> {
            val cellWidth = gridWidth / dockColumns

            val cellHeight = dockHeightPx / dockRows

            targetX =
                (moveGridItemResult.movingGridItem.startColumn * cellWidth) + leftPadding

            targetY =
                (moveGridItemResult.movingGridItem.startRow * cellHeight) + (screenHeight - bottomPadding - dockHeightPx)

            targetWidth = moveGridItemResult.movingGridItem.columnSpan * cellWidth

            targetHeight = moveGridItemResult.movingGridItem.rowSpan * cellHeight
        }
    }

    val animatedX = remember { Animatable(overlayIntOffset.x.toFloat()) }

    val animatedY = remember { Animatable(overlayIntOffset.y.toFloat()) }

    val animatedWidth =
        remember { Animatable(overlayIntSize.width.toFloat()) }

    val animatedHeight =
        remember { Animatable(overlayIntSize.height.toFloat()) }

    val animatedAlpha = remember { Animatable(1f) }

    val gridItemSettingsConverter = TwoWayConverter<GridItemSettings, AnimationVector2D>(
        convertToVector = { settings ->
            AnimationVector2D(
                settings.iconSize.toFloat(),
                settings.textSize.toFloat(),
            )
        },
        convertFromVector = { vector ->
            currentGridItemSettings.copy(
                iconSize = vector.v1.roundToInt(),
                textSize = vector.v2.roundToInt(),
            )
        },
    )

    val animatedGridItemSettings = remember {
        Animatable(currentGridItemSettings, gridItemSettingsConverter)
    }

    LaunchedEffect(key1 = moveGridItemResult.movingGridItem) {
        launch { animatedX.animateTo(targetX.toFloat()) }

        launch { animatedY.animateTo(targetY.toFloat()) }

        launch { animatedWidth.animateTo(targetWidth.toFloat()) }

        launch { animatedHeight.animateTo(targetHeight.toFloat()) }

        launch {
            if (moveGridItemResult.conflictingGridItem != null) {
                animatedAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 250),
                )
            }
        }

        launch {
            if (moveGridItemResult.movingGridItem.associate == Associate.Grid) {
                animatedGridItemSettings.animateTo(
                    currentGridItemSettings.copy(
                        iconSize = currentGridItemSettings.iconSize / 2,
                        textSize = currentGridItemSettings.textSize / 2,
                    ),
                )
            }
        }
    }

    GridItemContent(
        modifier = modifier
            .offset {
                IntOffset(
                    x = animatedX.value.roundToInt(),
                    y = animatedY.value.roundToInt(),
                )
            }
            .alpha(animatedAlpha.value)
            .size(
                with(density) {
                    DpSize(
                        width = animatedWidth.value.toDp(),
                        height = animatedHeight.value.toDp(),
                    )
                },
            ),
        gridItem = moveGridItemResult.movingGridItem,
        textColor = textColor,
        gridItemSettings = animatedGridItemSettings.value,
        iconPackInfoPackageName = iconPackInfoPackageName,
        isDragging = false,
        hasShortcutHostPermission = hasShortcutHostPermission,
        statusBarNotifications = statusBarNotifications,
    )
}
