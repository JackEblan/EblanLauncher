package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.AddGridItemUseCase
import com.eblan.launcher.domain.usecase.GetGridItemByCoordinatesUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
) : ViewModel() {
    val homeUiState =
        combine(gridRepository.gridItems, userDataRepository.userData) { gridItems, userData ->
            HomeUiState.Success(
                gridItems = gridItems.groupBy { gridItem -> gridItem.page },
                userData = userData,
            )
        }.flowOn(Dispatchers.Default).stateIn(
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

    private var _gridItemOverlayUiState =
        MutableStateFlow<GridItemOverlayUiState>(GridItemOverlayUiState.Idle)

    val gridItemOverlayUiState = _gridItemOverlayUiState.asStateFlow()

    val eblanApplicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val appWidgetProviderInfos =
        eblanApplicationInfoRepository.eblanApplicationInfos.map { applicationInfos ->
            applicationInfos.map { eblanApplicationInfo ->
                eblanApplicationInfo to appWidgetManagerWrapper.getInstalledProviderByPackageName(
                    packageName = eblanApplicationInfo.packageName,
                )
            }.filter { (_, appWidgetProviderInfos) ->
                appWidgetProviderInfos.isNotEmpty()
            }
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

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
            _gridItemOverlayUiState.update {
                GridItemOverlayUiState.Success(
                    gridItemOverlay = getGridItemByCoordinatesUseCase(
                        page = page,
                        x = x,
                        y = y,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                    ),
                )
            }
        }
    }

    fun resetGridItemOverlay() {
        viewModelScope.launch {
            _gridItemOverlayUiState.update {
                GridItemOverlayUiState.Idle
            }
        }
    }
}