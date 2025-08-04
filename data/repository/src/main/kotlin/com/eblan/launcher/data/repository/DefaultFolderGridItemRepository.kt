package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asModel
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
                entity.asModel()
            }
        }

    override suspend fun upsertFolderGridItem(folderGridItems: List<FolderGridItem>) {
        val entities = folderGridItems.map { folderGridItem ->
            folderGridItem.asEntity()
        }

        folderGridItemDao.upsertFolderGridItemEntities(entities = entities)
    }
}