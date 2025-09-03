package com.eblan.launcher.feature.settings.general

import androidx.lifecycle.ViewModel
import com.eblan.launcher.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel@Inject constructor(private val userDataRepository: UserDataRepository) :
    ViewModel() {
}