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
package com.eblan.launcher.framework.launcherapps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import androidx.annotation.RequiresApi
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanLauncherActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultLauncherAppsWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManagerWrapper: PackageManagerWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : LauncherAppsWrapper, AndroidLauncherAppsWrapper {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val userHandle = Process.myUserHandle()

    override val hasShortcutHostPermission
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && launcherApps.hasShortcutHostPermission()

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
                if (hasShortcutHostPermission) {
                    launch {
                        val launcherAppsShortcutInfo = shortcuts.map { shortcutInfo ->
                            toLauncherAppsShortcutInfo(shortcutInfo = shortcutInfo)
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
    }.flowOn(defaultDispatcher)

    override suspend fun getActivityList(): List<EblanLauncherActivityInfo> {
        return withContext(defaultDispatcher) {
            launcherApps.getActivityList(null, userHandle).map { launcherActivityInfo ->
                launcherActivityInfo.toEblanLauncherActivityInfo()
            }
        }
    }

    override fun startMainActivity(componentName: String?) {
        if (componentName != null) {
            launcherApps.startMainActivity(
                ComponentName.unflattenFromString(componentName),
                userHandle,
                Rect(),
                Bundle.EMPTY,
            )
        }
    }

    override suspend fun getShortcuts(): List<LauncherAppsShortcutInfo>? {
        return if (hasShortcutHostPermission) {
            val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            }

            launcherApps.getShortcuts(shortcutQuery, userHandle)?.map { shortcutInfo ->
                toLauncherAppsShortcutInfo(shortcutInfo = shortcutInfo)
            }
        } else {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getPinItemRequest(intent: Intent): LauncherApps.PinItemRequest {
        return launcherApps.getPinItemRequest(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun startShortcut(packageName: String, id: String) {
        launcherApps.startShortcut(packageName, id, null, null, userHandle)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun getShortcutIconDrawable(shortcutInfo: ShortcutInfo, density: Int): Drawable {
        return launcherApps.getShortcutIconDrawable(shortcutInfo, density)
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
    private suspend fun toLauncherAppsShortcutInfo(shortcutInfo: ShortcutInfo): LauncherAppsShortcutInfo {
        val icon = getShortcutIconDrawable(shortcutInfo, 0).toByteArray()

        return LauncherAppsShortcutInfo(
            shortcutId = shortcutInfo.id,
            packageName = shortcutInfo.`package`,
            shortLabel = shortcutInfo.shortLabel.toString(),
            longLabel = shortcutInfo.longLabel.toString(),
            icon = icon,
        )
    }
}
