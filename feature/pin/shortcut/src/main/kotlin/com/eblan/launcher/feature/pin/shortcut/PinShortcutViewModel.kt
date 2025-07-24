package com.eblan.launcher.feature.pin.shortcut

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.AddPinShortcutToHomeScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinShortcutViewModel @Inject constructor(
    private val gridRepository: GridRepository,
    private val addPinShortcutToHomeScreenUseCase: AddPinShortcutToHomeScreenUseCase,
) : ViewModel() {
    private val _addedToHomeScreen = MutableStateFlow<GridItem?>(null)

    val addedGridItem = _addedToHomeScreen.asStateFlow()

    fun addToHomeScreen(
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

    fun deleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            gridRepository.deleteGridItem(gridItem = gridItem)
        }
    }
}