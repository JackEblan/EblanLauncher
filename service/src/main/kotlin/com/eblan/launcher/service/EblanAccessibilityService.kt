package com.eblan.launcher.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.eblan.launcher.domain.model.GlobalAction
import javax.inject.Inject

internal class EblanAccessibilityService @Inject constructor() : AccessibilityService(){
    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != GlobalAction.ACTION) return

            val globalAction = intent.getStringExtra(GlobalAction.ACTION) ?: return

            when(GlobalAction.valueOf(globalAction)){
                GlobalAction.Notifications -> {
                    performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                }

                GlobalAction.QuickSettings -> {

                }
                GlobalAction.LockScreen -> {

                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onServiceConnected() {
        val filter = IntentFilter().apply {
            addAction(GlobalAction.ACTION)
        }

        ContextCompat.registerReceiver(
            applicationContext,
            commandReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        unregisterReceiver(commandReceiver)
    }
}