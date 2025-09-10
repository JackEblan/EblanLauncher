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
package com.eblan.launcher.framework.packagemanager

import android.content.Context
import android.content.pm.PackageManager
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultPackageManagerWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : PackageManagerWrapper {

    private val packageManager = context.packageManager

    override suspend fun getApplicationIcon(packageName: String): ByteArray? {
        return try {
            packageManager.getApplicationIcon(packageName).toByteArray()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun getApplicationLabel(packageName: String): String? {
        return withContext(defaultDispatcher) {
            try {
                val applicationInfo =
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    override fun getComponentName(packageName: String): String? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        return launchIntent?.component?.flattenToString()
    }
}
