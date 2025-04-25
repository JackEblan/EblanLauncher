package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MovePageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        gridItem: GridItem,
        x: Int,
        gridWidth: Int,
    ): PageDirection? {
        val userData = userDataRepository.userData.first()

        val cellWidth = gridWidth / userData.columns

        val width = gridItem.columnSpan * cellWidth

        return when {
            (x + width * 0.25) < 0 -> PageDirection.Left
            (x + width * 0.75) > gridWidth -> PageDirection.Right
            else -> null
        }
    }
}