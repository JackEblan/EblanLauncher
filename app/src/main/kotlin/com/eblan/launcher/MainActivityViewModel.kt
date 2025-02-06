package com.eblan.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.gridalgorithm.AStar
import com.eblan.launcher.domain.usecase.GridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.AStarGridAlgorithmUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val gridRepository = GridRepository()
    private val gridItemsByPageUseCase = GridItemsByPageUseCase(gridRepository = gridRepository)
    private val aStar = AStar()
    private val AStarGridAlgorithmUseCase = AStarGridAlgorithmUseCase(
        gridRepository = gridRepository, aStar = aStar
    )

    val gridItems = gridItemsByPageUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun updateGridItem(page: Int, gridItem: GridItem) {
        viewModelScope.launch {
            AStarGridAlgorithmUseCase(page = page, gridItem = gridItem)
        }
    }
}