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
        screenWidth: Int,
    ): PageDirection? {
        val userData = userDataRepository.userData.first()

        val cellWidth = screenWidth / userData.columns

        val width = gridItem.columnSpan * cellWidth

        return if ((x + width / 2) <= 0) {
            PageDirection.Left
        } else if ((x + width / 2) >= screenWidth) {
            PageDirection.Right
        } else {
            null
        }
    }
}