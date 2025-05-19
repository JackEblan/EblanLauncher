package com.eblan.launcher.feature.home.util

fun calculatePage(index: Int, infiniteScroll: Boolean, pageCount: Int): Int {
    return if (infiniteScroll) {
        val offsetIndex = index - (Int.MAX_VALUE / 2)
        offsetIndex - offsetIndex.floorDiv(pageCount) * pageCount
    } else {
        index
    }
}