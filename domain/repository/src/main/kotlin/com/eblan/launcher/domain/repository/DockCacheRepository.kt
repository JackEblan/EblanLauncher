package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.DockItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.SharedFlow

interface DockCacheRepository {
    val dockCacheItems: SharedFlow<List<DockItem>>

    suspend fun insertDockItems(dockItems: List<DockItem>)

    suspend fun insertDockItem(dockItem: DockItem)

    suspend fun updateDockItem(id: String, data: GridItemData)

    suspend fun deleteDockItem(dockItem: DockItem)

    suspend fun upsertDockItems(dockItems: List<DockItem>)
}