package com.eblan.launcher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.feature.pin.PinScreen
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : ComponentActivity() {

    @Inject
    lateinit var appWidgetHostWrapper: AppWidgetHostWrapper

    @Inject
    lateinit var appWidgetManagerWrapper: AppWidgetManagerWrapper

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(packageName)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val pinItemRequest = launcherAppsWrapper.getPinItemRequest(intent = intent)

            setContent {
                CompositionLocalProvider(
                    LocalAppWidgetHost provides appWidgetHostWrapper,
                    LocalAppWidgetManager provides appWidgetManagerWrapper,
                    LocalPinItemRequest provides pinItemRequestWrapper,
                    LocalLauncherApps provides launcherAppsWrapper,
                ) {
                    EblanLauncherTheme(
                        themeBrand = ThemeBrand.GREEN,
                        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                        dynamicTheme = false,
                    ) {
                        PinScreen(
                            pinItemRequest = pinItemRequest,
                            onDragStart = {
                                startActivity(homeIntent)

                                finish()
                            },
                            onFinish = ::finish,
                            onAddedToHomeScreenToast = { message ->
                                Toast.makeText(
                                    applicationContext,
                                    message,
                                    Toast.LENGTH_LONG,
                                ).show()
                            },
                        )
                    }
                }
            }
        }
    }
}
