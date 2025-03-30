package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.repository.GridRepository
import javax.inject.Inject

class DeleteGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
) {
    suspend operator fun invoke(id: String) {
        val gridItem = gridRepository.getGridItem(id = id)

        if (gridItem != null) {
            gridRepository.deleteGridItem(gridItem)
        }
    }
}