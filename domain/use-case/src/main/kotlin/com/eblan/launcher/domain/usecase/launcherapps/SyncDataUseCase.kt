/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.domain.usecase.launcherapps

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.FastAppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.FastLauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SyncDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val iconPackManager: IconPackManager,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userData.first()

            launch {
                updateEblanLauncherAppsActivityInfos(userData = userData)
            }

            launch {
                updateAppWidgetManagerAppWidgetProviderInfos()
            }

            launch {
                updateEblanLauncherShortcutInfos()
            }

            launch {
                updateIconPackInfos(
                    iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                    iconPackManager = iconPackManager,
                    launcherAppsWrapper = launcherAppsWrapper,
                    fileManager = fileManager,
                )
            }
        }
    }

    private suspend fun updateEblanLauncherShortcutInfos() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val oldFastLauncherAppsShortcutInfos =
            eblanShortcutInfoRepository.eblanShortcutInfos.first().map { eblanShortcutInfo ->
                FastLauncherAppsShortcutInfo(
                    packageName = eblanShortcutInfo.packageName,
                    serialNumber = eblanShortcutInfo.serialNumber,
                    lastUpdateTime = eblanShortcutInfo.lastUpdateTime,
                )
            }

        val newFastLauncherAppsShortcutInfos = launcherAppsWrapper.getFastShortcuts()

        if (oldFastLauncherAppsShortcutInfos == newFastLauncherAppsShortcutInfos) return

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts()

        updateEblanShortcutInfos(
            launcherAppsShortcutInfos = launcherAppsShortcutInfos,
            eblanShortcutInfoRepository = eblanShortcutInfoRepository,
            shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
        )
    }

    private suspend fun updateEblanLauncherAppsActivityInfos(userData: UserData) {
        val oldFastEblanLauncherAppsActivityInfo =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()
                .map { eblanApplicationInfo ->
                    FastLauncherAppsActivityInfo(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        packageName = eblanApplicationInfo.packageName,
                        lastUpdateTime = eblanApplicationInfo.lastUpdateTime,
                    )
                }

        val newFastLauncherAppsActivityInfo = launcherAppsWrapper.getFastActivityList()

        if (oldFastEblanLauncherAppsActivityInfo == newFastLauncherAppsActivityInfo) return

        val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

        updateEblanApplicationInfos(
            launcherAppsActivityInfos = launcherAppsActivityInfos,
            iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
            eblanApplicationInfoRepository = eblanApplicationInfoRepository,
            launcherAppsWrapper = launcherAppsWrapper,
            fileManager = fileManager,
            eblanShortcutConfigRepository = eblanShortcutConfigRepository,
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            packageManagerWrapper = packageManagerWrapper,
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
            iconPackManager = iconPackManager,
        )

        insertApplicationInfoGridItems(
            launcherAppsActivityInfos = launcherAppsActivityInfos,
            experimentalSettings = userData.experimentalSettings,
            homeSettings = userData.homeSettings,
        )
    }

    private suspend fun updateAppWidgetManagerAppWidgetProviderInfos() {
        val oldFastAppWidgetManagerAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()
                .map { eblanAppWidgetProviderInfo ->
                    FastAppWidgetManagerAppWidgetProviderInfo(
                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
                        packageName = eblanAppWidgetProviderInfo.packageName,
                        lastUpdateTime = eblanAppWidgetProviderInfo.lastUpdateTime,
                    )
                }

        val newFastAppWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerWrapper.getFastInstalledProviders()

        if (oldFastAppWidgetManagerAppWidgetProviderInfos == newFastAppWidgetManagerAppWidgetProviderInfos) return

        val appWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerWrapper.getInstalledProviders()

        updateEblanAppWidgetProviderInfos(
            appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            eblanAppWidgetProviderInfoRepository = eblanAppWidgetProviderInfoRepository,
        )

        updateWidgetGridItems(
            appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            widgetGridItemRepository = widgetGridItemRepository,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun insertApplicationInfoGridItems(
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
        experimentalSettings: ExperimentalSettings,
        homeSettings: HomeSettings,
    ) {
        if (!experimentalSettings.firstLaunch) return

        @OptIn(ExperimentalUuidApi::class)
        suspend fun insertApplicationInfoGridItem(
            index: Int,
            launcherAppsActivityInfo: LauncherAppsActivityInfo,
            columns: Int,
            associate: Associate,
        ) {
            val startColumn = index % columns

            val startRow = index / columns

            applicationInfoGridItemRepository.insertApplicationInfoGridItem(
                applicationInfoGridItem = ApplicationInfoGridItem(
                    id = Uuid.random().toHexString(),
                    folderId = null,
                    page = 0,
                    startColumn = startColumn,
                    startRow = startRow,
                    columnSpan = 1,
                    rowSpan = 1,
                    associate = associate,
                    componentName = launcherAppsActivityInfo.componentName,
                    packageName = launcherAppsActivityInfo.packageName,
                    icon = launcherAppsActivityInfo.activityIcon,
                    label = launcherAppsActivityInfo.activityLabel,
                    override = false,
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    customIcon = null,
                    customLabel = null,
                    gridItemSettings = homeSettings.gridItemSettings,
                    doubleTap = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeUp = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeDown = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                ),
            )
        }

        launcherAppsActivityInfos.take(homeSettings.columns * homeSettings.rows)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    launcherAppsActivityInfo = launcherAppsActivityInfo,
                    columns = homeSettings.columns,
                    associate = Associate.Grid,
                )
            }

        launcherAppsActivityInfos.drop(homeSettings.columns * homeSettings.rows)
            .take(homeSettings.dockColumns * homeSettings.dockRows)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    launcherAppsActivityInfo = launcherAppsActivityInfo,
                    columns = homeSettings.dockColumns,
                    associate = Associate.Dock,
                )
            }

        userDataRepository.updateExperimentalSettings(
            experimentalSettings = experimentalSettings.copy(
                firstLaunch = false,
            ),
        )
    }
}
