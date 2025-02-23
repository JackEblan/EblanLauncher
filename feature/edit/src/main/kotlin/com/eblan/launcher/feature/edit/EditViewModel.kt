package com.eblan.launcher.feature.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.feature.edit.navigation.EditRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

            gridRepository.updateGridItemData(
                id = editRouteData.id,
                data = data,
            )
        }
    }
}