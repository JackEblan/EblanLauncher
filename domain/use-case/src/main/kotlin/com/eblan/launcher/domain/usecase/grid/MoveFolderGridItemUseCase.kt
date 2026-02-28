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

            val targetColumn = (dragX / cellWidth).coerceIn(0, columns - 1)
            val targetRow = (dragY / cellHeight).coerceIn(0, rows - 1)

            val maxIndex = applicationInfoGridItems.size - 1
            val targetIndex = (targetRow * columns + targetColumn).coerceIn(0, maxIndex)

            val fromIndex = movingApplicationInfoGridItem.index

            val gridItems = buildList(applicationInfoGridItems.size) {
                for (applicationInfoGridItem in applicationInfoGridItems) {
                    when {
                        applicationInfoGridItem.id == movingApplicationInfoGridItem.id -> {
                            add(applicationInfoGridItem.copy(index = targetIndex))
                        }

                        fromIndex < targetIndex &&
                                applicationInfoGridItem.index in (fromIndex + 1)..targetIndex -> {
                            add(
                                applicationInfoGridItem.copy(index = applicationInfoGridItem.index - 1),
                            )
                        }

                        fromIndex > targetIndex &&
                                applicationInfoGridItem.index in targetIndex until fromIndex -> {
                            add(
                                applicationInfoGridItem.copy(index = applicationInfoGridItem.index + 1),
                            )
                        }

                        else -> {
                            add(applicationInfoGridItem)
                        }
                    }
                }
            }

            val folderGridItemData = folderGridItem.data as? GridItemData.Folder
                ?: error("Expected GridItemData.Folder")

            val newData = folderGridItemData.copy(
                gridItemsByPage = gridItems.getGridItemsByPage(),
            )

            gridCacheRepository.updateGridItemData(
                id = folderGridItem.id,
                data = newData,
            )
        }
    }
}