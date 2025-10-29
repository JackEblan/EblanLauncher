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
package com.eblan.launcher.framework.notificationmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.eblan.launcher.domain.framework.NotificationManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultNotificationManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    NotificationManagerWrapper, AndroidNotificationManagerWrapper {
    private val notificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createNotificationChannel(
        channelId: String,
        name: String,
        importance: Int,
    ) {
        val channel = NotificationChannel(
            channelId,
            name,
            importance,
        )

        notificationManager.createNotificationChannel(channel)
    }

    override fun notify(id: Int, notification: Notification) {
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(id, notification)
        }
    }

    override fun cancelSyncData() {
        notificationManager.cancel(AndroidNotificationManagerWrapper.GRID_ITEMS_SYNC_NOTIFICATION_ID)
    }

    override fun notifySyncData(
        contentTitle: String,
        contentText: String,
    ) {
        if (notificationManager.areNotificationsEnabled()) {
            val notification = NotificationCompat.Builder(
                context,
                AndroidNotificationManagerWrapper.CHANNEL_ID,
            ).setSmallIcon(R.drawable.baseline_cached_24)
                .setContentTitle("Syncing data")
                .setContentText("Editing grid items may cause unsaved changes").setOngoing(true)
                .setProgress(0, 0, true).build()

            notificationManager.notify(
                AndroidNotificationManagerWrapper.GRID_ITEMS_SYNC_NOTIFICATION_ID,
                notification,
            )
        }
    }
}
