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
package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun AnimatedDropGridItem(
    modifier: Modifier = Modifier,
    targetPage: Int,
    gridPadding: Int,
    screenWidth: Int,
    screenHeight: Int,
    pageIndicatorHeight: Int,
    paddingValues: PaddingValues,
    columns: Int,
    rows: Int,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    textColor: TextColor,
    iconPackInfoPackageName: String,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    moveGridItemResult: MoveGridItemResult?,
    folderDataById: FolderDataById?,
    statusBarNotifications: Map<String, Int>,
) {
    if (drag != Drag.End ||
        moveGridItemResult?.isSuccess != true ||
        moveGridItemResult.movingGridItem.page != targetPage ||
        folderDataById == null
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

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val gridLeft = leftPadding + gridPadding

    val gridTop = topPadding + gridPadding

    val gridWidthWithPadding = gridWidth - (gridPadding * 2)

    val gridHeightWithPadding = gridHeight - pageIndicatorHeight - (gridPadding * 2)

    val cellWidth = gridWidthWithPadding / columns

    val cellHeight = gridHeightWithPadding / rows

    val targetX = (moveGridItemResult.movingGridItem.startColumn * cellWidth) + gridLeft

    val targetY = (moveGridItemResult.movingGridItem.startRow * cellHeight) + gridTop

    val targetWidth = moveGridItemResult.movingGridItem.columnSpan * cellWidth

    val targetHeight = moveGridItemResult.movingGridItem.rowSpan * cellHeight

    val animatedX = remember { Animatable(overlayIntOffset.x.toFloat()) }

    val animatedY = remember { Animatable(overlayIntOffset.y.toFloat()) }

    val animatedWidth =
        remember { Animatable(overlayIntSize.width.toFloat()) }

    val animatedHeight =
        remember { Animatable(overlayIntSize.height.toFloat()) }

    val animatedAlpha = remember { Animatable(1f) }

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
            animatedGridItemSettings.animateTo(
                currentGridItemSettings.copy(
                    iconSize = currentGridItemSettings.iconSize / 2,
                    textSize = currentGridItemSettings.textSize / 2,
                ),
            )
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
