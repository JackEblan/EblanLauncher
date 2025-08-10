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
package com.eblan.launcher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.eblan.launcher.feature.settings.folder.navigation.folderSettingsScreen
import com.eblan.launcher.feature.settings.folder.navigation.navigateToFolderSettings
import com.eblan.launcher.feature.settings.gestures.navigation.gesturesSettingsScreen
import com.eblan.launcher.feature.settings.gestures.navigation.navigateToGesturesSettings
import com.eblan.launcher.feature.settings.home.navigation.homeSettingsScreen
import com.eblan.launcher.feature.settings.home.navigation.navigateToHomeSettings
import com.eblan.launcher.feature.settings.settings.navigation.SettingsRouteData
import com.eblan.launcher.feature.settings.settings.navigation.settingsScreen

@Composable
fun SettingsNavHost(
    navController: NavHostController,
    onFinish: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = SettingsRouteData::class,
    ) {
        settingsScreen(
            onFinish = onFinish,
            onGeneral = {},
            onHome = navController::navigateToHomeSettings,
            onAppDrawer = {},
            onFolder = navController::navigateToFolderSettings,
            onGestures = navController::navigateToGesturesSettings,
        )

        homeSettingsScreen(onNavigateUp = navController::navigateUp)

        folderSettingsScreen(onNavigateUp = navController::navigateUp)

        gesturesSettingsScreen(onNavigateUp = navController::navigateUp)
    }
}