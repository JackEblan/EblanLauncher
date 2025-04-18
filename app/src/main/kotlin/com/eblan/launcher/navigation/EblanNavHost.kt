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
import com.eblan.launcher.feature.edit.navigation.editScreen
import com.eblan.launcher.feature.edit.navigation.navigateToEdit
import com.eblan.launcher.feature.home.navigation.HomeRouteData
import com.eblan.launcher.feature.home.navigation.homeScreen

@Composable
fun EblanNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRouteData::class,
    ) {
        homeScreen(onEdit = navController::navigateToEdit)

        editScreen(onNavigationIconClick = navController::navigateUp)
    }
}