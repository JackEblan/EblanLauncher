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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.PackageManagerApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidPackageManagerWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
) : PackageManagerWrapper {

    private val packageManager = context.packageManager

    override suspend fun queryIntentActivities(): List<PackageManagerApplicationInfo> {
        val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return withContext(Dispatchers.Default) {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .map { resolveInfo ->
                    resolveInfo.activityInfo.applicationInfo.toPackageManagerApplicationInfo()
                }.sortedBy { applicationInfo ->
                    applicationInfo.label
                }
        }
    }

    override fun launchIntentForPackage(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }

    private suspend fun ApplicationInfo.toPackageManagerApplicationInfo(): PackageManagerApplicationInfo {
        val byteArray = withContext(Dispatchers.IO) {
            loadIcon(packageManager).toByteArray()
        }

        return PackageManagerApplicationInfo(
            icon = byteArray,
            packageName = packageName,
            label = loadLabel(packageManager).toString(),
        )
    }
}
