package com.eblan.launcher.framework.launcherapps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import androidx.annotation.RequiresApi
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanLauncherActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidLauncherAppsWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManagerWrapper: PackageManagerWrapper,
) : LauncherAppsWrapper {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val userHandle = Process.myUserHandle()

    override val hasShortcutHostPermission =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && launcherApps.hasShortcutHostPermission()

    override val launcherAppsEvent: Flow<LauncherAppsEvent> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                if (packageName != null) {
                    trySend(LauncherAppsEvent.PackageRemoved(packageName = packageName))
                }
            }

            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                if (packageName != null) {
                    trySend(LauncherAppsEvent.PackageAdded(packageName = packageName))
                }
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                if (packageName != null) {
                    trySend(LauncherAppsEvent.PackageChanged(packageName = packageName))
                }
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                // TODO: Show installed applications
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                // TODO: Hide installed applications
            }

            override fun onShortcutsChanged(
                packageName: String,
                shortcuts: MutableList<ShortcutInfo>,
                user: UserHandle,
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    launch {
                        val launcherAppsShortcutInfo = shortcuts.map { shortcutInfo ->
                            shortcutInfo.toLauncherAppsShortcutInfo()
                        }

                        trySend(
                            LauncherAppsEvent.ShortcutsChanged(
                                packageName = packageName,
                                launcherAppsShortcutInfos = launcherAppsShortcutInfo,
                            ),
                        )
                    }
                }
            }
        }

        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))

        awaitClose {
            launcherApps.unregisterCallback(callback)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getActivityList(): List<EblanLauncherActivityInfo> {
        return withContext(Dispatchers.Default) {
            launcherApps.getActivityList(null, userHandle).map { launcherActivityInfo ->
                launcherActivityInfo.toEblanLauncherActivityInfo()
            }
        }
    }

    override fun startMainActivity(componentName: String?) {
        if (componentName != null) {
            launcherApps.startMainActivity(
                ComponentName.unflattenFromString(componentName), userHandle, Rect(), Bundle.EMPTY,
            )
        }
    }

    override suspend fun getShortcuts(): List<LauncherAppsShortcutInfo>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            }

            launcherApps.getShortcuts(shortcutQuery, userHandle)?.map { shortcutInfo ->
                shortcutInfo.toLauncherAppsShortcutInfo()
            }
        } else {
            null
        }
    }

    private suspend fun LauncherActivityInfo.toEblanLauncherActivityInfo(): EblanLauncherActivityInfo {
        val icon = packageManagerWrapper.getApplicationIcon(applicationInfo.packageName)

        return EblanLauncherActivityInfo(
            componentName = componentName.flattenToString(),
            packageName = applicationInfo.packageName,
            icon = icon,
            label = packageManagerWrapper.getApplicationLabel(applicationInfo.packageName)
                .toString(),
        )
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun ShortcutInfo.toLauncherAppsShortcutInfo(): LauncherAppsShortcutInfo {
        val icon = withContext(Dispatchers.Default) {
            launcherApps.getShortcutIconDrawable(this@toLauncherAppsShortcutInfo, 0).toByteArray()
        }

        return LauncherAppsShortcutInfo(
            id = id,
            packageName = `package`,
            shortLabel = shortLabel.toString(),
            longLabel = longLabel.toString(),
            icon = icon,
        )
    }
}
