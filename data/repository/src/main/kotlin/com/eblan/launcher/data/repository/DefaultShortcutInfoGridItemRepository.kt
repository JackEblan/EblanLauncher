package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asGridItem
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.ShortcutInfoGridItemDao
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultShortcutInfoGridItemRepository @Inject constructor(private val shortcutInfoGridItemDao: ShortcutInfoGridItemDao) :
    ShortcutInfoGridItemRepository {
    override val shortcutInfoGridItems =
        shortcutInfoGridItemDao.getShortcutInfoGridItemEntities().map { entities ->
            entities.filterNot { entity -> entity.folderId == null }
                .map { entity ->
                    entity.asGridItem()
                }
        }

    override suspend fun upsertShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>) {
        val entities = shortcutInfoGridItems.map { shortcutInfoGridItem ->
            shortcutInfoGridItem.asEntity()
        }

        shortcutInfoGridItemDao.upsertShortcutInfoGridItemEntities(entities = entities)
    }

    override suspend fun upsertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem): Long {
        return shortcutInfoGridItemDao.upsertShortcutInfoGridItemEntity(
            shortcutInfoGridItem.asEntity(),
        )
    }

    override suspend fun updateShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) {
        shortcutInfoGridItemDao.updateShortcutInfoGridItemEntity(
            shortcutInfoGridItem.asEntity(),
        )
    }

    override suspend fun getShortcutInfoGridItem(id: String): ShortcutInfoGridItem? {
        return shortcutInfoGridItemDao.getShortcutInfoGridItemEntity(id = id)?.asModel()
    }

    override suspend fun deleteShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>) {
        val entities = shortcutInfoGridItems.map { shortcutInfoGridItem ->
            shortcutInfoGridItem.asEntity()
        }

        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntities(entities = entities)
    }

    override suspend fun deleteShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) {
        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntity(entity = shortcutInfoGridItem.asEntity())
    }
}