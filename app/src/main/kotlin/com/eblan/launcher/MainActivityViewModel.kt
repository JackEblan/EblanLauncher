package com.eblan.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.domain.usecase.AStarGridAlgorithmUseCase
import com.eblan.launcher.domain.usecase.AddGridItemUseCase
import com.eblan.launcher.domain.usecase.GridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val gridRepository = GridRepository()

    private val userDataRepository = UserDataRepository()

    private val gridItemsByPageUseCase = GridItemsByPageUseCase(
        gridRepository = gridRepository, userDataRepository = userDataRepository
    )

    private val aStarGridAlgorithmUseCase = AStarGridAlgorithmUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
    )

    private val moveGridItemUseCase = MoveGridItemUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
    )

    private val resizeGridItemUseCase = ResizeGridItemUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
    )

    private val addGridItemUseCase = AddGridItemUseCase(
        gridRepository = gridRepository,
        userDataRepository = userDataRepository,
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

    private var _updatedGridItem = MutableStateFlow<GridItem?>(null)

    @OptIn(FlowPreview::class)
    val updatedGridItem = _updatedGridItem.filterNotNull().debounce(1000).onEach { gridItem ->
        aStarGridAlgorithmUseCase(gridItem = gridItem)
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null
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
            _updatedGridItem.update {
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
    }

    fun resizeGridItem(
        page: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItem: GridItem?,
        anchor: Anchor,
    ) {
        viewModelScope.launch {
            _updatedGridItem.update {
                resizeGridItemUseCase(
                    page = page,
                    width = width,
                    height = height,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    gridItem = gridItem,
                    anchor = anchor,
                )
            }
        }
    }

    fun addGridItem(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            addGridItemUseCase(
                page = page, x = x, y = y, screenWidth = screenWidth, screenHeight = screenHeight
            )
        }
    }
}