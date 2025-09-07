package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCacheType
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

interface GridCacheRepository {
    val gridItemsCache: Flow<List<GridItem>>

    val gridItemCacheType: Flow<GridItemCacheType>

    fun insertGridItems(gridItems: List<GridItem>)

    fun insertGridItem(gridItem: GridItem)

    suspend fun deleteGridItems(gridItems: List<GridItem>)

    fun deleteGridItem(gridItem: GridItem)

    suspend fun updateGridItemData(id: String, data: GridItemData)

    suspend fun upsertGridItems(gridItems: List<GridItem>)

    fun updateGridItemCacheType(gridItemCacheType: GridItemCacheType)
}