package com.eblan.launcher.feature.edit

import android.appwidget.AppWidgetProviderInfo
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.GetEblanApplicationInfoInstalledProvidersUseCase
import com.eblan.launcher.feature.edit.navigation.EditRouteData
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
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
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val getEblanApplicationInfoInstalledProvidersUseCase: GetEblanApplicationInfoInstalledProvidersUseCase,
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

    private val _eblanApplicationInfoInstalledProviders =
        MutableStateFlow(emptyList<EblanApplicationInfo>())

    val eblanApplicationInfoInstalledProviders = _eblanApplicationInfoInstalledProviders.onStart {
        getInstalledProviderPackageNames()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _installedProvidersByPackageName =
        MutableStateFlow(emptyList<AppWidgetProviderInfo>())

    val installedProvidersByPackageName = _installedProvidersByPackageName.asStateFlow()

    fun addApplicationInfo(
        packageName: String,
        label: String,
    ) {
        viewModelScope.launch {
            val icon =
                eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = packageName)?.icon

            val data = GridItemData.ApplicationInfo(
                packageName = packageName,
                icon = icon,
                label = label,
            )

            val rowsAffected = gridRepository.updateGridItemData(
                id = "",
                data = data,
            )

            _gridRepositoryUpdate.update {
                rowsAffected > 0
            }
        }
    }

    fun getInstalledProviderByPackageName(packageName: String) {
        viewModelScope.launch {
            _installedProvidersByPackageName.update {
                appWidgetManagerWrapper.getInstalledProviderByPackageName(packageName = packageName)
            }
        }
    }

    fun addWidget(
        appWidgetId: Int,
        minWidth: Int,
        minHeight: Int,
    ) {
        viewModelScope.launch {
//            val data = GridItemData.Widget(
//                appWidgetId = appWidgetId,
//                minWidth = minWidth,
//                minHeight = minHeight,
//            )
//
//            val rowsAffected = gridRepository.updateGridItemData(
//                id = editRouteData.id,
//                data = data,
//            )
//
//            _gridRepositoryUpdate.update {
//                rowsAffected > 0
//            }
        }
    }

    fun addWidgetAndroidTwelve(
        appWidgetId: Int,
        minWidth: Int,
        minHeight: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        targetCellWidth: Int,
        targetCellHeight: Int,
    ) {
        viewModelScope.launch {
//            val data = GridItemData.WidgetAndroidTwelve(
//                appWidgetId = appWidgetId,
//                minWidth = minWidth,
//                minHeight = minHeight,
//                minResizeWidth = minResizeWidth,
//                minResizeHeight = minResizeHeight,
//                targetCellWidth = targetCellWidth,
//                targetCellHeight = targetCellHeight,
//            )
//
//            val rowsAffected = gridRepository.updateGridItemData(
//                id = editRouteData.id,
//                data = data,
//            )
//
//            _gridRepositoryUpdate.update {
//                rowsAffected > 0
//            }
        }
    }

    private fun getInstalledProviderPackageNames() {
        viewModelScope.launch {
            val installedProviderPackageNames =
                getEblanApplicationInfoInstalledProvidersUseCase(installedProviderPackageNames = appWidgetManagerWrapper.getInstalledProviderPackageNames())

            _eblanApplicationInfoInstalledProviders.update {
                installedProviderPackageNames
            }
        }
    }

    private fun getGridItem() {
        viewModelScope.launch {
            _gridItem.update {
                gridRepository.getGridItem(id = "")
            }
        }
    }
}