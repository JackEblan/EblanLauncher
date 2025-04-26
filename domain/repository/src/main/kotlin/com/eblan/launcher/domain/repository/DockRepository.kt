package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.DockItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

interface DockRepository {
    val dockItems: Flow<List<DockItem>>

    suspend fun upsertDockItems(dockItems: List<DockItem>)

    suspend fun upsertDockItem(dockItem: DockItem): Long

    suspend fun updateGridItemData(id: String, data: GridItemData): Int

    suspend fun getDockItemEntity(id: String): DockItem?

    suspend fun deleteDockItemEntity(dockItem: DockItem)
}