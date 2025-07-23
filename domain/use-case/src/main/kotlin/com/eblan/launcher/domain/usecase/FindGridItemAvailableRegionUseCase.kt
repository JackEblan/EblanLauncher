package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.findAvailableRegion
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FindGridItemAvailableRegionUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(gridItem: GridItem): GridItem? {
        return withContext(Dispatchers.Default) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val rows = homeSettings.rows

            val columns = homeSettings.columns

            val pageCount = homeSettings.pageCount

            val gridItems = gridRepository.gridItems.first()

            findAvailableRegion(
                gridItems = gridItems,
                pageCount = pageCount,
                rows = rows,
                columns = columns,
                gridItem = gridItem,
            )
        }
    }
}