package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.GridDao
import com.eblan.launcher.data.room.entity.GridItemEntity
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultGridRepository @Inject constructor(private val gridDao: GridDao) : GridRepository {
    override val gridItems = gridDao.getGridItemEntities().map { entities ->
        entities.map { entity ->
            entity.toGridItem()
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        val gridItemEntities = gridItems.map { gridItem ->
            gridItem.toGridItemEntity()
        }

        gridDao.upsertGridItemEntities(entities = gridItemEntities)
    }

    override suspend fun upsertGridItem(gridItem: GridItem) {
        gridDao.upsertGridItemEntity(entity = gridItem.toGridItemEntity())
    }

    override suspend fun updateGridItemData(id: Int, data: GridItemData): Int {
        return gridDao.updateGridItemData(id = id, data = data)
    }

    override suspend fun getGridItem(id: Int): GridItem? {
        return gridDao.getGridItemEntity(id = id)?.toGridItem()
    }

    private fun GridItemEntity.toGridItem(): GridItem {
        return GridItem(id = id, page = page, cells = cells, data = data)
    }

    private fun GridItem.toGridItemEntity(): GridItemEntity {
        return GridItemEntity(id = id, page = page, cells = cells, data = data)
    }
}