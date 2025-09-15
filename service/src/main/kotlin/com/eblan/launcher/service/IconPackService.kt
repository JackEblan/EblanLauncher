package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.usecase.UpdateIconPackUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IconPackService : Service() {
    @Inject
    lateinit var updateIconPackUseCase: UpdateIconPackUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val iconPackPackageName = intent?.getStringExtra(IconPackManager.ICON_PACK_PACKAGE_NAME)

        serviceScope.launch {
            if (iconPackPackageName != null) {
                updateIconPackUseCase(iconPackPackageName = iconPackPackageName)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }
}