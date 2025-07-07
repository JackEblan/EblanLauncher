package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.PageItem
import kotlinx.coroutines.flow.StateFlow

interface PageCacheRepository {
    val pageItems: StateFlow<List<PageItem>>

    fun insertPageItems(pageItems: List<PageItem>)
}