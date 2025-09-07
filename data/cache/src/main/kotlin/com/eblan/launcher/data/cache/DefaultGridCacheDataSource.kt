package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCacheType
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultGridCacheDataSource @Inject constructor(@Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher) :
    GridCacheDataSource {
    private val _gridItemsCache = MutableStateFlow(emptyList<GridItem>())

    override val gridItemsCache = _gridItemsCache.asStateFlow()

    private val _gridItemCacheType = MutableStateFlow(GridItemCacheType.Grid)

    override val gridItemCacheType = _gridItemCacheType.asStateFlow()

    override fun insertGridItems(gridItems: List<GridItem>) {
        _gridItemsCache.update {
            gridItems
        }
    }

    override fun insertGridItem(gridItem: GridItem) {
        _gridItemsCache.update { currentGridCacheItems ->
            currentGridCacheItems + gridItem
        }
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        _gridItemsCache.update { currentGridCacheItems ->
            currentGridCacheItems - gridItems.toSet()
        }
    }

    override fun deleteGridItem(gridItem: GridItem) {
        _gridItemsCache.update { currentGridCacheItems ->
            currentGridCacheItems.toMutableList().apply {
                removeIf { it.id == gridItem.id }
            }
        }
    }

    override suspend fun updateGridItemData(id: String, data: GridItemData) {
        withContext(defaultDispatcher) {
            _gridItemsCache.update { currentGridCacheItems ->
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
        withContext(defaultDispatcher) {
            _gridItemsCache.update { currentGridCacheItems ->
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

    override fun updateGridItemCacheType(gridItemCacheType: GridItemCacheType) {
        _gridItemCacheType.update {
            gridItemCacheType
        }
    }
}