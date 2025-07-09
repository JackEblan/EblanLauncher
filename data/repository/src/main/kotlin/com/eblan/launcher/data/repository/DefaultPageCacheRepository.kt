package com.eblan.launcher.data.repository

import com.eblan.launcher.data.cache.PageCacheDataSource
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.PageCacheRepository
import javax.inject.Inject

internal class DefaultPageCacheRepository @Inject constructor(private val pageCacheDataSource: PageCacheDataSource) :
    PageCacheRepository {
    override val pageItems = pageCacheDataSource.pageItems

    override val pageItemsToDelete = pageCacheDataSource.pageItemsToDelete

    override fun insertPageItems(pageItems: List<PageItem>) {
        pageCacheDataSource.insertPageItems(pageItems = pageItems)
    }

    override fun addEmptyPageItem() {
        pageCacheDataSource.addEmptyPageItem()
    }

    override suspend fun deletePageItems(id: Int) {
        pageCacheDataSource.deletePageItems(id = id)
    }
}