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
package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.usecase.AddPackageUseCase
import com.eblan.launcher.domain.usecase.ChangePackageUseCase
import com.eblan.launcher.domain.usecase.ChangeShortcutsUseCase
import com.eblan.launcher.domain.usecase.RemovePackageUseCase
import com.eblan.launcher.domain.usecase.UpdateEblanAppWidgetProviderInfosUseCase
import com.eblan.launcher.domain.usecase.UpdateEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.UpdateEblanShortcutInfosUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationInfoService : Service() {
    @Inject
    lateinit var updateEblanApplicationInfosUseCase: UpdateEblanApplicationInfosUseCase

    @Inject
    lateinit var updateEblanAppWidgetProviderInfosUseCase: UpdateEblanAppWidgetProviderInfosUseCase

    @Inject
    lateinit var updateEblanShortcutInfosUseCase: UpdateEblanShortcutInfosUseCase

    @Inject
    lateinit var addPackageUseCase: AddPackageUseCase

    @Inject
    lateinit var removePackageUseCase: RemovePackageUseCase

    @Inject
    lateinit var changePackageUseCase: ChangePackageUseCase

    @Inject
    lateinit var changeShortcutsUseCase: ChangeShortcutsUseCase

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            launch {
                launcherAppsWrapper.launcherAppsEvent.collect { launcherAppsEvent ->
                    when (launcherAppsEvent) {
                        is LauncherAppsEvent.PackageAdded -> {
                            addPackageUseCase(
                                serialNumber = launcherAppsEvent.serialNumber,
                                packageName = launcherAppsEvent.packageName,
                            )
                        }

                        is LauncherAppsEvent.PackageChanged -> {
                            changePackageUseCase(
                                serialNumber = launcherAppsEvent.serialNumber,
                                packageName = launcherAppsEvent.packageName,
                            )
                        }

                        is LauncherAppsEvent.PackageRemoved -> {
                            removePackageUseCase(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.ShortcutsChanged -> {
                            changeShortcutsUseCase(
                                serialNumber = launcherAppsEvent.serialNumber,
                                packageName = launcherAppsEvent.packageName,
                                launcherAppsShortcutInfos = launcherAppsEvent.launcherAppsShortcutInfos,
                            )
                        }
                    }
                }
            }

            launch {
                updateEblanApplicationInfosUseCase()

                updateEblanAppWidgetProviderInfosUseCase()

                updateEblanShortcutInfosUseCase()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }
}
