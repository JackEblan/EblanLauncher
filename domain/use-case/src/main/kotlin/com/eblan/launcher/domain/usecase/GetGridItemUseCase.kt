package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetGridItemUseCase @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(id: String): GridItem? {
        return withContext(defaultDispatcher) {
            val gridItems = getHomeDataUseCase().first().gridItems

            gridItems.find { gridItem ->
                gridItem.id == id
            }
        }
    }
}