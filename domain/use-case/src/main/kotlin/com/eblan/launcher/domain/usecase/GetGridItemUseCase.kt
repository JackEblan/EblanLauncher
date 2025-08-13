package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetGridItemUseCase @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
) {
    suspend operator fun invoke(id: String): GridItem? {
        return withContext(Dispatchers.Default) {
            val gridItems = getHomeDataUseCase().first().gridItems

            gridItems.find { gridItem ->
                gridItem.id == id
            }
        }
    }
}