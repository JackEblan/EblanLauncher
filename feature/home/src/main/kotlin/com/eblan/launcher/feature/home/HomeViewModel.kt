package com.eblan.launcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.PageCacheRepository
import com.eblan.launcher.domain.usecase.CachePageItemsUseCase
import com.eblan.launcher.domain.usecase.GetEblanApplicationComponentUseCase
import com.eblan.launcher.domain.usecase.GetEblanApplicationInfosByLabelUseCase
import com.eblan.launcher.domain.usecase.GetFolderDataByIdUseCase
import com.eblan.launcher.domain.usecase.GetHomeDataUseCase
import com.eblan.launcher.domain.usecase.MoveFolderGridItemUseCase
import com.eblan.launcher.domain.usecase.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemsAfterMoveUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemsAfterResizeUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemsUseCase
import com.eblan.launcher.domain.usecase.UpdatePageItemsUseCase
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val gridCacheRepository: GridCacheRepository,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    getEblanApplicationComponentUseCase: GetEblanApplicationComponentUseCase,
    private val cachePageItemsUseCase: CachePageItemsUseCase,
    private val updatePageItemsUseCase: UpdatePageItemsUseCase,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    pageCacheRepository: PageCacheRepository,
    private val updateGridItemsAfterResizeUseCase: UpdateGridItemsAfterResizeUseCase,
    private val updateGridItemsAfterMoveUseCase: UpdateGridItemsAfterMoveUseCase,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    private val moveFolderGridItemUseCase: MoveFolderGridItemUseCase,
    private val getFolderDataByIdUseCase: GetFolderDataByIdUseCase,
    getEblanApplicationInfosByLabelUseCase: GetEblanApplicationInfosByLabelUseCase,
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

    private val _screen = MutableStateFlow(Screen.Pager)

    val screen = _screen.asStateFlow()

    private val _moveGridItemResult = MutableStateFlow<MoveGridItemResult?>(null)

    val movedGridItemResult = _moveGridItemResult.asStateFlow()

    private val defaultDelay = 500L

    val pageItems = pageCacheRepository.pageItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private var moveGridItemJob: Job? = null

    private val _foldersDataById = MutableStateFlow(ArrayDeque<FolderDataById>())

    val foldersDataById = _foldersDataById.asStateFlow()

    private val _eblanApplicationLabel = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val eblanApplicationInfosByLabel =
        _eblanApplicationLabel.filterNotNull()
            .debounce(defaultDelay)
            .flatMapLatest { label ->
                getEblanApplicationInfosByLabelUseCase(label = label)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun moveGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            _moveGridItemResult.update {
                moveGridItemUseCase(
                    movingGridItem = movingGridItem,
                    x = x,
                    y = y,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                )
            }
        }
    }

    fun resizeGridItem(
        resizingGridItem: GridItem,
        rows: Int,
        columns: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            resizeGridItemUseCase(
                resizingGridItem = resizingGridItem,
                rows = rows,
                columns = columns,
            )
        }
    }

    fun moveFolderGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            _moveGridItemResult.update {
                moveFolderGridItemUseCase(
                    movingGridItem = movingGridItem,
                    x = x,
                    y = y,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                )
            }
        }
    }

    fun showGridCache(
        gridItems: List<GridItem>,
        screen: Screen,
    ) {
        viewModelScope.launch {
            gridCacheRepository.insertGridItems(gridItems = gridItems)

            gridCacheRepository.updateIsCache(isCache = true)

            delay(defaultDelay)

            _screen.update {
                screen
            }
        }
    }

    fun showPageCache(gridItems: List<GridItem>) {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            cachePageItemsUseCase(gridItems = gridItems)

            delay(defaultDelay)

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

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun updateScreen(screen: Screen) {
        _screen.update {
            screen
        }
    }

    fun resetGridCacheAfterResize(resizingGridItem: GridItem) {
        viewModelScope.launch {
            updateGridItemsAfterResizeUseCase(resizingGridItem = resizingGridItem)

            gridCacheRepository.updateIsCache(isCache = false)

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun resetGridCacheAfterMove(
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            updateGridItemsAfterMoveUseCase(
                movingGridItem = movingGridItem,
                conflictingGridItem = conflictingGridItem,
            )

            gridCacheRepository.updateIsCache(isCache = false)

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }

            _moveGridItemResult.update {
                null
            }
        }
    }

    fun resetGridCacheAfterMoveFolder() {
        viewModelScope.launch {
            val lastId = _foldersDataById.value.last().id

            updateGridItemsUseCase(gridItems = gridCacheRepository.gridCacheItems.first())

            getFolderDataByIdUseCase(id = lastId)?.let { folder ->
                _foldersDataById.update { currentFolders ->
                    ArrayDeque(currentFolders).apply {
                        val index = indexOfFirst { it.id == lastId }

                        set(index, folder)
                    }
                }

                _screen.update {
                    Screen.Folder
                }
            }

            delay(defaultDelay)

            gridCacheRepository.updateIsCache(isCache = false)

            _moveGridItemResult.update {
                null
            }
        }
    }

    fun cancelGridCache() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            gridCacheRepository.updateIsCache(isCache = false)

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }

            _moveGridItemResult.update {
                null
            }
        }
    }

    fun updateGridItemDataCache(gridItem: GridItem) {
        viewModelScope.launch {
            gridCacheRepository.updateGridItemData(id = gridItem.id, data = gridItem.data)
        }
    }

    fun deleteGridItemCache(gridItem: GridItem) {
        viewModelScope.launch {
            gridCacheRepository.deleteGridItem(gridItem = gridItem)
        }
    }

    fun deleteWidgetGridItemCache(
        gridItem: GridItem,
        appWidgetId: Int,
    ) {
        viewModelScope.launch {
            appWidgetHostWrapper.deleteAppWidgetId(appWidgetId = appWidgetId)

            gridCacheRepository.deleteGridItem(gridItem = gridItem)

            updateGridItemsUseCase(gridItems = gridCacheRepository.gridCacheItems.first())

            gridCacheRepository.updateIsCache(isCache = false)

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun showFolder(id: String) {
        viewModelScope.launch {
            getFolderDataByIdUseCase(id = id)?.let { folder ->
                _foldersDataById.update { currentFolders ->
                    ArrayDeque(currentFolders).apply {
                        add(folder)
                    }
                }

                _screen.update {
                    Screen.Folder
                }
            }
        }
    }

    fun addFolder(id: String) {
        viewModelScope.launch {
            getFolderDataByIdUseCase(id = id)?.let { folder ->
                _foldersDataById.update { currentFolders ->
                    ArrayDeque(currentFolders).apply {
                        add(folder)
                    }
                }
            }
        }
    }

    fun removeLastFolder() {
        _foldersDataById.update { currentFolders ->
            ArrayDeque(currentFolders).apply {
                removeLast()
            }
        }
    }

    fun moveGridItemOutsideFolder() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            moveGridItemJob = launch {
                _screen.update {
                    Screen.Loading
                }

                delay(defaultDelay)

                _foldersDataById.update {
                    ArrayDeque()
                }

                updateGridItemsUseCase(gridItems = gridCacheRepository.gridCacheItems.first())

                gridCacheRepository.updateIsCache(isCache = false)

                gridCacheRepository.insertGridItems(gridItems = getHomeDataUseCase().first().gridItems)

                gridCacheRepository.updateIsCache(isCache = true)

                _screen.update {
                    Screen.Drag
                }
            }
        }
    }

    fun getEblanApplicationInfosByLabel(label: String) {
        _eblanApplicationLabel.update {
            label
        }
    }
}