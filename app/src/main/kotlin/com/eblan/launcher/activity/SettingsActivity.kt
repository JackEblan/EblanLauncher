package com.eblan.launcher.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.eblan.launcher.model.SettingsActivityUiState
import com.eblan.launcher.model.ThemeSettings
import com.eblan.launcher.navigation.SettingsNavHost
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.viewmodel.SettingsActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var themeSettings by mutableStateOf(
            ThemeSettings(
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
                            themeSettings = uiState.themeSettings

                            handleEdgeToEdge(themeSettings = uiState.themeSettings)
                        }
                    }
                }
            }
        }

        setContent {
            val navController = rememberNavController()

            EblanLauncherTheme(
                themeBrand = themeSettings.themeBrand,
                darkThemeConfig = themeSettings.darkThemeConfig,
                dynamicTheme = themeSettings.dynamicTheme,
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