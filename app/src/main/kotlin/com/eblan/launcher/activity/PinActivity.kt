package com.eblan.launcher.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalLauncherApps
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.feature.pin.PinScreen
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.model.PinActivityUiState
import com.eblan.launcher.viewmodel.PinActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : ComponentActivity() {

    @Inject
    lateinit var androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper

    @Inject
    lateinit var androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper

    @Inject
    lateinit var androidLauncherAppsWrapper: AndroidLauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    private val viewModel: PinActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(packageName)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val pinItemRequest = androidLauncherAppsWrapper.getPinItemRequest(intent = intent)

            setContent {
                CompositionLocalProvider(
                    LocalAppWidgetHost provides androidAppWidgetHostWrapper,
                    LocalAppWidgetManager provides androidAppWidgetManagerWrapper,
                    LocalPinItemRequest provides pinItemRequestWrapper,
                    LocalLauncherApps provides androidLauncherAppsWrapper,
                ) {
                    val pinActivityUiState by viewModel.pinActivityUiState.collectAsStateWithLifecycle()

                    when (val state = pinActivityUiState) {
                        PinActivityUiState.Loading -> {
                        }

                        is PinActivityUiState.Success -> {
                            EblanLauncherTheme(
                                themeBrand = state.themeSettings.themeBrand,
                                darkThemeConfig = state.themeSettings.darkThemeConfig,
                                dynamicTheme = state.themeSettings.dynamicTheme,
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
    }
}
