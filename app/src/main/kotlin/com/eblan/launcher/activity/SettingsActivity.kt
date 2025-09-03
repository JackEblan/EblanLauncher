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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.model.SettingsActivityUiState
import com.eblan.launcher.navigation.SettingsNavHost
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.viewmodel.SettingsActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            val settingsActivityUiState by viewModel.settingsActivityUiState.collectAsStateWithLifecycle()

            when (val state = settingsActivityUiState) {
                SettingsActivityUiState.Loading -> {
                    enableEdgeToEdge()

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
                    handleEdgeToEdge(themeSettings = state.themeSettings)

                    EblanLauncherTheme(
                        themeBrand = state.themeSettings.themeBrand,
                        darkThemeConfig = state.themeSettings.darkThemeConfig,
                        dynamicTheme = state.themeSettings.dynamicTheme,
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
        }
    }
}