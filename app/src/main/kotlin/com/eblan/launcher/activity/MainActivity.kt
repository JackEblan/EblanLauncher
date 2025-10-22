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
package com.eblan.launcher.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.framework.drawable.AndroidDrawableWrapper
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.model.MainActivityUiState
import com.eblan.launcher.navigation.MainNavHost
import com.eblan.launcher.service.ApplicationInfoService
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalDrawable
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalWallpaperManager
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var applicationInfoServiceIntent: Intent

    @Inject
    lateinit var androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper

    @Inject
    lateinit var androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper

    @Inject
    lateinit var androidLauncherAppsWrapper: AndroidLauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    @Inject
    lateinit var androidPackageManagerWrapper: AndroidPackageManagerWrapper

    @Inject
    lateinit var androidDrawableWrapper: AndroidDrawableWrapper

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalAppWidgetHost provides androidAppWidgetHostWrapper,
                LocalAppWidgetManager provides androidAppWidgetManagerWrapper,
                LocalLauncherApps provides androidLauncherAppsWrapper,
                LocalPinItemRequest provides pinItemRequestWrapper,
                LocalWallpaperManager provides androidWallpaperManagerWrapper,
                LocalPackageManager provides androidPackageManagerWrapper,
                LocalDrawable provides androidDrawableWrapper,
            ) {
                val navController = rememberNavController()

                val mainActivityUiState by viewModel.mainActivityUiState.collectAsStateWithLifecycle()

                when (val state = mainActivityUiState) {
                    MainActivityUiState.Loading -> {
                        enableEdgeToEdge()
                    }

                    is MainActivityUiState.Success -> {
                        SideEffect {
                            handleEdgeToEdge(darkThemeConfig = state.applicationTheme.darkThemeConfig)
                        }

                        EblanLauncherTheme(
                            themeBrand = state.applicationTheme.themeBrand,
                            darkThemeConfig = state.applicationTheme.darkThemeConfig,
                            dynamicTheme = state.applicationTheme.dynamicTheme,
                        ) {
                            MainNavHost(
                                navController = navController,
                                onSettings = {
                                    startActivity(Intent(this, SettingsActivity::class.java))

                                    finish()
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        applicationInfoServiceIntent = Intent(this, ApplicationInfoService::class.java)

        startService(applicationInfoServiceIntent)

        androidAppWidgetHostWrapper.startListening()
    }

    override fun onStop() {
        super.onStop()

        androidAppWidgetHostWrapper.stopListening()
    }
}
