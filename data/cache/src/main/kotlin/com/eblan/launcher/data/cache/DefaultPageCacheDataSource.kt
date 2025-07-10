package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.model.PageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal class DefaultPageCacheDataSource @Inject constructor() : PageCacheDataSource {
    private val _pageItems = MutableStateFlow(emptyList<PageItem>())

    override val pageItems = _pageItems.asStateFlow()

    override fun insertPageItems(pageItems: List<PageItem>) {
        _pageItems.update {
            pageItems
        }
    }
}