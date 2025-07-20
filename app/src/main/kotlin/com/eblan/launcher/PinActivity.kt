package com.eblan.launcher

import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.feature.pin.widget.PinWidgetScreen
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : ComponentActivity() {

    @Inject
    lateinit var appWidgetHostWrapper: AppWidgetHostWrapper

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pinItemRequest = launcherAppsWrapper.getPinItemRequest(intent = intent)

            setContent {
                CompositionLocalProvider(
                    LocalAppWidgetHost provides appWidgetHostWrapper,
                    LocalPinItemRequest provides pinItemRequestWrapper,
                ) {
                    when (pinItemRequest.requestType) {
                        LauncherApps.PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                            PinWidgetScreen(
                                pinItemRequest = pinItemRequest,
                                onHome = {
                                    val homeIntent = Intent(Intent.ACTION_MAIN)
                                        .addCategory(Intent.CATEGORY_HOME)
                                        .setPackage(packageName)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                    startActivity(homeIntent)
                                },
                            )
                        }

                        LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT -> {

                        }
                    }
                }
            }
        }
    }
}
