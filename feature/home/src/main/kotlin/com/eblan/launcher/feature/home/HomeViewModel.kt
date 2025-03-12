package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.AddAppWidgetProviderInfoUseCase
import com.eblan.launcher.domain.usecase.AddApplicationInfoUseCase
import com.eblan.launcher.domain.usecase.AddGridItemResult
import com.eblan.launcher.domain.usecase.GetGridItemByCoordinatesUseCase
import com.eblan.launcher.domain.usecase.GroupGridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    groupGridItemsByPageUseCase: GroupGridItemsByPageUseCase,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    private val addApplicationInfoUseCase: AddApplicationInfoUseCase,
    private val getGridItemByCoordinatesUseCase: GetGridItemByCoordinatesUseCase,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val addAppWidgetProviderInfoUseCase: AddAppWidgetProviderInfoUseCase,
) : ViewModel() {
    val homeUiState = groupGridItemsByPageUseCase().map(HomeUiState::Success).stateIn(
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

    private var _gridItemByCoordinates = MutableStateFlow<Boolean?>(null)

    val gridItemByCoordinates = _gridItemByCoordinates.asStateFlow()

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

    private var _addGridItemResult = MutableStateFlow<AddGridItemResult?>(null)

    val addGridItemResult = _addGridItemResult.asStateFlow()

    fun moveGridItem(
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        width: Int,
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
                    width = width,
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

    fun addApplicationInfoGridItem(
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) {
        viewModelScope.launch {
            _addGridItemResult.update {
                addApplicationInfoUseCase(
                    page = page,
                    x = x,
                    y = y,
                    rowSpan = rowSpan,
                    columnSpan = columnSpan,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    data = data,
                )
            }
        }
    }

    fun addAppWidgetProviderInfoGridItem(
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ) {
        viewModelScope.launch {
            _addGridItemResult.update {
                addAppWidgetProviderInfoUseCase(
                    page = page,
                    x = x,
                    y = y,
                    rowSpan = rowSpan,
                    columnSpan = columnSpan,
                    minWidth = minWidth,
                    minHeight = minHeight,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    data = data,
                )
            }
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
            _gridItemByCoordinates.update {
                getGridItemByCoordinatesUseCase(
                    page = page,
                    x = x,
                    y = y,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                ) != null
            }
        }
    }

    fun resetGridItemByCoordinates() {
        _gridItemByCoordinates.update {
            null
        }
    }

    fun resetOverlay() {
        viewModelScope.launch {
            when(addGridItemResult.value){
                AddGridItemResult.Failed -> {

                }
                is AddGridItemResult.Success -> {

                }
                null -> Unit
            }

            _addGridItemResult.update {
                null
            }
        }
    }
}