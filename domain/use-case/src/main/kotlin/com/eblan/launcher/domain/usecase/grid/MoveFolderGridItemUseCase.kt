package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveFolderGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        folderGridItem: GridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        withContext(defaultDispatcher) {
            val cellWidth = gridWidth / columns
            val cellHeight = gridHeight / rows

            val targetColumn = dragX / cellWidth
            val targetRow = dragY / cellHeight

            val targetIndex = (targetRow * columns + targetColumn).coerceIn(0, applicationInfoGridItems.lastIndex)

            val fromIndex =
                applicationInfoGridItems.indexOfFirst { it.id == movingApplicationInfoGridItem.id }

            val gridItems = applicationInfoGridItems.toMutableList().apply {
                add(
                    index = targetIndex,
                    element = removeAt(fromIndex),
                )
            }.mapIndexed { index, applicationInfoGridItem ->
                applicationInfoGridItem.copy(index = index)
            }

            val folderGridItemData = folderGridItem.data as? GridItemData.Folder
                ?: error("Expected GridItemData.Folder")

            val newData = folderGridItemData.copy(
                gridItems = gridItems,
                gridItemsByPage = gridItems.getGridItemsByPage(),
            )

            gridCacheRepository.updateGridItemData(
                id = folderGridItem.id,
                data = newData,
            )
        }
    }
}