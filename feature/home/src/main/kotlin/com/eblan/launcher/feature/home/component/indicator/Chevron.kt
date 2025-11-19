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
package com.eblan.launcher.feature.home.component.indicator

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
internal fun Chevron(
    modifier: Modifier = Modifier,
    color: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chevron")

    val topY by infiniteTransition.animateFloat(
        initialValue = 105f,
        targetValue = 105f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                105f at 0
                65f at 533
                105f at 1000
            },
            repeatMode = RepeatMode.Restart,
        ),
    )

    val middleY by infiniteTransition.animateFloat(
        initialValue = 178f,
        targetValue = 178f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                178f at 0
                138f at 566
                178f at 1067
            },
            repeatMode = RepeatMode.Restart,
        ),
    )

    val bottomY by infiniteTransition.animateFloat(
        initialValue = 251f,
        targetValue = 251f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                251f at 0
                211f at 600
                251f at 1133
            },
            repeatMode = RepeatMode.Restart,
        ),
    )

    Canvas(modifier = modifier) {
        fun drawChevronUp(
            y: Float,
            color: Color,
            alpha: Float,
        ) {
            val centerX = size.width / 2

            val path = Path().apply {
                val size = 50f
                moveTo(centerX - size, y + size / 2)
                lineTo(centerX, y - size / 2)
                lineTo(centerX + size, y + size / 2)
            }

            drawPath(
                path = path,
                color = color.copy(alpha = alpha),
                style = Stroke(width = 30f, cap = StrokeCap.Round),
            )
        }

        drawChevronUp(bottomY, color = color, alpha = 1.00f)

        drawChevronUp(middleY, color = color, alpha = 0.80f)

        drawChevronUp(topY, color = color, alpha = 0.50f)
    }
}
