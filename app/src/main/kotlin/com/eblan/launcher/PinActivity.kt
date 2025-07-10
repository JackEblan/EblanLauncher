package com.eblan.launcher

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.eblan.launcher.domain.framework.AppWidgetHostDomainWrapper
import com.eblan.launcher.feature.pin.widget.PinWidgetScreen
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : ComponentActivity() {

    @Inject
    lateinit var appWidgetHostWrapper: AppWidgetHostWrapper

    @Inject
    lateinit var appWidgetHosDomainWrapper: AppWidgetHostDomainWrapper

    @Inject
    lateinit var appWidgetManagerWrapper: AppWidgetManagerWrapper

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    private var mFinishOnPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pinItemRequest = launcherAppsWrapper.getPinItemRequest(intent = intent)

            setContent {
                when (pinItemRequest.requestType) {
                    LauncherApps.PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                        PinWidgetScreen(
                            onHome = {
                                val intent = Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_HOME)
                                    .setPackage(packageName)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                                startActivity(intent)

                                mFinishOnPause = true
                            },
                        )
                    }

                    LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT -> {

                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mFinishOnPause) {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun acceptWidgetPin(pinItemRequest: LauncherApps.PinItemRequest) {
        val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

        val extras = Bundle().apply {
            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        if (pinItemRequest.accept(extras)) {
            val appWidgetProviderInfo = pinItemRequest.getAppWidgetProviderInfo(this)

            if (appWidgetProviderInfo != null && appWidgetManagerWrapper.bindAppWidgetIdIfAllowed(
                    appWidgetId = appWidgetId,
                    provider = appWidgetProviderInfo.provider,
                )
            ) {
                println("Did it work?")
            }
        } else {
            appWidgetHosDomainWrapper.deleteAppWidgetId(appWidgetId = appWidgetId)
        }

        finish()
    }
}
