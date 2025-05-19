package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateGridItemsUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke() {
        val groupedGridCacheItems =
            gridCacheRepository.gridCacheItems.first().groupBy { gridItem -> gridItem.page }

        if (gridCacheRepository.gridCacheItems.first()
                .maxOf { gridItem -> gridItem.page } > userDataRepository.userData.first().pageCount - 1
        ) {
            userDataRepository.updatePageCount(userDataRepository.userData.first().pageCount + 1)
        }

        repeat(userDataRepository.userData.first().pageCount) { page ->
            if (groupedGridCacheItems[page].isNullOrEmpty()) {
                userDataRepository.updatePageCount(pageCount = userDataRepository.userData.first().pageCount - 1)

                gridCacheRepository.shiftPagesAfterDeletedPage(page = page)
            }
        }

        gridRepository.upsertGridItems(gridItems = gridCacheRepository.gridCacheItems.first())
    }
}