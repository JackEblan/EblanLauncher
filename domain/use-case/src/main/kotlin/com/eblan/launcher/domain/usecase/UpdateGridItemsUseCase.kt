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
    suspend operator fun invoke(currentPage: Int): Int {
        var targetPage = currentPage

        withContext(Dispatchers.Default) {
            val gridCacheItems = gridCacheRepository.gridCacheItems.first()

            var pageCount = userDataRepository.userData.first().pageCount

            val gridCacheItemsByAssociateGrid = gridCacheItems.filter { gridItem ->
                gridItem.associate == Associate.Grid
            }

            val hasNewPage = gridCacheItemsByAssociateGrid.isNotEmpty() &&
                    gridCacheItemsByAssociateGrid.maxOf { gridItem -> gridItem.page } > pageCount - 1

            if (hasNewPage) {
                pageCount += 1

                userDataRepository.updatePageCount(pageCount)
            }

            repeat(pageCount) { page ->
                val isPageEmpty =
                    gridCacheItemsByAssociateGrid.none { gridItem -> gridItem.page == page }

                if (isPageEmpty && pageCount > 1) {
                    targetPage -= 1

                    pageCount -= 1

                    userDataRepository.updatePageCount(pageCount)

                    val shiftedGridItems = gridCacheItemsByAssociateGrid.map { gridItem ->
                        if (gridItem.page > page) {
                            gridItem.copy(page = gridItem.page - 1)
                        } else {
                            gridItem
                        }
                    }

                    gridCacheRepository.upsertGridItems(gridItems = shiftedGridItems)
                }
            }

            val newGridCacheItems = gridCacheRepository.gridCacheItems.first()

            gridRepository.upsertGridItems(gridItems = newGridCacheItems)
        }

        return targetPage
    }
}