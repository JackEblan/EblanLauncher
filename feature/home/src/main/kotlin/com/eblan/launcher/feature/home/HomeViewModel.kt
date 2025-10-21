/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home

import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.repository.FolderGridCacheRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.usecase.CachePageItemsUseCase
import com.eblan.launcher.domain.usecase.DeleteGridItemUseCase
import com.eblan.launcher.domain.usecase.GetEblanAppWidgetProviderInfosByLabelUseCase
import com.eblan.launcher.domain.usecase.GetEblanApplicationComponentUseCase
import com.eblan.launcher.domain.usecase.GetEblanApplicationInfosByLabelUseCase
import com.eblan.launcher.domain.usecase.GetEblanShortcutInfosByLabelUseCase
import com.eblan.launcher.domain.usecase.GetFolderDataByIdUseCase
import com.eblan.launcher.domain.usecase.GetGridItemsCacheUseCase
import com.eblan.launcher.domain.usecase.GetHomeDataUseCase
import com.eblan.launcher.domain.usecase.GetPinGridItemUseCase
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
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
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
    getHomeDataUseCase: GetHomeDataUseCase,
    private val gridCacheRepository: GridCacheRepository,
    private val folderGridCacheRepository: FolderGridCacheRepository,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    getEblanApplicationComponentUseCase: GetEblanApplicationComponentUseCase,
    private val cachePageItemsUseCase: CachePageItemsUseCase,
    private val updatePageItemsUseCase: UpdatePageItemsUseCase,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val updateGridItemsAfterResizeUseCase: UpdateGridItemsAfterResizeUseCase,
    private val updateGridItemsAfterMoveUseCase: UpdateGridItemsAfterMoveUseCase,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    private val moveFolderGridItemUseCase: MoveFolderGridItemUseCase,
    private val getFolderDataByIdUseCase: GetFolderDataByIdUseCase,
    getEblanApplicationInfosByLabelUseCase: GetEblanApplicationInfosByLabelUseCase,
    getEblanAppWidgetProviderInfosByLabelUseCase: GetEblanAppWidgetProviderInfosByLabelUseCase,
    getEblanShortcutInfosByLabelUseCase: GetEblanShortcutInfosByLabelUseCase,
    getGridItemsCacheUseCase: GetGridItemsCacheUseCase,
    private val deleteGridItemUseCase: DeleteGridItemUseCase,
    private val getPinGridItemUseCase: GetPinGridItemUseCase,
    private val userManagerWrapper: AndroidUserManagerWrapper,
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

    private val _pageItems = MutableStateFlow(emptyList<PageItem>())

    val pageItems = _pageItems.asStateFlow()

    private var moveGridItemJob: Job? = null

    private val _foldersDataById = MutableStateFlow(ArrayDeque<FolderDataById>())

    val foldersDataById = _foldersDataById.asStateFlow()

    private val _eblanApplicationLabel = MutableStateFlow<String?>(null)

    private val _eblanAppWidgetProviderInfoLabel = MutableStateFlow<String?>(null)

    private val _eblanShortcutInfoLabel = MutableStateFlow<String?>(null)

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

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val eblanAppWidgetProviderInfosByLabel =
        _eblanAppWidgetProviderInfoLabel.filterNotNull()
            .debounce(defaultDelay)
            .flatMapLatest { label ->
                getEblanAppWidgetProviderInfosByLabelUseCase(label = label)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap(),
            )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val eblanShortcutInfosByLabel =
        _eblanShortcutInfoLabel.filterNotNull()
            .debounce(defaultDelay)
            .flatMapLatest { label ->
                getEblanShortcutInfosByLabelUseCase(label = label)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap(),
            )

    val gridItemsCache = getGridItemsCacheUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GridItemCache(
            gridItemsCacheByPage = emptyMap(),
            dockGridItemsCache = emptyList(),
            folderGridItemsCacheByPage = emptyMap(),
        ),
    )

    private val _pinGridItem = MutableStateFlow<GridItem?>(null)

    val pinGridItem = _pinGridItem.asStateFlow()

    fun moveGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
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
                    columns = columns,
                    rows = rows,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                )
            }
        }
    }

    fun resizeGridItem(
        resizingGridItem: GridItem,
        columns: Int,
        rows: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            resizeGridItemUseCase(
                resizingGridItem = resizingGridItem,
                columns = columns,
                rows = rows,
            )
        }
    }

    fun moveFolderGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
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
                    columns = columns,
                    rows = rows,
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

            delay(defaultDelay)

            _moveGridItemResult.update {
                null
            }

            _screen.update {
                screen
            }
        }
    }

    fun showFolderGridCache(
        gridItems: List<GridItem>,
        screen: Screen,
    ) {
        viewModelScope.launch {
            folderGridCacheRepository.insertGridItems(gridItems = gridItems)

            delay(defaultDelay)

            _moveGridItemResult.update {
                null
            }

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

            _pageItems.update {
                cachePageItemsUseCase(gridItems = gridItems)
            }

            delay(defaultDelay)

            _screen.update {
                Screen.EditPage
            }
        }
    }

    fun saveEditPage(
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            updatePageItemsUseCase(
                id = id,
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

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun resetGridCacheAfterMoveFolder() {
        viewModelScope.launch {
            val lastId = _foldersDataById.value.last().id

            updateGridItemsUseCase(gridItems = folderGridCacheRepository.gridItemsCache.first())

            getFolderDataByIdUseCase(id = lastId)?.let { folder ->
                _foldersDataById.update { currentFolders ->
                    ArrayDeque(currentFolders).apply {
                        val index = indexOfFirst { it.id == lastId }

                        set(index, folder)
                    }
                }

                delay(defaultDelay)

                _screen.update {
                    Screen.Folder
                }
            }
        }
    }

    fun cancelGridCache() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun cancelFolderDragGridCache() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            val lastId = _foldersDataById.value.last().id

            getFolderDataByIdUseCase(id = lastId)?.let { folder ->
                _foldersDataById.update { currentFolders ->
                    ArrayDeque(currentFolders).apply {
                        val index = indexOfFirst { it.id == lastId }

                        set(index, folder)
                    }
                }

                delay(defaultDelay)

                _screen.update {
                    Screen.Folder
                }
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

            updateGridItemsUseCase(gridItems = gridCacheRepository.gridItemsCache.first())

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
                        clear()

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

    fun getEblanApplicationInfosByLabel(label: String) {
        _eblanApplicationLabel.update {
            label
        }
    }

    fun getEblanAppWidgetProviderInfosByLabel(label: String) {
        _eblanAppWidgetProviderInfoLabel.update {
            label
        }
    }

    fun getEblanShortcutInfosByLabel(label: String) {
        _eblanShortcutInfoLabel.update {
            label
        }
    }

    fun deleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            deleteGridItemUseCase(gridItem = gridItem)
        }
    }

    fun getPinGridItem(pinItemRequestType: PinItemRequestType) {
        viewModelScope.launch {
            _pinGridItem.update {
                getPinGridItemUseCase(
                    serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = Process.myUserHandle()),
                    pinItemRequestType = pinItemRequestType,
                )
            }
        }
    }

    fun resetPinGridItem() {
        _pinGridItem.update {
            null
        }
    }
}
