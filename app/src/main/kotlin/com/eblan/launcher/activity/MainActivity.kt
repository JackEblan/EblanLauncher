package com.eblan.launcher.activity


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.eblan.launcher.model.MainActivityThemeSettings
import com.eblan.launcher.model.MainActivityUiState
import com.eblan.launcher.model.ThemeSettings
import com.eblan.launcher.navigation.MainNavHost
import com.eblan.launcher.service.ApplicationInfoService
import com.eblan.launcher.util.handleWallpaperEdgeToEdge
import com.eblan.launcher.viewmodel.MainActivityViewModel
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

        var mainActivityThemeSettings by mutableStateOf(
            MainActivityThemeSettings(
                themeSettings = ThemeSettings(
                    themeBrand = ThemeBrand.Green,
                    darkThemeConfig = DarkThemeConfig.System,
                    dynamicTheme = false,
                ),
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
                            handleWallpaperEdgeToEdge(
                                mainActivityThemeSettings = uiState.mainActivityThemeSettings,
                                onUpdateThemeSettings = { newMainActivityThemeSettings ->
                                    mainActivityThemeSettings = newMainActivityThemeSettings
                                },
                            )
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

