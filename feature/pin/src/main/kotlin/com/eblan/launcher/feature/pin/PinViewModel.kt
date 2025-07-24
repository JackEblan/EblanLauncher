package com.eblan.launcher.feature.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.GridItem
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
) : ViewModel() {
    private val _addedToHomeScreen = MutableStateFlow<GridItem?>(null)

    val addedGridItem = _addedToHomeScreen.asStateFlow()

    fun addPinShortcutToHomeScreen(
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray,
    ) {
        viewModelScope.launch {
            _addedToHomeScreen.update {
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
            _addedToHomeScreen.update {
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
}