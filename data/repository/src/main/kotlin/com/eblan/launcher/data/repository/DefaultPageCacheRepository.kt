package com.eblan.launcher.data.repository

import com.eblan.launcher.data.cache.PageCacheDataSource
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.PageCacheRepository
import javax.inject.Inject

internal class DefaultPageCacheRepository @Inject constructor(private val pageCacheDataSource: PageCacheDataSource) :
    PageCacheRepository {
    override val pageItems = pageCacheDataSource.pageItems

    override fun insertPageItems(pageItems: List<PageItem>) {
        pageCacheDataSource.insertPageItems(pageItems = pageItems)
    }
}