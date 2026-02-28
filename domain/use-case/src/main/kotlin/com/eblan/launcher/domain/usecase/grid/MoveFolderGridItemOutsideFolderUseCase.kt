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

class MoveFolderGridItemOutsideFolderUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        folderGridItem: GridItem,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
    ) {
        withContext(defaultDispatcher) {
            val data =
                folderGridItem.data as? GridItemData.Folder ?: error("Expected GridItemData.Folder")

            val newData = data.copy(
                gridItems = applicationInfoGridItems.toMutableList().apply {
                    removeIf { applicationInfoGridItem ->
                        applicationInfoGridItem.id == movingApplicationInfoGridItem.id
                    }
                },
                previewGridItemsByPage = data.previewGridItemsByPage.toMutableList().apply {
                    removeIf { applicationInfoGridItem ->
                        applicationInfoGridItem.id == movingApplicationInfoGridItem.id
                    }
                },
            )

            gridCacheRepository.updateGridItemData(
                id = folderGridItem.id,
                data = newData,
            )
        }
    }
}