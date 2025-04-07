package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemMovement
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.AddAppWidgetProviderInfoUseCase
import com.eblan.launcher.domain.usecase.AddApplicationInfoUseCase
import com.eblan.launcher.domain.usecase.GetGridItemByCoordinatesUseCase
import com.eblan.launcher.domain.usecase.GroupGridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeWidgetGridItemUseCase
import com.eblan.launcher.domain.usecase.ShiftAlgorithmUseCase
import com.eblan.launcher.domain.usecase.UpdateWidgetGridItemDataUseCase
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
    private val shiftAlgorithmUseCase: ShiftAlgorithmUseCase,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    private val resizeWidgetGridItemUseCase: ResizeWidgetGridItemUseCase,
    private val addApplicationInfoUseCase: AddApplicationInfoUseCase,
    private val getGridItemByCoordinatesUseCase: GetGridItemByCoordinatesUseCase,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val addAppWidgetProviderInfoUseCase: AddAppWidgetProviderInfoUseCase,
    private val updateWidgetGridItemDataUseCase: UpdateWidgetGridItemDataUseCase,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    val homeUiState = groupGridItemsByPageUseCase().map(HomeUiState::Success).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    private var _gridItemMovement = MutableStateFlow<GridItemMovement?>(null)

    val gridItemMovement = _gridItemMovement.asStateFlow()

    private var _addGridItemMovement = MutableStateFlow<GridItemMovement?>(null)

    val addGridItemMovement = _addGridItemMovement.asStateFlow()

    private var _showBottomSheet = MutableStateFlow(false)

    val showBottomSheet = _showBottomSheet.asStateFlow()

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

    private var _addGridItem = MutableStateFlow<GridItem?>(null)

    val addGridItem = _addGridItem.asStateFlow()

    fun gridAlgorithm(gridItem: GridItem) {
        viewModelScope.launch {
            shiftAlgorithmUseCase(movingGridItem = gridItem)
        }
    }

    fun moveGridItem(
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            _gridItemMovement.update {
                moveGridItemUseCase(
                    page = page,
                    gridItem = gridItem,
                    x = x,
                    y = y,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )
            }
        }
    }

    fun moveAddGridItem(
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            _addGridItemMovement.update {
                moveGridItemUseCase(
                    page = page,
                    gridItem = gridItem,
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
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) {
        viewModelScope.launch {
            resizeGridItemUseCase(
                page = page,
                gridItem = gridItem,
                width = width,
                height = height,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                anchor = anchor,
            )
        }
    }

    fun resizeWidgetGridItem(
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) {
        viewModelScope.launch {
            resizeWidgetGridItemUseCase(
                page = page,
                gridItem = gridItem,
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
            _addGridItem.update {
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
        componentName: String,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            _addGridItem.update {
                addAppWidgetProviderInfoUseCase(
                    page = page,
                    componentName = componentName,
                    x = x,
                    y = y,
                    rowSpan = rowSpan,
                    columnSpan = columnSpan,
                    minWidth = minWidth,
                    minHeight = minHeight,
                    resizeMode = resizeMode,
                    minResizeWidth = minResizeWidth,
                    minResizeHeight = minResizeHeight,
                    maxResizeWidth = maxResizeWidth,
                    maxResizeHeight = maxResizeHeight,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )
            }
        }
    }

    fun showBottomSheet(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            _showBottomSheet.update {
                getGridItemByCoordinatesUseCase(
                    page = page,
                    x = x,
                    y = y,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                ) == null
            }
        }
    }

    fun updateWidget(gridItem: GridItem, appWidgetId: Int) {
        viewModelScope.launch {
            updateWidgetGridItemDataUseCase(gridItem = gridItem, appWidgetId = appWidgetId)
        }
    }

    fun updatePageCount(pageCount: Int) {
        viewModelScope.launch {
            userDataRepository.updatePageCount(pageCount = pageCount)
        }
    }

    fun resetShowBottomSheet() {
        _showBottomSheet.update {
            false
        }
    }

    fun resetGridItemMovement() {
        _gridItemMovement.update {
            null
        }
    }

    fun resetAddGridItem() {
        viewModelScope.launch {
            _addGridItem.update {
                null
            }

            _addGridItemMovement.update {
                null
            }
        }
    }
}