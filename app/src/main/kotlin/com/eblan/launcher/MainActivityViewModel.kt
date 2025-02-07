package com.eblan.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.usecase.AStarGridAlgorithmUseCase
import com.eblan.launcher.domain.usecase.GridItemsByPageUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val gridRepository = GridRepository()
    private val gridItemsByPageUseCase = GridItemsByPageUseCase(gridRepository = gridRepository)
    private val userDataRepository = UserDataRepository()
    private val aStarGridAlgorithmUseCase = AStarGridAlgorithmUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
    )

    val gridItems = gridItemsByPageUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun updateGridItem(page: Int, gridItem: GridItem) {
        viewModelScope.launch {
            aStarGridAlgorithmUseCase(page = page, gridItem = gridItem)
        }
    }
}