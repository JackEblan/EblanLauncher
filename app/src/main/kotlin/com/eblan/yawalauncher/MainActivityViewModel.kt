package com.eblan.yawalauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val gridRepository = GridRepository()

    val gridItems = gridRepository.gridItems.onStart {
        gridRepository.insertGridItems()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun updateGridItem(page: Int, index: Int, gridItem: GridItem) {
        viewModelScope.launch {
            gridRepository.updateGridItem(page = page, index = index, gridItem = gridItem)
        }
    }
}