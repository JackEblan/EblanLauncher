package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFolderDataByIdUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(id: String): FolderDataById? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val gridItems = (
                    applicationInfoGridItemRepository.gridItems.first() +
                            widgetGridItemRepository.gridItems.first() +
                            shortcutInfoGridItemRepository.gridItems.first() +
                            folderGridItemRepository.gridItems.first() +
                            shortcutConfigGridItemRepository.gridItems.first()
                    ).filter { gridItem ->
                    gridItem.folderId == id && isGridItemSpanWithinBounds(
                        gridItem = gridItem,
                        columns = homeSettings.folderColumns,
                        rows = homeSettings.folderRows,
                    )
                }

            folderGridItemRepository.getFolderGridItemData(id = id)?.let { folderGridItemData ->
                FolderDataById(
                    id = id,
                    label = folderGridItemData.label,
                    gridItems = gridItems,
                    gridItemsByPage = gridItems.groupBy { gridItem -> gridItem.page },
                    pageCount = folderGridItemData.pageCount,
                )
            }
        }
    }
}