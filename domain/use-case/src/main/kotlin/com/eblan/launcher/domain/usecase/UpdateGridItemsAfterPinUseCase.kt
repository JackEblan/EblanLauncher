package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsAfterPinUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(defaultDispatcher) {
            val initialPage = userDataRepository.userData.first().homeSettings.initialPage

            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                gridItem.page == initialPage
            }

            updateGridItemsUseCase(gridItems = gridItems)
        }
    }
}