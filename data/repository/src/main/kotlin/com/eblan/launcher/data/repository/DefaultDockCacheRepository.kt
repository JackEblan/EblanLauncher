package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.DockItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.DockCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultDockCacheRepository @Inject constructor() : DockCacheRepository {
    private val _dockCacheItems = MutableSharedFlow<List<DockItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val dockCacheItems = _dockCacheItems.asSharedFlow()

    private val currentGridCacheItems
        get() = _dockCacheItems.replayCache.firstOrNull() ?: emptyList()

    override suspend fun insertDockItems(dockItems: List<DockItem>) {
        _dockCacheItems.emit(dockItems)
    }

    override suspend fun insertDockItem(dockItem: DockItem) {
        val updatedDockItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            val index = updatedDockItems.indexOfFirst { it.id == dockItem.id }

            if (index != -1) {
                updatedDockItems[index] = dockItem
            } else {
                updatedDockItems.add(dockItem)

            }

            _dockCacheItems.emit(updatedDockItems)
        }
    }

    override suspend fun updateDockItem(id: String, data: GridItemData) {
        val updatedDockItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            val index = updatedDockItems.indexOfFirst { it.id == id }

            if (index != -1) {
                updatedDockItems[index] = updatedDockItems[index].copy(data = data)
            }

            _dockCacheItems.emit(updatedDockItems)
        }
    }

    override suspend fun deleteDockItem(dockItem: DockItem) {
        val updatedDockItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            val index = updatedDockItems.indexOfFirst { it.id == dockItem.id }

            if (index != -1) {
                updatedDockItems.removeAt(index)
            }

            _dockCacheItems.emit(updatedDockItems)
        }
    }

    override suspend fun upsertDockItems(dockItems: List<DockItem>) {
        val updatedDockItems = currentGridCacheItems.toMutableList()

        withContext(Dispatchers.Default) {
            dockItems.forEach { gridItem ->
                val index = updatedDockItems.indexOfFirst { it.id == gridItem.id }

                if (index != -1 && updatedDockItems[index] != gridItem) {
                    updatedDockItems[index] = gridItem
                }

                if (index == -1) {
                    updatedDockItems.add(gridItem)
                }
            }

            _dockCacheItems.emit(updatedDockItems)
        }
    }
}