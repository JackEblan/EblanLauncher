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
package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor

@Composable
internal fun Chevron(
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings,
    gridItems: List<GridItem>,
    dockGridItems: List<GridItem>,
    swipeY: Float,
    screenHeight: Int,
    textColor: TextColor,
) {
    val visible =
        (gestureSettings.swipeUp is GestureAction.OpenAppDrawer || gestureSettings.swipeDown is GestureAction.OpenAppDrawer) &&
            gridItems.isEmpty() &&
            dockGridItems.isEmpty() &&
            swipeY == screenHeight.toFloat()

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
    ) {
        Chevron(
            color = getSystemTextColor(textColor = textColor),
        )
    }
}

@Composable
internal fun Chevron(
    modifier: Modifier = Modifier,
    color: Color,
    count: Int = 3,
) {
    val topY = 105f // Y position of the topmost (lightest) chevron
    val spacing = 73f // Vertical gap between chevrons
    val delta = 40f // Distance each chevron moves upward
    val durationMs = 1100 // Total cycle duration
    val basePeakTimeMs = 533 // When the first chevron reaches peak (tuned from original)
    val staggerPerBarMs = 33 // Time offset between consecutive bars
    val returnDurationMs = 467 // Time from peak back to rest (1000-533 ≈ 467)
    val infiniteTransition = rememberInfiniteTransition(label = "Chevron")

    val chevrons = List(count) { index ->
        val restY = topY + index * spacing
        val delayMs = index * staggerPerBarMs
        val peakAtMs = basePeakTimeMs + delayMs
        val returnAtMs = peakAtMs + returnDurationMs

        infiniteTransition.animateFloat(
            initialValue = restY,
            targetValue = restY,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = durationMs
                    restY at 0
                    restY - delta at peakAtMs
                    restY at returnAtMs.coerceAtMost(durationMs - 1)
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "ChevronY_$index",
        )
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val chevronSize = 50f

        chevrons.forEachIndexed { index, y ->
            val alpha = 1f - (index.toFloat() / count) * 0.5f

            val path = Path().apply {
                moveTo(centerX - chevronSize, y.value + chevronSize / 2)
                lineTo(centerX, y.value - chevronSize / 2)
                lineTo(centerX + chevronSize, y.value + chevronSize / 2)
            }

            drawPath(
                path = path,
                color = color.copy(alpha = alpha),
                style = Stroke(width = 30f, cap = StrokeCap.Round),
            )
        }
    }
}
