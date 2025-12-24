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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.UserHandle
import com.eblan.launcher.domain.model.ManagedProfileResult
import com.eblan.launcher.domain.usecase.launcherapps.SyncDataUseCase
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncDataService : Service() {
    @Inject
    lateinit var syncDataUseCase: SyncDataUseCase

    @Inject
    lateinit var userManagerWrapper: AndroidUserManagerWrapper

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _managedProfileResult =
        MutableStateFlow<ManagedProfileResult?>(null)

    val managedProfileResult = _managedProfileResult.asStateFlow()

    @Suppress("DEPRECATION")
    private val managedProfileBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val userHandle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    Intent.EXTRA_USER,
                    UserHandle::class.java,
                )
            } else {
                intent.getParcelableExtra(Intent.EXTRA_USER)
            }

            if (userHandle != null) {
                _managedProfileResult.update {
                    ManagedProfileResult(
                        serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = userHandle),
                        isQuiteModeEnabled = userManagerWrapper.isQuietModeEnabled(userHandle = userHandle),
                    )
                }
            }

            serviceScope.launch {
                syncDataUseCase()
            }
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerReceiver(
            managedProfileBroadcastReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
                addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
                addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
            },
        )

        serviceScope.launch {
            syncDataUseCase()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(managedProfileBroadcastReceiver)

        serviceScope.cancel()
    }

    inner class LocalBinder : Binder() {
        fun getService(): SyncDataService = this@SyncDataService
    }
}
