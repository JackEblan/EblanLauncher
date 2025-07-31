package com.eblan.launcher


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.eblan.launcher.navigation.MainNavHost
import com.eblan.launcher.service.ApplicationInfoService
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

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

                EblanLauncherTheme(
                    themeBrand = ThemeBrand.GREEN,
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    dynamicTheme = false,
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

