package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.ApplicationInfoGridItemDao
import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultApplicationInfoGridItemRepository @Inject constructor(private val applicationInfoGridItemDao: ApplicationInfoGridItemDao) :
    ApplicationInfoGridItemRepository {
    override val applicationInfoGridItems =
        applicationInfoGridItemDao.getApplicationInfoGridItemEntities().map { entities ->
            entities.map { entity ->
                entity.asGridItem()
            }
        }

    override suspend fun upsertApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>) {
        val entities = applicationInfoGridItems.map { applicationInfoGridItem ->
            applicationInfoGridItem.asEntity()
        }

        applicationInfoGridItemDao.upsertApplicationInfoGridItemEntities(entities = entities)
    }

    override suspend fun upsertApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem): Long {
        return applicationInfoGridItemDao.upsertApplicationInfoGridItemEntity(
            applicationInfoGridItem.asEntity(),
        )
    }

    override suspend fun updateApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem) {
        applicationInfoGridItemDao.updateApplicationInfoGridItemEntity(
            applicationInfoGridItem.asEntity(),
        )
    }

    override suspend fun getApplicationInfoGridItem(id: String): ApplicationInfoGridItem? {
        return applicationInfoGridItemDao.getApplicationInfoGridItemEntity(id = id)?.asModel()
    }

    override suspend fun deleteApplicationInfoGridItems(applicationInfoGridItems: List<ApplicationInfoGridItem>) {
        val entities = applicationInfoGridItems.map { applicationInfoGridItem ->
            applicationInfoGridItem.asEntity()
        }

        applicationInfoGridItemDao.deleteApplicationInfoGridItemEntities(entities = entities)
    }

    override suspend fun deleteApplicationInfoGridItem(applicationInfoGridItem: ApplicationInfoGridItem) {
        applicationInfoGridItemDao.deleteApplicationInfoGridItemEntity(entity = applicationInfoGridItem.asEntity())
    }

    private fun ApplicationInfoGridItemEntity.asGridItem(): GridItem {
        return GridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = GridItemData.ApplicationInfo(
                componentName = componentName,
                packageName = packageName,
                icon = icon,
                label = label,
            ),
            associate = associate,
        )
    }

    private fun ApplicationInfoGridItemEntity.asModel(): ApplicationInfoGridItem {
        return ApplicationInfoGridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }

    private fun ApplicationInfoGridItem.asEntity(): ApplicationInfoGridItemEntity {
        return ApplicationInfoGridItemEntity(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }
}