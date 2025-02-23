package com.eblan.launcher.feature.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.feature.edit.navigation.EditRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val gridRepository: GridRepository,
) : ViewModel() {
    private val editRouteData = savedStateHandle.toRoute<EditRouteData>()

    val applicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _gridItem = MutableStateFlow<GridItem?>(null)

    val gridItem = _gridItem.onStart {
        getGridItem()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _gridRepositoryUpdate = MutableStateFlow<Boolean?>(null)

    val gridRepositoryUpdate = _gridRepositoryUpdate.asStateFlow()

    fun addApplicationInfo(
        packageName: String,
        label: String,
    ) {
        viewModelScope.launch {
            val icon =
                eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = packageName)?.icon

            val data = GridItemData.ApplicationInfo(
                gridItemId = editRouteData.id,
                packageName = packageName,
                icon = icon,
                label = label,
            )

            val rowsAffected = gridRepository.updateGridItemData(
                id = editRouteData.id,
                data = data,
            )

            _gridRepositoryUpdate.update {
                rowsAffected > 0
            }
        }
    }

    private fun getGridItem() {
        viewModelScope.launch {
            _gridItem.update {
                gridRepository.getGridItem(id = editRouteData.id)
            }
        }
    }
}