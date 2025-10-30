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
import com.eblan.launcher.domain.usecase.ManualSyncDataUseCase
import com.eblan.launcher.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.eblan.launcher.framework.notificationmanager.R as NotificationManagerWrapperR

@AndroidEntryPoint
class ManualSyncDataService : Service() {
    @Inject
    lateinit var manualSyncDataUseCase: ManualSyncDataUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var syncDataJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        syncDataJob?.cancel()

        val notification =
            NotificationCompat.Builder(this, AndroidNotificationManagerWrapper.CHANNEL_ID)
                .setSmallIcon(NotificationManagerWrapperR.drawable.baseline_cached_24)
                .setContentTitle("Manual syncing data")
                .setContentText("This may take a while")
                .setOngoing(true)
                .setProgress(0, 0, true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                AndroidNotificationManagerWrapper.GRID_ITEMS_SYNC_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(
                AndroidNotificationManagerWrapper.GRID_ITEMS_SYNC_NOTIFICATION_ID,
                notification,
            )
        }

        serviceScope.launch {
            syncDataJob = launch {
                manualSyncDataUseCase()

                stopForeground(STOP_FOREGROUND_REMOVE)

                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }
}
