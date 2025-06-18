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
        _gridCacheItems.update {
            gridItems
        }
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        withContext(Dispatchers.Default) {
            _gridCacheItems.update { currentGridCacheItems ->
                currentGridCacheItems.toMutableList().apply {
                    removeIf { it.id == gridItem.id }
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