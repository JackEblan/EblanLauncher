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
package com.eblan.launcher.feature.home

import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.framework.bytearray.AndroidByteArrayWrapper
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper

internal suspend fun handlePinItemRequest(
    pinItemRequest: PinItemRequest?,
    context: Context,
    launcherAppsWrapper: AndroidLauncherAppsWrapper,
    byteArrayWrapper: AndroidByteArrayWrapper,
    userManager: AndroidUserManagerWrapper,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null) {
        when (pinItemRequest.requestType) {
            PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                val appWidgetProviderInfo =
                    pinItemRequest.getAppWidgetProviderInfo(context)

                if (appWidgetProviderInfo != null) {
                    val preview = appWidgetProviderInfo.loadPreviewImage(context, 0)?.let {
                        byteArrayWrapper.createByteArray(drawable = it)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        onGetPinGridItem(
                            PinItemRequestType.Widget(
                                appWidgetId = 0,
                                componentName = appWidgetProviderInfo.provider.flattenToString(),
                                packageName = appWidgetProviderInfo.provider.packageName,
                                serialNumber = userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile),
                                configure = appWidgetProviderInfo.configure.flattenToString(),
                                minWidth = appWidgetProviderInfo.minWidth,
                                minHeight = appWidgetProviderInfo.minHeight,
                                resizeMode = appWidgetProviderInfo.resizeMode,
                                minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                                minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                                maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                                maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                                targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                                targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                                preview = preview,
                            ),
                        )
                    } else {
                        onGetPinGridItem(
                            PinItemRequestType.Widget(
                                appWidgetId = 0,
                                componentName = appWidgetProviderInfo.provider.flattenToString(),
                                packageName = appWidgetProviderInfo.provider.packageName,
                                serialNumber = userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile),
                                configure = appWidgetProviderInfo.configure.flattenToString(),
                                minWidth = appWidgetProviderInfo.minWidth,
                                minHeight = appWidgetProviderInfo.minHeight,
                                resizeMode = appWidgetProviderInfo.resizeMode,
                                minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                                minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                                maxResizeWidth = 0,
                                maxResizeHeight = 0,
                                targetCellHeight = 0,
                                targetCellWidth = 0,
                                preview = preview,
                            ),
                        )
                    }
                }
            }

            PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                val shortcutInfo = pinItemRequest.shortcutInfo

                if (shortcutInfo != null) {
                    onGetPinGridItem(
                        PinItemRequestType.ShortcutInfo(
                            serialNumber = userManager.getSerialNumberForUser(userHandle = shortcutInfo.userHandle),
                            shortcutId = shortcutInfo.id,
                            packageName = shortcutInfo.`package`,
                            shortLabel = shortcutInfo.shortLabel.toString(),
                            longLabel = shortcutInfo.longLabel.toString(),
                            isEnabled = shortcutInfo.isEnabled,
                            disabledMessage = shortcutInfo.disabledMessage?.toString(),
                            icon = launcherAppsWrapper.getShortcutIconDrawable(
                                shortcutInfo = shortcutInfo,
                                density = 0,
                            )?.let {
                                byteArrayWrapper.createByteArray(drawable = it)
                            },
                        ),
                    )
                }
            }
        }
    }
}
