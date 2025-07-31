package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

interface GridCacheDataSource {
    val gridCacheItems: Flow<List<GridItem>>

    val isCache: Flow<Boolean>

    fun insertGridItems(gridItems: List<GridItem>)

    suspend fun deleteGridItems(gridItems: List<GridItem>)

    fun deleteGridItem(gridItem: GridItem)

    suspend fun updateGridItemData(id: String, data: GridItemData)

    suspend fun upsertGridItems(gridItems: List<GridItem>)

    fun updateIsCache(isCache: Boolean)
}