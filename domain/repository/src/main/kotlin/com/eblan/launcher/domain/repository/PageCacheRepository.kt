package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.PageItem
import kotlinx.coroutines.flow.Flow

interface PageCacheRepository {
    val pageItems: Flow<List<PageItem>>

    fun insertPageItems(pageItems: List<PageItem>)
}