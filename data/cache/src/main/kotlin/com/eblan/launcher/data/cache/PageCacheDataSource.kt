package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.PageItem
import kotlinx.coroutines.flow.Flow

interface PageCacheDataSource {
    val pageItems: Flow<List<PageItem>>

    val pageItemsToDelete: Flow<List<PageItem>>

    fun insertPageItems(pageItems: List<PageItem>)

    fun addEmptyPageItem()

    suspend fun deletePageItems(id: Int)
}