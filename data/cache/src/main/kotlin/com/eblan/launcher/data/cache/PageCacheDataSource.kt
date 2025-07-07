package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.PageItem
import kotlinx.coroutines.flow.StateFlow

interface PageCacheDataSource {
    val pageItems: StateFlow<List<PageItem>>

    fun insertPageItems(pageItems: List<PageItem>)
}