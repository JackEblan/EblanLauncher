package com.eblan.launcher


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.navigation.GetoNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            EblanLauncherTheme(
                themeBrand = ThemeBrand.GREEN,
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                dynamicTheme = false,
            ) {
                Surface {
                    GetoNavHost(navController = navController)
                }
            }
        }
    }
}

