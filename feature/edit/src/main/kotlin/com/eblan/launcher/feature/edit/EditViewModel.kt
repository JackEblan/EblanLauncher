package com.eblan.launcher.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.repository.ApplicationInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(private val applicationInfoRepository: ApplicationInfoRepository) :
    ViewModel() {
    val applicationInfos = applicationInfoRepository.applicationInfos.onStart {
        applicationInfoRepository.insertApplicationInfos()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}