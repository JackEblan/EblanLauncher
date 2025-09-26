package com.eblan.launcher.feature.home.component.pager

import androidx.annotation.FloatRange
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberHomePagerState(
    infiniteScroll: Boolean,
    initialPage: Int = 0,
    @FloatRange(from = -0.5, to = 0.5) initialPageOffsetFraction: Float = 0f,
    pageCount: () -> Int
): PagerState {
    return rememberSaveable(
        infiniteScroll,
        saver = DefaultPagerState.Saver
    ) {
        DefaultPagerState(
            currentPage = initialPage,
            currentPageOffsetFraction = initialPageOffsetFraction,
            updatedPageCount = pageCount
        )
    }
        .apply { pageCountState.value = pageCount }
}

private class DefaultPagerState(
    currentPage: Int,
    currentPageOffsetFraction: Float,
    updatedPageCount: () -> Int
) : PagerState(currentPage, currentPageOffsetFraction) {

    var pageCountState = mutableStateOf(updatedPageCount)
    override val pageCount: Int
        get() = pageCountState.value.invoke()

    companion object {
        /** To keep current page and current page offset saved */
        val Saver: Saver<DefaultPagerState, *> =
            listSaver(
                save = {
                    listOf(
                        it.currentPage,
                        (it.currentPageOffsetFraction).coerceIn(-0.5f, 0.5f),
                        it.pageCount
                    )
                },
                restore = {
                    DefaultPagerState(
                        currentPage = it[0] as Int,
                        currentPageOffsetFraction = it[1] as Float,
                        updatedPageCount = { it[2] as Int }
                    )
                }
            )
    }
}