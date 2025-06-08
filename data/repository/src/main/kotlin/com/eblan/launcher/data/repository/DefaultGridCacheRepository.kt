package com.eblan.launcher.data.repository

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultGridCacheRepository @Inject constructor() : GridCacheRepository {
    private val _gridCacheItems = MutableStateFlow(emptyList<GridItem>())

    override val gridCacheItems = _gridCacheItems.asStateFlow()

    private var _isCache = MutableStateFlow(false)

    override val isCache = _isCache.asStateFlow()

    override suspend fun insertGridItems(gridItems: List<GridItem>) {
        _gridCacheItems.emit(gridItems)
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        val updatedGridItems = _gridCacheItems.value.toMutableList()

        withContext(Dispatchers.Default) {
            val index = updatedGridItems.indexOfFirst { it.id == gridItem.id }

            if (index != -1) {
                updatedGridItems.removeAt(index)
            }

            _gridCacheItems.emit(updatedGridItems)
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        val updatedGridItems = _gridCacheItems.value.toMutableList()

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

    override fun updateIsCache(isCache: Boolean) {
        _isCache.update {
            isCache
        }
    }
}