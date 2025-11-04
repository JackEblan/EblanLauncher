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
package com.eblan.launcher.feature.home.component.menu

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

class MenuPositionProvider(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val (x, y) = calculateMenuCoordinates(
            x = x,
            y = y,
            width = width,
            height = height,
            windowSize = windowSize,
            popupContentSize = popupContentSize,
        )

        return IntOffset(x = x, y = y)
    }

    private fun calculateMenuCoordinates(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        windowSize: IntSize,
        popupContentSize: IntSize,
    ): Pair<Int, Int> {
        val parentCenterX = x + width / 2
        val childXInitial = parentCenterX - popupContentSize.width / 2
        val childX = childXInitial.coerceIn(0, windowSize.width - popupContentSize.width)

        val topPositionY = y - popupContentSize.height
        val bottomPositionY = y + height

        val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY
        val childY = childYInitial.coerceIn(0, windowSize.height - popupContentSize.height)

        return childX to childY
    }
}
