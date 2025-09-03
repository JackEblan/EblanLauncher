package com.eblan.launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.model.SettingsActivityThemeSettings
import com.eblan.launcher.model.SettingsActivityUiState
import com.eblan.launcher.navigation.SettingsNavHost
import com.eblan.launcher.viewmodel.SettingsActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var settingsActivityThemeSettings by mutableStateOf(
            SettingsActivityThemeSettings(
                themeBrand = ThemeBrand.Green,
                darkThemeConfig = DarkThemeConfig.System,
                dynamicTheme = false,
            ),
        )

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        SettingsActivityUiState.Loading -> {
                            enableEdgeToEdge()
                        }

                        is SettingsActivityUiState.Success -> {
                            settingsActivityThemeSettings = uiState.settingsActivityThemeSettings

                            handleEdgeToEdge(settingsActivityThemeSettings = uiState.settingsActivityThemeSettings)
                        }
                    }
                }
            }
        }

        setContent {
            val navController = rememberNavController()

            EblanLauncherTheme(
                themeBrand = settingsActivityThemeSettings.themeBrand,
                darkThemeConfig = settingsActivityThemeSettings.darkThemeConfig,
                dynamicTheme = settingsActivityThemeSettings.dynamicTheme,
            ) {
                Surface {
                    SettingsNavHost(
                        navController = navController,
                        onFinish = {
                            val intent = Intent(this, MainActivity::class.java)

                            startActivity(intent)

                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun handleEdgeToEdge(settingsActivityThemeSettings: SettingsActivityThemeSettings) {
        when (settingsActivityThemeSettings.darkThemeConfig) {
            DarkThemeConfig.System -> {
                enableEdgeToEdge()
            }

            DarkThemeConfig.Light -> {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(
                        scrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                    ),
                    navigationBarStyle = SystemBarStyle.light(
                        scrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                    ),
                )
            }

            DarkThemeConfig.Dark -> {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                )
            }
        }
    }
}