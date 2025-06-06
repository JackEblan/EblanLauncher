package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

interface GridCacheRepository {
    val gridCacheItems: Flow<List<GridItem>>

    val isCache: Flow<Boolean>

    suspend fun insertGridItems(gridItems: List<GridItem>)

    suspend fun updateGridItem(id: String, data: GridItemData)

    suspend fun deleteGridItem(gridItem: GridItem)

    suspend fun upsertGridItems(gridItems: List<GridItem>)

    fun updateIsCache(isCache: Boolean)
}