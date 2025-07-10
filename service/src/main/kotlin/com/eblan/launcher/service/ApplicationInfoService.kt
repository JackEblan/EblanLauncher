package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.eblan.launcher.domain.framework.LauncherAppsDomainWrapper
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
import kotlinx.coroutines.flow.collectLatest
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
    lateinit var launcherAppsDomainWrapper: LauncherAppsDomainWrapper

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            launch {
                launcherAppsDomainWrapper.launcherAppsEvent.collectLatest { launcherAppsEvent ->
                    when (launcherAppsEvent) {
                        is LauncherAppsEvent.PackageAdded -> {
                            addPackageUseCase(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.PackageChanged -> {
                            changePackageUseCase(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.PackageRemoved -> {
                            removePackageUseCase(packageName = launcherAppsEvent.packageName)
                        }

                        is LauncherAppsEvent.ShortcutsChanged -> {
                            changeShortcutsUseCase(
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }
}