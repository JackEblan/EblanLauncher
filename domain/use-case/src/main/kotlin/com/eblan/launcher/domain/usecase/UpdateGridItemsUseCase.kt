package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() {
        val gridCacheItems = gridCacheRepository.gridCacheItems.first()

        var pageCount = userDataRepository.userData.first().pageCount

        withContext(Dispatchers.Default) {
            val gridCacheItemsByAssociateGrid = gridCacheItems.filter { gridItem ->
                gridItem.associate == Associate.Grid
            }

            val lastPageIsNullOrEmpty =
                gridCacheItemsByAssociateGrid.groupBy { gridItem -> gridItem.page }[pageCount - 1].isNullOrEmpty()

            if (lastPageIsNullOrEmpty) {
                pageCount -= 1

                userDataRepository.updatePageCount(pageCount)
            }

            val hasNewPage =
                gridCacheItemsByAssociateGrid.maxOf { gridItem -> gridItem.page } > pageCount - 1

            if (hasNewPage) {
                userDataRepository.updatePageCount(pageCount + 1)
            }
        }

        gridRepository.upsertGridItems(gridItems = gridCacheItems)
    }
}