package com.eblan.launcher.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eblan.launcher.domain.common.qualifier.ApplicationScope
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageBroadcastReceiver : BroadcastReceiver() {
    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope


    @Inject
    lateinit var packageManagerWrapper: PackageManagerWrapper


    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.data?.schemeSpecificPart ?: return

        appScope.launch {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                    actionPackageAdded(isReplacing = isReplacing, packageName = packageName)
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

                    actionPackageRemoved(isReplacing = isReplacing, packageName = packageName)
                }

                Intent.ACTION_PACKAGE_CHANGED -> {
                    // App components (like enabled activities) changed
                }

                Intent.ACTION_PACKAGE_REPLACED -> {
                    actionPackageReplaced(packageName = packageName)
                }
            }
        }
    }
}