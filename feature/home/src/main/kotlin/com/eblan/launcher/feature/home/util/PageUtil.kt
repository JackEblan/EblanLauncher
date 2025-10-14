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
package com.eblan.launcher.feature.home.util

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import kotlinx.coroutines.flow.onStart
import kotlin.math.absoluteValue

fun calculatePage(
    index: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
): Int {
    return if (infiniteScroll) {
        val offsetIndex = index - (Int.MAX_VALUE / 2)
        offsetIndex - offsetIndex.floorDiv(pageCount) * pageCount
    } else {
        index
    }
}

suspend fun handleWallpaperScroll(
    horizontalPagerState: PagerState,
    wallpaperScroll: Boolean,
    wallpaperManagerWrapper: AndroidWallpaperManagerWrapper,
    pageCount: Int,
    infiniteScroll: Boolean,
    windowToken: android.os.IBinder,
) {
    if (!wallpaperScroll) return

    var reverseXOffset: Float

    snapshotFlow { horizontalPagerState.currentPageOffsetFraction }.onStart {
        wallpaperManagerWrapper.setWallpaperOffsetSteps(
            xStep = 1f / (pageCount - 1),
            yStep = 1f,
        )
    }.collect { offsetFraction ->
        val page = calculatePage(
            index = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        val scrollProgress = page + offsetFraction

        if (scrollProgress < 0f) {
            reverseXOffset = offsetFraction.absoluteValue

            wallpaperManagerWrapper.setWallpaperOffsets(
                windowToken = windowToken,
                xOffset = reverseXOffset,
                yOffset = 0f,
            )
        } else if (scrollProgress > pageCount - 1) {
            reverseXOffset = 1f - offsetFraction

            wallpaperManagerWrapper.setWallpaperOffsets(
                windowToken = windowToken,
                xOffset = reverseXOffset,
                yOffset = 0f,
            )
        } else {
            val xOffset = scrollProgress / (pageCount - 1)

            wallpaperManagerWrapper.setWallpaperOffsets(
                windowToken = windowToken,
                xOffset = xOffset,
                yOffset = 0f,
            )
        }

        if (offsetFraction == 0f) {
            reverseXOffset = offsetFraction
        }
    }
}
