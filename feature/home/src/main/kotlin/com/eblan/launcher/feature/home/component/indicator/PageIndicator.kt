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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun PageIndicator(
    modifier: Modifier = Modifier,
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    color: Color,
) {
    val baseWidth = 8.dp
    val baseHeight = 8.dp
    val activeWidth = 16.dp

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (pageCount > 1) {
            repeat(pageCount) { index ->
                val dist by remember(key1 = index) {
                    derivedStateOf {
                        val rel =
                            (gridHorizontalPagerState.currentPage + gridHorizontalPagerState.currentPageOffsetFraction) - index

                        rel.coerceIn(-1f, 1f)
                    }
                }

                val width by remember {
                    derivedStateOf {
                        when (dist) {
                            0f -> activeWidth
                            in -1f..0f -> baseWidth + (activeWidth - baseWidth) * (1f + dist)
                            in 0f..1f -> baseWidth + (activeWidth - baseWidth) * (1f - dist)
                            else -> baseWidth
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .width(width)
                        .height(baseHeight)
                        .clip(CircleShape)
                        .background(
                            color = lerp(
                                start = color.copy(alpha = 0.5f),
                                stop = color,
                                fraction = 1f - abs(dist).coerceIn(0f, 1f),
                            ),
                        ),
                )
            }
        }
    }
}
