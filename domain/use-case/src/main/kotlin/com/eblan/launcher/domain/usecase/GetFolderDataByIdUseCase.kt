package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFolderDataByIdUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(id: String): FolderDataById? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            folderGridItemRepository.getFolderGridItemData(id = id)?.let { folderGridItemData ->
                val gridItemsSpanWithinBounds = folderGridItemData.gridItems.filter { gridItem ->
                    isGridItemSpanWithinBounds(
                        gridItem = gridItem,
                        rows = homeSettings.folderRows,
                        columns = homeSettings.folderColumns,
                    ) && gridItem.associate == Associate.Grid
                }.groupBy { gridItem -> gridItem.page }

                FolderDataById(
                    id = id,
                    label = folderGridItemData.label,
                    gridItems = folderGridItemData.gridItems,
                    gridItemsByPage = gridItemsSpanWithinBounds,
                    pageCount = folderGridItemData.pageCount,
                )
            }
        }
    }
}