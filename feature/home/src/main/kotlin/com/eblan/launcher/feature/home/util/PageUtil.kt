package com.eblan.launcher.feature.home.util

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import kotlinx.coroutines.flow.onStart
import kotlin.math.absoluteValue

fun calculatePage(index: Int, infiniteScroll: Boolean, pageCount: Int): Int {
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