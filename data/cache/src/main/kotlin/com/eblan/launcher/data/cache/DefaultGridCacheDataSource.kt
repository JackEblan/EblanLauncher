package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal class DefaultGridCacheDataSource @Inject constructor() : GridCacheDataSource {
    private val _gridCacheItems = MutableStateFlow(emptyList<GridItem>())

    override val gridCacheItems = _gridCacheItems.asStateFlow()

    private var _isCache = MutableStateFlow(false)

    override val isCache = _isCache.asStateFlow()

    override fun insertGridItems(gridItems: List<GridItem>) {
        _gridCacheItems.update {
            gridItems
        }
    }

    override fun deleteGridItem(gridItem: GridItem) {
        _gridCacheItems.update { currentGridCacheItems ->
            currentGridCacheItems.toMutableList().apply {
                removeIf { it.id == gridItem.id }
            }
        }
    }

    override fun upsertGridItems(gridItems: List<GridItem>) {
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

    override fun updateIsCache(isCache: Boolean) {
        _isCache.update {
            isCache
        }
    }
}