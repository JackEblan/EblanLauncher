package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToGridCell
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AddGridItemUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
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

            val gridItem = GridItem(page = page, id = id, cells = listOf(gridCell))

            val gridItems = gridRepository.gridItems.first().toMutableList().apply {
                add(gridItem)
            }

            gridRepository.updateGridItems(gridItems = gridItems)
        }
    }
}