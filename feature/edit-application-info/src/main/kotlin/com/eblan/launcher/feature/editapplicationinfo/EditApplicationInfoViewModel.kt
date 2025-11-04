package com.eblan.launcher.feature.editapplicationinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.feature.editapplicationinfo.navigation.EditApplicationInfoRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditApplicationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
) : ViewModel() {
    private val editApplicationInfoRouteData =
        savedStateHandle.toRoute<EditApplicationInfoRouteData>()

    private val _editApplicationInfoUiState =
        MutableStateFlow<EditApplicationInfoUiState>(EditApplicationInfoUiState.Loading)

    val editApplicationInfoUiState = _editApplicationInfoUiState.onStart {
        getEblanApplicationInfo()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditApplicationInfoUiState.Loading,
    )

    fun updateEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo) {
        viewModelScope.launch {
            eblanApplicationInfoRepository.upsertEblanApplicationInfo(eblanApplicationInfo = eblanApplicationInfo)
        }
    }

    private fun getEblanApplicationInfo() {
        viewModelScope.launch {
            _editApplicationInfoUiState.update {
                EditApplicationInfoUiState.Success(
                    eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfo(
                        serialNumber = editApplicationInfoRouteData.serialNumber,
                        packageName = editApplicationInfoRouteData.packageName
                    )
                )
            }
        }
    }
}