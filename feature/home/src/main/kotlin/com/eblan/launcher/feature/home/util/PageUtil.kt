package com.eblan.launcher.feature.home.util

fun calculatePage(index: Int, infiniteScroll: Boolean, pageCount: Int): Int {
    return if (infiniteScroll) {
        val offsetIndex = index - (Int.MAX_VALUE / 2)
        offsetIndex - offsetIndex.floorDiv(pageCount) * pageCount
    } else {
        index
    }
}

fun calculateTargetPage(
    currentPage: Int,
    index: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
): Int {
    return if (infiniteScroll) {
        val offset = currentPage - (Int.MAX_VALUE / 2)
        val currentReal = offset - Math.floorDiv(
            offset,
            pageCount,
        ) * pageCount
        val delta = index - currentReal
        currentPage + delta
    } else {
        index
    }
}
