package com.eblan.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.domain.usecase.AStarGridAlgorithmUseCase
import com.eblan.launcher.domain.usecase.GridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val gridRepository = GridRepository()
    private val gridItemsByPageUseCase = GridItemsByPageUseCase(gridRepository = gridRepository)
    private val userDataRepository = UserDataRepository()
    private val aStarGridAlgorithmUseCase = AStarGridAlgorithmUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
    )

    private val moveGridItemUseCase = MoveGridItemUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
        aStarGridAlgorithmUseCase = aStarGridAlgorithmUseCase
    )

    private val resizeGridItemUseCase = ResizeGridItemUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
        aStarGridAlgorithmUseCase = aStarGridAlgorithmUseCase
    )

    private var _screenDimension = MutableStateFlow<ScreenDimension?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val gridItems = _screenDimension.filterNotNull().flatMapLatest { screenDimension ->
        gridItemsByPageUseCase(screenDimension = screenDimension)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    fun updateScreenDimension(screenWidth: Int, screenHeight: Int) {
        _screenDimension.update {
            ScreenDimension(screenWidth = screenWidth, screenHeight = screenHeight)
        }
    }

    fun moveGridItem(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItemPixel: GridItemPixel?
    ) {
        viewModelScope.launch {
            moveGridItemUseCase(
                page = page,
                x = x,
                y = y,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                gridItemPixel = gridItemPixel
            )
        }
    }

    fun resizeGridItem(
        page: Int,
        newPixelWidth: Int,
        newPixelHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItem: GridItem?,
    ) {
        viewModelScope.launch {
            resizeGridItemUseCase(
                page = page,
                newPixelWidth = newPixelWidth,
                newPixelHeight = newPixelHeight,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                gridItem = gridItem
            )
        }
    }
}