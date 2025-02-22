package com.eblan.launcher.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eblan.launcher.domain.common.ApplicationScope
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.repository.InMemoryApplicationInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var inMemoryApplicationInfoRepository: InMemoryApplicationInfoRepository

    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        appScope.launch {
            inMemoryApplicationInfoRepository.updateInMemoryApplicationInfos(packageManagerWrapper.queryIntentActivities())
        }
    }
}