package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultGridCacheDataSource @Inject constructor() : GridCacheDataSource {
    private val _gridCacheItems = MutableStateFlow(emptyList<GridItem>())

    override val gridCacheItems = _gridCacheItems.asStateFlow()

    private val _isCache = MutableStateFlow(false)

    override val isCache = _isCache.asStateFlow()

    override fun insertGridItems(gridItems: List<GridItem>) {
        _gridCacheItems.update {
            gridItems
        }
    }

    override fun insertGridItem(gridItem: GridItem) {
        _gridCacheItems.update { currentGridCacheItems ->
            currentGridCacheItems + gridItem
        }
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        _gridCacheItems.update { currentGridCacheItems ->
            currentGridCacheItems - gridItems.toSet()
        }
    }

    override fun deleteGridItem(gridItem: GridItem) {
        _gridCacheItems.update { currentGridCacheItems ->
            currentGridCacheItems.toMutableList().apply {
                removeIf { it.id == gridItem.id }
            }
        }
    }

    override suspend fun updateGridItemData(id: String, data: GridItemData) {
        withContext(Dispatchers.Default) {
            _gridCacheItems.update { currentGridCacheItems ->
                currentGridCacheItems.toMutableList().apply {
                    val index = indexOfFirst { it.id == id }

                    if (index != -1) {
                        set(index, get(index).copy(data = data))
                    }
                }
            }
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        withContext(Dispatchers.Default) {
            _gridCacheItems.update { currentGridCacheItems ->
                currentGridCacheItems.toMutableList().apply {
                    gridItems.forEach { gridItem ->
                        val index = indexOfFirst { it.id == gridItem.id }

                        if (index != -1) {
                            if (get(index) != gridItem) {
                                set(index, gridItem)
                            }
                        } else {
                            add(gridItem)
                        }
                    }
                }
            }
        }
    }

    override fun updateIsCache(isCache: Boolean) {
        _isCache.update {
            isCache
        }
    }
}