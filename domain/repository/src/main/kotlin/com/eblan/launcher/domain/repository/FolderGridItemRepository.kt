package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.flow.Flow

interface FolderGridItemRepository {
    val folderGridItems: Flow<List<GridItem>>
}