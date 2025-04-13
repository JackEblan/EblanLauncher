package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class DefaultGridCacheRepository : GridCacheRepository {
    private val _gridCacheItems = MutableSharedFlow<List<GridItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val gridCacheItems = _gridCacheItems.asSharedFlow()

    private val currentGridCacheItems get() = _gridCacheItems.replayCache.firstOrNull() ?: emptyList()

    override suspend fun insertGridItems(gridItems: List<GridItem>) {
        _gridCacheItems.emit(gridItems)
    }

    override suspend fun updateGridItems(gridItems: List<GridItem>) {
        val updatedGridItems = currentGridCacheItems.toMutableList()

        gridItems.forEach { newItem ->
            val index = updatedGridItems.indexOfFirst { it.id == newItem.id }

            if (index != -1 && updatedGridItems[index] != newItem) {
                updatedGridItems[index] = newItem
            }
        }

        _gridCacheItems.emit(updatedGridItems)
    }
}