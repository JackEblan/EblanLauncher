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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset

fun handleOnDragEnd(
    density: Density,
    dragIntOffset: IntOffset,
    screenHeight: Int,
    gridPadding: Int,
    pageIndicatorHeight: Int,
    paddingValues: PaddingValues,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val verticalPadding = topPadding + bottomPadding

    val gridHeight = screenHeight - verticalPadding

    val dragY = dragIntOffset.y - topPadding

    val isOnTopGrid = dragY < gridPadding

    val isOnBottomGrid = dragY > gridHeight - pageIndicatorHeight - gridPadding

    val isVerticalBounds = !isOnTopGrid && !isOnBottomGrid

    if (isVerticalBounds) {
        onDragEnd()
    } else {
        onDragCancel()
    }
}
