package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.GroupGridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemsUseCase
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    groupGridItemsByPageUseCase: GroupGridItemsByPageUseCase,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    private val launcherAppsWrapper: LauncherAppsWrapper,
) : ViewModel() {
    val homeUiState = groupGridItemsByPageUseCase().map(HomeUiState::Success).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    val eblanApplicationInfos =
        eblanApplicationInfoRepository.eblanApplicationInfos.map { eblanApplicationInfos ->
            eblanApplicationInfos.sortedBy { eblanApplicationInfo ->
                eblanApplicationInfo.label
            }
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val eblanAppWidgetProviderInfosByGroup =
        eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfos ->
            eblanAppWidgetProviderInfos.sortedBy { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.eblanApplicationInfo.label
            }.groupBy { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.eblanApplicationInfo
            }
        }.flowOn(
            Dispatchers.Default,
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    private var _screen = MutableStateFlow(Screen.Pager)

    val screen = _screen.asStateFlow()

    private var _shiftedAlgorithm = MutableStateFlow<Boolean?>(null)

    val shiftedAlgorithm = _shiftedAlgorithm.asStateFlow()

    private var _targetPage = MutableStateFlow(0)

    val targetPage = _targetPage.asStateFlow()

    private var moveGridItemJob: Job? = null

    fun moveGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancel()

            moveGridItemJob = launch {
                delay(100L)

                _shiftedAlgorithm.update {
                    moveGridItemUseCase(
                        movingGridItem = movingGridItem,
                        x = x,
                        y = y,
                        rows = rows,
                        columns = columns,
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                    ) != null
                }
            }
        }
    }

    fun resizeGridItem(
        resizingGridItem: GridItem,
        rows: Int,
        columns: Int,
    ) {
        viewModelScope.launch {
            _shiftedAlgorithm.update {
                resizeGridItemUseCase(
                    resizingGridItem = resizingGridItem,
                    rows = rows,
                    columns = columns,
                ) != null
            }
        }
    }

    fun deleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            gridCacheRepository.deleteGridItem(gridItem = gridItem)
        }
    }

    fun showGridCache(screen: Screen) {
        viewModelScope.launch {
            gridCacheRepository.insertGridItems(gridItems = gridRepository.gridItems.first())

            gridCacheRepository.updateIsCache(isCache = true)

            _screen.update {
                screen
            }
        }
    }

    fun startMainActivity(componentName: String?) {
        launcherAppsWrapper.startMainActivity(componentName = componentName)
    }

    fun resetGridCache(currentPage: Int) {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            _targetPage.update {
                updateGridItemsUseCase(currentPage = currentPage)
            }

            gridCacheRepository.updateIsCache(isCache = false)

            _screen.update {
                Screen.Pager
            }
        }
    }
}