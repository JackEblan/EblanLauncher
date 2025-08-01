package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.PageCacheRepository
import com.eblan.launcher.domain.usecase.CachePageItemsUseCase
import com.eblan.launcher.domain.usecase.GetEblanApplicationComponentUseCase
import com.eblan.launcher.domain.usecase.GetHomeDataUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemsUseCase
import com.eblan.launcher.domain.usecase.UpdatePageItemsUseCase
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHomeDataUseCase: GetHomeDataUseCase,
    private val gridCacheRepository: GridCacheRepository,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    getEblanApplicationComponentUseCase: GetEblanApplicationComponentUseCase,
    private val cachePageItemsUseCase: CachePageItemsUseCase,
    private val updatePageItemsUseCase: UpdatePageItemsUseCase,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    pageCacheRepository: PageCacheRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
) : ViewModel() {
    val homeUiState = getHomeDataUseCase().map(HomeUiState::Success).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    val eblanApplicationComponentUiState =
        getEblanApplicationComponentUseCase().map(EblanApplicationComponentUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EblanApplicationComponentUiState.Loading,
            )

    private var _screen = MutableStateFlow(Screen.Pager)

    val screen = _screen.asStateFlow()

    private var _movedGridItems = MutableStateFlow(false)

    val movedGridItems = _movedGridItems.asStateFlow()

    private var _updatedGridItem = MutableStateFlow<GridItem?>(null)

    val updatedGridItem = _updatedGridItem.asStateFlow()

    val pageItems = pageCacheRepository.pageItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun moveGridItem(
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        viewModelScope.launch {
            _movedGridItems.update {
                moveGridItemUseCase(
                    gridItems = gridItems.toMutableList(),
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

    fun resizeGridItem(
        gridItems: List<GridItem>,
        resizingGridItem: GridItem,
        rows: Int,
        columns: Int,
    ) {
        viewModelScope.launch {
            _movedGridItems.update {
                resizeGridItemUseCase(
                    gridItems = gridItems.toMutableList(),
                    resizingGridItem = resizingGridItem,
                    rows = rows,
                    columns = columns,
                ) != null
            }
        }
    }

    fun showGridCache(screen: Screen) {
        viewModelScope.launch {
            _updatedGridItem.update {
                null
            }

            gridCacheRepository.updateIsCache(isCache = true)

            _screen.update {
                screen
            }
        }
    }

    fun showPageCache() {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            cachePageItemsUseCase()

            _screen.update {
                Screen.EditPage
            }
        }
    }

    fun saveEditPage(
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            updatePageItemsUseCase(
                initialPage = initialPage,
                pageItems = pageItems,
                pageItemsToDelete = pageItemsToDelete,
            )

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun cancelEditPage() {
        _screen.update {
            Screen.Pager
        }
    }

    fun resetGridCache() {
        viewModelScope.launch {
            updateGridItemsUseCase()

            gridCacheRepository.updateIsCache(isCache = false)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun updateGridItemDataCache(gridItem: GridItem) {
        viewModelScope.launch {
            gridCacheRepository.updateGridItemData(id = gridItem.id, data = gridItem.data)

            _updatedGridItem.update {
                gridItem
            }
        }
    }

    fun deleteGridItemCache(gridItem: GridItem) {
        viewModelScope.launch {
            when (val data = gridItem.data) {
                is GridItemData.Widget -> {
                    appWidgetHostWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)

                    gridCacheRepository.deleteGridItem(gridItem = gridItem)
                }

                else -> {
                    gridCacheRepository.deleteGridItem(gridItem = gridItem)
                }
            }
        }
    }
}