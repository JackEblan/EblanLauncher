package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.repository.DockCacheRepository
import com.eblan.launcher.domain.repository.DockRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.DeletePageUseCase
import com.eblan.launcher.domain.usecase.GroupGridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.MovePageUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeWidgetGridItemUseCase
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.framework.wallpapermanager.WallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val userDataRepository: UserDataRepository,
    private val movePageUseCase: MovePageUseCase,
    private val deletePageUseCase: DeletePageUseCase,
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val dockRepository: DockRepository,
    private val dockCacheRepository: DockCacheRepository,
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
            applicationInfos.associateWith { eblanApplicationInfo ->
                appWidgetManagerWrapper.getInstalledProviderByPackageName(
                    packageName = eblanApplicationInfo.packageName,
                )
            }.filterValues { it.isNotEmpty() }
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val gridCacheItems = gridCacheRepository.gridCacheItems.map { gridItems ->
        gridItems.groupBy { gridItem -> gridItem.page }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    private var _pageDirection = MutableStateFlow<PageDirection?>(null)

    val pageDirection = _pageDirection.asStateFlow()

    private var _screen = MutableStateFlow(Screen.Pager)

    val screen = _screen.asStateFlow()

    private var _wallpaper = MutableStateFlow<ByteArray?>(null)

    val wallpaper = _wallpaper.onStart {
        getWallpaper()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val dockCacheItems = dockCacheRepository.dockCacheItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private var gridItemJob: Job? = null

    private var gridItemDelayTimeInMillis = 100L

    fun moveGridItem(
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
        dockHeight: Int,
    ) {
        viewModelScope.launch {
            gridItemJob?.cancelAndJoin()

            gridItemJob = launch {
                delay(gridItemDelayTimeInMillis)

                _pageDirection.update {
                    movePageUseCase(
                        gridItem = gridItem,
                        x = x,
                        gridWidth = gridWidth,
                    )
                }

                moveGridItemUseCase(
                    page = page,
                    gridItem = gridItem,
                    x = x,
                    y = y,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    dockHeight = dockHeight,
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

    fun updateWidgetGridItem(id: String, data: GridItemData, appWidgetId: Int) {
        viewModelScope.launch {
            if (data is GridItemData.Widget) {
                gridCacheRepository.updateGridItem(
                    id = id,
                    data = data.copy(appWidgetId = appWidgetId),
                )
            }
        }
    }

    fun deleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            gridCacheRepository.deleteGridItem(gridItem = gridItem)
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

    fun updateScreen(screen: Screen) {
        _screen.update {
            screen
        }
    }

    fun showGridCache(screen: Screen) {
        viewModelScope.launch {
            gridCacheRepository.insertGridItems(gridItems = gridRepository.gridItems.first())

            dockCacheRepository.insertDockItems(dockItems = dockRepository.dockItems.first())

            _screen.update {
                screen
            }
        }
    }

    fun launchApplication(packageName: String) {
        packageManagerWrapper.launchIntentForPackage(packageName = packageName)
    }

    fun getWallpaper() {
        viewModelScope.launch {
            _wallpaper.update {
                wallpaperManagerWrapper.getWallpaper()
            }
        }
    }

    fun resetGridCache() {
        viewModelScope.launch {
            gridRepository.upsertGridItems(gridItems = gridCacheRepository.gridCacheItems.first())

            dockRepository.upsertDockItems(dockItems = dockCacheRepository.dockCacheItems.first())

            _screen.update {
                Screen.Pager
            }
        }
    }
}