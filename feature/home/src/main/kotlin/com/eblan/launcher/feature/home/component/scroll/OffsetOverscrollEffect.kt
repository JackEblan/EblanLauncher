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
package com.eblan.launcher.feature.home.component.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.sign

internal class OffsetOverscrollEffect(
    private val offsetY: Animatable<Float, AnimationVector1D>,
    private val onVerticalDrag: (Float) -> Unit,
    private val onDragEnd: (Float) -> Unit,
) : OverscrollEffect {
    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        val sameDirection = sign(delta.y) == sign(offsetY.value)

        val consumedByPreScroll = if (abs(offsetY.value) > 0.5 && !sameDirection) {
            val prevOverscrollValue = offsetY.value

            val newOverscrollValue = offsetY.value + delta.y

            if (sign(prevOverscrollValue) != sign(newOverscrollValue)) {
                onVerticalDrag(0f)

                Offset(x = 0f, y = delta.y + prevOverscrollValue)
            } else {
                onVerticalDrag(delta.y)

                delta.copy(x = 0f)
            }
        } else {
            Offset.Zero
        }

        val leftForScroll = delta - consumedByPreScroll

        val consumedByScroll = performScroll(leftForScroll)

        val overscrollDelta = leftForScroll - consumedByScroll

        if (abs(overscrollDelta.y) > 0.5 && source == NestedScrollSource.UserInput) {
            onVerticalDrag(overscrollDelta.y)
        }

        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        val remaining = velocity - performFling(velocity)

        onDragEnd(remaining.y)
    }

    override val isInProgress: Boolean
        get() = offsetY.value != 0f

    override val node: DelegatableNode
        get() = super.node
}
