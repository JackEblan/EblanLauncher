package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.GridItemOverlay
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.AddGridItemUseCase
import com.eblan.launcher.domain.usecase.GetGridItemByCoordinatesUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    gridRepository: GridRepository,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    private val addGridItemUseCase: AddGridItemUseCase,
    private val getGridItemByCoordinatesUseCase: GetGridItemByCoordinatesUseCase,
) : ViewModel() {
    val homeUiState =
        combine(gridRepository.gridItems, userDataRepository.userData) { gridItems, userData ->
            HomeUiState.Success(
                gridItems = gridItems.groupBy { gridItem -> gridItem.page },
                userData = userData,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    private var _gridItemBoundary = MutableStateFlow<GridItemBoundary?>(null)

    val gridItemBoundary = _gridItemBoundary.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private var _gridItemOverlay = MutableStateFlow<GridItemOverlay?>(null)

    val gridItemOverlay = _gridItemOverlay.asStateFlow()

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
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) {
        viewModelScope.launch {
            resizeGridItemUseCase(
                page = page,
                id = id,
                width = width,
                height = height,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                anchor = anchor,
            )
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

    fun getGridItemByCoordinates(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            _gridItemOverlay.update {
                getGridItemByCoordinatesUseCase(
                    page = page,
                    x = x,
                    y = y,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )
            }
        }
    }

    fun resetGridItemOverlay() {
        viewModelScope.launch {
            _gridItemOverlay.update {
                null
            }
        }
    }
}