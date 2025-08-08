package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.flow.Flow

interface FolderGridItemRepository {
    val folderGridItems: Flow<List<GridItem>>

    suspend fun getFolderGridItemData(id: String): GridItemData.Folder?

    suspend fun upsertFolderGridItems(folderGridItems: List<FolderGridItem>)

    suspend fun upsertFolderGridItem(folderGridItem: FolderGridItem): Long

    suspend fun updateFolderGridItem(folderGridItem: FolderGridItem)

    suspend fun deleteFolderGridItem(folderGridItem: FolderGridItem)
}