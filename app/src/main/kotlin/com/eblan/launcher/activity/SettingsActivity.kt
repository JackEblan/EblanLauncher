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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.framework.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.model.SettingsActivityUiState
import com.eblan.launcher.navigation.SettingsNavHost
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.viewmodel.SettingsActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsActivityViewModel by viewModels()

    @Inject
    lateinit var androidPackageManagerWrapper: AndroidPackageManagerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalPackageManager provides androidPackageManagerWrapper,
            ) {
                val navController = rememberNavController()

                val settingsActivityUiState by viewModel.settingsActivityUiState.collectAsStateWithLifecycle()

                when (val state = settingsActivityUiState) {
                    SettingsActivityUiState.Loading -> {
                        SideEffect {
                            enableEdgeToEdge()
                        }

                        EblanLauncherTheme(
                            themeBrand = ThemeBrand.Green,
                            darkThemeConfig = DarkThemeConfig.System,
                            dynamicTheme = false,
                        ) {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    is SettingsActivityUiState.Success -> {
                        SideEffect {
                            handleEdgeToEdge(darkThemeConfig = state.themeSettings.darkThemeConfig)
                        }

                        EblanLauncherTheme(
                            themeBrand = state.themeSettings.themeBrand,
                            darkThemeConfig = state.themeSettings.darkThemeConfig,
                            dynamicTheme = state.themeSettings.dynamicTheme,
                        ) {
                            Surface {
                                SettingsNavHost(
                                    navController = navController,
                                    onFinish = {
                                        startActivity(Intent(this, MainActivity::class.java))

                                        finish()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
