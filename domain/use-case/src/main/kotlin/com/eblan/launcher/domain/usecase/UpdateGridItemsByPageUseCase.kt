package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsByPageUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(page: Int) {
        withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                gridItem.page == page
            }

            updateGridItemsUseCase(gridItems = gridItems)
        }
    }
}