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
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalFileManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.local.LocalWallpaperManager
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.model.MainActivityThemeSettings
import com.eblan.launcher.model.MainActivityUiState
import com.eblan.launcher.navigation.MainNavHost
import com.eblan.launcher.service.ApplicationInfoService
import com.eblan.launcher.util.handleWallpaperEdgeToEdge
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
    lateinit var fileManager: FileManager

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                val mainActivityUiState by viewModel.mainActivityUiState.collectAsStateWithLifecycle()

                when (val state = mainActivityUiState) {
                    MainActivityUiState.Loading -> {
                        enableEdgeToEdge()
                    }

                    is MainActivityUiState.Success -> {
                        SideEffect {
                            handleWallpaperEdgeToEdge(mainActivityThemeSettings = state.mainActivityThemeSettings)
                        }

                        val mainActivityThemeSettings = getMainActivityThemeSettings(
                            mainActivityThemeSettings = state.mainActivityThemeSettings,
                        )

                        EblanLauncherTheme(
                            themeBrand = mainActivityThemeSettings.themeSettings.themeBrand,
                            darkThemeConfig = mainActivityThemeSettings.themeSettings.darkThemeConfig,
                            dynamicTheme = mainActivityThemeSettings.themeSettings.dynamicTheme,
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

    private fun getMainActivityThemeSettings(mainActivityThemeSettings: MainActivityThemeSettings): MainActivityThemeSettings {
        return when (mainActivityThemeSettings.themeSettings.darkThemeConfig) {
            DarkThemeConfig.System -> {
                when (mainActivityThemeSettings.themeSettings.darkThemeConfig) {
                    DarkThemeConfig.System -> {
                        if (mainActivityThemeSettings.hintSupportsDarkTheme) {
                            mainActivityThemeSettings.copy(
                                themeSettings = mainActivityThemeSettings.themeSettings.copy(
                                    darkThemeConfig = DarkThemeConfig.Light,
                                ),
                            )
                        } else {
                            mainActivityThemeSettings.copy(
                                themeSettings = mainActivityThemeSettings.themeSettings.copy(
                                    darkThemeConfig = DarkThemeConfig.Dark,
                                ),
                            )
                        }
                    }

                    DarkThemeConfig.Light, DarkThemeConfig.Dark -> {
                        mainActivityThemeSettings
                    }
                }
            }

            DarkThemeConfig.Light, DarkThemeConfig.Dark -> mainActivityThemeSettings
        }
    }
}

