package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.DeletePageUseCase
import com.eblan.launcher.domain.usecase.GroupGridItemsByPageUseCase
import com.eblan.launcher.domain.usecase.ShiftAlgorithmUseCase
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
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
import kotlinx.coroutines.flow.flatMapLatest
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
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val userDataRepository: UserDataRepository,
    private val deletePageUseCase: DeletePageUseCase,
    private val gridRepository: GridRepository,
    private val gridCacheRepository: GridCacheRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val shiftAlgorithmUseCase: ShiftAlgorithmUseCase,
) : ViewModel() {
    private val _isCache = MutableStateFlow(false)

    val homeUiState = _isCache.flatMapLatest { isCache ->
        groupGridItemsByPageUseCase(isCache = isCache)
    }.map(HomeUiState::Success).stateIn(
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

    private var _screen = MutableStateFlow(Screen.Pager)

    val screen = _screen.asStateFlow()

    private var gridItemJob: Job? = null

    private var gridItemDelayTimeInMillis = 100L

    private var _shiftedAlgorithm = MutableStateFlow<Boolean?>(null)

    val shiftedAlgorithm = _shiftedAlgorithm.asStateFlow()

    fun moveGridItem(
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) {
        viewModelScope.launch {
            gridItemJob?.cancelAndJoin()

            gridItemJob = launch {
                delay(gridItemDelayTimeInMillis)

                _shiftedAlgorithm.update {
                    shiftAlgorithmUseCase(
                        movingGridItem = gridItem,
                        rows = rows,
                        columns = columns,
                    ) != null
                }
            }
        }
    }

    fun resizeGridItem(
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) {
        viewModelScope.launch {
            gridItemJob?.cancelAndJoin()

            gridItemJob = launch {
                delay(gridItemDelayTimeInMillis)

                _shiftedAlgorithm.update {
                    shiftAlgorithmUseCase(
                        movingGridItem = gridItem,
                        rows = rows,
                        columns = columns,
                    ) != null
                }
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

            _screen.update {
                screen
            }

            _isCache.update {
                true
            }
        }
    }

    fun launchApplication(packageName: String) {
        packageManagerWrapper.launchIntentForPackage(packageName = packageName)
    }

    fun resetGridCache() {
        viewModelScope.launch {
            gridRepository.upsertGridItems(gridItems = gridCacheRepository.gridCacheItems.first())

            _screen.update {
                Screen.Pager
            }

            _isCache.update {
                false
            }
        }
    }
}