package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.DockDao
import com.eblan.launcher.data.room.entity.DockItemEntity
import com.eblan.launcher.domain.model.DockItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.DockRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultDockRepository @Inject constructor(private val dockDao: DockDao) :
    DockRepository {
    override val dockItems = dockDao.getGridItemEntities().map { entities ->
        entities.map { entity ->
            entity.toDockItem()
        }
    }

    override suspend fun upsertDockItems(dockItems: List<DockItem>) {
        val dockItemEntities = dockItems.map { dockItem ->
            dockItem.toDockItemEntity()
        }
        dockDao.upsertDockItemEntities(entities = dockItemEntities)
    }

    override suspend fun upsertDockItem(dockItem: DockItem): Long {
        return dockDao.upsertDockItemEntity(entity = dockItem.toDockItemEntity())
    }

    override suspend fun updateGridItemData(id: String, data: GridItemData): Int {
        return dockDao.updateGridItemData(id = id, data = data)
    }

    override suspend fun getDockItemEntity(id: String): DockItem? {
        return dockDao.getDockItemEntity(id = id)?.toDockItem()
    }

    override suspend fun deleteDockItemEntity(dockItem: DockItem) {
        dockDao.deleteDockItemEntity(entity = dockItem.toDockItemEntity())
    }

    private fun DockItemEntity.toDockItem(): DockItem {
        return DockItem(
            id = id,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = data,
        )
    }

    private fun DockItem.toDockItemEntity(): DockItemEntity {
        return DockItemEntity(
            id = id,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = data,
        )
    }
}