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

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.framework.drawable.AndroidDrawableWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultPackageManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
    private val androidDrawableWrapper: AndroidDrawableWrapper,
) : PackageManagerWrapper, AndroidPackageManagerWrapper {

    private val packageManager = context.packageManager

    override val hasSystemFeatureAppWidgets
        get() = packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)

    override suspend fun getApplicationIcon(packageName: String): ByteArray? {
        return withContext(defaultDispatcher) {
            try {
                androidDrawableWrapper.createByteArray(
                    drawable = packageManager.getApplicationIcon(
                        packageName,
                    ),
                )
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    override suspend fun getApplicationLabel(packageName: String): String? {
        return withContext(defaultDispatcher) {
            try {
                val applicationInfo =
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    override fun getComponentName(packageName: String): String? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        return launchIntent?.component?.flattenToString()
    }

    override fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        val defaultLauncherPackage = resolveInfo?.activityInfo?.packageName

        return defaultLauncherPackage == context.packageName
    }

    override suspend fun getIconPackInfoByPackageNames(): List<String> {
        val intents = listOf(
            Intent("app.lawnchair.icons.THEMED_ICON"),
            Intent("org.adw.ActivityStarter.THEMES"),
            Intent("com.novalauncher.THEME"),
            Intent("org.adw.launcher.THEMES"),
        )

        val resolveInfos = mutableSetOf<ResolveInfo>()

        return withContext(defaultDispatcher) {
            intents.forEach { intent ->
                resolveInfos.addAll(
                    packageManager.queryIntentActivities(
                        intent,
                        PackageManager.GET_META_DATA,
                    ),
                )
            }

            resolveInfos.map { resolveInfo ->
                resolveInfo.activityInfo.packageName
            }.distinct()
        }
    }

    override suspend fun getActivityIcon(
        componentName: String,
        packageName: String,
    ): ByteArray? {
        return withContext(defaultDispatcher) {
            try {
                val drawable = ComponentName.unflattenFromString(componentName)
                    ?.let(packageManager::getActivityIcon)

                if (drawable != null) {
                    androidDrawableWrapper.createByteArray(drawable = drawable)
                } else {
                    null
                }
            } catch (_: PackageManager.NameNotFoundException) {
                getApplicationIcon(packageName = packageName)
            }
        }
    }

    override fun isComponentExported(componentName: ComponentName): Boolean {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = componentName
        }

        val activityInfo =
            intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY)

        return activityInfo != null && activityInfo.exported
    }
}
