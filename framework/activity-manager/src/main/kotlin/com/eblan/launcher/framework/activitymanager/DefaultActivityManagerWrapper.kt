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
package com.eblan.launcher.framework.activitymanager

import android.app.ActivityManager
import android.content.Context
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultActivityManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : AndroidActivityManagerWrapper {
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    override suspend fun isInForeground(): Boolean {
        val runningAppProcessInfos = activityManager.runningAppProcesses ?: return false

        return withContext(defaultDispatcher) {
            runningAppProcessInfos.any { runningAppProcessInfo ->
                runningAppProcessInfo.processName == context.packageName &&
                    runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
    }
}
