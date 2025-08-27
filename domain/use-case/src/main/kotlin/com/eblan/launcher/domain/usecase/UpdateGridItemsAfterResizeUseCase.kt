package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsAfterResizeUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(resizingGridItem: GridItem) {
        withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridCacheItems.first()

            when (resizingGridItem.associate) {
                Associate.Grid -> {
                    updateGridItemsUseCase(
                        gridItems = gridItems.filter { gridItem ->
                            gridItem.page == resizingGridItem.page
                        },
                    )
                }

                Associate.Dock -> {
                    updateGridItemsUseCase(gridItems = gridItems)
                }
            }
        }
    }
}