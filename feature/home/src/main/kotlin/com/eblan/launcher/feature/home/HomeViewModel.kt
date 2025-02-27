package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.ScreenDimension
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.AStarGridAlgorithmUseCase
import com.eblan.launcher.domain.usecase.AddGridItemUseCase
import com.eblan.launcher.domain.usecase.GridItemBoundaryUseCase
import com.eblan.launcher.domain.usecase.GridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val gridItemsByPageUseCase: GridItemsByPageUseCase,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    private val addGridItemUseCase: AddGridItemUseCase,
    private val gridItemBoundaryUseCase: GridItemBoundaryUseCase,
) : ViewModel() {

    private var _screenDimension = MutableStateFlow<ScreenDimension?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeUiState =
        combine(_screenDimension, userDataRepository.userData) { screenDimension, userData ->
            screenDimension?.let { it to userData }
        }.filterNotNull().flatMapLatest { (screenDimension, userData) ->
            gridItemsByPageUseCase(
                screenDimension = screenDimension,
                rows = userData.rows,
                columns = userData.columns,
            ).map { gridItemsByPage ->
                HomeUiState.Success(
                    gridItems = gridItemsByPage,
                    screenDimension = screenDimension,
                    pageCount = userData.pageCount,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    private var _updatedGridItem = MutableStateFlow<GridItem?>(null)

    @OptIn(FlowPreview::class)
    val updatedGridItem = _updatedGridItem.filterNotNull().debounce(1000).onEach { gridItem ->
        aStarGridAlgorithmUseCase(movingGridItem = gridItem)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private var _gridItemBoundary = MutableStateFlow<GridItemBoundary?>(null)

    @OptIn(FlowPreview::class)
    val gridItemBoundary = _gridItemBoundary.debounce(1000).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun updateScreenDimension(screenWidth: Int, screenHeight: Int) {
        _screenDimension.update {
            ScreenDimension(screenWidth = screenWidth, screenHeight = screenHeight)
        }
    }

    fun moveGridItem(
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            _gridItemBoundary.update {
                gridItemBoundaryUseCase(
                    id = id,
                    x = x,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )
            }

            _updatedGridItem.update {
                moveGridItemUseCase(
                    page = page,
                    id = id,
                    x = x,
                    y = y,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )
            }
        }
    }

    fun resizeGridItem(
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
        anchor: Anchor,
    ) {
        viewModelScope.launch {
            _updatedGridItem.update {
                resizeGridItemUseCase(
                    page = page,
                    id = id,
                    width = width,
                    height = height,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
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
                page = page,
                x = x,
                y = y,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            )
        }
    }
}