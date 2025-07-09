package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.PageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultPageCacheDataSource @Inject constructor() : PageCacheDataSource {
    private val _pageItems = MutableStateFlow(emptyList<PageItem>())

    override val pageItems = _pageItems.asStateFlow()

    private val _pageItemsToDelete = MutableStateFlow(emptyList<PageItem>())

    override val pageItemsToDelete = _pageItemsToDelete.asStateFlow()

    override fun insertPageItems(pageItems: List<PageItem>) {
        _pageItems.update {
            pageItems
        }
    }

    override fun addEmptyPageItem() {
        _pageItems.update { currentPageItems ->
            currentPageItems.toMutableList().apply {
                add(PageItem(id = size, emptyList()))
            }
        }
    }

    override suspend fun deletePageItems(id: Int) {
        withContext(Dispatchers.Default) {
            _pageItems.update { currentPageItems ->
                currentPageItems.toMutableList().apply {
                    val index = indexOfFirst { pageItem -> pageItem.id == id }

                    if (index != -1) {
                        val pageItemToDelete = removeAt(index)

                        _pageItemsToDelete.update { currentPageItemsToDelete ->
                            currentPageItemsToDelete + pageItemToDelete
                        }
                    }
                }
            }
        }
    }
}