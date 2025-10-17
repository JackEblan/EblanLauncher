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
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.usecase.UpdateIconPackInfosUseCase
import com.eblan.launcher.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IconPackInfoService : Service() {
    @Inject
    lateinit var updateIconPackInfosUseCase: UpdateIconPackInfosUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var iconPackInfoJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val iconPackInfoPackageName =
            intent?.getStringExtra(IconPackManager.ICON_PACK_INFO_PACKAGE_NAME)

        val iconPackInfoLabel = intent?.getStringExtra(IconPackManager.ICON_PACK_INFO_LABEL)

        if (iconPackInfoPackageName != null && iconPackInfoLabel != null) {
            iconPackInfoJob?.cancel()

            val notification =
                NotificationCompat.Builder(this, AndroidNotificationManagerWrapper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_cached_24)
                    .setContentTitle("Eblan Launcher")
                    .setContentText("Importing $iconPackInfoLabel to cache, this may take a few seconds")
                    .setOngoing(true)
                    .setProgress(0, 0, true)
                    .build()

            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }

            ServiceCompat.startForeground(
                this,
                AndroidNotificationManagerWrapper.ICON_PACK_INFO_SERVICE_NOTIFICATION_ID,
                notification,
                foregroundServiceType,
            )

            serviceScope.launch {
                iconPackInfoJob = launch {
                    updateIconPackInfosUseCase(packageName = iconPackInfoPackageName)

                    stopForeground(STOP_FOREGROUND_REMOVE)

                    stopSelf()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }
}
