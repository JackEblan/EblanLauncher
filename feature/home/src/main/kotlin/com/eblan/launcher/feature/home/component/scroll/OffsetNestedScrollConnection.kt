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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class OffsetNestedScrollConnection(
    private val scope: CoroutineScope,
    private val overscrollAlpha: Animatable<Float, AnimationVector1D>,
    private val overscrollOffset: Animatable<Float, AnimationVector1D>,
    private val overscrollFactor: Float,
    private val onFling: suspend () -> Unit,
    private val onFastFling: suspend () -> Unit,
) : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        scope.launch {
            if (available.y > 0) {
                val newOverscrollValue =
                    overscrollOffset.value + (available.y * overscrollFactor)

                overscrollOffset.snapTo(newOverscrollValue)

                overscrollAlpha.snapTo(newOverscrollValue)
            } else {
                val newOverscrollValue =
                    overscrollOffset.value + (available.y * overscrollFactor)

                overscrollOffset.snapTo(newOverscrollValue)

                overscrollAlpha.snapTo(newOverscrollValue)
            }
        }

        return super.onPostScroll(consumed, available, source)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        handleOnFling(
            remaining = available - consumed,
            overscrollAlpha = overscrollAlpha,
            overscrollOffset = overscrollOffset,
            onFastFling = onFastFling,
            onFling = onFling,
        )

        return super.onPostFling(consumed, available)
    }
}
