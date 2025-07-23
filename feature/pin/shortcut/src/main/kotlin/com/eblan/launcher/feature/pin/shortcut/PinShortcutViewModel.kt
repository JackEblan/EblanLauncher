package com.eblan.launcher.feature.pin.shortcut

import androidx.lifecycle.ViewModel
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.FindGridItemAvailableRegionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PinShortcutViewModel @Inject constructor(
    private val gridRepository: GridRepository,
    private val findGridItemAvailableRegionUseCase: FindGridItemAvailableRegionUseCase,
) : ViewModel() {

    fun findGridItemAvailableRegion(gridItem: GridItem) {

    }
}