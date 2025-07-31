package com.eblan.launcher.data.repository

import com.eblan.launcher.data.cache.GridCacheDataSource
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import javax.inject.Inject

internal class DefaultGridCacheRepository @Inject constructor(private val gridCacheDataSource: GridCacheDataSource) :
    GridCacheRepository {
    override val gridCacheItems = gridCacheDataSource.gridCacheItems

    override val isCache = gridCacheDataSource.isCache

    override fun insertGridItems(gridItems: List<GridItem>) {
        gridCacheDataSource.insertGridItems(gridItems = gridItems)
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        gridCacheDataSource.deleteGridItems(gridItems = gridItems)
    }

    override fun deleteGridItem(gridItem: GridItem) {
        gridCacheDataSource.deleteGridItem(gridItem = gridItem)
    }

    override suspend fun updateGridItemData(id: String, data: GridItemData) {
        gridCacheDataSource.updateGridItemData(id = id, data = data)
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        gridCacheDataSource.upsertGridItems(gridItems = gridItems)
    }

    override fun updateIsCache(isCache: Boolean) {
        gridCacheDataSource.updateIsCache(isCache = isCache)
    }
}