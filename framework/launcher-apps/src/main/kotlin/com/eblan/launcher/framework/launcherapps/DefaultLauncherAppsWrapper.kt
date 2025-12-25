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

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process.myUserHandle
import android.os.UserHandle
import androidx.annotation.RequiresApi
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsEvent
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.ShortcutQueryFlag
import com.eblan.launcher.framework.bytearray.AndroidByteArrayWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultLauncherAppsWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val androidByteArrayWrapper: AndroidByteArrayWrapper,
    private val userManagerWrapper: AndroidUserManagerWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : LauncherAppsWrapper, AndroidLauncherAppsWrapper {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    override val hasShortcutHostPermission
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && launcherApps.hasShortcutHostPermission()

    override val launcherAppsEvent: Flow<LauncherAppsEvent> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                if (packageName != null && user != null) {
                    trySend(
                        LauncherAppsEvent.PackageRemoved(
                            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = user),
                            packageName = packageName,
                        ),
                    )
                }
            }

            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                if (packageName != null && user != null) {
                    trySend(
                        LauncherAppsEvent.PackageAdded(
                            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = user),
                            packageName = packageName,
                        ),
                    )
                }
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                if (packageName != null && user != null) {
                    trySend(
                        LauncherAppsEvent.PackageChanged(
                            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = user),
                            packageName = packageName,
                        ),
                    )
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
    }.flowOn(defaultDispatcher)

    override suspend fun getActivityList(): List<LauncherAppsActivityInfo> {
        return withContext(defaultDispatcher) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                launcherApps.profiles.flatMap { userHandle ->
                    currentCoroutineContext().ensureActive()

                    launcherApps.getActivityList(null, userHandle).map { launcherActivityInfo ->
                        currentCoroutineContext().ensureActive()

                        launcherActivityInfo.toEblanLauncherActivityInfo()
                    }
                }
            } else {
                launcherApps.getActivityList(null, myUserHandle()).map { launcherActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    launcherActivityInfo.toEblanLauncherActivityInfo()
                }
            }
        }
    }

    override suspend fun getActivityList(
        serialNumber: Long,
        packageName: String,
    ): List<LauncherAppsActivityInfo> {
        val userHandle = userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber)

        return launcherApps.getActivityList(packageName, userHandle).map { launcherActivityInfo ->
            currentCoroutineContext().ensureActive()

            launcherActivityInfo.toEblanLauncherActivityInfo()
        }
    }

    override suspend fun getShortcuts(): List<LauncherAppsShortcutInfo>? {
        return withContext(defaultDispatcher) {
            if (hasShortcutHostPermission) {
                val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    launcherApps.profiles.filter { userHandle ->
                        userManagerWrapper.isUserRunning(userHandle = userHandle) &&
                            userManagerWrapper.isUserUnlocked(userHandle = userHandle) &&
                            !userManagerWrapper.isQuietModeEnabled(userHandle = userHandle)
                    }.flatMap { userHandle ->
                        currentCoroutineContext().ensureActive()

                        launcherApps.getShortcuts(shortcutQuery, userHandle)
                            ?.map { shortcutInfo ->
                                currentCoroutineContext().ensureActive()

                                shortcutInfo.toLauncherAppsShortcutInfo()
                            } ?: emptyList()
                    }
                } else {
                    launcherApps.getShortcuts(shortcutQuery, myUserHandle())?.map { shortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        shortcutInfo.toLauncherAppsShortcutInfo()
                    }
                }
            } else {
                null
            }
        }
    }

    override suspend fun getShortcutsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<LauncherAppsShortcutInfo>? {
        return withContext(defaultDispatcher) {
            val userHandle = userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber)

            if (hasShortcutHostPermission && userHandle != null) {
                val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                    setPackage(packageName)

                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                    )
                }

                launcherApps.getShortcuts(shortcutQuery, userHandle)?.map { shortcutInfo ->
                    currentCoroutineContext().ensureActive()

                    shortcutInfo.toLauncherAppsShortcutInfo()
                }
            } else {
                null
            }
        }
    }

    override suspend fun getShortcutConfigActivityList(
        serialNumber: Long,
        packageName: String,
    ): List<LauncherAppsActivityInfo> {
        return withContext(defaultDispatcher) {
            val userHandle = userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && userHandle != null) {
                launcherApps.getShortcutConfigActivityList(packageName, userHandle)
                    .map { launcherActivityInfo ->
                        currentCoroutineContext().ensureActive()

                        launcherActivityInfo.toEblanLauncherActivityInfo()
                    }
            } else {
                emptyList()
            }
        }
    }

    override fun startMainActivity(
        serialNumber: Long,
        componentName: String,
        sourceBounds: Rect,
    ) {
        val userHandle = userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber)

        if (userHandle != null) {
            launcherApps.startMainActivity(
                ComponentName.unflattenFromString(componentName),
                userHandle,
                sourceBounds,
                Bundle.EMPTY,
            )
        }
    }

    override fun startMainActivity(
        componentName: String,
        sourceBounds: Rect,
    ) {
        launcherApps.startMainActivity(
            ComponentName.unflattenFromString(componentName),
            myUserHandle(),
            sourceBounds,
            Bundle.EMPTY,
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getPinItemRequest(intent: Intent): LauncherApps.PinItemRequest {
        return launcherApps.getPinItemRequest(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun startShortcut(
        serialNumber: Long,
        packageName: String,
        id: String,
        sourceBounds: Rect,
    ) {
        val userHandle = userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber)

        try {
            if (userHandle != null &&
                userManagerWrapper.isUserRunning(userHandle = userHandle) &&
                userManagerWrapper.isUserUnlocked(userHandle = userHandle) &&
                !userManagerWrapper.isQuietModeEnabled(userHandle = userHandle)
            ) {
                launcherApps.startShortcut(
                    packageName,
                    id,
                    sourceBounds,
                    null,
                    userHandle,
                )
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun startShortcut(
        packageName: String,
        id: String,
        sourceBounds: Rect,
    ) {
        try {
            if (
                userManagerWrapper.isUserRunning(userHandle = myUserHandle()) &&
                userManagerWrapper.isUserUnlocked(userHandle = myUserHandle()) &&
                !userManagerWrapper.isQuietModeEnabled(userHandle = myUserHandle())
            ) {
                launcherApps.startShortcut(
                    packageName,
                    id,
                    sourceBounds,
                    null,
                    myUserHandle(),
                )
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun getShortcutIconDrawable(
        shortcutInfo: ShortcutInfo?,
        density: Int,
    ): Drawable? {
        return if (shortcutInfo != null) {
            launcherApps.getShortcutIconDrawable(shortcutInfo, density)
        } else {
            null
        }
    }

    override fun startAppDetailsActivity(
        serialNumber: Long,
        componentName: String,
        sourceBounds: Rect,
    ) {
        launcherApps.startAppDetailsActivity(
            ComponentName.unflattenFromString(componentName),
            userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber),
            sourceBounds,
            Bundle.EMPTY,
        )
    }

    override suspend fun getShortcutConfigIntent(
        serialNumber: Long,
        packageName: String,
        componentName: String,
    ): IntentSender? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !hasShortcutHostPermission) return null

        return withContext(defaultDispatcher) {
            val userHandle = userManagerWrapper.getUserForSerialNumber(serialNumber = serialNumber)

            val launcherActivityInfo = if (userHandle != null) {
                launcherApps.getShortcutConfigActivityList(packageName, userHandle)
                    .find { launcherActivityInfo ->
                        launcherActivityInfo.componentName.flattenToString() == componentName
                    }
            } else {
                null
            }

            launcherActivityInfo?.let(launcherApps::getShortcutConfigActivityIntent)
        }
    }

    private suspend fun LauncherActivityInfo.toEblanLauncherActivityInfo(): LauncherAppsActivityInfo {
        return LauncherAppsActivityInfo(
            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = user),
            componentName = componentName.flattenToString(),
            packageName = applicationInfo.packageName,
            activityIcon = getIcon(0).let { drawable ->
                androidByteArrayWrapper.createByteArray(drawable = drawable)
            },
            applicationIcon = applicationInfo.loadIcon(context.packageManager).let { drawable ->
                androidByteArrayWrapper.createByteArray(drawable = drawable)
            },
            label = label.toString(),
        )
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun ShortcutInfo.toLauncherAppsShortcutInfo(): LauncherAppsShortcutInfo {
        val icon = getShortcutIconDrawable(this, 0)?.let { drawable ->
            androidByteArrayWrapper.createByteArray(drawable = drawable)
        }

        val shortcutQueryFlag = when {
            isPinned -> {
                ShortcutQueryFlag.Pinned
            }

            isDynamic -> {
                ShortcutQueryFlag.Dynamic
            }

            else -> {
                ShortcutQueryFlag.Manifest
            }
        }

        return LauncherAppsShortcutInfo(
            shortcutId = id,
            packageName = `package`,
            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = userHandle),
            shortLabel = shortLabel.toString(),
            longLabel = longLabel.toString(),
            isEnabled = isEnabled,
            icon = icon,
            shortcutQueryFlag = shortcutQueryFlag,
        )
    }
}
