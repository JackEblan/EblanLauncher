package com.eblan.launcher.feature.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.AppWidgetHostDomainWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.AddPinShortcutToHomeScreenUseCase
import com.eblan.launcher.domain.usecase.AddPinWidgetToHomeScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val gridRepository: GridRepository,
    private val addPinShortcutToHomeScreenUseCase: AddPinShortcutToHomeScreenUseCase,
    private val addPinWidgetToHomeScreenUseCase: AddPinWidgetToHomeScreenUseCase,
    private val appWidgetHostDomainWrapper: AppWidgetHostDomainWrapper,
) : ViewModel() {
    private val _gridItem = MutableStateFlow<GridItem?>(null)

    val gridItem = _gridItem.asStateFlow()

    private val _isBoundWidget = MutableStateFlow(false)

    val isBoundWidget = _isBoundWidget.asStateFlow()

    fun addPinShortcutToHomeScreen(
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray,
    ) {
        viewModelScope.launch {
            _gridItem.update {
                addPinShortcutToHomeScreenUseCase(
                    id = id,
                    packageName = packageName,
                    shortLabel = shortLabel,
                    longLabel = longLabel,
                    byteArray = byteArray,
                )
            }
        }
    }

    fun addPinWidgetToHomeScreen(
        className: String,
        componentName: String,
        configure: String?,
        packageName: String,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
    ) {
        viewModelScope.launch {
            _gridItem.update {
                addPinWidgetToHomeScreenUseCase(
                    className = className,
                    componentName = componentName,
                    configure = configure,
                    packageName = packageName,
                    targetCellHeight = targetCellHeight,
                    targetCellWidth = targetCellWidth,
                    minWidth = minWidth,
                    minHeight = minHeight,
                    resizeMode = resizeMode,
                    minResizeWidth = minResizeWidth,
                    minResizeHeight = minResizeHeight,
                    maxResizeWidth = maxResizeWidth,
                    maxResizeHeight = maxResizeHeight,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                )
            }
        }
    }

    fun deleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            gridRepository.deleteGridItem(gridItem = gridItem)
        }
    }

    fun deleteWidgetGridItem(gridItem: GridItem, appWidgetId: Int) {
        viewModelScope.launch {
            appWidgetHostDomainWrapper.deleteAppWidgetId(appWidgetId = appWidgetId)

            gridRepository.deleteGridItem(gridItem = gridItem)
        }
    }

    fun updateGridItemData(id: Int, data: GridItemData) {
        viewModelScope.launch {
            gridRepository.updateGridItemData(id = id, data = data)

            _isBoundWidget.update {
                true
            }
        }
    }
}