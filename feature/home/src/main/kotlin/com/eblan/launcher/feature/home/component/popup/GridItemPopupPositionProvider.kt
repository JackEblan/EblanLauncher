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
package com.eblan.launcher.feature.home.component.popup

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

internal class GridItemPopupPositionProvider(
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
        val parentCenterX = x + width / 2

        val childXInitial = parentCenterX - popupContentSize.width / 2

        val childX = childXInitial.coerceIn(0, windowSize.width - popupContentSize.width)

        val topPositionY = y - popupContentSize.height

        val bottomPositionY = y + height

        val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY

        val childY = childYInitial.coerceIn(0, windowSize.height - popupContentSize.height)

        return IntOffset(x = childX, y = childY)
    }
}
