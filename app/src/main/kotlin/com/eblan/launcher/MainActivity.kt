package com.eblan.launcher


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalFileManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.local.LocalWallpaperManager
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.model.MainActivityUiState
import com.eblan.launcher.model.ThemeSettings
import com.eblan.launcher.navigation.MainNavHost
import com.eblan.launcher.service.ApplicationInfoService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
    lateinit var fileManager: FileManager

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var themeSettings by mutableStateOf(
            ThemeSettings(
                themeBrand = ThemeBrand.Green,
                darkThemeConfig = DarkThemeConfig.System,
                dynamicTheme = false,
                hintSupportsDarkTheme = false,
            ),
        )

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        MainActivityUiState.Loading -> {
                            enableEdgeToEdge()
                        }

                        is MainActivityUiState.Success -> {
                            themeSettings = uiState.themeSettings

                            handleEdgeToEdge(themeSettings = uiState.themeSettings)
                        }
                    }
                }
            }
        }

        setContent {
            CompositionLocalProvider(
                LocalAppWidgetHost provides androidAppWidgetHostWrapper,
                LocalAppWidgetManager provides androidAppWidgetManagerWrapper,
                LocalLauncherApps provides androidLauncherAppsWrapper,
                LocalPinItemRequest provides pinItemRequestWrapper,
                LocalFileManager provides fileManager,
                LocalWallpaperManager provides androidWallpaperManagerWrapper,
            ) {
                val navController = rememberNavController()

                EblanLauncherTheme(
                    themeBrand = themeSettings.themeBrand,
                    darkThemeConfig = themeSettings.darkThemeConfig,
                    dynamicTheme = themeSettings.dynamicTheme,
                ) {
                    MainNavHost(
                        navController = navController,
                        onSettings = {
                            startActivity(
                                Intent(
                                    this,
                                    SettingsActivity::class.java,
                                ),
                            )

                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun handleEdgeToEdge(themeSettings: ThemeSettings) {
        when (themeSettings.darkThemeConfig) {
            DarkThemeConfig.System -> {
                if (themeSettings.hintSupportsDarkTheme) {
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
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                    )
                }
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

    override fun onStart() {
        super.onStart()
        applicationInfoServiceIntent = Intent(this, ApplicationInfoService::class.java)

        startService(applicationInfoServiceIntent)

        androidAppWidgetHostWrapper.startListening()

    }

    override fun onStop() {
        super.onStop()
        stopService(applicationInfoServiceIntent)

        androidAppWidgetHostWrapper.stopListening()
    }
}

