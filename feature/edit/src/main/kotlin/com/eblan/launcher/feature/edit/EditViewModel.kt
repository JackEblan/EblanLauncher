package com.eblan.launcher.feature.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.EblanLauncherApplicationInfo
import com.eblan.launcher.domain.model.GridItemType
import com.eblan.launcher.domain.repository.ApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.InMemoryApplicationInfoRepository
import com.eblan.launcher.feature.edit.navigation.EditRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    inMemoryApplicationInfoRepository: InMemoryApplicationInfoRepository,
    private val applicationInfoRepository: ApplicationInfoRepository,
    private val gridRepository: GridRepository,
) : ViewModel() {
    private val editRouteData = savedStateHandle.toRoute<EditRouteData>()

    val applicationInfos = inMemoryApplicationInfoRepository.applicationInfos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addApplicationInfo(
        type: GridItemType,
        packageName: String,
        flags: Int,
        icon: ByteArray?,
        label: String,
    ) {
        viewModelScope.launch {
            applicationInfoRepository.upsertApplicationInfo(
                gridItemId = editRouteData.id,
                applicationInfo = EblanLauncherApplicationInfo(
                    gridItemId = editRouteData.id,
                    packageName = packageName,
                    flags = flags,
                    icon = icon,
                    label = label,
                ),
            )

            gridRepository.updateGridItemType(id = editRouteData.id, type = type)
        }
    }
}