package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.DockItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.DockCacheRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import java.util.Collections
import javax.inject.Inject

class MoveDockGridItemUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val dockCacheRepository: DockCacheRepository,
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        gridItem: GridItem,
        x: Int,
        gridWidth: Int,
    ) {
        val userData = userDataRepository.userData.first()

        val dockItems = dockCacheRepository.dockCacheItems.first().toMutableList()

        val cellWidth = gridWidth / userData.columns

        val startColumn = x / cellWidth

        if (dockItems.size < userData.columns) {
            val dockItem = DockItem(
                id = gridItem.id,
                startRow = gridItem.startRow,
                startColumn = startColumn,
                rowSpan = gridItem.rowSpan,
                columnSpan = gridItem.columnSpan,
                data = gridItem.data,
            )

            dockCacheRepository.insertDockItem(dockItem = dockItem)
        } else {
            val dockItem = dockItems.find { it.startColumn == startColumn }

            if (dockItem != null) {
                val fromIndex = dockItems.indexOfFirst { it.id == gridItem.id }

                val toIndex = dockItems.indexOfFirst { it.id == dockItem.id }

                if (fromIndex != -1 && toIndex != -1) {
                    Collections.swap(dockItems, fromIndex, toIndex)

                    dockCacheRepository.insertDockItems(dockItems)
                }
            }
        }

        gridCacheRepository.deleteGridItem(gridItem)
    }
}