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
package com.eblan.launcher.ui.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.drawable.AndroidDrawableWrapper
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

val LocalAppWidgetHost = staticCompositionLocalOf<AndroidAppWidgetHostWrapper> {
    error("No AppWidgetHost provided")
}

val LocalAppWidgetManager = staticCompositionLocalOf<AndroidAppWidgetManagerWrapper> {
    error("No AppWidgetManager provided")
}

val LocalLauncherApps = staticCompositionLocalOf<AndroidLauncherAppsWrapper> {
    error("No LauncherAppsWrapper provided")
}

val LocalPinItemRequest = staticCompositionLocalOf<PinItemRequestWrapper> {
    error("No PinItemRequest provided")
}

val LocalWallpaperManager = staticCompositionLocalOf<AndroidWallpaperManagerWrapper> {
    error("No WallpaperManagerWrapper provided")
}

val LocalPackageManager = staticCompositionLocalOf<AndroidPackageManagerWrapper> {
    error("No AndroidPackageManager provided")
}

val LocalDrawable = staticCompositionLocalOf<AndroidDrawableWrapper> {
    error("No DrawableWrapper provided")
}

val LocalUserManager = staticCompositionLocalOf<AndroidUserManagerWrapper> {
    error("No UserManagerWrapper provided")
}
