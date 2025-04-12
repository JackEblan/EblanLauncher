package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.AddAppWidgetProviderInfoUseCase
import com.eblan.launcher.domain.usecase.AddApplicationInfoUseCase
import com.eblan.launcher.domain.usecase.DeletePageUseCase
import com.eblan.launcher.domain.usecase.GroupGridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.MovePageUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeWidgetGridItemUseCase
import com.eblan.launcher.domain.usecase.UpdateWidgetGridItemDataUseCase
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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
    private val resizeWidgetGridItemUseCase: ResizeWidgetGridItemUseCase,
    private val addApplicationInfoUseCase: AddApplicationInfoUseCase,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val addAppWidgetProviderInfoUseCase: AddAppWidgetProviderInfoUseCase,
    private val updateWidgetGridItemDataUseCase: UpdateWidgetGridItemDataUseCase,
    private val userDataRepository: UserDataRepository,
    private val movePageUseCase: MovePageUseCase,
    private val deletePageUseCase: DeletePageUseCase,
) : ViewModel() {
    val homeUiState = groupGridItemsByPageUseCase().map(HomeUiState::Success).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

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

    private var _pageDirection = MutableStateFlow<PageDirection?>(null)

    val pageDirection = _pageDirection.asStateFlow()

    private var _addGridItemDimensions = MutableStateFlow<GridItemDimensions?>(null)

    val addGridItemDimensions = _addGridItemDimensions.asStateFlow()

    private var gridItemJob: Job? = null

    private var gridItemDelayTimeInMillis = 500L

    fun moveGridItem(
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        viewModelScope.launch {
            gridItemJob?.cancelAndJoin()

            gridItemJob = launch {
                delay(gridItemDelayTimeInMillis)

                _pageDirection.update {
                    movePageUseCase(
                        gridItem = gridItem,
                        x = x,
                        screenWidth = screenWidth,
                    )
                }

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
            gridItemJob?.cancelAndJoin()

            gridItemJob = launch {
                delay(gridItemDelayTimeInMillis)

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
            gridItemJob?.cancelAndJoin()

            gridItemJob = launch {
                delay(gridItemDelayTimeInMillis)

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
            _addGridItemDimensions.update {
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
            _addGridItemDimensions.update {
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

    fun deletePage(page: Int) {
        viewModelScope.launch {
            deletePageUseCase(page = page)
        }
    }

    fun resetAddGridItemDimensions() {
        viewModelScope.launch {
            _addGridItemDimensions.update {
                null
            }
        }
    }
}