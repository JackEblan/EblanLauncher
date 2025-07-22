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

    override suspend fun updateWidgetGridItemData(id: Int, appWidgetId: Int) {
        withContext(Dispatchers.Default) {
            _gridCacheItems.update { currentGridCacheItems ->
                currentGridCacheItems.toMutableList().apply {
                    val gridItem = find { it.id == id }

                    val data = gridItem?.data

                    if (data is GridItemData.Widget) {
                        val index = indexOfFirst { it.id == gridItem.id }

                        val newData = data.copy(appWidgetId = appWidgetId)

                        set(index, gridItem.copy(data = newData))
                    }
                }
            }
        }
    }

    override suspend fun updateShortcutGridItemData(id: Int, icon: String?) {
        withContext(Dispatchers.Default) {
            _gridCacheItems.update { currentGridCacheItems ->
                currentGridCacheItems.toMutableList().apply {
                    val gridItem = find { it.id == id }

                    val data = gridItem?.data

                    if (data is GridItemData.ShortcutInfo) {
                        val index = indexOfFirst { it.id == gridItem.id }

                        val newData = data.copy(icon = icon)

                        set(index, gridItem.copy(data = newData))
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