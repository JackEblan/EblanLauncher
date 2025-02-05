package com.eblan.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val gridRepository = GridRepository()

    val gridItems = gridRepository.gridItems.onStart {
        gridRepository.insertGridItems()
    }.map { gridItems ->
        gridItems.groupBy { gridItem -> gridItem.page }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun updateGridItem(page: Int, gridItem: GridItem) {
        viewModelScope.launch {
            gridRepository.updateGridItem(
                page = page, gridItem = gridItem
            )
        }
    }
}