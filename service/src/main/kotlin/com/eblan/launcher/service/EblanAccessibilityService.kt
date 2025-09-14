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

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.eblan.launcher.domain.framework.PerformGlobalAction
import com.eblan.launcher.domain.model.GlobalAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class EblanAccessibilityService : AccessibilityService() {
    @Inject
    lateinit var performGlobalAction: PerformGlobalAction

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        serviceScope.launch {
            performGlobalAction.globalAction.collect { globalAction ->
                when (globalAction) {
                    GlobalAction.Notifications -> {
                        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                    }

                    GlobalAction.QuickSettings -> {
                        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
                    }

                    GlobalAction.LockScreen -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                        }
                    }

                    GlobalAction.Recents -> {
                        performGlobalAction(GLOBAL_ACTION_RECENTS)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
    }
}
