package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToGridCell
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val gridCell = coordinatesToGridCell(
                x = x,
                y = y,
                rows = userData.rows,
                columns = userData.columns,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            )

            val id = Random.nextInt()

            val gridItem =
                GridItem(page = page, id = id, cells = listOf(gridCell))

            val gridItems = gridRepository.gridItems.first().toMutableList().apply {
                add(gridItem)
            }

            gridRepository.updateGridItems(gridItems = gridItems)
        }
    }
}