package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.flow.Flow

interface GridCacheRepository {
    val gridCacheItems: Flow<List<GridItem>>

    val isCache: Flow<Boolean>

    fun insertGridItems(gridItems: List<GridItem>)

    fun deleteGridItem(gridItem: GridItem)

    suspend fun upsertGridItems(gridItems: List<GridItem>)

    fun updateIsCache(isCache: Boolean)

    suspend fun updateWidgetGridItemData(id: Int, appWidgetId: Int)

    suspend fun updateShortcutGridItemData(id: Int, icon: String?)
}