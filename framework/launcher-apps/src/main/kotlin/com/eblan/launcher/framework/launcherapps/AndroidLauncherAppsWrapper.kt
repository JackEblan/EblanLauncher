package com.eblan.launcher.framework.launcherapps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanLauncherActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AndroidLauncherAppsWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManagerWrapper: PackageManagerWrapper,
) :
    LauncherAppsWrapper, LauncherAppsController {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val userHandle
        get() = Process.myUserHandle()

    override val launcherAppsEvent: Flow<LauncherAppsEvent> = callbackFlow {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                if (packageName != null) {
                    trySendBlocking(LauncherAppsEvent.PackageRemoved(packageName = packageName))
                }
            }

            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                if (packageName != null) {
                    trySendBlocking(LauncherAppsEvent.PackageAdded(packageName = packageName))
                }
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                if (packageName != null) {
                    trySendBlocking(LauncherAppsEvent.PackageChanged(packageName = packageName))
                }
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                // TODO: Later?
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                // TODO: Later?
            }
        }

        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))

        awaitClose {
            launcherApps.unregisterCallback(callback)
        }
    }

    override suspend fun getActivityList(): List<EblanLauncherActivityInfo> {
        return withContext(Dispatchers.Default) {
            launcherApps.getActivityList(null, userHandle).map { launcherActivityInfo ->
                launcherActivityInfo.toEblanLauncherActivityInfo()
            }
        }
    }

    override fun startMainActivity(
        component: ComponentName?,
        sourceBounds: Rect,
        opts: Bundle?,
    ) {
        launcherApps.startMainActivity(
            component, userHandle, sourceBounds, opts,
        )
    }

    private suspend fun LauncherActivityInfo.toEblanLauncherActivityInfo(): EblanLauncherActivityInfo {
        val byteArray = packageManagerWrapper.getApplicationIcon(applicationInfo.packageName)

        return EblanLauncherActivityInfo(
            componentName = componentName.flattenToString(),
            packageName = applicationInfo.packageName,
            icon = byteArray,
            label = packageManagerWrapper.getApplicationLabel(applicationInfo.packageName)
                .toString(),
        )
    }
}