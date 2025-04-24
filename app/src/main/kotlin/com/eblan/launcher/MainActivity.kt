package com.eblan.launcher


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalWindowManager
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import com.eblan.launcher.framework.windowmanager.WindowManagerWrapper
import com.eblan.launcher.navigation.EblanNavHost
import com.eblan.launcher.service.ApplicationInfoService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var applicationInfoServiceIntent: Intent

    @Inject
    lateinit var appWidgetHostWrapper: AppWidgetHostWrapper

    @Inject
    lateinit var appWidgetManagerWrapper: AppWidgetManagerWrapper

    @Inject
    lateinit var windowManagerWrapper: WindowManagerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CompositionLocalProvider(
                LocalAppWidgetHost provides appWidgetHostWrapper,
                LocalAppWidgetManager provides appWidgetManagerWrapper,
                LocalWindowManager provides windowManagerWrapper,
            ) {
                val navController = rememberNavController()

                EblanLauncherTheme(
                    themeBrand = ThemeBrand.GREEN,
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    dynamicTheme = false,
                ) {
                    Surface {
                        EblanNavHost(navController = navController)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        applicationInfoServiceIntent = Intent(this, ApplicationInfoService::class.java)

        startService(applicationInfoServiceIntent)

        appWidgetHostWrapper.startListening()

    }

    override fun onStop() {
        super.onStop()
        stopService(applicationInfoServiceIntent)

        appWidgetHostWrapper.stopListening()
    }
}

