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
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val fileManager: FileManager,
    private val iconPackManager: IconPackManager,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userData.first()

            if (!userData.experimentalSettings.syncData) return@withContext

            updateEblanApplicationInfo(
                packageName = packageName,
                serialNumber = serialNumber,
                iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
            )

            updateEblanAppWidgetProviderInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateEblanShortcutInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    private suspend fun updateEblanApplicationInfo(
        packageName: String,
        serialNumber: Long,
        iconPackInfoPackageName: String,
    ) {
        val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        updateEblanApplicationInfos(
            launcherAppsActivityInfos = launcherAppsActivityInfos,
            iconPackInfoPackageName = iconPackInfoPackageName,
            eblanApplicationInfoRepository = eblanApplicationInfoRepository,
            launcherAppsWrapper = launcherAppsWrapper,
            fileManager = fileManager,
            eblanShortcutConfigRepository = eblanShortcutConfigRepository,
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            packageManagerWrapper = packageManagerWrapper,
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
            iconPackManager = iconPackManager,
        )
    }

    private suspend fun updateEblanAppWidgetProviderInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.serialNumber == serialNumber && appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }

        updateEblanAppWidgetProviderInfos(
            appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            eblanAppWidgetProviderInfoRepository = eblanAppWidgetProviderInfoRepository,
        )
    }

    private suspend fun updateEblanShortcutInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcutsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        updateEblanShortcutInfos(
            launcherAppsShortcutInfos = launcherAppsShortcutInfos,
            eblanShortcutInfoRepository = eblanShortcutInfoRepository,
            shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
        )
    }
}
