package com.eblan.launcher.feature.pinnedwidget

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.eblan.launcher.domain.framework.AppWidgetHostDomainWrapper
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import javax.inject.Inject

class ConfirmPinAppWidgetActivity : AppCompatActivity() {

    @Inject
    lateinit var appWidgetHostWrapper: AppWidgetHostWrapper

    @Inject
    lateinit var appWidgetHosDomainWrapper: AppWidgetHostDomainWrapper

    @Inject
    lateinit var appWidgetManagerWrapper: AppWidgetManagerWrapper

    @Inject
    lateinit var launcherAppsWrapper: LauncherAppsWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pinItemRequest = launcherAppsWrapper.getPinItemRequest(intent = intent)

            if (pinItemRequest.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_APPWIDGET) {
                showConfirmationDialog(pinItemRequest = pinItemRequest)
            } else {
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showConfirmationDialog(pinItemRequest: LauncherApps.PinItemRequest) {
        AlertDialog.Builder(this)
            .setTitle("Add Widget")
            .setMessage("Allow this widget to be added to your home screen?")
            .setPositiveButton("Add") { _, _ -> acceptWidgetPin(pinItemRequest = pinItemRequest) }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
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
