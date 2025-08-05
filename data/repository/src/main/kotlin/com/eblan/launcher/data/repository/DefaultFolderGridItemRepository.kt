package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asGridItem
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderGridItemRepository @Inject constructor(private val folderGridItemDao: FolderGridItemDao) :
    FolderGridItemRepository {
    override val folderGridItems =
        folderGridItemDao.getFolderGridItemWrapperEntities().map { entities ->
            entities.map { entity ->
                entity.asGridItem()
            }
        }

    override suspend fun upsertFolderGridItems(folderGridItems: List<FolderGridItem>) {
        val entities = folderGridItems.map { folderGridItem ->
            folderGridItem.asEntity()
        }

        folderGridItemDao.upsertFolderGridItemEntities(entities = entities)
    }

    override suspend fun upsertFolderGridItem(folderGridItem: FolderGridItem): Long {
        return folderGridItemDao.upsertFolderGridItemEntity(entity = folderGridItem.asEntity())
    }

    override suspend fun updateFolderGridItem(folderGridItem: FolderGridItem) {
        folderGridItemDao.updateFolderGridItemEntity(entity = folderGridItem.asEntity())
    }

    override suspend fun deleteFolderGridItem(folderGridItem: FolderGridItem) {
        folderGridItemDao.deleteFolderGridItemEntity(entity = folderGridItem.asEntity())
    }
}