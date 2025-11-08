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
package com.eblan.launcher.feature.home.component.overscroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

internal class OffsetOverscrollEffect(
    private val scope: CoroutineScope,
    private val overscrollAlpha: Animatable<Float, AnimationVector1D>,
    private val overscrollOffset: Animatable<Float, AnimationVector1D>,
    private val overscrollFactor: Float,
    private val onFling: suspend () -> Unit,
    private val onFastFling: suspend () -> Unit,
) : OverscrollEffect {
    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        val sameDirection = sign(delta.y) == sign(overscrollOffset.value)

        val consumedByPreScroll = if (abs(overscrollOffset.value) > 0.5 && !sameDirection) {
            val prevOverscrollValue = overscrollOffset.value

            val newOverscrollValue = overscrollOffset.value + (delta.y * overscrollFactor)

            if (sign(prevOverscrollValue) != sign(newOverscrollValue)) {
                scope.launch {
                    overscrollOffset.snapTo(0f)

                    overscrollAlpha.snapTo(0f)
                }

                Offset(x = 0f, y = delta.y + prevOverscrollValue)
            } else {
                scope.launch {
                    overscrollOffset.snapTo(newOverscrollValue)

                    overscrollAlpha.snapTo(newOverscrollValue)
                }

                delta.copy(x = 0f)
            }
        } else {
            Offset.Zero
        }

        val leftForScroll = delta - consumedByPreScroll

        val consumedByScroll = performScroll(leftForScroll)

        val overscrollDelta = leftForScroll - consumedByScroll

        if (abs(overscrollDelta.y) > 0.5 && source == NestedScrollSource.UserInput) {
            scope.launch {
                val newOverscrollValue = overscrollOffset.value + (overscrollDelta.y * overscrollFactor)

                overscrollOffset.snapTo(newOverscrollValue)

                if (overscrollOffset.value > 0f) {
                    overscrollAlpha.snapTo(newOverscrollValue)
                } else {
                    overscrollAlpha.snapTo(0f)
                }
            }
        }

        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        val consumed = performFling(velocity)

        val remaining = velocity - consumed

        overscrollAlpha.snapTo(0f)

        if (overscrollOffset.value <= 0f && remaining.y > 10000f) {
            overscrollOffset.snapTo(0f)

            onFastFling()
        } else if (overscrollOffset.value > 500f) {
            overscrollOffset.snapTo(0f)

            onFling()
        } else {
            overscrollOffset.animateTo(
                targetValue = 0f,
                initialVelocity = remaining.y,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    override val isInProgress: Boolean
        get() = overscrollOffset.value != 0f

    override val node: DelegatableNode
        get() = super.node
}
