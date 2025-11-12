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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Velocity

internal suspend fun handleOnFling(
    remaining: Velocity,
    overscrollAlpha: Animatable<Float, AnimationVector1D>,
    overscrollOffset: Animatable<Float, AnimationVector1D>,
    onFastFling: suspend () -> Unit,
    onFling: suspend () -> Unit,
) {
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
