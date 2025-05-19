package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultGridCacheRepository @Inject constructor() : GridCacheRepository {
    private val _gridCacheItems = MutableSharedFlow<List<GridItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val gridCacheItems = _gridCacheItems.asSharedFlow()

    private val currentGridCacheItems
        get() = _gridCacheItems.replayCache.firstOrNull() ?: emptyList()

    override suspend fun insertGridItems(gridItems: List<GridItem>) {
        _gridCacheItems.emit(gridItems)
    }

    override suspend fun updateGridItem(id: String, data: GridItemData) {
        val updatedGridItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            val index = updatedGridItems.indexOfFirst { it.id == id }

            if (index != -1) {
                updatedGridItems[index] = updatedGridItems[index].copy(data = data)
            }

            _gridCacheItems.emit(updatedGridItems)
        }
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        val updatedGridItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            val index = updatedGridItems.indexOfFirst { it.id == gridItem.id }

            if (index != -1) {
                updatedGridItems.removeAt(index)
            }

            _gridCacheItems.emit(updatedGridItems)
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        val updatedGridItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            gridItems.forEach { gridItem ->
                val index = updatedGridItems.indexOfFirst { it.id == gridItem.id }

                if (index != -1 && updatedGridItems[index] != gridItem) {
                    updatedGridItems[index] = gridItem
                }

                if (index == -1) {
                    updatedGridItems.add(gridItem)
                }
            }

            _gridCacheItems.emit(updatedGridItems)
        }
    }

    override suspend fun shiftPagesAfterDeletedPage(page: Int) {
        withContext(Dispatchers.Default) {
            val shiftGridItemsPage =
                currentGridCacheItems.map { gridItem ->
                    if (gridItem.page > page) {
                        gridItem.copy(page = gridItem.page - 1)
                    } else {
                        gridItem
                    }
                }

            _gridCacheItems.emit(shiftGridItemsPage)
        }
    }
}